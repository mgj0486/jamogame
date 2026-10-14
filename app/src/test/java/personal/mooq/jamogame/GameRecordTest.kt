package personal.mooq.jamogame

import org.junit.Assert.assertEquals
import org.junit.Test
import personal.mooq.jamogame.game.GameRecord
import personal.mooq.jamogame.game.GameResult

class GameRecordTest {

    private val records = listOf(
        GameRecord("단어", GameResult.WON, 2, 1760000000000, listOf("ㅇㅏㄴㅋㅡ", "ㄷㅏㄴㅇㅓ")),
        GameRecord("라이브", GameResult.REVEALED, 0, 1759990000000),
        GameRecord("모범", GameResult.LOST, 5, 1759900000000, List(5) { "ㅂㅏㄴㅋㅡ" }),
    )

    @Test
    fun `기록을 저장했다 그대로 읽는다`() {
        assertEquals(records, GameRecord.decodeAll(GameRecord.encodeAll(records)))
    }

    @Test
    fun `기록이 없으면 빈 목록이다`() {
        assertEquals(emptyList<GameRecord>(), GameRecord.decodeAll(null))
        assertEquals(emptyList<GameRecord>(), GameRecord.decodeAll(""))
    }

    @Test
    fun `시도한 줄이 없던 예전 기록도 읽는다`() {
        // 시도 목록을 담기 전 형식(4칸)으로 저장된 기록
        val old = "단어\tWON\t3\t1760000000000"
        assertEquals(
            listOf(GameRecord("단어", GameResult.WON, 3, 1760000000000, emptyList())),
            GameRecord.decodeAll(old),
        )
    }

    @Test
    fun `깨진 줄은 건너뛴다`() {
        val stored = GameRecord.encodeAll(records) + "\n망가진줄\n단어\tNOPE\t1\t2\t"
        assertEquals(records, GameRecord.decodeAll(stored))
    }
}
