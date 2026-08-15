package com.foxnet.medications.Database

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.foxnet.medications.ProgressViewModel

@Dao
interface ChartDb {

}

@Database(
    entities = [
        Medication::class,
        Prescription::class,
        PrescriptionAdministration::class,
        AdministrationRecord::class,
    ],
    version = 1,
    exportSchema = false
)
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