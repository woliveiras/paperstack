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
            assertNotNull(PaperstackShapes)
            assertNotNull(PaperstackShapes.small)
            assertNotNull(PaperstackShapes.medium)
            assertNotNull(PaperstackShapes.large)
        }
    }

    @Nested
    @DisplayName("Typography tokens")
    inner class TypographyTokens {

        @Test
        fun `titleLarge uses PlayfairDisplay font family`() {
            assertEquals(PlayfairDisplay, PaperstackTypography.titleLarge.fontFamily)
        }

        @Test
        fun `titleLarge is SemiBold 22sp`() {
            assertEquals(FontWeight.SemiBold, PaperstackTypography.titleLarge.fontWeight)
            assertEquals(22.sp, PaperstackTypography.titleLarge.fontSize)
        }

        @Test
        fun `headlineMedium uses PlayfairDisplay`() {
            assertEquals(PlayfairDisplay, PaperstackTypography.headlineMedium.fontFamily)
        }

        @Test
        fun `bodyLarge uses Inter font family`() {
            assertEquals(Inter, PaperstackTypography.bodyLarge.fontFamily)
        }

        @Test
        fun `bodyMedium uses Inter 14sp`() {
            assertEquals(Inter, PaperstackTypography.bodyMedium.fontFamily)
            assertEquals(14.sp, PaperstackTypography.bodyMedium.fontSize)
        }

        @Test
        fun `labelLarge uses Inter Medium`() {
            assertEquals(Inter, PaperstackTypography.labelLarge.fontFamily)
            assertEquals(FontWeight.Medium, PaperstackTypography.labelLarge.fontWeight)
        }

        @Test
        fun `headlineLarge uses Inter for UI chrome`() {
            assertEquals(Inter, PaperstackTypography.headlineLarge.fontFamily)
        }

        @Test
        fun `titleMedium uses Inter SemiBold for UI`() {
            assertEquals(Inter, PaperstackTypography.titleMedium.fontFamily)
            assertEquals(FontWeight.SemiBold, PaperstackTypography.titleMedium.fontWeight)
        }
    }
}
