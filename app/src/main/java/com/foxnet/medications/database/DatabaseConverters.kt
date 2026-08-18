package com.foxnet.medications.database

import androidx.room3.ColumnTypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period

class DatabaseConverters {
    @ColumnTypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @ColumnTypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @ColumnTypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @ColumnTypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @ColumnTypeConverter
    fun periodToString(value: Period?): String? = value?.toString()

    @ColumnTypeConverter
    fun stringToPeriod(value: String?): Period? = value?.let(Period::parse)

    @ColumnTypeConverter
    fun injectionSiteToString(value: InjectionSite?): String? = value?.name

    @ColumnTypeConverter
    fun stringToInjectionSite(value: String?): InjectionSite? = value?.let(InjectionSite::valueOf)
}
