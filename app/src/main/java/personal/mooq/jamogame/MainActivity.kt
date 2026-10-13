package personal.mooq.jamogame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import personal.mooq.jamogame.game.DictionaryApi
import personal.mooq.jamogame.game.GameSettings
import personal.mooq.jamogame.game.GameStorage
import personal.mooq.jamogame.game.JamoGame
import personal.mooq.jamogame.game.WordDictionary
import personal.mooq.jamogame.game.WordSource
import personal.mooq.jamogame.ui.JamoGameScreen
import personal.mooq.jamogame.ui.SettingsScreen
import personal.mooq.jamogame.ui.theme.JamogameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JamogameTheme {
                JamoApp()
            }
        }
    }
}

@Composable
fun JamoApp() {
    val context = LocalContext.current
    val settings = remember { GameSettings(context.applicationContext) }
    val words = remember { WordSource(context.applicationContext.assets) }
    val storage = remember { GameStorage(context.applicationContext) }
    val dictionary = remember { WordDictionary(context.applicationContext.assets) }
    val api = remember { DictionaryApi() }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // 자모 개수를 바꾸면 그 길이의 단어로 판이 새로 시작된다.
    val game = rememberSaveable(
        settings.jamoCount,
        saver = JamoGame.saverFor(settings.jamoCount),
    ) {
        storage.restore(settings.jamoCount) ?: JamoGame(words.random(settings.jamoCount))
    }

    // 한 줄 제출하거나 새 판을 시작할 때마다 기기에 남긴다.
    LaunchedEffect(game, game.answer, game.submitted.size, game.revealed) {
        storage.save(game)
    }

    if (showSettings) {
        BackHandler { showSettings = false }
        SettingsScreen(
            jamoCount = settings.jamoCount,
            onJamoCountChange = { settings.jamoCount = it },
            onBack = { showSettings = false },
        )
    } else {
        JamoGameScreen(
            game = game,
            checking = checking,
            onSubmit = {
                // 사전에 없는 자모 조합은 아예 올리지 않는다.
                // 내장 목록으로 먼저 보고, 거기 없을 때만 사전 API 에 물어본다.
                when {
                    // 정답은 무조건 통과. 사전이나 네트워크 사정으로 정답이 막히면 안 된다.
                    !game.isInputFull || game.isInputAnswer -> game.submit()
                    dictionary.contains(game.input) -> game.submit()
                    checking -> Unit
                    else -> {
                        val guess = game.input.toList()
                        checking = true
                        scope.launch {
                            val exists = api.exists(guess)
                            checking = false
                            // 기다리는 사이 입력이 바뀌었으면 결과를 버린다
                            if (!game.inputMatches(guess)) return@launch
                            if (exists == false) {
                                Toast.makeText(context, "사전에 없는 단어예요", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                // 확인에 실패했으면(네트워크 등) 막지 않는다
                                game.submit()
                            }
                        }
                    }
                }
            },
            onNewGame = { game.restart(words.random(settings.jamoCount)) },
            onOpenSettings = { showSettings = true },
        )
    }
}
