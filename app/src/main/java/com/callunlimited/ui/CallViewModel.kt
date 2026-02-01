package com.callunlimited.ui

import androidx.lifecycle.ViewModel
import com.callunlimited.sip.CallHistoryEntry
import com.callunlimited.sip.SipManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.Call
import org.linphone.core.RegistrationState
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val sipManager: SipManager
) : ViewModel() {

    val registrationState: StateFlow<RegistrationState> = sipManager.registrationState
    val callState: StateFlow<Call.State?> = sipManager.callState
    val callDuration: StateFlow<Int> = sipManager.callDuration
    val isMuted: StateFlow<Boolean> = sipManager.isMuted
    val isSpeakerOn: StateFlow<Boolean> = sipManager.isSpeakerOn
    val callHistory: StateFlow<List<CallHistoryEntry>> = sipManager.callHistory

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber

    fun onDigitPressed(digit: String) {
        _dialedNumber.value += digit
    }

    fun onNumberPasted(number: String) {
        // Clean the number from non-numeric characters except +
        val cleaned = number.filter { it.isDigit() || it == '+' }
        _dialedNumber.value = cleaned
    }

    fun onDeletePressed() {
        if (_dialedNumber.value.isNotEmpty()) {
            _dialedNumber.value = _dialedNumber.value.dropLast(1)
        }
    }

    fun clearDialedNumber() {
        _dialedNumber.value = ""
    }

    fun makeCall() {
        if (_dialedNumber.value.isNotEmpty()) {
            sipManager.makeCall(_dialedNumber.value)
        }
    }

    fun endCall() {
        sipManager.terminateCall()
    }

    fun toggleMute() {
        sipManager.toggleMute()
    }

    fun toggleSpeaker() {
        sipManager.toggleSpeaker()
    }
}
