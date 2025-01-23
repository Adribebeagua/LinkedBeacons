package com.example.linkedBeacons.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.linkedBeacons.data.BeaconAppUiState
import com.example.linkedBeacons.data.DataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser

/**
 * [BeaconAppViewModel] holds information about the app state.
 * It is used to set the parsers in the [setParsers] method.
 * It holds the current state of the app if it is currently monitoring or ranging.
 * It also holds the current state of the parsers selected by the user.
 *
 * @see BeaconAppUiState
 * @see DataSource
 * @see BeaconParser
 * @see update
 *
 */

class BeaconAppViewModel(application: Application) : AndroidViewModel(application) {


    /**
     * Current State of the app (Functionalities and parsers).
     */
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> get() = _isMonitoring

    private val _isRanging = MutableStateFlow(false)
    val isRanging: StateFlow<Boolean> get() = _isRanging

    fun setMonitoring(value: Boolean) {
        _isMonitoring.value = value
    }

    fun setRanging(value: Boolean) {
        _isRanging.value = value
    }

    private val _uiState = MutableStateFlow(BeaconAppUiState())
    val uiState: StateFlow<BeaconAppUiState> = _uiState.asStateFlow()

    /**
     * Set the desired parsers in [setParsers] for the [selectedParsers] state.
     * Multiple parsers can be selected.
     * To add more parsers you only have to add the necessary information in the [DataSource] class.
     */
    fun setParsers(selectedParsers: List<String>) {
        _uiState.update { currentState ->
            currentState.copy(parserType = selectedParsers, quantity = selectedParsers.size)
        }

        val beaconManager = BeaconManager.getInstanceForApplication(getApplication())
        BeaconManager.setDebug(true)

        // Create a map of all available parsers for lookup
        val availableParsersMap =
            DataSource.parsers.associate { it.first to Pair(it.third, it.second) }

        // Remove parsers that are not selected
        val parsersToRemove = beaconManager.beaconParsers.filter { parser ->
            !selectedParsers.contains(parser.identifier)
        }
        parsersToRemove.forEach { parser ->
            beaconManager.beaconParsers.remove(parser)
        }
    // Remove parsers that are not selected
    // beaconManager.beaconParsers.removeAll { parser ->
    // !selectedParsers.contains(parser.identifier) }


        // Add new parsers
        selectedParsers.forEach { parserId ->
            if (beaconManager.beaconParsers.none { it.identifier == parserId }) {
                val (parserLayout, manufacturerCode) = availableParsersMap[parserId]
                    ?: throw IllegalArgumentException("Unknown parser ID: $parserId")
                val parser = BeaconParser(parserId).setBeaconLayout(parserLayout)
                parser.setHardwareAssistManufacturerCodes(intArrayOf(manufacturerCode))
                beaconManager.beaconParsers.add(parser)
            }
        }
    }

    /**
     * Reset the parsers state
     */
    fun resetOrder() {
        val beaconManager = BeaconManager.getInstanceForApplication(getApplication())
        BeaconManager.setDebug(true)
        beaconManager.beaconParsers.clear()
        _uiState.value = BeaconAppUiState(0, emptyList())
    }

}

