package com.paperstack.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paperstack.domain.model.ARXIV_CATEGORIES

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Welcome to Paperstack",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.displayName,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onContinue,
            enabled = state.isNameValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue")
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
    val grouped = ARXIV_CATEGORIES.groupBy { it.group }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = onConfirm,
                    enabled = state.canProceedFromCategories && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.isSaving) "Saving…" else confirmLabel)
                }
                state.error?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "Choose your topics",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select at least one category to follow.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            grouped.forEach { (group, categories) ->
                item(key = "header_$group") {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                items(items = categories, key = { it.code }) { category ->
                    val checked = state.selectedCategories.contains(category.code)
                    ListItem(
                        headlineContent = { Text(category.name) },
                        supportingContent = { Text(category.code) },
                        trailingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onToggleCategory(category.code) },
                            )
                        },
                        modifier = Modifier.clickable {
                            onToggleCategory(category.code)
                        },
                    )
                }
            }
        }
    }
}
