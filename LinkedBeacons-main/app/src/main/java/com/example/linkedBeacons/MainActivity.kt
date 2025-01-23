package com.example.linkedBeacons

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.linkedBeacons.beaconreference.BeaconReferenceApplication
import com.example.linkedBeacons.permissions.BeaconScanPermissionsActivity
import com.example.linkedBeacons.ui.theme.MainTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    /**
     * Declares a property named `beaconReferenceApplication` in the `MainActivity` class.
     * This property will be initialized in the `onCreate` method of the `MainActivity` class,
     * where it is assigned the application instance cast to `BeaconReferenceApplication`.
     * This allows the `MainActivity` to access the `BeaconReferenceApplication` instance
     * and its properties or methods.
     */
    private lateinit var beaconReferenceApplication: BeaconReferenceApplication


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        /**
         * Initializes the beaconReferenceApplication property by casting the application instance
         * to BeaconReferenceApplication.
         * Retrieves the regionViewModel from the ViewModelHolder object in the
         * BeaconReferenceApplication. We have to access to the same instance
         * in order to display the data in the UI.
         */
        beaconReferenceApplication = application as BeaconReferenceApplication
        val regionViewModel = BeaconReferenceApplication.ViewModelHolder.regionViewModel

        setContent {
            MainTheme {
                BeaconApp(regionViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!BeaconScanPermissionsActivity.allPermissionsGranted(
                this,
                true
            )
        ) {
            val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
            intent.putExtra("backgroundAccessRequested", true)
            startActivity(intent)
        }
    }

    /**
     *   Ensures that the user has granted all permissions before continuing.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!BeaconScanPermissionsActivity.allPermissionsGranted(this, true)) {
            val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
            intent.putExtra("backgroundAccessRequested", true)
            startActivity(intent)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
