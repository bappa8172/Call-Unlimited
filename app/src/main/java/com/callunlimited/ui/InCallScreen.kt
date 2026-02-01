package com.callunlimited.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.linphone.core.Call
import java.util.Locale

@Composable
fun InCallScreen(viewModel: CallViewModel) {
    val callState by viewModel.callState.collectAsState()
    val callDuration by viewModel.callDuration.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val dialedNumber by viewModel.dialedNumber.collectAsState()

    val statusText = when (callState) {
        Call.State.OutgoingInit, Call.State.OutgoingProgress -> "Calling..."
        Call.State.OutgoingRinging -> "Ringing..."
        Call.State.StreamsRunning, Call.State.Connected -> "Answered"
        Call.State.End, Call.State.Released -> "Call Ended"
        Call.State.Error -> "Error"
        else -> "Connecting..."
    }

    val isConnected = callState == Call.State.StreamsRunning || callState == Call.State.Connected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dialedNumber,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = if (isConnected) Color(0xFF4CAF50) else Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Only show timer when answered/connected
            AnimatedVisibility(
                visible = isConnected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = formatDuration(callDuration),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallActionButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                checked = isMuted,
                onClick = { viewModel.toggleMute() }
            )

            CallActionButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "Speaker",
                checked = isSpeakerOn,
                onClick = { viewModel.toggleSpeaker() }
            )
        }

        FloatingActionButton(
            onClick = { viewModel.endCall() },
            containerColor = Color.Red,
            contentColor = Color.White,
            modifier = Modifier.size(80.dp),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.CallEnd,
                contentDescription = "End Call",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
