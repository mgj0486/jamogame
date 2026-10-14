package personal.mooq.jamogame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import personal.mooq.jamogame.game.GameRecord
import personal.mooq.jamogame.game.GameResult
import personal.mooq.jamogame.game.Hangul
import personal.mooq.jamogame.game.scoreGuess
import personal.mooq.jamogame.ui.theme.CorrectGreen
import personal.mooq.jamogame.ui.theme.JamogameTheme
import personal.mooq.jamogame.ui.theme.PresentYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<GameRecord>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("지난 기록", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "아직 끝낸 판이 없어요",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(records) { record ->
                RecordRow(record)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
private fun RecordRow(record: GameRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.answer, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatDate(record.finishedAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            ResultBadge(record.result)
        }
        if (record.guesses.isNotEmpty()) {
            MiniBoard(record)
        }
    }
}

/** 그 판에서 시도한 줄들. 게임판과 같은 채점 색으로 작게 다시 그린다. */
@Composable
private fun MiniBoard(record: GameRecord) {
    val answerJamos = Hangul.decompose(record.answer)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        record.guesses.forEach { guess ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                scoreGuess(answerJamos, guess.map { it.toString() }).forEach { tile ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MatchColors.background(tile.match)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tile.jamo,
                            color = MatchColors.onMatch,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultBadge(result: GameResult) {
    // 보드 타일과 같은 색 규칙을 쓴다. 맞힌 판은 초록, 포기는 노랑, 못 맞힌 판은 회색.
    val background = when (result) {
        GameResult.WON -> CorrectGreen
        GameResult.REVEALED -> PresentYellow
        GameResult.LOST -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(6.dp), color = background) {
        Text(
            text = result.label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA).format(Date(epochMillis))

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    JamogameTheme {
        HistoryScreen(
            records = listOf(
                GameRecord(
                    "단어", GameResult.WON, 2, 1760000000000,
                    listOf("ㅇㅏㄴㅋㅡ", "ㄷㅏㄴㅇㅓ"),
                ),
                GameRecord(
                    "모범", GameResult.REVEALED, 1, 1759990000000,
                    listOf("ㅂㅏㄴㅋㅡ"),
                ),
            ),
            onBack = {},
        )
    }
}
