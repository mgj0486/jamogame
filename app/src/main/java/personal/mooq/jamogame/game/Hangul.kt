package personal.mooq.jamogame.game

/**
 * 한글 음절을 호환 자모(ㄱ, ㅏ ...)로 쪼개는 유틸.
 *
 * 게임은 음절이 아니라 자모 단위로 돌아가고, 키보드에는 기본 자모만 두기 때문에
 * 쌍자음과 이중모음은 모두 기본 자모의 조합으로 쪼갠다. (깎 -> ㄱ ㄱ ㅏ ㄱ ㄱ)
 */
object Hangul {

    private const val BASE = 0xAC00
    private const val LAST = 0xD7A3

    private val CHOSEONG = listOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
        "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
    )

    private val JUNGSEONG = listOf(
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ",
        "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ",
    )

    /** 0번은 "받침 없음". */
    private val JONGSEONG = listOf(
        "", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ",
        "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
    )

    /** 키보드에 두는 기본 자음 14개. */
    val BASIC_CONSONANTS = listOf(
        "ㄱ", "ㄴ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
    )

    /** 키보드에 두는 기본 모음 10개. */
    val BASIC_VOWELS = listOf("ㅏ", "ㅑ", "ㅓ", "ㅕ", "ㅗ", "ㅛ", "ㅜ", "ㅠ", "ㅡ", "ㅣ")

    /** 쌍자음은 같은 자음 두 번. (ㄲ -> ㄱ ㄱ) */
    private val DOUBLED_CONSONANTS = mapOf(
        "ㄲ" to listOf("ㄱ", "ㄱ"),
        "ㄸ" to listOf("ㄷ", "ㄷ"),
        "ㅃ" to listOf("ㅂ", "ㅂ"),
        "ㅆ" to listOf("ㅅ", "ㅅ"),
        "ㅉ" to listOf("ㅈ", "ㅈ"),
    )

    /** 겹받침은 자음 두 개로. (맑 -> ㅁ ㅏ ㄹ ㄱ) */
    private val COMPOUND_JONGSEONG = mapOf(
        "ㄳ" to listOf("ㄱ", "ㅅ"),
        "ㄵ" to listOf("ㄴ", "ㅈ"),
        "ㄶ" to listOf("ㄴ", "ㅎ"),
        "ㄺ" to listOf("ㄹ", "ㄱ"),
        "ㄻ" to listOf("ㄹ", "ㅁ"),
        "ㄼ" to listOf("ㄹ", "ㅂ"),
        "ㄽ" to listOf("ㄹ", "ㅅ"),
        "ㄾ" to listOf("ㄹ", "ㅌ"),
        "ㄿ" to listOf("ㄹ", "ㅍ"),
        "ㅀ" to listOf("ㄹ", "ㅎ"),
        "ㅄ" to listOf("ㅂ", "ㅅ"),
    )

    /** 이중모음은 기본 모음들로. (왜 -> ㅇ ㅗ ㅏ ㅣ) */
    private val COMPOUND_VOWELS = mapOf(
        "ㅐ" to listOf("ㅏ", "ㅣ"),
        "ㅒ" to listOf("ㅑ", "ㅣ"),
        "ㅔ" to listOf("ㅓ", "ㅣ"),
        "ㅖ" to listOf("ㅕ", "ㅣ"),
        "ㅘ" to listOf("ㅗ", "ㅏ"),
        "ㅙ" to listOf("ㅗ", "ㅏ", "ㅣ"),
        "ㅚ" to listOf("ㅗ", "ㅣ"),
        "ㅝ" to listOf("ㅜ", "ㅓ"),
        "ㅞ" to listOf("ㅜ", "ㅓ", "ㅣ"),
        "ㅟ" to listOf("ㅜ", "ㅣ"),
        "ㅢ" to listOf("ㅡ", "ㅣ"),
    )

    /** 마지막 글자에 받침이 있는지. 조사를 고를 때 쓴다. (눈물 -> true, 단어 -> false) */
    fun endsWithFinalConsonant(word: String): Boolean {
        val code = word.lastOrNull()?.code ?: return false
        return code in BASE..LAST && (code - BASE) % 28 != 0
    }

    private val CHOSEONG_INDEX = CHOSEONG.withIndex().associate { (i, v) -> v to i }
    private val JUNGSEONG_INDEX = JUNGSEONG.withIndex().associate { (i, v) -> v to i }
    private val JONGSEONG_INDEX = JONGSEONG.withIndex().associate { (i, v) -> v to i }

    /** 기본 자모 -> 합쳐진 자모. [decompose] 가 쓰는 표들을 뒤집은 것. */
    private val MERGED: Map<String, String> =
        (DOUBLED_CONSONANTS + COMPOUND_JONGSEONG + COMPOUND_VOWELS).entries
            .associate { (whole, parts) -> parts.joinToString("") to whole }

    /** "단어" -> [ㄷ, ㅏ, ㄴ, ㅇ, ㅓ] / "왜" -> [ㅇ, ㅗ, ㅏ, ㅣ] / "깎" -> [ㄱ, ㄱ, ㅏ, ㄱ, ㄱ] */
    fun decompose(word: String): List<String> = buildList {
        for (ch in word) {
            val code = ch.code
            if (code !in BASE..LAST) {
                add(ch.toString())
                continue
            }
            val index = code - BASE
            val cho = CHOSEONG[index / (21 * 28)]
            addAll(DOUBLED_CONSONANTS[cho] ?: listOf(cho))

            val jung = JUNGSEONG[index / 28 % 21]
            addAll(COMPOUND_VOWELS[jung] ?: listOf(jung))

            val jong = JONGSEONG[index % 28]
            if (jong.isNotEmpty()) {
                // 겹받침을 먼저 쪼개고, 그 조각이 쌍자음이면 한 번 더 쪼갠다. (ㄲ -> ㄱ ㄱ)
                (COMPOUND_JONGSEONG[jong] ?: listOf(jong)).forEach { part ->
                    addAll(DOUBLED_CONSONANTS[part] ?: listOf(part))
                }
            }
        }
    }

    /**
     * [decompose] 의 역방향. 자모 목록을 음절 문자열로 되돌린다.
     *
     * 같은 자모열이 여러 가지로 읽힐 수 있어서([ㅎ,ㅏ,ㄱ,ㄱ,ㅛ] -> "학교" / "하끄"...)
     * 가능한 읽기를 모두 돌려준다. 사전에 물어볼 후보를 만드는 데 쓴다.
     * 자모가 음절로 떨어지지 않으면 빈 목록.
     */
    fun compositions(jamos: List<String>, limit: Int = 8): List<String> {
        val results = mutableListOf<String>()

        fun merged(from: Int, length: Int): String? = when (length) {
            1 -> jamos.getOrNull(from)
            else -> {
                if (from + length > jamos.size) null
                else MERGED[jamos.subList(from, from + length).joinToString("")]
            }
        }

        fun walk(at: Int, acc: StringBuilder) {
            if (results.size >= limit) return
            if (at == jamos.size) {
                results.add(acc.toString())
                return
            }
            // 초성 1~2자(쌍자음) + 중성 1~3자(이중모음) + 종성 0~2자(쌍·겹받침)
            for (choLen in 1..2) {
                val cho = CHOSEONG_INDEX[merged(at, choLen) ?: continue] ?: continue
                for (jungLen in 1..3) {
                    val jungAt = at + choLen
                    val jung = JUNGSEONG_INDEX[merged(jungAt, jungLen) ?: continue] ?: continue
                    for (jongLen in 0..2) {
                        val jongAt = jungAt + jungLen
                        if (jongAt + jongLen > jamos.size) continue
                        val jong = when (jongLen) {
                            0 -> 0
                            else -> JONGSEONG_INDEX[merged(jongAt, jongLen) ?: continue] ?: continue
                        }
                        acc.append((BASE + (cho * 21 + jung) * 28 + jong).toChar())
                        walk(jongAt + jongLen, acc)
                        acc.setLength(acc.length - 1)
                        if (results.size >= limit) return
                    }
                }
            }
        }

        walk(0, StringBuilder())
        return results
    }
}
