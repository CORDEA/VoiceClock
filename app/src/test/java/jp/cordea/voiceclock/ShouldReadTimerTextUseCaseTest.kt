package jp.cordea.voiceclock

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration

class ShouldReadTimerTextUseCaseTest {
    @Test
    fun execute() {
        val useCase = ShouldReadTimerTextUseCase()

        assertThat(useCase.execute(Duration.ZERO, 10)).isFalse()

        assertThat(useCase.execute(Duration.ofMinutes(1), 0)).isTrue()
        assertThat(useCase.execute(Duration.ofMinutes(2), 0)).isTrue()
        assertThat(useCase.execute(Duration.ofHours(1), 0)).isTrue()

        assertThat(useCase.execute(Duration.ofSeconds(40), 40)).isTrue()
        assertThat(useCase.execute(Duration.ofSeconds(100), 40)).isTrue()
        assertThat(useCase.execute(Duration.ofSeconds(80), 40)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(360), 40)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(1), 40)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(50), 40)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(60), 40)).isFalse()

        assertThat(useCase.execute(Duration.ofMinutes(1), 60)).isTrue()
        assertThat(useCase.execute(Duration.ofMinutes(61), 60)).isTrue()
        assertThat(useCase.execute(Duration.ofMinutes(121), 60)).isTrue()
        assertThat(useCase.execute(Duration.ofSeconds(61), 60)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(110), 60)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(59), 60)).isFalse()
        assertThat(useCase.execute(Duration.ofMinutes(2), 60)).isFalse()
        assertThat(useCase.execute(Duration.ofMinutes(50), 60)).isFalse()
        assertThat(useCase.execute(Duration.ofMinutes(11), 60)).isFalse()

        assertThat(useCase.execute(Duration.ofMinutes(2), 120)).isTrue()
        assertThat(useCase.execute(Duration.ofMinutes(62), 120)).isTrue()
        assertThat(useCase.execute(Duration.ofMinutes(121), 120)).isFalse()
        assertThat(useCase.execute(Duration.ofMinutes(50), 120)).isFalse()
        assertThat(useCase.execute(Duration.ofMinutes(11), 120)).isFalse()

        assertThat(useCase.execute(Duration.ofHours(1), 3600)).isTrue()
        assertThat(useCase.execute(Duration.ofSeconds(3601), 3600)).isFalse()
        assertThat(useCase.execute(Duration.ofSeconds(3660), 3600)).isFalse()
        assertThat(useCase.execute(Duration.ofHours(2), 3600)).isFalse()
        assertThat(useCase.execute(Duration.ofHours(3), 3600)).isFalse()
        assertThat(useCase.execute(Duration.ofHours(50), 3600)).isFalse()
        assertThat(useCase.execute(Duration.ofHours(11), 3600)).isFalse()
    }
}
