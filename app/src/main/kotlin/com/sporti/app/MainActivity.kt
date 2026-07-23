package com.sporti.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Text
import com.sporti.core.designsystem.R
import com.sporti.core.designsystem.theme.SportiTheme
import com.sporti.feature.analytics.AnalyticsRoute
import com.sporti.feature.explore.ExploreRoute
import com.sporti.feature.home.HomeRoute
import com.sporti.feature.library.LibraryRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SportiTheme { SportiApp() } }
    }
}

private enum class Destination(val icon: Int, val label: Int) {
    HOME(R.drawable.ic_home, com.sporti.app.R.string.nav_home),
    EXPLORE(R.drawable.ic_matches, com.sporti.app.R.string.nav_sport),
    LIBRARY(R.drawable.ic_library, com.sporti.app.R.string.nav_library),
    ANALYTICS(R.drawable.ic_analytics, com.sporti.app.R.string.nav_analytics),
}

@Composable
private fun SportiApp() {
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { item ->
                    val selected = item == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { destination = item },
                        icon = { Icon(painterResource(item.icon), contentDescription = null) },
                        label = { Text(stringResource(item.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            transitionSpec = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(220)) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(180))
            },
            label = "main_navigation",
        ) { screen ->
            when (screen) {
                Destination.HOME -> HomeRoute(onOpenExplore = { destination = Destination.EXPLORE })
                Destination.EXPLORE -> ExploreRoute()
                Destination.LIBRARY -> LibraryRoute()
                Destination.ANALYTICS -> AnalyticsRoute()
            }
        }
    }
}
