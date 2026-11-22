package app.casal.financas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.casal.financas.ui.AppRoot
import app.casal.financas.ui.LoadingScreen
import app.casal.financas.ui.Session
import app.casal.financas.ui.SessionViewModel
import app.casal.financas.ui.auth.AuthScreen
import app.casal.financas.ui.auth.PairingScreen
import app.casal.financas.ui.theme.CasalTheme

class MainActivity : ComponentActivity() {
    private val sessionVm: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CasalTheme {
                val session by sessionVm.session.collectAsStateWithLifecycle()
                val action by sessionVm.action.collectAsStateWithLifecycle()
                when (val s = session) {
                    Session.Loading -> LoadingScreen()
                    Session.SignedOut -> AuthScreen(action, sessionVm::signIn, sessionVm::signUp)
                    is Session.NeedsHousehold -> PairingScreen(
                        displayName = s.displayName,
                        action = action,
                        onCreate = { sessionVm.createHousehold(s.uid, s.displayName) },
                        onJoin = { code -> sessionVm.joinHousehold(s.uid, s.displayName, code) },
                        onSignOut = sessionVm::signOut,
                    )
                    is Session.Ready -> AppRoot(s)
                }
            }
        }
    }
}
