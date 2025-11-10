package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val eventInterval: Long
    val eventTic: StateFlow<Int>
    val nrOfEvents: StateFlow<Int>

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkVisualMatch()

    fun checkAudioMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _nBack = MutableStateFlow(2)
    override val nBack: StateFlow<Int>
        get() = _nBack

    private val _nrOfEvents = MutableStateFlow(10)
    override val nrOfEvents: StateFlow<Int>
        get() = _nrOfEvents
    private val _eventTic = MutableStateFlow(0)
    override val eventTic: StateFlow<Int> = _eventTic.asStateFlow()

    private var currentIndex: Int =0
    private var job: Job? = null  // coroutine job for the game event
    override val eventInterval: Long = 2000L  // 2000 ms (2s)

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var visualEvents = emptyArray<Int>()  // Array with all events
    private var audioEvents = emptyArray<Int>()

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        _score.value = 0
        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        visualEvents = nBackHelper.generateNBackString(nrOfEvents.value, 9, 30, nBack.value).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        audioEvents = nBackHelper.generateNBackString(nrOfEvents.value, 9, 30, nBack.value).toList().toTypedArray()
        Log.d("GameVM", "The following visual sequence was generated: ${visualEvents.contentToString()}")
        Log.d("GameVM", "The following audio sequence was generated: ${audioEvents.contentToString()}")

        job = viewModelScope.launch {
            delay(500L)
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(audioEvents)
                GameType.AudioVisual -> runAudioVisualGame(audioEvents, visualEvents)
                GameType.Visual -> runVisualGame(visualEvents)
            }
            if(_score.value > _highscore.value){
                _highscore.value = _score.value

                viewModelScope.launch {
                    userPreferencesRepository.saveHighScore(_highscore.value)
                }
            }

        }
    }

    override fun checkVisualMatch() {
        if(_gameState.value.visualMatchChecked) return
        if(currentIndex < nBack.value) return

        val currentVisualEventValue = _gameState.value.visualEventValue
        val previouseVisualEventValue = visualEvents[currentIndex-nBack.value]

        if(currentVisualEventValue == previouseVisualEventValue){
            _score.value += 1
        }
        _gameState.value = _gameState.value.copy(visualMatchChecked = true)
    }

    override fun checkAudioMatch() {
        if(_gameState.value.audioMatchChecked) return
        if(currentIndex < nBack.value) return

        val currentAudioEventValue = _gameState.value.visualEventValue
        val previouseAudioEventValue = audioEvents[currentIndex-nBack.value]

        if(currentAudioEventValue == previouseAudioEventValue){
            _score.value += 1
        }
        _gameState.value = _gameState.value.copy(audioMatchChecked = true)
    }
    private suspend fun runVisualGame(events: Array<Int>) {
        for ((index, value) in events.withIndex()) {
            currentIndex = index
            _gameState.value = _gameState.value.copy(
                visualEventValue = value,
                visualMatchChecked = false
            )
            _eventTic.value += 1
            delay(eventInterval)
        }
    }

    private suspend fun runAudioGame(events: Array<Int>) {
        for ((index, value) in events.withIndex()) {
            currentIndex = index
            _gameState.value = _gameState.value.copy(
                audioEventValue = value,
                audioMatchChecked = false
            )
            _eventTic.value += 1
            delay(eventInterval)
        }
    }

    private suspend fun runAudioVisualGame(audioEvents: Array<Int>, visualEvents: Array<Int>){
        for (index in visualEvents.indices) {
            currentIndex = index

            val visualValue = visualEvents[index]
            val audioValue = audioEvents[index]

            _gameState.value = _gameState.value.copy(
                visualEventValue = visualValue,
                audioEventValue = audioValue,
                visualMatchChecked = false,
                audioMatchChecked = false
            )

            // Trigga uppdatering i UI:t
            _eventTic.value += 1

            delay(eventInterval)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val visualEventValue: Int = -1,
    val audioEventValue: Int = -1, // The value of the array string
    val visualMatchChecked: Boolean = false,
    val audioMatchChecked: Boolean = false
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val eventInterval: Long
        get() = 2000L
    override val eventTic: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()
    override val nrOfEvents: StateFlow<Int>
        get() = MutableStateFlow(10).asStateFlow()

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkVisualMatch() {
    }

    override fun checkAudioMatch() {
    }
}