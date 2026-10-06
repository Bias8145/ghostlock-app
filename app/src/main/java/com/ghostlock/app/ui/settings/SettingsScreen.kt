package com.ghostlock.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostlock.app.R
import com.ghostlock.app.data.GhostlockPrefs
import com.ghostlock.app.ui.theme.ThemeRepository
import com.ghostlock.app.ui.theme.UiMode
import kotlinx.coroutines.launch

/**
 * Interface for actions that the SettingsScreen can trigger.
 * The implementing class (usually an Activity or ViewModel) should provide the actual implementations.
 */
interface GhostlockActions {
    fun onShizukuClick()
    fun onSafeModeChanged(enabled: Boolean)
    fun onOpenAdvanced()
    fun onExportProfile()
    fun onImportOffsetsHocon(content: String)
    fun onImportOffsetsJson(content: String)
    fun onCloseParameters()
    fun onShowAbout()
}

@Composable
fun SettingsScreen(
    actions: GhostlockActions,
    themeRepository: ThemeRepository
) {
    val context = LocalContext.current

    // File picker for importing config
    val openFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handlePickedFile(context, it, actions) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { actions.onCloseParameters() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(settingsItems(context, themeRepository, openFilePicker, actions)) { item ->
                item()
            }
        }
    }
}

/* ---------- DATA SETTINGS ---------- */
private data class SettingsItem(
    val title: String,
    val icon: Int? = null,
    val content: @Composable () -> Unit
)

private fun SettingsItem(
    title: String,
    icon: Int? = null,
    content: @Composable () -> Unit
) = SettingsItem(title, icon, content)

@Composable
private fun settingsItems(
    context: Context,
    themeRepository: ThemeRepository,
    openFilePicker: ManagedActivityResultLauncher<String, Uri?>,
    actions: GhostlockActions
): List<SettingsItem> {
    return listOf(
        // ---------- DISPLAY ----------
        SettingsItem(
            title = stringResource(R.string.settings_theme_title),
            icon = R.drawable.ic_theme, // add this icon to drawable if needed
            content = { ThemeSelector(themeRepository) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_language_title),
            icon = R.drawable.ic_language,
            content = { LanguageSelector() }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_text_scale_title),
            icon = R.drawable.ic_text_size,
            content = { TextScaleSelector() }
        ),
        // ---------- FUNCTIONALITY ----------
        SettingsItem(
            title = stringResource(R.string.settings_shizuku),
            icon = R.drawable.ic_shizuku,
            content = { ShizukuShortcut(actions) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_safe_mode),
            icon = R.drawable.ic_safe_mode,
            content = { SafeModeSwitchPreference(themeRepository, actions) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_cpu_pair),
            icon = R.drawable.ic_cpu,
            content = { CpuPairPreference(actions) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_advanced),
            icon = R.drawable.ic_advanced,
            content = { AdvancedShortcut(actions) }
        ),
        // ---------- DATA & PRIVACY ----------
        SettingsItem(
            title = stringResource(R.string.settings_export_profile),
            icon = R.drawable.ic_export,
            content = { ExportProfileShortcut(actions) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_import_config),
            icon = R.drawable.ic_import,
            content = { ImportConfigShortcut(openFilePicker) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_clear_logs),
            icon = R.drawable.ic_delete,
            content = { ClearLogsShortcut(actions) }
        ),
        // ---------- ABOUT ----------
        SettingsItem(
            title = stringResource(R.string.settings_about_title),
            icon = R.drawable.ic_info,
            content = { AboutShortcut(actions) }
        ),
        SettingsItem(
            title = stringResource(R.string.settings_github),
            icon = R.drawable.ic_github,
            content = { GithubShortcut() }
        )
    )
}

