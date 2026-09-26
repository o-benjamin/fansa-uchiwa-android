package com.fansauchiwa.analytics.featuredoor

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureDoorOrderTest {

    @Test
    fun shuffled_anySeed_containsEachDoorExactlyOnce() {
        (0 until 50).forEach { seed ->
            val order = FeatureDoorOrder.shuffled(Random(seed))

            assertEquals(FeatureDoor.entries.size, order.size)
            assertEquals(FeatureDoor.entries.toSet(), order.toSet())
        }
    }

    @Test
    fun shuffled_sameSeed_returnsSameOrder() {
        assertEquals(
            FeatureDoorOrder.shuffled(Random(42)),
            FeatureDoorOrder.shuffled(Random(42))
        )
    }

    @Test
    fun shuffled_manySeeds_putsEveryDoorFirstAtLeastOnce() {
        // 並びが固定だと上の入口ほど押されやすく比較がゆがむため、先頭が入れ替わることを確かめる
        val firstDoors = (0 until 100).map { FeatureDoorOrder.shuffled(Random(it)).first() }.toSet()

        assertTrue(firstDoors.containsAll(FeatureDoor.entries))
    }

    @Test
    fun forThisLaunch_readTwice_returnsSameOrder() {
        assertEquals(FeatureDoorOrder.forThisLaunch, FeatureDoorOrder.forThisLaunch)
    }
}
