package personal.mooq.jamogame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.mooq.jamogame.game.GameStatus
import personal.mooq.jamogame.game.JamoGame
import personal.mooq.jamogame.game.MAX_ATTEMPTS
import personal.mooq.jamogame.ui.theme.JamogameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamoGameScreen(
    game: JamoGame,
    checking: Boolean,
    onSubmit: () -> Unit,
    onNewGame: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider()

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // 타일이 정사각형이라 보드 높이는 폭에 따라간다. 남은 높이에 모든 줄이 들어갈
                // 만큼으로 폭을 제한해서 화면이 짧아도 보드가 잘리지 않게 한다.
                val columns = game.jamoCount
                val tileFromHeight = (maxHeight - BoardTileGap * (MAX_ATTEMPTS - 1)) / MAX_ATTEMPTS
                val boardWidth = (tileFromHeight * columns + BoardTileGap * (columns - 1))
                    .coerceAtMost(320.dp)
                GameBoard(
                    submitted = game.submitted,
                    input = game.input,
                    columns = columns,
                    modifier = Modifier.widthIn(max = boardWidth),
                )
            }

            StatusArea(game, checking, onNewGame)

            Spacer(Modifier.height(14.dp))

            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                val over = game.status == GameStatus.LOST
                JamoKeyboard(
                    keyMatches = game.keyMatches,
                    onKeyPress = game::press,
                    onBackspace = game::backspace,
                    onSubmit = onSubmit,
                    submitEnabled = game.isInputFull && game.status == GameStatus.PLAYING && !checking,
                    modifier = if (over) Modifier.blur(14.dp) else Modifier,
                )
                if (over) {
                    AnswerOverlay(
                        answer = game.answer,
                        jamos = game.answerJamos,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }
    }
}

/**
 * 키보드 바로 위 영역. 결과/안내 한 줄과, 길게 눌러야 먹히는 버튼 두 개.
 */
@Composable
private fun StatusArea(game: JamoGame, checking: Boolean, onNewGame: () -> Unit) {
    // 짧게 눌렀을 때 띄우는 안내. 다시 입력을 시작하면 지운다.
    var hint by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(game.input.size, game.submitted.size) { hint = null }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            StatusText(game, checking, hint)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LongPressButton(
                text = "길게 눌러 정답확인",
                onLongPress = {
                    hint = null
                    game.revealAnswer()
                },
                onShortPress = { hint = "길게 누르면 정답을 확인해요" },
                modifier = Modifier.weight(1f),
            )
            LongPressButton(
                text = "길게 눌러 새 게임",
                onLongPress = {
                    hint = null
                    onNewGame()
                },
                onShortPress = { hint = "길게 누르면 새 게임을 시작해요" },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatusText(game: JamoGame, checking: Boolean, hint: String?) {
    when {
        checking -> Text(
            text = "사전에서 찾는 중…",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        game.message != null -> Text(
            text = game.message!!,
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        hint != null -> Text(
            text = hint,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        game.status == GameStatus.WON -> Text(
            text = "정답! ${game.submitted.size}/$MAX_ATTEMPTS 번에 맞췄어요",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/** 판이 끝나면 키보드를 가리고 정답을 보여준다. 흐림은 API 31 미만에서 무시되므로 막도 함께 깐다. */
@Composable
private fun AnswerOverlay(answer: String, jamos: List<String>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            // 흐린 키보드가 눌리지 않도록 터치를 여기서 받아 버린다
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "정답",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = answer,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = jamos.joinToString("  "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun JamoGameScreenPreview() {
    JamogameTheme {
        val game = JamoGame("단어").apply {
            press("ㅇ"); press("ㅏ"); press("ㄴ"); press("ㅋ"); press("ㅡ")
            submit()
            press("ㄷ"); press("ㅏ")
        }
        JamoGameScreen(game, checking = false, onSubmit = {}, onNewGame = {}, onOpenSettings = {})
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun JamoGameScreenSixPreview() {
    JamogameTheme {
        JamoGameScreen(
            JamoGame("라이브"),
            checking = false,
            onSubmit = {},
            onNewGame = {},
            onOpenSettings = {},
        )
    }
}