/* ---------- SETTINGS COMPONENTS ---------- */
@Composable
fun ThemeSelector(repository: ThemeRepository) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_theme_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            UiMode.values().forEach { mode ->
                val isSelected = repository.uiMode.value == mode
                RadioButton(
                    selected = isSelected,
                    onClick = { repository.setUserChoice(mode) },
                    enabled = true
                )
                Text(
                    text = when (mode) {
                        UiMode.SYSTEM -> stringResource(R.string.theme_system)
                        UiMode.LIGHT -> stringResource(R.string.theme_light)
                        UiMode.DARK -> stringResource(R.string.theme_dark)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageSelector() {
    Text(
        text = stringResource(R.string.not_yet_implemented),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(24.dp)
    )
}

@Composable
fun TextScaleSelector() {
    var scale by remember { mutableStateOf(1f) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_text_scale_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = scale,
            onValueChange = { scale = it },
            valueRange = 0.8f..1.2f,
            steps = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${(scale * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun ShizukuShortcut(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_shizuku)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Terminal, contentDescription = null)
        },
        onClick = { actions.onShizukuClick() }
    )
}

@Composable
fun SafeModeSwitchPreference(repository: ThemeRepository, actions: GhostlockActions) {
    val prefs = GhostlockPrefs
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { enabled = prefs.isSafeModeEnabled(context) }
    ListItem(
        headline = { Text(stringResource(R.string.settings_safe_mode)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null)
        },
        trailingIcon = {
            Switch(
                checked = enabled.value,
                onCheckedChange = {
                    enabled = it
                    scope.launch { prefs.setSafeMode(context, it) }
                    actions.onSafeModeChanged(it)
                }
            )
        }
    )
}

@Composable
fun CpuPairPreference(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_cpu_pair)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Speed, contentDescription = null)
        },
        onClick = { actions.onOpenAdvanced() }
    )
}

@Composable
fun AdvancedShortcut(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_advanced)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        },
        onClick = { actions.onOpenAdvanced() }
    )
}

@Composable
fun ExportProfileShortcut(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_export_profile)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
        },
        onClick = { actions.onExportProfile() }
    )
}

@Composable
fun ImportConfigShortcut(openFilePicker: (Uri?) -> Unit) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_import_config)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
        },
        onClick = { openFilePicker.launch("*/*") }
    )
}

@Composable
fun ClearLogsShortcut(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_clear_logs)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        },
        onClick = {
            actions.onCloseParameters() // placeholder; replace with actual confirm dialog
        }
    )
}

@Composable
fun AboutShortcut(actions: GhostlockActions) {
    ListItem(
        headline = { Text(stringResource(R.string.settings_about_title)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Info, contentDescription = null)
        },
        onClick = { actions.onShowAbout() }
    )
}

@Composable
fun GithubShortcut() {
    ListItem(
        headline = { Text(stringResource(R.string.settings_github)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Web, contentDescription = null)
        },
        onClick = {
            val context = LocalContext.current
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Bias8145/ghostlock-app"))
            context.startActivity(intent)
        }
    )
}

/* ---------- HELPER: ListItem ---------- */
@Composable
fun ListItem(
    headline: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                headline()
            }
            Spacer(modifier = Modifier.width(12.dp))
            trailingIcon()
        }
    }
}

/* ---------- HELPER: HANDLE PICKED FILE ---------- */
fun handlePickedFile(context: Context, uri: Uri, actions: GhostlockActions) {
    val contentResolver = context.contentResolver
    val displayName = runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.let {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it != null && it.moveToFirst()) {
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else null
        } ?: uri.lastPathSegment
    } ?: "unknown_file"

    if (displayName.endsWith(".conf", ignoreCase = true) ||
        displayName.endsWith(".hocon", ignoreCase = true)) {
        val input = contentResolver.openInputStream(uri)
        input.use { stream ->
            val text = stream.bufferedReader().use { it.readText() }
            actions.onImportOffsetsHocon(text)
        }
    } else if (displayName.endsWith(".json", ignoreCase = true)) {
        val input = contentResolver.openInputStream(uri)
        input.use { stream ->
            val text = stream.bufferedReader().use { it.readText() }
            actions.onImportOffsetsJson(text)
        }
    } else {
        // Unsupported format; could show a toast or dialog (omitted for brevity)
    }
}