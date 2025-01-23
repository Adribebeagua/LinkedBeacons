package com.example.linkedBeacons

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.linkedBeacons.data.DataSource
import com.example.linkedBeacons.data.BeaconAppUiState
import com.example.linkedBeacons.ui.ConfigSummaryScreen
import com.example.linkedBeacons.ui.BeaconAppViewModel
import com.example.linkedBeacons.ui.SelectOptionScreen
import com.example.linkedBeacons.ui.StartOrderScreen
import com.example.linkedbeacons.R
import org.altbeacon.beacon.RegionViewModel

/**
 * enum values that represent the screens in the app
 */
enum class BeaconAppScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Parsers(title = R.string.select_persers),
    Summary(title = R.string.config_summary)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun BeaconTopAppBar(
    currentScreen: BeaconAppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun BeaconBottomAppBar(
    currentScreen: BeaconAppScreen,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            BottomAppBarButton(
                icon = Icons.Default.Home,
                contentDescription = "Scan Screen",
                onClick = {
                    if (currentScreen != BeaconAppScreen.Start) {
                        navController.navigate(BeaconAppScreen.Start.name)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            BottomAppBarButton(
                icon = Icons.Default.Settings,
                contentDescription = "Configuration",
                onClick = {
                    if (currentScreen != BeaconAppScreen.Parsers) {
                        navController.navigate(BeaconAppScreen.Parsers.name)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            BottomAppBarButton(
                icon = Icons.Default.Person,
                contentDescription = "Profile",
                onClick = {
                    if (currentScreen != BeaconAppScreen.Summary) {
                        navController.navigate(BeaconAppScreen.Summary.name)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BottomAppBarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}


@Composable
fun BeaconApp(
    regionViewModel: RegionViewModel?,
    //beaconViewModel: BeaconViewModel ,
    viewModel: BeaconAppViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = BeaconAppScreen.valueOf(
        backStackEntry?.destination?.route ?: BeaconAppScreen.Start.name
    )

    Scaffold(
        topBar = {
            BeaconTopAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BeaconBottomAppBar(
                currentScreen = currentScreen,
                navController = navController
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = BeaconAppScreen.Start.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = BeaconAppScreen.Start.name) {
                StartOrderScreen(
                    viewModel = viewModel,
                    regionViewModel = regionViewModel,
                    // beaconViewModel = beaconViewModel,
                    onNextButtonClicked = {
                        navController.navigate(BeaconAppScreen.Parsers.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = BeaconAppScreen.Parsers.name) {
                SelectOptionScreen(
                    options = DataSource.parsers.map {it.first},
                    selectedParsers = uiState.parserType,
                    onSelectionChanged = {
                        selectedParsers -> viewModel.setParsers(selectedParsers)
                     },
                    onNextButtonClicked = {
                        navController.navigate(BeaconAppScreen.Summary.name)
                    },
                    onCancelButtonClicked = {
                        resetConfigAndNavigateToStart(viewModel)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = BeaconAppScreen.Summary.name) {
                val context = LocalContext.current
                ConfigSummaryScreen(
                    uiState = uiState,
                    onCancelButtonClicked = {
                        resetConfigAndNavigateToStart(viewModel)
                    },
                    onSendButtonClicked = { subject: String, summary: String ->
                        shareConfig(context, subject = subject, summary = summary)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Resets the [BeaconAppUiState] and pops up to [BeaconAppScreen.Start]
 */
private fun resetConfigAndNavigateToStart(
    viewModel: BeaconAppViewModel,
    //navController: NavHostController
) {
    viewModel.resetOrder()
   // navController.popBackStack(BeaconAppScreen.Start.name, inclusive = false)
}

/**
 * Creates an intent to share order details
 */
private fun shareConfig(context: Context, subject: String, summary: String) {
    // Create an ACTION_SEND implicit intent with order details in the intent extras
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_parser_config)
        )
    )
}
