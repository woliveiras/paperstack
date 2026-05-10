package com.paperstack.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.domain.model.Paper
import com.paperstack.ui.feed.PaperCard
import com.paperstack.ui.theme.Spacing

@Composable
fun SavedScreen(
    onPaperClick: (Paper) -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var paperToRemove by remember { mutableStateOf<Paper?>(null) }

    paperToRemove?.let { paper ->
        AlertDialog(
            onDismissRequest = { paperToRemove = null },
            title = { Text("Remove from saved?") },
            text = { Text("\"${paper.title}\" will be removed. You'll lose this reference permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.remove(paper)
                        paperToRemove = null
                    },
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { paperToRemove = null }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
                )
                HorizontalDivider()
            }
        }

        when {
            state.isLoading -> Unit
            state.papers.isEmpty() -> EmptyState()
            else -> SavedList(
                papers = state.papers,
                onPaperClick = onPaperClick,
                onRemove = { paperToRemove = it },
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "No saved papers yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = "Papers you bookmark will appear here for easy access",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

@Composable
private fun SavedList(
    papers: List<Paper>,
    onPaperClick: (Paper) -> Unit,
    onRemove: (Paper) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items(items = papers, key = { it.id }) { paper ->
            PaperCard(
                paper = paper,
                isSaved = true,
                onClick = { onPaperClick(paper) },
                onToggleSave = { onRemove(paper) },
            )
        }
    }
}
