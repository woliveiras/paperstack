package com.paperstack.ui.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.domain.model.ARXIV_CATEGORIES
import com.paperstack.domain.model.Paper
import com.paperstack.domain.model.Settings
import com.paperstack.ui.theme.Spacing
import kotlinx.coroutines.launch

private const val PAPER_CONTENT_TYPE = "paper"
private const val LOAD_MORE_CONTENT_TYPE = "load_more"
private const val ABSTRACT_PREVIEW_LENGTH = 120
private const val ABSTRACT_MAX_LINES = 2
private const val TITLE_MAX_LINES = 2

@Composable
fun FeedScreen(
    settings: Settings,
    onPaperClick: (Paper) -> Unit,
    onAddCategories: () -> Unit,
    onCategorySwitch: (String) -> Unit,
    onNavigateToFilter: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (drawerState.isOpen || drawerState.targetValue == DrawerValue.Open) {
                DrawerContent(
                    settings = settings,
                    onCategorySelected = { code ->
                        onCategorySwitch(code)
                        scope.launch { drawerState.close() }
                    },
                    onAddCategories = {
                        scope.launch { drawerState.close() }
                        onAddCategories()
                    },
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedTopBar(
                currentCategory = ARXIV_CATEGORIES.firstOrNull { it.code == settings.activeCategory }?.name
                    ?: settings.activeCategory,
                onMenuClick = { scope.launch { drawerState.open() } },
                onFilterClick = onNavigateToFilter,
            )
            when {
                state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
                state.error != null && state.visiblePapers.isEmpty() -> ErrorContent(
                    message = state.error ?: "Unknown error",
                    onRetry = { viewModel.retry(settings.activeCategory) },
                    modifier = Modifier.weight(1f),
                )
                else -> FeedContent(
                    papers = state.visiblePapers,
                    savedIds = savedIds,
                    canShowLoadMore = state.canShowLoadMore,
                    isPrefetching = state.isPrefetching,
                    hasBufferedPapers = state.buffer.isNotEmpty(),
                    onPaperClick = onPaperClick,
                    onLoadMore = viewModel::loadMore,
                    onToggleSave = viewModel::toggleSave,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FeedTopBar(
    currentCategory: String,
    onMenuClick: () -> Unit,
    onFilterClick: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open menu",
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "PaperStack",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = currentCategory,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filter papers",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(Spacing.md))
        Button(onClick = onRetry) { Text("Retry") }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FeedContent(
    papers: List<Paper>,
    savedIds: Set<String>,
    canShowLoadMore: Boolean,
    isPrefetching: Boolean,
    hasBufferedPapers: Boolean,
    onPaperClick: (Paper) -> Unit,
    onLoadMore: () -> Unit,
    onToggleSave: (Paper) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items(
            items = papers,
            key = { it.id },
            contentType = { PAPER_CONTENT_TYPE },
        ) { paper ->
            FeedPaperItem(
                paper = paper,
                isSaved = paper.id in savedIds,
                onPaperClick = onPaperClick,
                onToggleSave = onToggleSave,
            )
        }

        if (canShowLoadMore) {
            item(contentType = LOAD_MORE_CONTENT_TYPE) {
                TextButton(
                    onClick = onLoadMore,
                    enabled = !isPrefetching || hasBufferedPapers,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.sm),
                ) {
                    if (isPrefetching && !hasBufferedPapers) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                    }
                    Text(
                        text = "Load more papers",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedPaperItem(
    paper: Paper,
    isSaved: Boolean,
    onPaperClick: (Paper) -> Unit,
    onToggleSave: (Paper) -> Unit,
) {
    val display = remember(paper.id, paper.title, paper.authors, paper.submittedDate, paper.abstract, paper.comment) {
        PaperCardDisplay(
            authorsText = when {
                paper.authors.size <= 3 -> paper.authors.joinToString(", ")
                else -> "${paper.authors.take(3).joinToString(", ")} et al."
            },
            dateText = paper.submittedDate.take(10),
            abstractPreview = paper.abstract.take(ABSTRACT_PREVIEW_LENGTH) +
                if (paper.abstract.length > ABSTRACT_PREVIEW_LENGTH) "…" else "",
            comment = paper.comment?.takeIf { it.isNotBlank() },
        )
    }
    val onClick = remember(paper.id, onPaperClick) { { onPaperClick(paper) } }
    val onSave = remember(paper.id, onToggleSave) { { onToggleSave(paper) } }
    PaperCard(
        title = paper.title,
        display = display,
        isSaved = isSaved,
        onClick = onClick,
        onToggleSave = onSave,
    )
}

private data class PaperCardDisplay(
    val authorsText: String,
    val dateText: String,
    val abstractPreview: String,
    val comment: String?,
)

@Composable
internal fun PaperCard(
    paper: Paper,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = remember(paper.id, paper.authors, paper.submittedDate, paper.abstract, paper.comment) {
        PaperCardDisplay(
            authorsText = when {
                paper.authors.size <= 3 -> paper.authors.joinToString(", ")
                else -> "${paper.authors.take(3).joinToString(", ")} et al."
            },
            dateText = paper.submittedDate.take(10),
            abstractPreview = paper.abstract.take(ABSTRACT_PREVIEW_LENGTH) +
                if (paper.abstract.length > ABSTRACT_PREVIEW_LENGTH) "…" else "",
            comment = paper.comment?.takeIf { it.isNotBlank() },
        )
    }
    PaperCard(
        title = paper.title,
        display = display,
        isSaved = isSaved,
        onClick = onClick,
        onToggleSave = onToggleSave,
        modifier = modifier,
    )
}

@Composable
private fun PaperCard(
    title: String,
    display: PaperCardDisplay,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = TITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isSaved) "Remove from saved" else "Save paper",
                    tint = if (isSaved) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableWithoutRipple(onToggleSave),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = display.authorsText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = display.dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            )
            if (display.comment != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = display.comment,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = display.abstractPreview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = ABSTRACT_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
