package com.foxnet.medications.database

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room3.ColumnTypeConverters
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.foxnet.medications.viewmodels.ProgressViewModel
import com.foxnet.medications.viewmodels.PrescriptionsViewModel
import com.foxnet.medications.viewmodels.InventoryViewModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface ChartDb {
    @Query(
        """
        SELECT
            Prescription.id AS prescriptionId,
            Prescription.active AS active,
            Medication.name AS medicationName,
            Medication.type AS medicationType,
            Medication.defaultDose AS dose,
            Medication.defaultDoseUnit AS doseUnit,
            Prescription.schedule AS schedule,
            Prescription.startDate AS startDate,
            Prescription.endDate AS endDate,
            Prescription.duration AS duration,
            (
                SELECT PrescriptionAdministration.time
                FROM PrescriptionAdministration
                WHERE PrescriptionAdministration.prescriptionId = Prescription.id
                ORDER BY PrescriptionAdministration.time
                LIMIT 1
            ) AS administrationTime
        FROM Prescription
        INNER JOIN Medication ON Medication.id = Prescription.medicationId
        ORDER BY Prescription.active DESC, Medication.name
        """
    )
    fun observePrescriptions(): Flow<List<PrescriptionSummary>>

    @Query("SELECT id, name, type, defaultDose, defaultDoseUnit, inventoryQuantity FROM Medication ORDER BY name")
    fun observeMedications(): Flow<List<Medication>>

    @Insert
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Insert
    suspend fun insertPrescription(prescription: Prescription): Long

    @Insert
    suspend fun insertPrescriptionAdministration(administration: PrescriptionAdministration)

    @Transaction
    suspend fun createPrescription(
        medicationId: Int,
        prescription: Prescription,
        administrations: List<PrescriptionAdministration>,
    ) {
        val prescriptionId = insertPrescription(prescription.copy(medicationId = medicationId)).toInt()
        administrations.forEach { administration ->
            insertPrescriptionAdministration(administration.copy(prescriptionId = prescriptionId))
        }
    }

    @Transaction
    suspend fun createMedicationAndPrescription(
        medication: Medication,
        prescription: Prescription,
        administrations: List<PrescriptionAdministration>,
    ) {
        val medicationId = insertMedication(medication).toInt()
        createPrescription(medicationId, prescription, administrations)
    }

    @Query(
        """
        SELECT
            PrescriptionAdministration.id AS prescriptionAdministrationId,
            Medication.id AS medicationId,
            Medication.name AS medicationName,
            COALESCE(PrescriptionAdministration.doseUnit, Medication.defaultDoseUnit) AS doseUnit,
            COALESCE(PrescriptionAdministration.dose, Medication.defaultDose) AS dose,
            Prescription.withFood AS withFood,
            PrescriptionAdministration.time AS time,
            PrescriptionAdministration.event AS event,
            AdministrationRecord.id AS administrationRecordId,
            AdministrationRecord.skipped AS skipped
        FROM PrescriptionAdministration
        INNER JOIN Prescription ON Prescription.id = PrescriptionAdministration.prescriptionId
        INNER JOIN Medication ON Medication.id = Prescription.medicationId
        LEFT JOIN AdministrationRecord ON AdministrationRecord.id = (
            SELECT existingRecord.id FROM AdministrationRecord AS existingRecord
            WHERE existingRecord.prescriptionAdministrationId = PrescriptionAdministration.id
              AND existingRecord.date = :date
            ORDER BY existingRecord.id DESC
            LIMIT 1
        )
        ORDER BY PrescriptionAdministration.time IS NULL, PrescriptionAdministration.time, Medication.name
        """
    )
    fun observePrescriptionAdministrations(date: LocalDate): Flow<List<PrescriptionAdministrationTask>>

    @Insert
    suspend fun insertAdministrationRecord(record: AdministrationRecord)

    @Update
    suspend fun updateAdministrationRecord(record: AdministrationRecord)
}

data class PrescriptionAdministrationTask(
    val prescriptionAdministrationId: Int,
    val medicationId: Int,
    val medicationName: String,
    val doseUnit: String,
    val dose: Int,
    val withFood: Boolean,
    val time: LocalTime?,
    val event: String?,
    val administrationRecordId: Int?,
    val skipped: Boolean?,
)

data class PrescriptionSummary(
    val prescriptionId: Int,
    val active: Boolean,
    val medicationName: String,
    val medicationType: MedicationType,
    val dose: Int,
    val doseUnit: String,
    val schedule: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val duration: java.time.Period?,
    val administrationTime: LocalTime?,
)

@Database(
    entities = [
        Medication::class,
        Prescription::class,
        PrescriptionAdministration::class,
        AdministrationRecord::class,
    ],
    version = 5,
    exportSchema = false
)
@ColumnTypeConverters(DatabaseConverters::class)
abstract class MedicationDatabase : RoomDatabase() {

    abstract fun chart(): ChartDb

    companion object {
        @Volatile
        private var INSTANCE: MedicationDatabase? = null

        fun getDb(context: Context): MedicationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = MedicationDatabase::class.java,
                    name = "medication_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PersistentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            val dao = MedicationDatabase.getDb(context).chart()
            return ProgressViewModel(dao) as T
        }
        if (modelClass.isAssignableFrom(PrescriptionsViewModel::class.java)) {
            val dao = MedicationDatabase.getDb(context).chart()
            return PrescriptionsViewModel(dao) as T
        }
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            val dao = MedicationDatabase.getDb(context).chart()
            return InventoryViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class!")
    }
}
