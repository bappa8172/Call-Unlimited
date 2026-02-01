package com.callunlimited.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callunlimited.sip.CallHistoryEntry
import org.linphone.core.RegistrationState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DialerScreen(viewModel: CallViewModel) {
    val dialedNumber by viewModel.dialedNumber.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Recents, 1: Keypad

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF8F9FF), Color(0xFFFFFFFF))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            DialerTopBar(registrationState)
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Recents") },
                    icon = { Icon(Icons.Rounded.History, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Keypad") },
                    icon = { Icon(Icons.Rounded.Dialpad, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                }, label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HistorySection(callHistory) { number ->
                        viewModel.onNumberPasted(number)
                        selectedTab = 1
                    }
                    1 -> DialerSection(
                        dialedNumber = dialedNumber,
                        onDigitPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onDigitPressed(it)
                        },
                        onDeletePress = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onDeletePressed()
                        },
                        onClearAll = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearDialedNumber()
                        },
                        onPaste = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            clipboardManager.getText()?.text?.let { viewModel.onNumberPasted(it) }
                        },
                        onCallClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.makeCall()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DialerTopBar(registrationState: RegistrationState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (registrationState == RegistrationState.Ok) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (registrationState == RegistrationState.Ok) Color(0xFF4CAF50) else Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (registrationState == RegistrationState.Ok) "Online" else "Connecting...",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (registrationState == RegistrationState.Ok) Color(0xFF2E7D32) else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerSection(
    dialedNumber: String,
    onDigitPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onClearAll: () -> Unit,
    onPaste: () -> Unit,
    onCallClick: () -> Unit
) {
    var showPasteMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        // Number Display Area (No placeholder, Contextual Paste)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .combinedClickable(
                    onLongClick = { showPasteMenu = true },
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            if (dialedNumber.isNotEmpty()) {
                Text(
                    text = dialedNumber,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF1A237E),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            
            DropdownMenu(
                expanded = showPasteMenu,
                onDismissRequest = { showPasteMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Paste") },
                    onClick = {
                        onPaste()
                        showPasteMenu = false
                    },
                    leadingIcon = { Icon(Icons.Rounded.ContentPaste, null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val digits = listOf(
            "1" to "", "2" to "ABC", "3" to "DEF",
            "4" to "GHI", "5" to "JKL", "6" to "MNO",
            "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
            "*" to "", "0" to "+", "#" to ""
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(digits) { (digit, letters) ->
                ModernDialButton(digit, letters) { onDigitPress(digit) }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onCallClick,
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Rounded.Call, null, modifier = Modifier.size(32.dp))
            }

            if (dialedNumber.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(start = 120.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = onDeletePress,
                        modifier = Modifier
                            .size(56.dp)
                            .combinedClickable(
                                onClick = onDeletePress,
                                onLongClick = onClearAll
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                            contentDescription = "Delete",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDialButton(digit: String, letters: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnimation")

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (isPressed) Color(0xFFEEEEEE) else Color.White,
        shadowElevation = if (isPressed) 0.dp else 2.dp,
        modifier = Modifier
            .size(76.dp)
            .scale(scale)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(digit, fontSize = 30.sp, fontWeight = FontWeight.Normal, color = Color(0xFF1A237E))
            if (letters.isNotEmpty()) {
                Text(letters, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun HistorySection(history: List<CallHistoryEntry>, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Recent Calls",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
        )
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recent calls", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(history) { entry ->
                    HistoryItem(entry, onSelect)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(entry: CallHistoryEntry, onSelect: (String) -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect(entry.number) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFF5F5F5), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(entry.number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${sdf.format(Date(entry.timestamp))} • ${entry.duration}s", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            IconButton(onClick = { onSelect(entry.number) }) {
                Icon(Icons.Rounded.Call, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }
    }
}
