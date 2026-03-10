package com.chigo.tappgospinwheel.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chigo.tappgospinwheel.data.repository.WheelRepository
import com.chigo.tappgospinwheel.model.BaseResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpinWheelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WheelRepository(application)
    private val _wheelState = MutableStateFlow<WheelState?>(null)
    val wheelState: StateFlow<WheelState?> = _wheelState

    private val _cooldownState = MutableStateFlow<CooldownState>(CooldownState.Ready)
    val cooldownState: StateFlow<CooldownState> = _cooldownState

    val spinCount: Int get() = repository.getSpinCount()

    init {
        loadWheel()
        checkCooldown()
    }

    fun loadWheel() {
        viewModelScope.launch {
            val cached = repository.getCachedState()
            if (cached != null) {
                _wheelState.value = cached
                refreshInBackground()
                return@launch
            }

            _wheelState.value = WheelState.Loading

            when (val result = repository.fetchFreshConfig()) {
                is BaseResponse.Success -> _wheelState.value = result.data
                is BaseResponse.Error   -> _wheelState.value = WheelState.Error(result.message)
                is BaseResponse.Loading -> Unit
            }
        }
    }

    fun onSpinComplete(degrees: Float) {
        repository.saveSpinResult(degrees)
        repository.incrementSpinCount()
        repository.saveLastSpinTime()
        checkCooldown()
    }

    fun checkCooldown() {
        _cooldownState.value = if (repository.isCooldownActive()) {
            CooldownState.Active(repository.getTimeUntilNextSpin())
        } else {
            CooldownState.Ready
        }
    }

    private fun refreshInBackground() {
        viewModelScope.launch {
            repository.refreshIfExpired()
        }
    }
}