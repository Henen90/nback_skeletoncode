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
    val eventInterval: StateFlow<Long>
    val eventTic: StateFlow<Int>
    val nrOfEvents: StateFlow<Int>
    val visualSize: StateFlow<Int>
    val audioSize: StateFlow<Int>

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkVisualMatch()

    fun checkAudioMatch()

    fun setNBack(n: Int)

    fun setEventInterval(interval: Long)

    fun setNrOfEvents(nrOfEvents: Int)

    fun setVisualSize(size: Int)

    fun setAudioSize(size: Int)
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

    private val _eventInterval = MutableStateFlow(2000L)
    override val eventInterval: StateFlow<Long>  // 2000 ms (2s)
        get() = _eventInterval


    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var visualEvents = emptyArray<Int>()  // Array with all events
    private var audioEvents = emptyArray<Int>()

    private val _visualSize = MutableStateFlow(9)
    override val visualSize: StateFlow<Int>
        get() = _visualSize

    private val _audioSize = MutableStateFlow(9)
    override val audioSize: StateFlow<Int>
        get() = _audioSize

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun setNBack(n: Int){
        _nBack.value = n
    }

    override fun setEventInterval(interval: Long) {
        _eventInterval.value = interval
    }

    override fun setNrOfEvents(nrOfEvents: Int){
        _nrOfEvents.value = nrOfEvents
    }

    override fun setVisualSize(size: Int) {
        _visualSize.value = size
    }

    override fun setAudioSize(size: Int){
        _audioSize.value = size
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        _score.value = 0
        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        visualEvents = nBackHelper.generateNBackString(nrOfEvents.value, visualSize.value, 30, nBack.value).toList().toTypedArray()
        audioEvents = nBackHelper.generateNBackString(nrOfEvents.value, audioSize.value, 30, nBack.value).toList().toTypedArray()
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

        val currentAudioEventValue = _gameState.value.audioEventValue
        val previouseAudioEventValue = audioEvents[currentIndex-nBack.value]

        if(currentAudioEventValue == previouseAudioEventValue){
            _score.value += 1
        }
        _gameState.value = _gameState.value.copy(audioMatchChecked = true)
    }
    private suspend fun runVisualGame(events: Array<Int>) {
        if(visualSize.value == 25) delay(1000L)
        if(visualSize.value == 16) delay(500L)
        for ((index, value) in events.withIndex()) {
            currentIndex = index
            _gameState.value = _gameState.value.copy(
                visualEventValue = value,
                visualMatchChecked = false
            )
            _eventTic.value += 1
            delay(_eventInterval.value)
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
            delay(_eventInterval.value)
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

            _eventTic.value += 1

            delay(_eventInterval.value)
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
    override val eventInterval: StateFlow<Long>
        get() = MutableStateFlow(2000L).asStateFlow()
    override val eventTic: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()
    override val nrOfEvents: StateFlow<Int>
        get() = MutableStateFlow(10).asStateFlow()
    override val visualSize: StateFlow<Int>
        get() = MutableStateFlow(9).asStateFlow()
    override val audioSize: StateFlow<Int>
        get() = MutableStateFlow(9).asStateFlow()

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkVisualMatch() {
    }

    override fun checkAudioMatch() {
    }

    override fun setNBack(n: Int) {
    }

    override fun setEventInterval(interval: Long) {
    }

    override fun setNrOfEvents(nrOfEvents: Int){
    }

    override fun setVisualSize(size: Int) {
    }

    override fun setAudioSize(size: Int) {
    }
}