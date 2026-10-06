package com.ghostlock.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.icon.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.textSize
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.fragment.navArgs
import com.ghostlock.app.R
import com.ghostlock.app.domain.model.*
import com.ghostlock.app.domain.model.ShizukuStatus
import com.ghostlock.app.domain.model.UserProfileFile
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.theme.*
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import kotlinx.coroutines.launch

class GhostlockActivity : ComponentActivity() {
    private val viewModel: GhostlockViewModel by viewModels()
    private val navController = rememberNavController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhostlockApp(
                state = viewModel.state,
                actions = viewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun GhostlockApp(
    state: GhostlockUiState,
    actions: GhostlockActions,
    navController: NavHostController
) {
    // Use Miuix theme's color schemes (assumed compatible with Material3)
    val lightScheme = lightColorScheme()   // from top.yukonga.miuix.kmp.theme
    val darkScheme = darkColorScheme()
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkScheme else lightScheme
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("GhostLock") },
                    actions = {
                        IconButton(onClick = { /* TODO: overflow menu */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            bottomBar = {
                CenterAlignedBottomAppBar(
                    fabAnchorCutoutShape = RoundedCornerShape(50%),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    FloatingActionButton(
                        onClick = { actions.onRun() },
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                }
            },
            content = { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    composable("home") { HomeScreen(state, actions) }
                    composable("advanced") { AdvancedScreen(state, actions) }
                    composable("about") { AboutScreen(state, actions) }
                    composable("parameters") { ParameterScreen(state, actions) }
                    composable("loadConfig") { LoadConfigScreen(state, actions) }
                    composable("builtin") { BuiltinProfileScreen(state, actions) }
                    composable("userProfileDetail") {
                        val name = it.arguments?.getString("name") ?: return@composable
                        UserProfileDetailScreen(state, actions, name)
                    }
                    composable("profileOverride") { ProfileOverrideScreen(state, actions) }
                    composable("advancedOverride") { AdvancedOverrideScreen(state, actions) }
                }
            }
        )
    }
}

@Composable
fun HomeScreen(state: GhostlockUiState, actions: GhostlockActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("KERNEL & PARAMETERS")
        ActivationStatusCard(
            supported = state.kernelSupported,
            profileAvailable = state.executionHasProfile,
            profileValid = state.profileInvalidPaths.isEmpty(),
            shizukuEnabled = state.shizukuEnabled,
            shizukuStatus = state.shizukuStatus,
            onParametersClick = actions::onOpenParameters,
            onShizukuClick = actions::onStatusClick,
        )
        DeviceInfoCard(
            deviceName = state.deviceName,
            socName = state.socName,
            kernelRelease = state.kernelRelease,
        )
        SectionTitle("RUNTIME")
        ManagerStatusCard(manager = state.manager)
        SectionTitle("EXECUTION")
        if (state.cpuPairLabels.isNotEmpty()) {
            CpuPairSelector(
                labels = state.cpuPairLabels,
                selectedIndex = state.cpuPairIndex,
                customPair = state.customCpuPair,
                onSelected = actions::onCpuPairSelected
            )
        }
        SafeModeSwitch(
            checked = state.safeModeEnabled,
            onCheckedChange = actions::onSafeModeChanged
        )
        ShizukuSwitch(
            checked = state.shizukuEnabled,
            onCheckedChange = actions::onShizukuChanged,
            status = state.shizukuStatus
        )
        AdvancedSettingsButton(onClick = actions::onOpenAdvanced)
        RunButton(
            running = state.running,
            enabled = state.kernelSupported &&
                    state.executionHasProfile &&
                    (!state.shizukuEnabled ||
                            state.shizukuStatus == ShizukuStatus.READY) &&
                    state.profileInvalidPaths.isEmpty(),
            onClick = actions::onRun,
            onBlockedClick = actions::onProfileInvalid
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ActivationStatusCard(
    supported: Boolean,
    profileAvailable: Boolean,
    profileValid: Boolean,
    shizukuEnabled: Boolean,
    shizukuStatus: ShizukuStatus,
    onParametersClick: () -> Unit,
    onShizukuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ready = supported && profileAvailable && profileValid && (!shizukuEnabled || shizukuStatus == ShizukuStatus.READY)
    val missing = !supported || !profileAvailable
    val (bgColor, titleRes, icon) = when {
        ready -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            R.string.kernel_supported,
            Icons.Default.CheckCircleOutline
        )
        missing -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            if (!supported) R.string.kernel_unsupported else R.string.kernel_profile_required,
            Icons.Default.ErrorOutline
        )
        else -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            if (!profileValid) R.string.kernel_profile_invalid else R.string.shizuku_label,
            Icons.Default.ErrorOutline
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { if (supported && profileAvailable && profileValid) onShizukuClick() else onParametersClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                tint = when {
                    ready -> MaterialTheme.colorScheme.onSecondaryContainer
                    missing -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(when {
                        ready && shizukuEnabled -> R.string.kernel_profile_ready_shizuku
                        ready -> R.string.kernel_profile_ready
                        !supported -> R.string.kernel_unsupported_summary
                        !profileAvailable -> R.string.kernel_profile_required_summary
                        !profileValid -> R.string.kernel_profile_invalid_summary
                        shizukuStatus == ShizukuStatus.PERMISSION_REQUIRED ->
                            R.string.shizuku_status_permission_required
                        else -> R.string.shizuku_status_not_running
                    }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeviceInfoCard(
    deviceName: String,
    socName: String,
    kernelRelease: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(R.string.device_label, deviceName)
            InfoRow(R.string.soc_label, socName)
            InfoRow(R.string.kernel_label, kernelRelease)
        }
    }
}

@Composable
fun InfoRow(titleRes: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f)
                .wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
fun CpuPairSelector(
    labels: List<String>,
    selectedIndex: Int,
    customPair: CpuPair?,
    onSelected: (Int) -> Unit
) {
    val customText = customPair?.let {
        stringResource(
            R.string.cpu_pair_custom,
            "${it.primary}, ${it.consumer}"
        )
    }
    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = { /* handled elsewhere */ }
    ) {
        TextField(
            label = { Text(stringResource(R.string.cpu_pair_label)) },
            value = if (customPair == null) labels[selectedIndex] else customText ?: "",
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            readOnly = true
        )
    }
    // In a real implementation, clicking the field would open a dropdown.
    // For brevity, we omit the dropdown menu; the actual UI should use ExposedDropdownMenu.
}

@Composable
fun SafeModeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SwitchChecked(
        checked = checked,
        onCheckedChange = onCheckedChange,
        title = stringResource(R.string.safe_mode_label),
        description = stringResource(R.string.safe_mode_summary)
    )
}

@Composable
fun ShizukuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    status: ShizukuStatus
) {
    SwitchChecked(
        checked = checked,
        onCheckedChange = onCheckedChange,
        title = stringResource(R.string.shizuku_label),
        description = stringResource(
            when {
                !checked -> R.string.shizuku_summary
                status == ShizukuStatus.READY -> R.string.shizuku_status_ready
                status == ShizukuStatus.PERMISSION_REQUIRED -> R.string.shizuku_status_permission_required
                status == ShizukuStatus.NOT_RUNNING -> R.string.shizuku_status_not_running
                else -> R.string.shizuku_status_checking
            }
        )
    )
}

@Composable
private fun SwitchChecked(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdvancedSettingsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.advanced_settings))
    }
}

