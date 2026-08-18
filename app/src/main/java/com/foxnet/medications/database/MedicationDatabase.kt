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
import androidx.room3.Update
import com.foxnet.medications.viewmodels.ProgressViewModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface ChartDb {
    @Query(
        """
        SELECT
            PrescriptionAdministration.id AS prescriptionAdministrationId,
            Medication.id AS medicationId,
            Medication.name AS medicationName,
            PrescriptionAdministration.doseUnit AS doseUnit,
            PrescriptionAdministration.dose AS dose,
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

@Database(
    entities = [
        Medication::class,
        Prescription::class,
        PrescriptionAdministration::class,
        AdministrationRecord::class,
    ],
    version = 2,
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
        throw IllegalArgumentException("Unknown ViewModel class!")
    }
}
