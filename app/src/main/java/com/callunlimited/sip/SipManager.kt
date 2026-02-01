package com.callunlimited.sip

import android.content.Context
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.linphone.core.*
import javax.inject.Inject
import javax.inject.Singleton

data class CallHistoryEntry(
    val number: String,
    val timestamp: Long,
    val duration: Int,
    val status: String
)

@Singleton
class SipManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val factory = Factory.instance()
    private val core: Core
    private val handler = Handler(Looper.getMainLooper())
    private val iterationRunnable = object : Runnable {
        override fun run() {
            core.iterate()
            // Update call duration if there's an active call
            _callDuration.value = core.currentCall?.duration ?: 0
            handler.postDelayed(this, 20)
        }
    }

    private val _registrationState = MutableStateFlow(RegistrationState.None)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    private val _callState = MutableStateFlow<Call.State?>(null)
    val callState: StateFlow<Call.State?> = _callState

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    private val _callHistory = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val callHistory: StateFlow<List<CallHistoryEntry>> = _callHistory

    private var lastDialedNumber: String = ""

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            state?.let { _registrationState.value = it }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            _callState.value = state
            if (state == Call.State.Released || state == Call.State.Error) {
                // Save to history when call ends
                val entry = CallHistoryEntry(
                    number = lastDialedNumber,
                    timestamp = System.currentTimeMillis(),
                    duration = call.duration,
                    status = state.name
                )
                _callHistory.value = listOf(entry) + _callHistory.value.take(19) // Keep last 20

                _callState.value = null
                _callDuration.value = 0
            }
        }
    }

    init {
        core = factory.createCore(null, null, context)
        
        // --- STUDIO MASTER QUALITY CONFIGURATION ---
        
        // 1. Audio Processing
        core.isEchoCancellationEnabled = true // Essential for speakerphone
        core.isAgcEnabled = true // Auto Gain Control (AGC) normalizes volume
        
        // 2. Network Quality of Service (QoS)
        core.audioDscp = 46 // EF - Expedited Forwarding
        core.sipDscp = 26 // AF31
        
        // 3. Network Adaptation
        core.isAdaptiveRateControlEnabled = true 
        core.isAudioAdaptiveJittcompEnabled = true
        
        // UNLIMITED BANDWIDTH
        core.uploadBandwidth = 0 
        core.downloadBandwidth = 0
        
        // 4. Codec Optimization
        val audioPayloads = core.audioPayloadTypes
        
        // Disable ALL first
        for (payload in audioPayloads) {
            payload.enable(false)
        }
        
        // Enable OPUS with MAXIMUM Fidelity
        val opus = audioPayloads.find { it.mimeType.equals("opus", ignoreCase = true) }
        opus?.let {
            it.enable(true)
            // 256kbps is "Studio Master Quality".
            // This captures the full spectrum of audio with absolute transparency.
            it.normalBitrate = 256000 
        }

        // G.722 (HD Voice) as backup
        val g722 = audioPayloads.find { it.mimeType.equals("G722", ignoreCase = true) }
        g722?.let {
            it.enable(true)
        }
        
        // PCMU/PCMA (Legacy backup)
        val pcmu = audioPayloads.find { it.mimeType.equals("PCMU", ignoreCase = true) }
        pcmu?.let { it.enable(true) }
        val pcma = audioPayloads.find { it.mimeType.equals("PCMA", ignoreCase = true) }
        pcma?.let { it.enable(true) }
        
        core.addListener(coreListener)
        core.setLogCollectionPath(context.cacheDir.absolutePath)
        core.enableLogCollection(LogCollectionState.Enabled)
        
        core.start()
        handler.post(iterationRunnable)
    }

    fun register(username: String, domain: String, password: String) {
        core.clearAccounts()
        core.clearAllAuthInfo()

        val authInfo = factory.createAuthInfo(username, null, password, null, null, domain, null)
        val params = core.createAccountParams()
        val identity = factory.createAddress("sip:$username@$domain")
        params.identityAddress = identity
        
        val serverAddress = factory.createAddress("sip:$domain;transport=udp")
        params.serverAddress = serverAddress
        params.isRegisterEnabled = true
        
        val account = core.createAccount(params)
        core.addAuthInfo(authInfo)
        core.addAccount(account)
        core.defaultAccount = account
    }

    fun makeCall(destination: String) {
        lastDialedNumber = destination
        val prefixedDestination = if (destination.startsWith("00")) destination else "00$destination"
        
        val domain = core.defaultAccount?.params?.serverAddress?.domain ?: "45.32.106.183"
        // interpretUrl is deprecated in newer SDKs, createAddress is preferred for robust parsing
        val address = core.createAddress("sip:$prefixedDestination@$domain") ?: return
        
        val params = core.createCallParams(null)
        params?.let {
            it.mediaEncryption = MediaEncryption.None
            core.inviteAddressWithParams(address, it)
        }
    }

    fun terminateCall() {
        core.currentCall?.terminate()
    }

    fun toggleMute() {
        val newValue = !_isMuted.value
        core.isMicEnabled = !newValue
        _isMuted.value = newValue
    }

    fun toggleSpeaker() {
        val newValue = !_isSpeakerOn.value
        val speaker = core.audioDevices.firstOrNull { it.type == AudioDevice.Type.Speaker }
        val earpiece = core.audioDevices.firstOrNull { it.type == AudioDevice.Type.Earpiece }
        
        if (newValue) {
            speaker?.let { core.inputAudioDevice = it; core.outputAudioDevice = it }
        } else {
            earpiece?.let { core.inputAudioDevice = it; core.outputAudioDevice = it }
        }
        _isSpeakerOn.value = newValue
    }
}
