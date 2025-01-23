package com.example.linkedBeacons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.linkedBeacons.data.BeaconAppUiState
import com.example.linkedbeacons.R

/**
 * This composable expects [uiState] that represents the order state,
 * [onCancelButtonClicked]
 * lambda that triggers canceling the order and passes the final configuration
 * to [onSendButtonClicked] lambda
 */

@Composable
fun ConfigSummaryScreen(
    uiState: BeaconAppUiState,
    onCancelButtonClicked: () -> Unit,
    onSendButtonClicked: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    val resources = LocalContext.current.resources
    val numberOfParsers = resources.getQuantityString(
        R.plurals.parsers,
        uiState.quantity,
        uiState.quantity
    )
    //Load and format a string resource with the parameters.
    val configSummary = stringResource(
        R.string.config_details,
        numberOfParsers,
        uiState.parserType,
        uiState.quantity
    )
    val newConfig = stringResource(R.string.new_parser_config)
    //Create a list of config summary to display
    val items = listOf(
        // Summary line 1: display selected quantity of parsers.
        Pair(stringResource(R.string.quantity), numberOfParsers),
        // Summary line 2: display selected parsers.
        Pair(
            stringResource(R.string.parsers_currently_active),
            uiState.parserType.ifEmpty { listOf("none") }.joinToString { it.lowercase() })
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            items.forEach { item ->
                Text(item.first.uppercase())
                Text(text = item.second, fontWeight = FontWeight.Bold)
                Divider(thickness = dimensionResource(R.dimen.thickness_divider))
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        }
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSendButtonClicked(newConfig, configSummary) }
                ) {
                    Text(stringResource(R.string.send))
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCancelButtonClicked
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

