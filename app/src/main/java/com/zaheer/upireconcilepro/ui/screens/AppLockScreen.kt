package com.zaheer.upireconcilepro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    biometricEnabled: Boolean = false,
    onPinEntered: (String) -> Unit,
    onBiometricAuth: () -> Unit,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var shake by remember { mutableStateOf(false) }

    val shakeOffset by animateFloatAsState(
        targetValue = if (shake) 10f else 0f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = { shake = false },
        label = "shake"
    )

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(200)
            onPinEntered(pin)
            // Simulate PIN validation
            if (pin == "1234") { // Demo PIN
                onAuthSuccess()
            } else {
                isError = true
                shake = true
                delay(500)
                pin = ""
                isError = false
                onAuthFailed()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isError) "Incorrect PIN. Try again." else "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            PinDisplay(
                pinLength = pin.length,
                isError = isError,
                shakeOffset = shakeOffset
            )

            Spacer(modifier = Modifier.height(48.dp))

            NumericKeypad(
                onNumberClick = { number ->
                    if (pin.length < 4) {
                        pin += number
                    }
                },
                onBackspace = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                }
            )

            if (biometricEnabled) {
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onBiometricAuth,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Biometric")
                }
            }
        }
    }
}

@Composable
private fun PinDisplay(
    pinLength: Int,
    isError: Boolean,
    shakeOffset: Float
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.offset(x = shakeOffset.dp)
    ) {
        repeat(4) { index ->
            PinDot(
                isFilled = index < pinLength,
                isError = isError
            )
        }
    }
}

@Composable
private fun PinDot(
    isFilled: Boolean,
    isError: Boolean
) {
    val color = when {
        isError -> MaterialTheme.colorScheme.error
        isFilled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size((20 * scale).dp)
            .then(
                if (isFilled) {
                    Modifier.background(color, CircleShape)
                } else {
                    Modifier.border(2.dp, color, CircleShape)
                }
            )
    )
}

@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("1", onNumberClick, Modifier.weight(1f))
            KeypadButton("2", onNumberClick, Modifier.weight(1f))
            KeypadButton("3", onNumberClick, Modifier.weight(1f))
        }

        // Row 2: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("4", onNumberClick, Modifier.weight(1f))
            KeypadButton("5", onNumberClick, Modifier.weight(1f))
            KeypadButton("6", onNumberClick, Modifier.weight(1f))
        }

        // Row 3: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KeypadButton("7", onNumberClick, Modifier.weight(1f))
            KeypadButton("8", onNumberClick, Modifier.weight(1f))
            KeypadButton("9", onNumberClick, Modifier.weight(1f))
        }

        // Row 4: empty, 0, backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            KeypadButton("0", onNumberClick, Modifier.weight(1f))
            KeypadBackspaceButton(onBackspace, Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeypadButton(
    number: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable {
                isPressed = true
                onClick(number)
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 4.dp
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .alpha(scale)
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun KeypadBackspaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable {
                isPressed = true
                onClick()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .alpha(scale)
        ) {
            Icon(
                Icons.Default.Backspace,
                contentDescription = "Backspace",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLockScreenPreview() {
    UPIReconcileProTheme {
        AppLockScreen(
            biometricEnabled = true,
            onPinEntered = {},
            onBiometricAuth = {},
            onAuthSuccess = {},
            onAuthFailed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLockScreenNoBiometricPreview() {
    UPIReconcileProTheme {
        AppLockScreen(
            biometricEnabled = false,
            onPinEntered = {},
            onBiometricAuth = {},
            onAuthSuccess = {},
            onAuthFailed = {}
        )
    }
}
