package com.example.lambda_bbb_events.model.services

import com.example.lambda_bbb_events.model.services.ImageProcessor.Companion.sampleSizeFor
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies image processing helpers. */
class ImageProcessorUnitTest {

    @Test
    fun noTargetDecodesFullSize() {
        assertEquals(1, sampleSizeFor(1440, 2560, null, null))
        assertEquals(1, sampleSizeFor(1440, 2560, 0, -5))
    }

    @Test
    fun targetLargerThanImageDecodesFullSize() {
        assertEquals(1, sampleSizeFor(800, 600, 1080, 720))
    }

    @Test
    fun shrinksByLargestPowerOfTwoThatStillCoversWidth() {
        // 1440 / 4 = 360 still covers 360, 1440 / 8 = 180 does not
        assertEquals(4, sampleSizeFor(1440, 2560, 360, null))
        assertEquals(2, sampleSizeFor(1440, 2560, 361, null))
    }

    @Test
    fun heightLimitsShrinkingForWideImagesInTallSlots() {
        // A 120x130dp card slot at 3x density, showing a landscape photo cropped to fill
        assertEquals(2, sampleSizeFor(1440, 960, 360, 390))
        // Width alone would allow 4, which would leave the image blurry when cropped
        assertEquals(4, sampleSizeFor(1440, 960, 360, null))
    }

    @Test
    fun heightOnlyTargetIsRespected() {
        assertEquals(8, sampleSizeFor(1440, 2560, null, 320))
    }

    @Test
    fun invalidSourceDecodesFullSize() {
        assertEquals(1, sampleSizeFor(0, 0, 100, 100))
    }
}
