package com.example.linkedBeacons.data


import org.altbeacon.beacon.BeaconParser
/**
Here is the data source for the app.
It contains the parsers for the different types of beacons that the app can detect.
The parsers are stored in a list of triples, where each triple contains the name of the beacon type,
the manufacturer code, and the layout of the beacon.
You can add more parsers by adding new triples to the list.
 */
object DataSource {
    val parsers = listOf(
        Triple("AltBeacon", 0x0118, BeaconParser.ALTBEACON_LAYOUT), // AltBeacon
        Triple("iBeacon", 0x004c, "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"), // iBeacon
        Triple("Eddystone UID", 0x00e0, BeaconParser.EDDYSTONE_UID_LAYOUT), // Eddystone EDDYSTONE_UID_LAYOUT
        Triple("Eddystone URL", 0x00e0, BeaconParser.EDDYSTONE_URL_LAYOUT) // Eddystone EDDYSTONE_URL_LAYOUT
    )

}
