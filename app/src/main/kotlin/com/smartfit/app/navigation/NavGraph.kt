package com.smartfit.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.smartfit.app.data.local.datastore.UserPreferences
import com.smartfit.app.data.local.db.SmartFitDatabase
import com.smartfit.app.data.remote.RetrofitClient
import com.smartfit.app.data.repository.ActivityRepository
import com.smartfit.app.ui.screens.*
import com.smartfit.app.viewmodel.*

object Routes {
    const val SPLASH        = "splash"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val HOME          = "home"
    const val ACTIVITY_LOG  = "activity_log"
    const val ADD_ACTIVITY  = "add_activity"
    const val EDIT_ACTIVITY = "edit_activity/{activityId}"
    const val FOOD_LOG      = "food_log"
    const val ADD_FOOD      = "add_food"
    const val SUMMARY       = "summary"
    const val PROFILE       = "profile"
    const val SUGGESTIONS   = "suggestions"

    fun editActivity(id: Int) = "edit_activity/$id"
}

// ── ViewModel Factory helpers ──────────────────────────────────────
class AuthViewModelFactory(
    private val repo: ActivityRepository,
    private val prefs: UserPreferences
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>) =
        AuthViewModel(repo, prefs) as T
}

class ActivityViewModelFactory(
    private val repo: ActivityRepository,
    private val userId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>) =
        ActivityViewModel(repo, userId) as T
}

class ProfileViewModelFactory(
    private val repo: ActivityRepository,
    private val prefs: UserPreferences,
    private val userId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>) =
        ProfileViewModel(repo, prefs, userId) as T
}

@Composable
fun SmartFitNavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context    = LocalContext.current
    val db         = remember { SmartFitDatabase.getInstance(context) }
    val userPrefs  = remember { UserPreferences(context) }
    val repository = remember { 
        ActivityRepository(
            db.activityDao(), 
            db.userDao(), 
            db.foodDao(), 
            RetrofitClient.apiService,
            RetrofitClient.ninjaService
        ) 
    }

    val loggedInUserId by userPrefs.loggedInUserId.collectAsState(initial = -1)
    val isLoggedIn     by userPrefs.isLoggedIn.collectAsState(initial = false)
    val startDest      = if (isLoggedIn && loggedInUserId != -1) Routes.HOME else Routes.SPLASH

    val enter  = fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 6 }
    val exit   = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 6 }
    val pEnter = fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 6 }
    val pExit  = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 6 }

    NavHost(navController, startDestination = startDest,
        enterTransition = { enter }, exitTransition = { exit },
        popEnterTransition = { pEnter }, popExitTransition = { pExit }
    ) {

        composable(Routes.SPLASH) {
            SplashScreen {
                val dest = if (isLoggedIn && loggedInUserId != -1) Routes.HOME else Routes.LOGIN
                navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }

        composable(Routes.LOGIN) {
            val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository, userPrefs))
            LoginScreen(vm, isDarkTheme,
                onLoginSuccess  = { navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository, userPrefs))
            RegisterScreen(vm, isDarkTheme,
                onRegisterSuccess = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } } },
                onLoginClick      = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            val pvm: ProfileViewModel  = viewModel(factory = ProfileViewModelFactory(repository, userPrefs, loggedInUserId))
            HomeScreen(vm, pvm, isDarkTheme,
                onNavigate = { navController.navigate(it) }
            )
        }

        composable(Routes.ACTIVITY_LOG) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            ActivityLogScreen(vm, isDarkTheme,
                onAddClick  = { navController.navigate(Routes.ADD_ACTIVITY) },
                onEditClick = { navController.navigate(Routes.editActivity(it)) },
                onNavigate  = { navController.navigate(it) }
            )
        }

        composable(Routes.ADD_ACTIVITY) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            val pvm: ProfileViewModel  = viewModel(factory = ProfileViewModelFactory(repository, userPrefs, loggedInUserId))
            val profState by pvm.uiState.collectAsState()
            AddEditActivityScreen(vm, loggedInUserId, null,
                userWeightKg = profState.user?.weightKg ?: 70f,
                isDark = isDarkTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EDIT_ACTIVITY,
            arguments = listOf(navArgument("activityId") { type = NavType.IntType })
        ) { back ->
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            val pvm: ProfileViewModel  = viewModel(factory = ProfileViewModelFactory(repository, userPrefs, loggedInUserId))
            val profState by pvm.uiState.collectAsState()
            AddEditActivityScreen(vm, loggedInUserId,
                activityId   = back.arguments?.getInt("activityId"),
                userWeightKg = profState.user?.weightKg ?: 70f,
                isDark       = isDarkTheme,
                onBack       = { navController.popBackStack() }
            )
        }

        composable(Routes.FOOD_LOG) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            FoodLogScreen(vm, isDarkTheme,
                onAddClick = { navController.navigate(Routes.ADD_FOOD) },
                onNavigate = { navController.navigate(it) }
            )
        }

        composable(Routes.ADD_FOOD) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            AddFoodScreen(vm, loggedInUserId, isDarkTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SUMMARY) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            SummaryScreen(vm, isDarkTheme, onNavigate = { navController.navigate(it) })
        }

        composable(Routes.PROFILE) {
            val vm: ProfileViewModel  = viewModel(factory = ProfileViewModelFactory(repository, userPrefs, loggedInUserId))
            val authVm: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository, userPrefs))
            ProfileScreen(vm, authVm, isDarkTheme,
                onLogout   = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                onNavigate = { navController.navigate(it) }
            )
        }

        composable(Routes.SUGGESTIONS) {
            val vm: ActivityViewModel = viewModel(factory = ActivityViewModelFactory(repository, loggedInUserId))
            SuggestionsScreen(vm, isDarkTheme, onBack = { navController.popBackStack() })
        }
    }
}
