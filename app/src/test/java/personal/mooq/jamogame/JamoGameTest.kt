package personal.mooq.jamogame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import personal.mooq.jamogame.game.GameResult
import personal.mooq.jamogame.game.GameStatus
import personal.mooq.jamogame.game.JamoGame
import personal.mooq.jamogame.game.MAX_ATTEMPTS
import personal.mooq.jamogame.game.Match
import org.junit.Test

class JamoGameTest {

    private fun JamoGame.guess(vararg jamos: String) {
        jamos.forEach { press(it) }
        submit()
    }

    private fun JamoGame.lastMatches() = submitted.last().map { it.match }

    @Test
    fun `자리까지 맞으면 초록 자모만 맞으면 노랑 없으면 회색`() {
        val game = JamoGame("단어") // ㄷ ㅏ ㄴ ㅇ ㅓ
        game.guess("ㄷ", "ㅓ", "ㅁ", "ㅏ", "ㄴ")
        assertEquals(
            listOf(Match.CORRECT, Match.PRESENT, Match.ABSENT, Match.PRESENT, Match.PRESENT),
            game.lastMatches(),
        )
    }

    @Test
    fun `정답에 든 개수보다 많이 입력한 중복 자모는 회색으로 남는다`() {
        val game = JamoGame("단어") // ㅇ 은 한 개뿐이라 노랑도 한 개까지만
        game.guess("ㅇ", "ㅇ", "ㅋ", "ㅋ", "ㅋ")
        assertEquals(
            listOf(Match.PRESENT, Match.ABSENT, Match.ABSENT, Match.ABSENT, Match.ABSENT),
            game.lastMatches(),
        )
    }

    @Test
    fun `자리를 맞춘 자모가 먼저 초록을 가져간다`() {
        val game = JamoGame("단어")
        game.guess("ㅇ", "ㅋ", "ㅋ", "ㅇ", "ㅓ") // 네 번째 ㅇ 이 제자리
        assertEquals(
            listOf(Match.ABSENT, Match.ABSENT, Match.ABSENT, Match.CORRECT, Match.CORRECT),
            game.lastMatches(),
        )
    }

    @Test
    fun `정답을 입력하면 사전 검사 없이 알아본다`() {
        val game = JamoGame("단어")
        listOf("ㄷ", "ㅏ", "ㄴ", "ㅇ").forEach { game.press(it) }
        assertEquals(false, game.isInputAnswer)
        game.press("ㅓ")
        assertTrue(game.isInputAnswer)
    }

    @Test
    fun `입력 비교는 자모 목록과 맞춰본다`() {
        // 입력은 SnapshotStateList 라 == 로 비교하면 늘 false 다. 그 함정을 막는 테스트.
        val game = JamoGame("단어")
        listOf("ㄱ", "ㅏ", "ㅂ", "ㅏ", "ㅇ").forEach { game.press(it) }
        assertTrue(game.inputMatches(listOf("ㄱ", "ㅏ", "ㅂ", "ㅏ", "ㅇ")))
        assertEquals(false, game.inputMatches(listOf("ㄱ", "ㅏ", "ㅂ", "ㅏ", "ㄴ")))
    }

    @Test
    fun `다 맞추면 승리한다`() {
        val game = JamoGame("단어")
        game.guess("ㄷ", "ㅏ", "ㄴ", "ㅇ", "ㅓ")
        assertEquals(GameStatus.WON, game.status)
        assertTrue(game.lastMatches().all { it == Match.CORRECT })
    }

    @Test
    fun `자모가 덜 차면 제출되지 않는다`() {
        val game = JamoGame("단어")
        game.press("ㄷ")
        game.submit()
        assertEquals(0, game.submitted.size)
        assertEquals(1, game.input.size) // 입력은 그대로 남는다
    }

    @Test
    fun `정해진 횟수를 모두 틀리면 패배한다`() {
        val game = JamoGame("단어")
        repeat(MAX_ATTEMPTS) { game.guess("ㅋ", "ㅡ", "ㅋ", "ㅡ", "ㅋ") }
        assertEquals(GameStatus.LOST, game.status)
        game.press("ㄷ")
        assertEquals(0, game.input.size) // 끝난 뒤 입력은 무시
    }

    @Test
    fun `키보드 색은 같은 자모의 가장 좋은 결과를 쓴다`() {
        val game = JamoGame("단어")
        game.guess("ㅓ", "ㅋ", "ㅋ", "ㅋ", "ㅋ") // ㅓ 는 위치가 틀려 노랑
        assertEquals(Match.PRESENT, game.keyMatches["ㅓ"])
        game.guess("ㅋ", "ㅋ", "ㅋ", "ㅋ", "ㅓ") // 자리까지 맞아 초록
        assertEquals(Match.CORRECT, game.keyMatches["ㅓ"])
        assertEquals(Match.ABSENT, game.keyMatches["ㅋ"])
    }

    @Test
    fun `끝난 판의 결과를 가린다`() {
        val won = JamoGame("단어")
        assertEquals(null, won.result)
        won.guess("ㄷ", "ㅏ", "ㄴ", "ㅇ", "ㅓ")
        assertEquals(GameResult.WON, won.result)

        val lost = JamoGame("단어")
        repeat(MAX_ATTEMPTS) { lost.guess("ㅋ", "ㅡ", "ㅋ", "ㅡ", "ㅋ") }
        assertEquals(GameResult.LOST, lost.result)

        val gaveUp = JamoGame("단어")
        gaveUp.revealAnswer()
        assertEquals(GameResult.REVEALED, gaveUp.result)
    }

    @Test
    fun `새 게임을 시작하면 기록 표시가 풀린다`() {
        val game = JamoGame("단어")
        game.revealAnswer()
        game.markRecorded()
        assertTrue(game.recorded)
        game.restart("사람")
        assertEquals(false, game.recorded)
        assertEquals(null, game.result)
    }

    @Test
    fun `새 게임은 보드와 키보드 상태를 비운다`() {
        val game = JamoGame("단어")
        game.guess("ㄷ", "ㅏ", "ㄴ", "ㅇ", "ㅓ")
        game.restart()
        assertEquals(0, game.submitted.size)
        assertEquals(GameStatus.PLAYING, game.status)
        assertTrue(game.keyMatches.isEmpty())
    }
}
