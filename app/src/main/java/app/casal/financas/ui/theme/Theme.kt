package app.casal.financas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

/** Cores das duas pessoas — usadas em cartões, barras e seletores para ficar fácil de distinguir. */
object PersonColors {
    val me = Color(0xFF4F46E5)      // índigo
    val partner = Color(0xFFE0457B) // rosa
    val ok = Color(0xFF16A36A)
    val danger = Color(0xFFD64545)
}

private val Light = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF1EA),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4F46E5),
    tertiary = Color(0xFFE0457B),
    background = Color(0xFFF5F7F8),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7ECEE),
    surfaceContainer = Color(0xFFEEF2F3),
    onSurfaceVariant = Color(0xFF55646B),
    error = PersonColors.danger,
)

private val Dark = darkColorScheme(
    primary = Color(0xFF4FD1C0),
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF0B4F49),
    onPrimaryContainer = Color(0xFFCCF1EA),
    secondary = Color(0xFF9A93FF),
    tertiary = Color(0xFFFF8DB3),
    background = Color(0xFF0E1415),
    surface = Color(0xFF172022),
    surfaceVariant = Color(0xFF263235),
    surfaceContainer = Color(0xFF1D282A),
    onSurfaceVariant = Color(0xFFA9B8BD),
    error = Color(0xFFFF7A7A),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun CasalTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
