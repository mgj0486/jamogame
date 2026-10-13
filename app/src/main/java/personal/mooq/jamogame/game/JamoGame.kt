package personal.mooq.jamogame.game

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

/** 시도 가능 횟수. */
const val MAX_ATTEMPTS = 5

/** 자모 하나의 채점 결과. rank 가 높을수록 더 많은 정보를 담은 상태. */
enum class Match(val rank: Int) {
    /** 정답에 없는 자모 */
    ABSENT(1),

    /** 정답에 있지만 위치가 다름 */
    PRESENT(2),

    /** 자모와 위치 모두 일치 */
    CORRECT(3),
}

data class Tile(val jamo: String, val match: Match)

enum class GameStatus { PLAYING, WON, LOST }

/**
 * 게임 상태 홀더. Compose state 로 들고 있어서 값이 바뀌면 화면이 따라 바뀐다.
 */
class JamoGame(answer: String) {

    var answer by mutableStateOf(answer)
        private set

    /** 정답을 자모로 쪼갠 것. 채점 기준. */
    var answerJamos by mutableStateOf(Hangul.decompose(answer))
        private set

    /** 한 줄에 들어가는 자모 개수. 정답 길이가 곧 판의 길이다. */
    val jamoCount: Int get() = answerJamos.size

    /** 제출이 끝난 줄들. */
    private val _submitted = mutableStateListOf<List<Tile>>()
    val submitted: List<List<Tile>> get() = _submitted

    /** 지금 입력 중인 줄. */
    private val _input = mutableStateListOf<String>()
    val input: List<String> get() = _input

    var status by mutableStateOf(GameStatus.PLAYING)
        private set

    /** 한 줄 안내 문구. (자모가 덜 찼을 때 등) */
    var message by mutableStateOf<String?>(null)
        private set

    /** 정답을 펼쳐 봤는지. 시도를 모두 쓴 패배와 구분해 저장해 둔다. */
    var revealed by mutableStateOf(false)
        private set

    /** 키보드 키에 칠할 색. 같은 자모를 여러 번 썼으면 가장 좋은 결과를 쓴다. */
    val keyMatches: Map<String, Match> by derivedStateOf {
        buildMap {
            _submitted.flatten().forEach { tile ->
                val prev = this[tile.jamo]
                if (prev == null || tile.match.rank > prev.rank) put(tile.jamo, tile.match)
            }
        }
    }

    val isInputFull: Boolean get() = _input.size == jamoCount

    /**
     * 지금 입력이 [jamos] 와 같은지.
     *
     * `input == jamos` 로 쓰면 안 된다. 입력은 SnapshotStateList 라 일반 List 와는
     * structural equality 가 성립하지 않아 항상 false 가 나온다.
     */
    fun inputMatches(jamos: List<String>): Boolean = _input.toList() == jamos

    /** 지금 입력이 정답과 같은지. 정답은 어떤 사전 검사도 거치지 않고 통과시켜야 한다. */
    val isInputAnswer: Boolean get() = inputMatches(answerJamos)

    fun press(jamo: String) {
        if (status != GameStatus.PLAYING || isInputFull) return
        message = null
        _input.add(jamo)
    }

    fun backspace() {
        if (status != GameStatus.PLAYING || _input.isEmpty()) return
        message = null
        _input.removeAt(_input.lastIndex)
    }

    fun submit() {
        if (status != GameStatus.PLAYING) return
        if (!isInputFull) {
            message = "자모 ${jamoCount}개를 모두 채워주세요"
            return
        }
        val guess = _input.toList()
        _input.clear()
        message = null
        play(guess)
    }

    /** 채점해서 한 줄 올린다. 저장해 둔 판을 되살릴 때도 같은 길을 쓴다. */
    fun play(guess: List<String>) {
        if (guess.size != jamoCount || _submitted.size >= MAX_ATTEMPTS) return
        _submitted.add(evaluate(guess))
        status = when {
            guess == answerJamos -> GameStatus.WON
            _submitted.size >= MAX_ATTEMPTS -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }
    }

    /** 정답을 펼쳐 보고 판을 끝낸다. 포기와 같다. */
    fun revealAnswer() {
        if (status != GameStatus.PLAYING) return
        revealed = true
        status = GameStatus.LOST
        message = null
    }

    fun restart(newAnswer: String = answer) {
        answer = newAnswer
        answerJamos = Hangul.decompose(newAnswer)
        _submitted.clear()
        _input.clear()
        status = GameStatus.PLAYING
        revealed = false
        message = null
    }

    /**
     * 워들과 같은 2패스 채점. 먼저 자리까지 맞은 자모를 집계해서 빼고,
     * 남은 자모 중에 있으면 PRESENT. 중복 자모를 과하게 노랗게 칠하지 않기 위함.
     */
    private fun evaluate(guess: List<String>): List<Tile> {
        val remaining = answerJamos.toMutableList()
        val matches = arrayOfNulls<Match>(guess.size)

        guess.forEachIndexed { i, jamo ->
            if (answerJamos.getOrNull(i) == jamo) {
                matches[i] = Match.CORRECT
                remaining.remove(jamo)
            }
        }
        guess.forEachIndexed { i, jamo ->
            if (matches[i] != null) return@forEachIndexed
            matches[i] = if (remaining.remove(jamo)) Match.PRESENT else Match.ABSENT
        }
        return guess.mapIndexed { i, jamo -> Tile(jamo, matches[i]!!) }
    }

    companion object {
        /**
         * 화면 회전이나 프로세스 종료 뒤에도 진행 중인 판을 되살리기 위한 saver.
         *
         * 저장된 판의 자모 개수가 [expectedJamoCount] 와 다르면 복원을 포기한다.
         * 앱이 꺼져 있는 동안 설정이 바뀐 경우라, 옛 판을 되살리면 설정과 어긋나기 때문.
         */
        @Suppress("UNCHECKED_CAST")
        fun saverFor(expectedJamoCount: Int): Saver<JamoGame, Any> = Saver(
            save = { game ->
                listOf(
                    game.answer,
                    game.submitted.map { row -> row.map { it.jamo } },
                    game.input.toList(),
                    game.revealed,
                )
            },
            restore = { saved ->
                val state = saved as List<Any>
                val restored = JamoGame(state[0] as String)
                if (restored.jamoCount != expectedJamoCount) return@Saver null
                restored.apply {
                    (state[1] as List<List<String>>).forEach { row -> play(row) }
                    _input.addAll(state[2] as List<String>)
                    if (state[3] as Boolean) revealAnswer()
                }
            },
        )
    }
}
