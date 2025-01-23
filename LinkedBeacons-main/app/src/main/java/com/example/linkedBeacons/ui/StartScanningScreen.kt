package com.example.linkedBeacons.ui

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.linkedbeacons.R
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.RegionViewModel

/**
 * Composable that allows the user range and visualize the beacons in the app.
 */

@Composable
fun StartOrderScreen(
    viewModel: BeaconAppViewModel,
    regionViewModel: RegionViewModel?,
    onNextButtonClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val beacons = remember { mutableStateOf(emptyList<MutableState<Beacon>>()) }
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val isRanging by viewModel.isRanging.collectAsState()

    // Observe the beacons LiveData
    regionViewModel?.rangedBeacons?.observe(LocalLifecycleOwner.current) { newBeacons ->
        beacons.value = newBeacons.map { mutableStateOf(it) }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        println("NewScanScreen: ${beacons.value.count()} beacons detected")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

            if (isRanging) {

                Image(
                    painter = painterResource(R.drawable.logo_bueno),
                    contentDescription = null,
                    modifier = Modifier.width(100.dp)
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                Text("Ranging enabled: ${beacons.value.count()} beacons detected")

                Divider(
                    thickness = dimensionResource(R.dimen.thickness_divider),
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))
                )

                BeaconListScreen(beacons = beacons.value)


            } else {
                Image(
                    painter = painterResource(R.drawable.logo_bueno),
                    contentDescription = null,
                    modifier = Modifier.width(500.dp)
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                Text(
                    text = stringResource(R.string.visualize_Beacons),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.padding_medium)
            )
        ) {
            BeaconActionButton(
                labelResourceId1 = R.string.start_monitoring_beacons,
                labelResourceId2 = R.string.stop_monitoring_beacons,
                onClick = {
                    viewModel.setMonitoring(!isMonitoring)
                },
                pressed = isMonitoring,
                modifier = Modifier.fillMaxWidth(),
            )
            BeaconActionButton(
                labelResourceId1 = R.string.start_ranging_beacons,
                labelResourceId2 = R.string.stop_ranging_beacons,
                onClick = {
                    viewModel.setRanging(!isRanging)
                },
                pressed = isRanging,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun BeaconListScreen(beacons: List<MutableState<Beacon>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // Specify the fixed size here
            .background(Color.LightGray) // Optional: to see the container
    ) {
        LazyColumn() {
            beacons.forEach { beaconState ->
                item {
                    BeaconItem(beaconState.value)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BeaconItem(beacon: Beacon) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Parser ID: ${beacon.parserIdentifier}")
            Text("RSSI: ${beacon.rssi}")
            if (beacon.distance != -1.0) {
                Text("Distance: ${beacon.distance} m")
            }
            // Conditional parameters
            beacon.id1?.let {
                if (it.toString().isNotEmpty()) {
                    Text("ID1: $it")
                }
            }
            beacon.identifiers.getOrNull(1)?.let {
                if (it.toString().isNotEmpty()) {
                    Text("ID2: $it")
                }
            } ?: Text("ID2: Not available")
            beacon.identifiers.getOrNull(2)?.let {
                if (it.toString().isNotEmpty()) {
                    Text("ID3: $it")
                }
            } ?: Text("ID3: Not available")

            Text("DataFields: ${beacon.dataFields?.joinToString() ?: "Not available"}")

            beacon.extraDataFields?.forEachIndexed { index, dataField ->
                Text("DataField $index: ${dataField?.toString() ?: "Not available"}")
            } ?: Text("No extra data fields available")

            beacon.bluetoothName?.let {
                if (it.isNotEmpty()) {
                    Text("Bluetooth Name: $it")
                }
            }
            beacon.bluetoothAddress?.let {
                if (it.isNotEmpty()) {
                    Text("Bluetooth Address: $it")
                }
            }
        }
    }
}

/**
 * Customizable button composable that displays the
 * [labelResourceId1] or [labelResourceId2] depending on [pressed] pressed.
 * Triggers [onClick] lambda when this composable is clicked
 */

@Composable
fun BeaconActionButton(
    @StringRes labelResourceId1: Int,
    @StringRes labelResourceId2: Int,
    pressed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.widthIn(min = 250.dp)
    ) {
        Text(if (pressed) stringResource(labelResourceId2) else stringResource(labelResourceId1))
    }
}