@Composable
fun RunButton(
    running: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onBlockedClick: () -> Unit
) {
    if (running) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        Button(
            onClick = {
                if (enabled) onClick() else onBlockedClick()
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (enabled) stringResource(R.string.action_run) else stringResource(R.string.action_blocked),
                color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Placeholder screens for other destinations
@Composable
fun AdvancedScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Advanced Screen", actions::onCloseAdvanced)
}

@Composable
fun AboutScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("About Screen", actions::onCloseAbout)
}

@Composable
fun ParameterScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Parameter Screen", actions::onCloseParameters)
}

@Composable
fun LoadConfigScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Load Config Screen", actions::onCloseLoadConfig)
}

@Composable
fun BuiltinProfileScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Builtin Profile Screen", actions::onCloseBuiltinProfiles)
}

@Composable
fun UserProfileDetailScreen(
    state: GhostlockUiState,
    actions: GhostlockActions,
    name: String
) {
    ScreenPlaceholder("UserProfileDetail: $name", actions::onCloseUserProfileDetail)
}

@Composable
fun ProfileOverrideScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Profile Override Screen", actions::onCloseProfileOverrides)
}

@Composable
fun AdvancedOverrideScreen(state: GhostlockUiState, actions: GhostlockActions) {
    ScreenPlaceholder("Advanced Override Screen", actions::onCloseAdvancedOverrides)
}

@Composable
fun ScreenPlaceholder(
    title: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(64.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.close))
        }
    }
}

@Composable
fun ManagerStatusCard(manager: ManagerDetection) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            color = when {
                manager.spoofed -> MaterialTheme.colorScheme.errorContainer
                manager.identityVerified -> MaterialTheme.colorScheme.secondaryContainer
                manager.installed -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "RUNTIME MANAGER",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (manager.installed) manager.name else "No Manager",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            !manager.installed -> "Not installed"
                            manager.spoofed -> "Identity Mismatch"
                            manager.identityVerified -> "Verified"
                            manager.recognized -> "Recognized"
                            else -> "Unknown"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            manager.spoofed -> MaterialTheme.colorScheme.error
                            manager.identityVerified -> MaterialTheme.colorScheme.secondary
                            manager.recognized -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRowText("PACKAGE", manager.packageName.ifEmpty { "—" })
                    InfoRowText("SIGNATURE", if (manager.identityVerified) "Verified" else "Not Verified")
                    InfoRowText("INTEGRITY", if (manager.installed) {
                        if (manager.spoofed) "Mismatch" else "Recognized"
                    } else "Not available")
                }
            }
        }
    }
}

@Composable
private fun InfoRowText(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// Mock extensions for nullable safety
private fun String?.ifEmpty(defaultValue: String): String = if (this == null || this.isEmpty()) defaultValue else this

// Assuming these resources exist; adjust as needed
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)

// Dummy implementations for theme functions (replace with actual imports)
// We assume the imported lightColorScheme and darkColorScheme from miuix theme are compatible with Material3.
// If not, you would need to map them to Material3 ColorScheme manually.
@Composable
fun lightColorScheme() = top.yukonga.miuix.kmp.theme.lightColorScheme()
@Composable
fun darkColorScheme() = top.yukonga.miuix.kmp.theme.darkColorScheme()