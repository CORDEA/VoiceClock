package jp.cordea.voiceclock.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.cordea.voiceclock.StartTtsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startTtsUseCase: StartTtsUseCase
) : ViewModel() {
    fun onTtsReceived() {
        startTtsUseCase.execute()
    }
}
