package com.zaheer.upireconcilepro.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zaheer.upireconcilepro.ui.theme.UPIReconcileProTheme
import kotlinx.coroutines.delay

enum class ProcessingStep {
    PARSING, DETECTING, RECONCILING, COMPLETE
}

@Composable
fun ProcessingScreen(
    currentStep: ProcessingStep = ProcessingStep.PARSING,
    progress: Float = 0f,
    onComplete: () -> Unit = {}
) {
    var step by remember { mutableStateOf(ProcessingStep.PARSING) }
    var currentProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Simulate processing steps
        step = ProcessingStep.PARSING
        for (i in 0..100 step 5) {
            currentProgress = i / 100f
            delay(50)
        }
        delay(500)

        step = ProcessingStep.DETECTING
        for (i in 0..100 step 5) {
            currentProgress = i / 100f
            delay(50)
        }
        delay(500)

        step = ProcessingStep.RECONCILING
        for (i in 0..100 step 5) {
            currentProgress = i / 100f
            delay(50)
        }
        delay(500)

        step = ProcessingStep.COMPLETE
        currentProgress = 1f
        delay(1000)
        onComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedCircularProgress(
                progress = currentProgress,
                isComplete = step == ProcessingStep.COMPLETE
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when (step) {
                    ProcessingStep.PARSING -> "Parsing CSV Files"
                    ProcessingStep.DETECTING -> "Detecting Columns"
                    ProcessingStep.RECONCILING -> "Reconciling Transactions"
                    ProcessingStep.COMPLETE -> "Complete!"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (step) {
                    ProcessingStep.PARSING -> "Reading and validating your CSV files..."
                    ProcessingStep.DETECTING -> "Identifying date, amount, and reference columns..."
                    ProcessingStep.RECONCILING -> "Matching invoices with payments..."
                    ProcessingStep.COMPLETE -> "Reconciliation completed successfully!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            ProcessingStepsList(currentStep = step)

            if (step == ProcessingStep.COMPLETE) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Processing Complete",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View your reconciliation dashboard",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedCircularProgress(
    progress: Float,
    isComplete: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            
            // Background circle
            drawCircle(
                color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            if (!isComplete) {
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFF6200EE),
                    startAngle = -90f + rotation,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFF03DAC5),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        if (isComplete) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
        } else {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProcessingStepsList(currentStep: ProcessingStep) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProcessingStepItem(
            icon = Icons.Default.Description,
            title = "Parsing CSV Files",
            isActive = currentStep == ProcessingStep.PARSING,
            isCompleted = currentStep.ordinal > ProcessingStep.PARSING.ordinal
        )

        ProcessingStepItem(
            icon = Icons.Default.Search,
            title = "Detecting Columns",
            isActive = currentStep == ProcessingStep.DETECTING,
            isCompleted = currentStep.ordinal > ProcessingStep.DETECTING.ordinal
        )

        ProcessingStepItem(
            icon = Icons.Default.Sync,
            title = "Reconciling Transactions",
            isActive = currentStep == ProcessingStep.RECONCILING,
            isCompleted = currentStep.ordinal > ProcessingStep.RECONCILING.ordinal
        )

        ProcessingStepItem(
            icon = Icons.Default.Done,
            title = "Complete",
            isActive = currentStep == ProcessingStep.COMPLETE,
            isCompleted = currentStep == ProcessingStep.COMPLETE
        )
    }
}

@Composable
private fun ProcessingStepItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (isActive && !isCompleted) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessingScreenPreview() {
    UPIReconcileProTheme {
        ProcessingScreen(
            currentStep = ProcessingStep.RECONCILING,
            progress = 0.65f,
            onComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessingScreenCompletePreview() {
    UPIReconcileProTheme {
        ProcessingScreen(
            currentStep = ProcessingStep.COMPLETE,
            progress = 1f,
            onComplete = {}
        )
    }
}
