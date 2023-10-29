package com.example.buffbites

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.buffbites.ui.OrderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buffbites.data.Datasource
import com.example.buffbites.ui.ChooseDeliveryTimeScreen
import com.example.buffbites.ui.ChooseMenuScreen
import com.example.buffbites.ui.OrderSummaryScreen
import com.example.buffbites.ui.StartOrderScreen


enum class OrderScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Meal(title = R.string.choose_meal),
    Delivery(title = R.string.choose_delivery_time),
    Summary(title = R.string.order_summary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffAppBar(
    currentScreen: OrderScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = OrderScreen.valueOf(
        backStackEntry?.destination?.route ?: OrderScreen.Start.name
    )
    Scaffold(
        topBar = {
           BuffAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()


        NavHost(
                navController = navController,
                startDestination = OrderScreen.Start.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = OrderScreen.Start.name) {
                    StartOrderScreen(
                        restaurantOptions = Datasource.restaurants,
                        onNextButtonClicked = {
                            viewModel.updateVendor(it)
                            navController.navigate(OrderScreen.Meal.name)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            composable(route = OrderScreen.Meal.name) {
                    ChooseMenuScreen(
                        options =  uiState.selectedVendor?.menuItems,
                        onNextButtonClicked = { navController.navigate(OrderScreen.Delivery.name) },
                        onCancelButtonClicked = {
                            cancelOrderAndNavigateToStart(viewModel, navController)
                        },
                        onSelectionChanged = { viewModel.updateMeal(it) },
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            composable(route = OrderScreen.Delivery.name) {
                    ChooseDeliveryTimeScreen(
                        subtotal = uiState.orderSubtotal,
                        onNextButtonClicked = { navController.navigate(OrderScreen.Summary.name) },
                        onCancelButtonClicked = {
                            cancelOrderAndNavigateToStart(viewModel, navController)
                        },
                        options = uiState.availableDeliveryTimes,
                        onSelectionChanged = { viewModel.updateDeliveryTime(it) },
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                composable(route = OrderScreen.Summary.name) {
                    val context = LocalContext.current
                    OrderSummaryScreen(
                        orderUiState = uiState,
                        onCancelButtonClicked = {
                            cancelOrderAndNavigateToStart(viewModel, navController)
                        },
//                        onSendButtonClicked = { subject: String, summary: String ->
//                            shareOrder(context, subject = subject, summary = summary)
//                        },
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.navigate(OrderScreen.Start.name)
}
