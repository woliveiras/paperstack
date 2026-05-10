package com.paperstack.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = viewModel::toggleSave,
                        enabled = !state.isTogglingSave,
                    ) {
                        Icon(
                            imageVector = if (state.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (state.isSaved) "Saved" else "Save",
                            tint = if (state.isSaved) MaterialTheme.colorScheme.onSurface
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                HorizontalDivider()
            }
        }

        state.paper?.let { paper ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md),
            ) {
                Spacer(modifier = Modifier.height(Spacing.lg))

                // Serif title
                Text(
                    text = paper.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Authors
                Text(
                    text = paper.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                // Metadata chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    MetadataChip(text = paper.submittedDate.take(10))
                    MetadataChip(text = paper.primaryCategory)
                    if (!paper.comment.isNullOrBlank()) {
                        MetadataChip(text = paper.comment)
                    }
                }

                if (paper.categories.size > 1) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        paper.categories.drop(1).forEach { cat ->
                            MetadataChip(text = cat)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.lg))

                // Abstract header
                Text(
                    text = "ABSTRACT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f,
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = paper.abstract,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(Spacing.xl))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.md))

                // Stacked action buttons
                Button(
                    onClick = viewModel::toggleSave,
                    enabled = !state.isTogglingSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        imageVector = if (state.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(if (state.isSaved) "Saved" else "Save")
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                OutlinedButton(
                    onClick = {
                        val uri = Uri.parse("https://arxiv.org/abs/${paper.id}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        Icons.Filled.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Read online")
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                when (val dl = state.downloadState) {
                    is DownloadState.Idle -> OutlinedButton(
                        onClick = { viewModel.download(paper) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text("Download PDF")
                    }

                    is DownloadState.Downloading -> Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            val pct = (dl.progress * 100).toInt()
                            Text(if (pct > 0) "Downloading $pct%" else "Downloading…")
                        }
                        if (dl.progress > 0f) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            LinearProgressIndicator(
                                progress = { dl.progress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    is DownloadState.Downloaded -> OutlinedButton(
                        onClick = { viewModel.openPdf(paper, context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                    ) {
                        Icon(
                            Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text("Open PDF")
                    }

                    is DownloadState.Error -> Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = dl.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        OutlinedButton(
                            onClick = { viewModel.download(paper) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Retry download")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        )
    }
}
