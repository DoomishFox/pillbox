package com.foxnet.medications

//import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.foxnet.medications.ui.theme.MedicationsTheme

enum class AppDestinations(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
) {
    PROGRESS(label = R.string.home_nav, icon = R.drawable.heart_check_24px, contentDescription = R.string.home_nav),
    MEDICATIONS(label = R.string.medications_nav, icon = R.drawable.admin_meds_24px, contentDescription = R.string.medications_nav),
    INVENTORY(label = R.string.profile_nav, icon = R.drawable.medication_24px, contentDescription = R.string.profile_nav),
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    //@OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentDestination by rememberSaveable {
                mutableStateOf(AppDestinations.PROGRESS)
            }

            MedicationsTheme(
                dynamicColor = true
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) { innerPadding ->
                    NavigationSuiteScaffold(
                        modifier = Modifier.padding(innerPadding.copy(bottom = 0.dp)),
                        navigationItems = {
                            AppDestinations.entries.forEach {
                                NavigationSuiteItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(it.icon),
                                            contentDescription = stringResource(it.contentDescription)
                                        )
                                    },
                                    label = { Text(stringResource(it.label)) },
                                    selected = it == currentDestination,
                                    onClick = { currentDestination = it }
                                )
                            }
                        },
                    ) {
                        when (currentDestination) {
                            AppDestinations.PROGRESS -> Progress(outerPadding = innerPadding)
                            AppDestinations.MEDICATIONS -> Medications()
                            AppDestinations.INVENTORY -> Inventory()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Medications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text("whoops")
    }
}

@Composable
fun Inventory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text("inventory")
    }
}

@Composable
fun IconBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(IconButtonDefaults.smallContainerSize())
            .border(ButtonDefaults.outlinedButtonBorder(), shape = IconButtonDefaults.standardShape)
            .clip(IconButtonDefaults.standardShape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(content = content)
    }
}

@Composable
fun PaddingValues.copy(
    start: Dp = calculateStartPadding(LocalLayoutDirection.current),
    top: Dp = calculateTopPadding(),
    end: Dp = calculateEndPadding(LocalLayoutDirection.current),
    bottom: Dp = calculateBottomPadding(),
) = PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom
)
