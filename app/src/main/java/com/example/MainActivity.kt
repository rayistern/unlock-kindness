package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.UnlockEvent
import com.example.data.UnlockRepository
import com.example.service.UnlockService
import com.example.ui.PennyDropViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    private val viewModel by viewModels<PennyDropViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(applicationContext)
                val pref = PreferencesManager(applicationContext)
                val repo = UnlockRepository(db.unlockDao())
                @Suppress("UNCHECKED_CAST")
                return PennyDropViewModel(repo, pref) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isAutomationActive by viewModel.isAutomationActive.collectAsStateWithLifecycle()
                
                LaunchedEffect(isAutomationActive) {
                    if (isAutomationActive) {
                        val serviceIntent = Intent(this@MainActivity, UnlockService::class.java)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                    } else {
                        stopService(Intent(this@MainActivity, UnlockService::class.java))
                    }
                }

                PennyDropScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PennyDropScreen(viewModel: PennyDropViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("home") } // "home", "activity", "settings"
    
    val isAutomationActive by viewModel.isAutomationActive.collectAsStateWithLifecycle()
    val unlocksToday by viewModel.unlocksToday.collectAsStateWithLifecycle()
    val totalSent by viewModel.totalSent.collectAsStateWithLifecycle()
    val unpaidBalance by viewModel.unpaidBalance.collectAsStateWithLifecycle()
    val recipientName by viewModel.recipientName.collectAsStateWithLifecycle()
    val cashtag by viewModel.cashtag.collectAsStateWithLifecycle()
    val amountPerUnlock by viewModel.amountPerUnlock.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()

    // Dynamic notification runtime checks for API 33+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MERKOS 302",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "activity",
                    onClick = { currentTab = "activity" },
                    icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Activity") },
                    label = { Text("Activity") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "settings",
                    onClick = { currentTab = "settings" },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Welcome / Description Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Good to see you,",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "Ready to make an impact?",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Every unlock is a micro-donation. Small, consistent acts of kindness compound effortlessly to support Merkos 302.",
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        // Unpaid Balance Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF2B2B2B), Color(0xFF111111))
                                    ),
                                    shape = RoundedCornerShape(36.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(36.dp)
                                )
                                .padding(vertical = 56.dp, horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "UNPAID BALANCE",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = String.format("$%.2f", unpaidBalance),
                                    color = Color.White,
                                    fontSize = 80.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-2).sp
                                )
                            }
                        }
                        
                        // Send via Cash App button
                        AnimatedVisibility(
                            visible = unpaidBalance >= 1.0,
                            enter = expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            Button(
                                onClick = {
                                    val amountStr = String.format(Locale.US, "%.2f", unpaidBalance)
                                    val url = "https://cash.app/\$merkos/$amountStr?note=Microdonations"
                                    
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    
                                    viewModel.markAllAsPaid()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(horizontal = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.cashapp_green)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Send, contentDescription = "Send", tint = Color.White)
                                    Text(
                                        text = "Send via Cash App", 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 16.sp,
                                        letterSpacing = 0.5.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Microdonations", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Auto background daemon", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isAutomationActive,
                                    onCheckedChange = { viewModel.toggleAutomation(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Unlocks Today Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = unlocksToday.toString(), fontSize = 32.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "TODAY",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                // Total Sent Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = String.format("$%.0f", totalSent), fontSize = 32.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ALL TIME",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                    }
                }

                "activity" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Transaction Ledger", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Complete historical breakdown of tracked unlocks.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        if (allEvents.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Analytics, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    Text(
                                        text = "No unlocks recorded yet.",
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(allEvents) { event ->
                                    TransactionItemRow(event)
                                }
                            }
                        }
                    }
                }

                "settings" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text("Device Permissions", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Post Notifications Needed", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                    Text("Android 13+ requires notification permission to trigger background service.", fontSize = 12.sp)
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Grant Permission")
                                    }
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Active", tint = Color(0xFF2E7D32))
                                    Column {
                                        Text("System Service Primed", fontWeight = FontWeight.SemiBold)
                                        Text("The unlock broadcast daemon has valid background authorizations.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Text("Automation Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        // Customize dynamic value
                        val amountText = remember(amountPerUnlock) { mutableStateOf(String.format(Locale.US, "%.2f", amountPerUnlock)) }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Transaction Val ($)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            OutlinedTextField(
                                value = amountText.value,
                                onValueChange = {
                                    amountText.value = it
                                    val newVal = it.toFloatOrNull()
                                    if (newVal != null && newVal >= 0f) {
                                        viewModel.updateAmountPerUnlock(newVal)
                                    }
                                },
                                label = { Text("Cost Per Unlock") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("$ ") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(event: UnlockEvent) {
    val dateValue = remember(event.timestamp) {
        try {
            val sdf = SimpleDateFormat("MMM dd, yyyy \u00B7 HH:mm", Locale.getDefault())
            sdf.format(Date(event.timestamp))
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    val isPaid = event.status == "PAID"
    val statusColor = if (isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(event.recipientName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(dateValue, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Text(
                    text = String.format("+\$%.2f", event.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = statusColor
                )
            }
        }
    }
}
