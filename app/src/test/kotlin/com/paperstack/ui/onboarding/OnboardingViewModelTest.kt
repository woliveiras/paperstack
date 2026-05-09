package com.paperstack.ui.onboarding

import app.cash.turbine.test
import com.paperstack.data.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(settingsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `Initial state` {

        @Test
        fun `starts on Name step`() = runTest {
            viewModel.state.test {
                assertEquals(OnboardingStep.Name, awaitItem().step)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `display name is empty`() = runTest {
            viewModel.state.test {
                assertTrue(awaitItem().displayName.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `no categories selected`() = runTest {
            viewModel.state.test {
                assertTrue(awaitItem().selectedCategories.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setName` {

        @Test
        fun `updates displayName in state`() = runTest {
            viewModel.state.test {
                skipItems(1) // initial
                viewModel.setName("Alice")
                assertEquals("Alice", awaitItem().displayName)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `isNameValid is false for blank name`() = runTest {
            viewModel.setName("   ")
            viewModel.state.test {
                assertFalse(awaitItem().isNameValid)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `isNameValid is false for name longer than 50 chars`() = runTest {
            viewModel.setName("A".repeat(51))
            viewModel.state.test {
                assertFalse(awaitItem().isNameValid)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `isNameValid is true for valid name`() = runTest {
            viewModel.setName("Alice")
            viewModel.state.test {
                assertTrue(awaitItem().isNameValid)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `nextStep` {

        @Test
        fun `moves to Categories step when name is valid`() = runTest {
            viewModel.setName("Alice")
            viewModel.state.test {
                skipItems(1) // state after setName
                viewModel.nextStep()
                assertEquals(OnboardingStep.Categories, awaitItem().step)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `stays on Name step when name is invalid`() = runTest {
            viewModel.setName("")
            viewModel.nextStep()
            // No state change — check current value directly
            assertEquals(OnboardingStep.Name, viewModel.state.value.step)
        }
    }

    @Nested
    inner class `toggleCategory` {

        @Test
        fun `adds category when not selected`() = runTest {
            viewModel.state.test {
                skipItems(1) // initial
                viewModel.toggleCategory("cs.AI")
                val state = awaitItem()
                assertTrue(state.selectedCategories.contains("cs.AI"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `removes category when already selected`() = runTest {
            viewModel.toggleCategory("cs.AI")
            viewModel.state.test {
                skipItems(1) // state after first toggle
                viewModel.toggleCategory("cs.AI")
                val state = awaitItem()
                assertFalse(state.selectedCategories.contains("cs.AI"))
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `can select multiple categories`() = runTest {
            viewModel.toggleCategory("cs.AI")
            viewModel.toggleCategory("cs.LG")
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state.selectedCategories.containsAll(listOf("cs.AI", "cs.LG")))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `completeOnboarding` {

        @BeforeEach
        fun setUpValidState() {
            viewModel.setName("Alice")
            viewModel.toggleCategory("cs.AI")
        }

        @Test
        fun `calls repository with trimmed name and selected categories`() = runTest {
            coEvery { settingsRepository.completeOnboarding(any(), any()) } returns Unit

            viewModel.completeOnboarding()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                settingsRepository.completeOnboarding("Alice", listOf("cs.AI"))
            }
        }

        @Test
        fun `sets isDone true on success`() = runTest {
            coEvery { settingsRepository.completeOnboarding(any(), any()) } returns Unit

            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertTrue(viewModel.state.value.isDone)
        }

        @Test
        fun `sets error message on repository failure`() = runTest {
            coEvery { settingsRepository.completeOnboarding(any(), any()) } throws Exception("network error")

            viewModel.completeOnboarding()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isDone)
            assertEquals("network error", state.error)
        }

        @Test
        fun `sets isSaving true while repository call is in progress`() = runTest {
            coEvery { settingsRepository.completeOnboarding(any(), any()) } returns Unit

            viewModel.state.test {
                skipItems(1) // current state (after setName + toggleCategory)
                viewModel.completeOnboarding()
                val savingState = awaitItem() // isSaving = true emitted before launch
                assertTrue(savingState.isSaving)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `error is null on initial state`() = runTest {
            viewModel.state.test {
                assertNull(awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
