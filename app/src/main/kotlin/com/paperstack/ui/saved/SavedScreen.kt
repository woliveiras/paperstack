package com.paperstack.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.domain.model.Paper

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Saved") })
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Unit
            state.papers.isEmpty() -> EmptyState(modifier = Modifier.padding(innerPadding))
            else -> SavedList(
                papers = state.papers,
                onPaperClick = onPaperClick,
                onRemove = { paperToRemove = it },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No saved papers yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(items = papers, key = { it.id }) { paper ->
            SavedPaperCard(
                paper = paper,
                onClick = { onPaperClick(paper) },
                onRemove = { onRemove(paper) },
            )
        }
    }
}

@Composable
private fun SavedPaperCard(
    paper: Paper,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = paper.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Remove from saved",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val authorsText = when {
                paper.authors.size <= 3 -> paper.authors.joinToString(", ")
                else -> "${paper.authors.take(3).joinToString(", ")} et al."
            }
            Text(
                text = authorsText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = paper.submittedDate.take(10),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!paper.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paper.comment,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = paper.abstract.take(200) + if (paper.abstract.length > 200) "…" else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
