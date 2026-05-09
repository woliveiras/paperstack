package com.paperstack.ui.feed

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperstack.domain.model.ARXIV_CATEGORIES
import com.paperstack.domain.model.Settings

private fun categoryDisplayName(code: String): String {
    val name = ARXIV_CATEGORIES.firstOrNull { it.code == code }?.name
    return if (name != null) "$name ($code)" else code
}

@Composable
fun DrawerContent(
    settings: Settings,
    onCategorySelected: (String) -> Unit,
    onAddCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier.width(280.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Categories",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        settings.selectedCategories.forEach { code ->
            NavigationDrawerItem(
                label = { Text(categoryDisplayName(code)) },
                selected = code == settings.activeCategory,
                onClick = { onCategorySelected(code) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text("+ Add categories") },
            selected = false,
            onClick = onAddCategories,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}
