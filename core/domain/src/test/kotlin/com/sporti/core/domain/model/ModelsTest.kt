package com.sporti.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ModelsTest {
    @Test
    fun `training session keeps lap order`() {
        val session = TrainingSession(durationMillis = 3_000, lapsMillis = listOf(900, 1_100, 1_000), createdAtMillis = 1)
        assertThat(session.lapsMillis).containsExactly(900L, 1_100L, 1_000L).inOrder()
    }
}
