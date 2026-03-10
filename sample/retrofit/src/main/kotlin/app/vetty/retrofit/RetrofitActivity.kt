package app.vetty.retrofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

// datasource
@Serializable
data class CharactersResponse(
    val results: List<Character>,
)

@Serializable
data class Character(
    val id: Int,
    val name: String,
    val origin: CharacterOrigin,
)

@Serializable
data class CharacterOrigin(
    val name: String,
    val url: String,
)

interface CharacterAPI {
    @GET("character")
    suspend fun getCharacters(): CharactersResponse

    companion object {
        val instance: CharacterAPI
            get() {
                val loggingInterceptor =
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }

                val client =
                    OkHttpClient
                        .Builder()
                        .addInterceptor(loggingInterceptor)
                        .build()

                val networkJson = Json { ignoreUnknownKeys = true }

                return Retrofit
                    .Builder()
                    .baseUrl("https://rickandmortyapi.com/api/")
                    .client(client)
                    .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
                    .build()
                    .create(CharacterAPI::class.java)
            }
    }
}

// viewmodel and state

sealed interface CharactersState {
    data object Loading : CharactersState

    data class Success(
        val characters: List<Character>,
    ) : CharactersState

    data class Error(
        val message: String,
    ) : CharactersState
}

class CharactersViewModel : ViewModel() {
    private val api = CharacterAPI.instance

    private val _state = MutableStateFlow<CharactersState>(CharactersState.Loading)
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    init {
        fetchCharacters()
    }

    private fun fetchCharacters() {
        viewModelScope.launch {
            _state.value = CharactersState.Loading
            try {
                val response = api.getCharacters()
                _state.value = CharactersState.Success(response.results)
            } catch (e: Exception) {
                _state.value = CharactersState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}

// presentation

@Composable
fun CharactersScreen(viewModel: CharactersViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (val currentState = state) {
                is CharactersState.Loading -> {
                    CircularProgressIndicator()
                }

                is CharactersState.Error -> {
                    Text(
                        text = "Error: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                is CharactersState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(currentState.characters) { character ->
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = character.id.toString())
                                Text(text = " · ")
                                Text(
                                    text = character.name,
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetroTheme {
                CharactersScreen()
            }
        }
    }
}
