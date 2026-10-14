package personal.mooq.jamogame.game

/** 끝난 판 하나의 기록. */
data class GameRecord(
    val answer: String,
    val result: GameResult,
    /** 쓴 시도 횟수. 포기면 포기할 때까지 쓴 횟수. */
    val attempts: Int,
    val finishedAt: Long,
    /** 제출한 줄들. 한 줄은 자모를 이어 붙인 문자열. */
    val guesses: List<String> = emptyList(),
) {
    /** SharedPreferences 에 한 줄로 담는다. 단어에는 구분자가 들어갈 수 없어 안전하다. */
    fun encode(): String = listOf(
        answer,
        result.name,
        attempts,
        finishedAt,
        guesses.joinToString(GUESS),
    ).joinToString(FIELD)

    companion object {
        private const val FIELD = "\t"
        private const val RECORD = "\n"
        private const val GUESS = ","

        fun decode(line: String): GameRecord? {
            val parts = line.split(FIELD)
            // 시도한 줄을 담기 전에 저장된 기록도 읽을 수 있게 4칸짜리를 허용한다
            if (parts.size !in 4..5) return null
            val result = GameResult.entries.find { it.name == parts[1] } ?: return null
            val attempts = parts[2].toIntOrNull() ?: return null
            val finishedAt = parts[3].toLongOrNull() ?: return null
            val guesses = parts.getOrNull(4).orEmpty()
                .split(GUESS)
                .filter { it.isNotEmpty() }
            return GameRecord(parts[0], result, attempts, finishedAt, guesses)
        }

        fun encodeAll(records: List<GameRecord>): String =
            records.joinToString(RECORD) { it.encode() }

        fun decodeAll(stored: String?): List<GameRecord> =
            stored.orEmpty().split(RECORD).mapNotNull { if (it.isEmpty()) null else decode(it) }
    }
}

enum class GameResult(val label: String) {
    /** 다 맞혔다. */
    WON("정답"),

    /** 시도를 다 썼다. */
    LOST("오답"),

    /** 정답 확인을 눌렀다. */
    REVEALED("포기"),
}
