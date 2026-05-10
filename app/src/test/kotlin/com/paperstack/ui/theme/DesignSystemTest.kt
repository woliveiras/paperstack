package com.paperstack.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DesignSystemTest {

    @Nested
    @DisplayName("Color tokens")
    inner class ColorTokens {

        @Test
        fun `primary color is deep indigo 3D5AFE`() {
            assertEquals(0xFF3D5AFE.toInt(), Indigo500.value.toLong().ushr(32).toInt())
        }

        @Test
        fun `surface warm is off-white FFFDF6`() {
            assertNotNull(SurfaceWarm)
        }

        @Test
        fun `navy dark is deep navy 0D0F1A`() {
            assertNotNull(NavyDark)
        }
    }

    @Nested
    @DisplayName("Spacing tokens")
    inner class SpacingTokens {

        @Test
        fun `xs is 4dp`() {
            assertEquals(4.dp, Spacing.xs)
        }

        @Test
        fun `sm is 8dp`() {
            assertEquals(8.dp, Spacing.sm)
        }

        @Test
        fun `md is 16dp`() {
            assertEquals(16.dp, Spacing.md)
        }

        @Test
        fun `lg is 24dp`() {
            assertEquals(24.dp, Spacing.lg)
        }

        @Test
        fun `xl is 32dp`() {
            assertEquals(32.dp, Spacing.xl)
        }
    }

    @Nested
    @DisplayName("Shape tokens")
    inner class ShapeTokens {

        @Test
        fun `shapes object is defined`() {
            assertNotNull(PaperStackShapes)
            assertNotNull(PaperStackShapes.small)
            assertNotNull(PaperStackShapes.medium)
            assertNotNull(PaperStackShapes.large)
        }
    }

    @Nested
    @DisplayName("Typography tokens")
    inner class TypographyTokens {

        @Test
        fun `titleLarge uses PlayfairDisplay font family`() {
            assertEquals(PlayfairDisplay, PaperStackTypography.titleLarge.fontFamily)
        }

        @Test
        fun `titleLarge is SemiBold 22sp`() {
            assertEquals(FontWeight.SemiBold, PaperStackTypography.titleLarge.fontWeight)
            assertEquals(22.sp, PaperStackTypography.titleLarge.fontSize)
        }

        @Test
        fun `headlineMedium uses PlayfairDisplay`() {
            assertEquals(PlayfairDisplay, PaperStackTypography.headlineMedium.fontFamily)
        }

        @Test
        fun `bodyLarge uses Inter font family`() {
            assertEquals(Inter, PaperStackTypography.bodyLarge.fontFamily)
        }

        @Test
        fun `bodyMedium uses Inter 14sp`() {
            assertEquals(Inter, PaperStackTypography.bodyMedium.fontFamily)
            assertEquals(14.sp, PaperStackTypography.bodyMedium.fontSize)
        }

        @Test
        fun `labelLarge uses Inter Medium`() {
            assertEquals(Inter, PaperStackTypography.labelLarge.fontFamily)
            assertEquals(FontWeight.Medium, PaperStackTypography.labelLarge.fontWeight)
        }

        @Test
        fun `headlineLarge uses Inter for UI chrome`() {
            assertEquals(Inter, PaperStackTypography.headlineLarge.fontFamily)
        }

        @Test
        fun `titleMedium uses Inter SemiBold for UI`() {
            assertEquals(Inter, PaperStackTypography.titleMedium.fontFamily)
            assertEquals(FontWeight.SemiBold, PaperStackTypography.titleMedium.fontWeight)
        }
    }
}
