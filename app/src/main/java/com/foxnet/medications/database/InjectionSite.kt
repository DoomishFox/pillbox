package com.foxnet.medications.database

/**
 * Stable storage values for injectable medication sites. UI copy can map these values to
 * patient-friendly labels without making display text part of the database schema.
 */
enum class InjectionSite {
    LEFT_THIGH,
    RIGHT_THIGH,
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_STOMACH,
    RIGHT_STOMACH,
    LEFT_BUTTOCK,
    RIGHT_BUTTOCK,
}
