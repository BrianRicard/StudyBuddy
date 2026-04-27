package com.studybuddy.core.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Navigates to [route] only if the current back-stack entry is in the [Lifecycle.State.RESUMED]
 * state. This is the canonical fix for the Compose Navigation "double-navigate" issue where
 * rapid taps, ripple/animation re-triggers, or recomposition during a transition cause the
 * source composable to invoke `navigate()` twice before the destination change completes,
 * pushing two entries onto the back stack and breaking the Back button.
 *
 * See: https://issuetracker.google.com/issues/289302623 and Ian Lake's "Don't navigate twice"
 * guidance for Jetpack Compose Navigation.
 */
fun NavController.navigateSafely(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
        navigate(route, builder)
    }
}
