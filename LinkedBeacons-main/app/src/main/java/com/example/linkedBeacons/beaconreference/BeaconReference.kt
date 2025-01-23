package com.example.linkedBeacons.beaconreference

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.example.linkedBeacons.MainActivity
import com.example.linkedBeacons.permissions.BeaconScanPermissionsActivity
import com.example.linkedbeacons.R
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconParser.EDDYSTONE_UID_LAYOUT
import org.altbeacon.beacon.BeaconParser.EDDYSTONE_URL_LAYOUT
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.RegionViewModel


// Define the class BeaconReferenceApplication which extends Application and implements ViewModelStoreOwner
class BeaconReferenceApplication(override val viewModelStore: ViewModelStore) : Application(),
    ViewModelStoreOwner {
        // Secondary constructor that initializes the ViewModelStore
    constructor() : this(ViewModelStore())
    // Declare a nullable Region variable named region
    private var region: Region? = null
    // Override the onCreate method to initialize the application
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Check if all permissions are granted, if so, initialize the region
        if (BeaconScanPermissionsActivity.allPermissionsGranted(this, true)) {
            region = Region("all-beacons", null, null, null)
        }
        defaultSetupParser()

        // Enabling debugging will send lots of verbose debug information from the library to Logcat
        // this is useful for troubleshooting problems

        // BeaconManager.setDebug(true)

        setupBeaconScanning()
    }

    private fun defaultSetupParser() {

        /** We remove all the preset beacon parsers and let the user adds them */
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        BeaconManager.setDebug(true)
        beaconManager.beaconParsers.clear()

         /** Default Beacon Parsers
        // iBeacon
        val parser = BeaconParser("iBeacon").setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.beaconParsers.add(parser)

        // Eddystone
        val parser2 = BeaconParser().setBeaconLayout(EDDYSTONE_UID_LAYOUT)
        parser2.setHardwareAssistManufacturerCodes(arrayOf(0x00e0).toIntArray())
        beaconManager.beaconParsers.add(parser2)

        // Eddystone URL
        val parser3 = BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT)
        parser3.setHardwareAssistManufacturerCodes(arrayOf(0x00e0).toIntArray())
        beaconManager.beaconParsers.add(parser3)
          */
    }

    private fun setupBeaconScanning() {

        val beaconManager = BeaconManager.getInstanceForApplication(this)
        try {
            setupForegroundService()
        } catch (e: SecurityException) {
            Log.d(
                TAG,
                "Not setting up foreground service scanning until location permission granted by user"
            )
            return
        }

        /** We can change scanning foreground frequency */
        //beaconManager.setEnableScheduledScanJobs(false)
        //beaconManager.setBackgroundBetweenScanPeriod(0)
        //beaconManager.setBackgroundScanPeriod(2500)

        /** Ranging callbacks will drop out if no beacons are detected
         Monitoring callbacks will be delayed by up to 25 minutes on region exit */

        // beaconManager.setIntentScanningStrategyEnabled(true)

        /** The code below will start "monitoring" and "Ranging" for beacons
         * matching the region definition at the top of this file */
        region?.let { beaconManager.startMonitoring(it) }
        region?.let { beaconManager.startRangingBeacons(it) }

        /** Live data setup */
        val regionViewModel =
            BeaconManager.getInstanceForApplication(this).getRegionViewModel(region)
        // Store the created ViewModel in the "singleton" holder class
        ViewModelHolder.regionViewModel = regionViewModel
        // observer will be called each time
        // the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observeForever(centralMonitoringObserver)
        // observer will be called each time
        // a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observeForever(centralRangingObserver)

    }

    private fun setupForegroundService() {
        val builder = Notification.Builder(this, "BeaconReferenceApp")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Scanning for Beacons")
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        builder.setContentIntent(pendingIntent)

        val channel = NotificationChannel(
            "beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "My Notification Channel Description"
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.id)

        ////// FOREGROUND SERVICE SCANNING //////

        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(this)
            .enableForegroundServiceScanning(builder.build(), 456)
        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }

    private val centralMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            Log.d(TAG, "outside beacon region: $region")
        } else {
            Log.d(TAG, "inside beacon region: $region")
            sendNotification()
        }
    }

    private val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val rangeAgeMillis =
            System.currentTimeMillis() - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)

        if (rangeAgeMillis < 10000) {
            println("Ranged: ${beacons.count()} beacons")
            Log.d(MainActivity.TAG, "Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                Log.d(TAG, "$beacon about ${beacon.distance} meters away")
            }
        } else {
            Log.d(MainActivity.TAG, "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, "beacon-ref-notification-id")
            .setContentTitle("Beacon Reference Application")
            .setContentText("A beacon is nearby.")
            .setSmallIcon(R.mipmap.ic_launcher)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))

        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        val channel = NotificationChannel(
            "beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "My Notification Channel Description"
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.id)
        notificationManager.notify(1, builder.build())
    }

    companion object {
        private lateinit var instance: BeaconReferenceApplication
        fun getContext(): Context {
            return instance.applicationContext
        }

        const val TAG = "BeaconReference"
    }

    object ViewModelHolder {
        var regionViewModel: RegionViewModel? = null
    }
}

