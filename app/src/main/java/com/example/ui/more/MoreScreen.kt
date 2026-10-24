package com.example.ui.more

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusDanger
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationDestination

@Composable
fun MoreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val warnDays by viewModel.expiryWarningDays.collectAsStateWithLifecycle()

    var showClearDemoConfirm by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showReloadDemoConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Food Distribution Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GreenPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Designed for dual-owner food wholesale & retail distribution operations.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Operational Modules
        item {
            Text("OPERATIONS & ANALYTICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    MoreMenuRow(
                        title = "Delivery & Dispatch Tracking",
                        subtitle = "Track orders out for delivery and driver assignments",
                        icon = Icons.Default.LocalShipping,
                        iconTint = GreenPrimary,
                        onClick = { viewModel.navigateTo(NavigationDestination.Deliveries) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    MoreMenuRow(
                        title = "Business Reports & Analytics",
                        subtitle = "Daily sales, status breakdown, and top products",
                        icon = Icons.Default.Assessment,
                        iconTint = GreenPrimary,
                        onClick = { viewModel.navigateTo(NavigationDestination.Reports) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    MoreMenuRow(
                        title = "Smart Operations Assistant (AI)",
                        subtitle = "Restocking advice, WhatsApp order parsing, and Q&A",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = AmberAccent,
                        onClick = { viewModel.navigateTo(NavigationDestination.AiAssistant) }
                    )
                }
            }
        }

        // Food Safety & FEFO Configuration
        item {
            Text("FOOD SAFETY & FEFO SETTINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("First-Expiry-First-Out (FEFO) Warning Period", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Alert when packaged food products are within this many days of expiry:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15L, 30L, 60L, 90L).forEach { days ->
                            FilterChip(
                                selected = warnDays == days,
                                onClick = { viewModel.expiryWarningDays.value = days },
                                label = { Text("$days days", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = GreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Data Management & Demo Actions
        item {
            Text("DATA BACKUP & MANAGEMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    MoreMenuRow(
                        title = "Export Business Data (CSV)",
                        subtitle = "Share or backup products, orders, customers, and payments",
                        icon = Icons.Default.Download,
                        iconTint = GreenPrimary,
                        onClick = {
                            val csvData = viewModel.generateCsvExport()
                            shareCsvExport(context, csvData)
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    MoreMenuRow(
                        title = "Reload Sample Demo Data",
                        subtitle = "Restores sample groceries, shops, orders, and payments",
                        icon = Icons.Default.CloudDownload,
                        iconTint = GreenPrimary,
                        onClick = { showReloadDemoConfirm = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    MoreMenuRow(
                        title = "Clear Demo Records",
                        subtitle = "Removes only pre-loaded sample data, keeping your records",
                        icon = Icons.Default.RestartAlt,
                        iconTint = AmberAccent,
                        onClick = { showClearDemoConfirm = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    MoreMenuRow(
                        title = "Reset Entire Database",
                        subtitle = "Permanently deletes all data and starts fresh",
                        icon = Icons.Default.Delete,
                        iconTint = StatusDanger,
                        onClick = { showClearAllConfirm = true }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showReloadDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showReloadDemoConfirm = false },
            title = { Text("Reload Demo Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reload sample food items, orders, and customer accounts for testing.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadSampleData()
                        showReloadDemoConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Reload Demo Data") }
            },
            dismissButton = {
                TextButton(onClick = { showReloadDemoConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDemoConfirm = false },
            title = { Text("Clear Demo Records?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all items labelled as demo data.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDemoData()
                        showClearDemoConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                ) { Text("Clear Demo Records") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDemoConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Reset Entire Database?", fontWeight = FontWeight.Bold, color = StatusDanger) },
            text = { Text("WARNING: This will permanently wipe all products, orders, customers, and payments from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) { Text("Wipe All Data") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MoreMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

fun shareCsvExport(context: Context, csvData: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvData)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export Business Data"))
}
