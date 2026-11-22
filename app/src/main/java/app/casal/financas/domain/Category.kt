package app.casal.financas.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

enum class Category(val label: String, val icon: ImageVector, val color: Color) {
    MARKET("Mercado", Icons.Rounded.ShoppingCart, Color(0xFF2E9E6B)),
    FOOD("Restaurante", Icons.Rounded.Restaurant, Color(0xFFE8793A)),
    TRANSPORT("Transporte", Icons.Rounded.DirectionsCar, Color(0xFF3F7CD8)),
    HOME("Casa", Icons.Rounded.Home, Color(0xFF8A63D2)),
    BILLS("Contas", Icons.Rounded.Receipt, Color(0xFF5B6B7A)),
    HEALTH("Saúde", Icons.Rounded.Favorite, Color(0xFFD9506A)),
    LEISURE("Lazer", Icons.Rounded.Celebration, Color(0xFFE0A526)),
    SHOPPING("Compras", Icons.Rounded.ShoppingBag, Color(0xFFC25AA8)),
    EDUCATION("Educação", Icons.Rounded.School, Color(0xFF2A9DB5)),
    OTHER("Outros", Icons.Rounded.MoreHoriz, Color(0xFF8C8C8C));

    companion object {
        fun fromKey(key: String?): Category = entries.firstOrNull { it.name == key } ?: OTHER
    }
}
