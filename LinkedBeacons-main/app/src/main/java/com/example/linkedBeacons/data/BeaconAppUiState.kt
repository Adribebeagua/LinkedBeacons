
package com.example.linkedBeacons.data

/**
 * Data class that represents the current UI state in terms of [quantity] and [parserType]
 */
data class BeaconAppUiState(
    /** Selected parsers quantity (1, 2, 3 or 4) */
    val quantity: Int = 0,
    /** Parsers String Name (such as "iBeacon", "Eddystone URL", "Eddystone UID") */
    val parserType: List<String> = emptyList(),
)
