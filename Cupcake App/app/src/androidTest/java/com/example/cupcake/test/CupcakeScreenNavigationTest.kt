package com.example.cupcake.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.cupcake.CupcakeApp
import com.example.cupcake.CupcakeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.cupcake.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CupcakeScreenNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    // Test data
    private val expectedRoutes = listOf(
        CupcakeScreen.Start.name,
        CupcakeScreen.Flavor.name,
        CupcakeScreen.Pickup.name,
        CupcakeScreen.Summary.name
    )

    @Before
    fun setupCupcakeNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            CupcakeApp(navController = navController)
        }
    }

    // Helper Functions
    private fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        val formatter = SimpleDateFormat("E MMM d", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun performNavigateUp() {
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).performClick()
    }

    // Navigation Helper Functions
    private fun navigateToScreen(screen: CupcakeScreen) {
        when (screen) {
            CupcakeScreen.Start -> {} // Already at start
            CupcakeScreen.Flavor -> navigateToFlavorScreen()
            CupcakeScreen.Pickup -> navigateToPickupScreen()
            CupcakeScreen.Summary -> navigateToSummaryScreen()
        }
    }

    private fun navigateToFlavorScreen() {
        composeTestRule.onNodeWithStringId(R.string.one_cupcake).performClick()
    }

    private fun navigateToPickupScreen() {
        navigateToFlavorScreen()
        composeTestRule.onNodeWithStringId(R.string.chocolate).performClick()
        composeTestRule.onNodeWithStringId(R.string.next).performClick()
    }

    private fun navigateToSummaryScreen() {
        navigateToPickupScreen()
        composeTestRule.onNodeWithText(getFormattedDate()).performClick()
        composeTestRule.onNodeWithStringId(R.string.next).performClick()
    }

    // Test Cases
    @Test
    fun cupcakeNavHost_verifyStartDestination() {
        navController.assertCurrentRouteName(CupcakeScreen.Start.name)
    }

    @Test
    fun cupcakeNavHost_navigateBackThroughScreens_returnsToStart() {
        // Navigate through all screens
        navigateToSummaryScreen()

        // Verify back navigation through each screen
        expectedRoutes.reversed().forEach { expectedRoute ->
            navController.assertCurrentRouteName(expectedRoute)
            if (expectedRoute != CupcakeScreen.Start.name) {
                performNavigateUp()
            }
        }
    }
    //Test Naming Convention: thingUnderTest_TriggerOfTest_ResultOfTest
    @Test
    fun cupcakeNavHost_cancelFromAnyScreen_returnsToStartScreen() {
        // Test cancel from each screen
        listOf(CupcakeScreen.Flavor, CupcakeScreen.Pickup, CupcakeScreen.Summary).forEach { screen ->
            // Navigate to screen
            navigateToScreen(screen)

            // Verify current screen
            navController.assertCurrentRouteName(screen.name)

            // Click cancel
            composeTestRule.onNodeWithStringId(R.string.cancel).performClick()

            // Verify navigation to start
            navController.assertCurrentRouteName(CupcakeScreen.Start.name)
        }
    }

    @Test
    fun cupcakeNavHost_navigateForwardThroughScreens_reachSummary() {
        // Start -> Flavor
        navigateToFlavorScreen()
        navController.assertCurrentRouteName(CupcakeScreen.Flavor.name)

        // Flavor -> Pickup
        composeTestRule.onNodeWithStringId(R.string.chocolate).performClick()
        composeTestRule.onNodeWithStringId(R.string.next).performClick()
        navController.assertCurrentRouteName(CupcakeScreen.Pickup.name)

        // Pickup -> Summary
        composeTestRule.onNodeWithText(getFormattedDate()).performClick()
        composeTestRule.onNodeWithStringId(R.string.next).performClick()
        navController.assertCurrentRouteName(CupcakeScreen.Summary.name)
    }

    @Test
    fun cupcakeNavHost_checkBackButtonVisibility_showsOnAllScreensExceptStart() {
        // Should not exist on start screen
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertDoesNotExist()

        // Should exist on all other screens
        listOf(CupcakeScreen.Flavor, CupcakeScreen.Pickup, CupcakeScreen.Summary).forEach { screen ->
            navigateToScreen(screen)
            composeTestRule.onNodeWithContentDescription(backText).assertExists()
        }
    }

    @Test
    fun cupcakeNavHost_completeNavigation_verifyBackStackStructure() {
        // Navigate through all screens
        navigateToSummaryScreen()

        // Verify navigation stack
        val backStack = navController.backStack.map { it.destination.route }
        assertEquals(expectedRoutes.size, backStack.size)
        assertTrue(backStack.containsAll(expectedRoutes))
    }
}