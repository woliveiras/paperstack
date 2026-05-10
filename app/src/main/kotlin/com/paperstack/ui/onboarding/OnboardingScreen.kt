package com.paperstack.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.R
import com.paperstack.domain.model.ARXIV_CATEGORIES
import com.paperstack.ui.theme.Spacing

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.isDone) {
        onComplete()
        return
    }

    Scaffold { innerPadding ->
        when (state.step) {
            OnboardingStep.Name -> NameStep(
                state = state,
                onNameChange = viewModel::setName,
                onContinue = viewModel::nextStep,
                modifier = Modifier.padding(innerPadding),
            )

            OnboardingStep.Categories -> CategoriesStep(
                state = state,
                onToggleCategory = viewModel::toggleCategory,
                onConfirm = viewModel::completeOnboarding,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun NameStep(
    state: OnboardingState,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md, vertical = Spacing.xl),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_paperstack),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Welcome to Paperstack",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "What should we call you?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onNameChange,
                placeholder = { Text("Your name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = onContinue,
            enabled = state.isNameValid,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.lg),
        ) {
            Text(
                text = "Continue",
                modifier = Modifier.padding(vertical = Spacing.sm),
            )
        }
    }
}

@Composable
internal fun CategoriesStep(
    state: OnboardingState,
    onToggleCategory: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "Get Started",
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Sticky header
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
        ) {
            Text(
                text = "Choose your topics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "Select at least one category to follow.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider()

        // Scrollable category list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            items(items = ARXIV_CATEGORIES, key = { it.code }) { category ->
                val isSelected = state.selectedCategories.contains(category.code)
                CategoryRow(
                    name = category.name,
                    code = category.code,
                    isSelected = isSelected,
                    onToggle = { onToggleCategory(category.code) },
                )
            }
        }

        // Sticky bottom bar
        HorizontalDivider()
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onConfirm,
                enabled = state.canProceedFromCategories && !state.isSaving,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.isSaving) "Saving…" else confirmLabel,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    fontWeight = FontWeight.Medium,
                )
            }
            if (state.selectedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                val count = state.selectedCategories.size
                Text(
                    text = "$count ${if (count == 1) "category" else "categories"} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.error?.let { msg ->
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    name: String,
    code: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.sm))
            .clickable(onClick = onToggle)
            .padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (isSelected) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp),
                        )
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
