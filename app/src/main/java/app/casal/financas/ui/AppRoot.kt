package app.casal.financas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.casal.financas.data.ServiceLocator
import app.casal.financas.ui.budget.BudgetScreen
import app.casal.financas.ui.expense.ExpenseEditorScreen
import app.casal.financas.ui.home.HistoryScreen
import app.casal.financas.ui.home.HomeScreen
import app.casal.financas.ui.settings.SettingsScreen
import app.casal.financas.ui.settings.shareInvite

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Início", Icons.Rounded.Home),
    EXPENSES("expenses", "Gastos", Icons.Rounded.Receipt),
    BUDGET("budget", "Orçamento", Icons.Rounded.AccountBalanceWallet),
    SETTINGS("settings", "Ajustes", Icons.Rounded.Settings),
}

private const val EDITOR = "expense/{id}"

/** App logado e pareado: barra inferior + navegação entre as telas. */
@Composable
fun AppRoot(ready: Session.Ready) {
    val vm: FinanceViewModel = viewModel(
        key = ready.household.id + ready.uid,
        factory = FinanceViewModel.Factory(ServiceLocator.repository, ready.uid, ready.household),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val context = LocalContext.current

    val s = state
    if (s == null) {
        LoadingScreen()
        return
    }

    fun openEditor(id: String?) = nav.navigate("expense/${id ?: "new"}")

    Scaffold(
        bottomBar = {
            if (Tab.entries.any { it.route == route }) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Tab.HOME.route, modifier = Modifier.padding(padding)) {
            composable(Tab.HOME.route) {
                HomeScreen(
                    s, vm::previousCycle, vm::nextCycle, vm::goToCurrentCycle,
                    onAddExpense = { openEditor(null) },
                    onOpenExpense = { openEditor(it) },
                    onSeeAll = { nav.navigate(Tab.EXPENSES.route) },
                    onEditBudget = { nav.navigate(Tab.BUDGET.route) },
                    onShareInvite = { shareInvite(context, s.household.inviteCode) },
                )
            }
            composable(Tab.EXPENSES.route) {
                HistoryScreen(s, vm::previousCycle, vm::nextCycle, vm::goToCurrentCycle, { openEditor(null) }, { openEditor(it) })
            }
            composable(Tab.BUDGET.route) {
                BudgetScreen(
                    s, vm::previousCycle, vm::nextCycle, vm::goToCurrentCycle,
                    onSave = { vm.saveBudgets(s.cycleId, it) },
                    onCopyPrevious = { done -> vm.copyBudgetsFromPrevious(s.cycleId, done) },
                )
            }
            composable(Tab.SETTINGS.route) {
                SettingsScreen(s.household, s.me, vm::setCycleStartDay, vm::signOut)
            }
            composable(EDITOR, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id")?.takeIf { it != "new" }
                val existing = id?.let { eid -> s.expenses.firstOrNull { it.id == eid } }
                ExpenseEditorScreen(
                    state = s,
                    existing = existing,
                    onBack = { nav.popBackStack() },
                    onSave = { owner, cents, desc, cat, date -> vm.saveExpense(existing, owner, cents, desc, cat, date) },
                    onDelete = vm::deleteExpense,
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
