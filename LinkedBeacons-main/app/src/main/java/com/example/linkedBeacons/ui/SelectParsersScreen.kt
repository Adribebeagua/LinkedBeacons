package com.example.linkedBeacons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.linkedbeacons.R

/**
 * Composable that displays the list of items as [Checkbox] options,
 * [onSelectionChanged] lambda that notifies the parent composable when a new value is selected,
 * [onCancelButtonClicked] lambda that cancels the configuration when user clicks [R.string.reset_all] and
 * [onNextButtonClicked] lambda that triggers the navigation to next screen.
 */

@Composable
fun SelectOptionScreen(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedParsers: List<String>,
    onSelectionChanged: (List<String>) -> Unit = {},
    onCancelButtonClicked: () -> Unit = {},
    onNextButtonClicked: (List<String>) -> Unit = {}, // Pass selected parsers
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            options.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedParsers.contains(item),
                            onClick = {
                                val newSelectedSounds = if (selectedParsers.contains(item)) {
                                    selectedParsers - item
                                } else {
                                    selectedParsers + item
                                }
                                onSelectionChanged(newSelectedSounds)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedParsers.contains(item),
                        onCheckedChange = {
                            val newSelectedFlavors = if (selectedParsers.contains(item)) {
                                selectedParsers - item
                            } else {
                                selectedParsers + item
                            }
                            onSelectionChanged(newSelectedFlavors)
                        }
                    )
                    Text(item)
                }
            }
            Divider(
                thickness = dimensionResource(R.dimen.thickness_divider),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancelButtonClicked
            ) {
                Text(stringResource(R.string.reset_all))
            }
            Button(
                modifier = Modifier.weight(1f),
                enabled = selectedParsers.isNotEmpty(),
                onClick = { onNextButtonClicked(selectedParsers)} // Pass selected parsers
            ) {
                Text(stringResource(R.string.apply_changes))
            }
        }
    }
}