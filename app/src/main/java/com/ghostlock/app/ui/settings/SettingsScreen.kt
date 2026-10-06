@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ghostlock.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    val openFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handlePickedFile(context, it, actions) }
    }

    val settings = settingsItems(context, themeRepository, openFilePicker, actions)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
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
            items(settings.size) { index ->
                settings[index]()
            }
        }
    }
}

@Composable
private fun settingsItems(
    context: Context,
    themeRepository: ThemeRepository,
    openFilePicker: ActivityResultLauncher<String>,
    actions: GhostlockActions
): List<@Composable () -> Unit> {
    val prefs = remember { GhostlockPrefs(context) }
    val scope = rememberCoroutineScope()

    return listOf(
        { SectionHeader("General") },
        { ThemePreference(themeRepository) },
        { LanguagePreference() },
        { TextScalePreference() },
        { SafeModeSwitchPreference(prefs, scope, actions) },
        { SectionHeader("Runtime") },
        { ShizukuPreference(actions) },
        { SectionHeader("Configuration") },
        { ExportConfigShortcut(actions) },
        { ImportConfigShortcut(openFilePicker) },
        { AdvancedShortcut(actions) },
        { SectionHeader("About") },
        { AboutShortcut(actions) }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ThemePreference(repository: ThemeRepository) {
    var mode by remember { mutableStateOf(repository.uiMode.value) }
    PreferenceCard(
        title = "Theme",
        subtitle = when (mode) {
            UiMode.SYSTEM -> "System default"
            UiMode.LIGHT -> "Light"
            UiMode.DARK -> "Dark"
        },
        onClick = {
            mode = when (mode) {
                UiMode.SYSTEM -> UiMode.LIGHT
                UiMode.LIGHT -> UiMode.DARK
                UiMode.DARK -> UiMode.SYSTEM
            }
            repository.setUiMode(mode)
        }
    )
}

@Composable
private fun LanguagePreference() {
    PreferenceCard(title = "Language", subtitle = "English", onClick = {})
}

@Composable
private fun TextScalePreference() {
    PreferenceCard(title = "Text size", subtitle = "Default", onClick = {})
}

@Composable
private fun SafeModeSwitchPreference(
    prefs: GhostlockPrefs,
    scope: kotlinx.coroutines.CoroutineScope,
    actions: GhostlockActions
) {
    var enabled by remember { mutableStateOf(false) }
    PreferenceCard(
        title = "Safe mode",
        subtitle = if (enabled) "Enabled" else "Disabled",
        onClick = {
            enabled = !enabled
            scope.launch { prefs.setSafeMode(enabled) }
            actions.onSafeModeChanged(enabled)
        }
    )
}

@Composable
private fun ShizukuPreference(actions: GhostlockActions) {
    PreferenceCard(title = "Shizuku", subtitle = "Tap to configure", onClick = actions::onShizukuClick)
}

@Composable
private fun ExportConfigShortcut(actions: GhostlockActions) {
    PreferenceCard(title = "Export configuration", subtitle = "Save current settings", onClick = actions::onExportProfile)
}

@Composable
private fun ImportConfigShortcut(openFilePicker: ActivityResultLauncher<String>) {
    PreferenceCard(
        title = "Import configuration",
        subtitle = "Load a configuration file",
        onClick = { openFilePicker.launch("*/*") }
    )
}

@Composable
private fun AdvancedShortcut(actions: GhostlockActions) {
    PreferenceCard(title = "Advanced", subtitle = "Advanced runtime parameters", onClick = actions::onOpenAdvanced)
}

@Composable
private fun AboutShortcut(actions: GhostlockActions) {
    PreferenceCard(title = "About", subtitle = "GhostLock information", onClick = actions::onShowAbout)
}

@Composable
private fun PreferenceCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun handlePickedFile(context: Context, uri: Uri, actions: GhostlockActions) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    val name = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull() ?: uri.lastPathSegment.orEmpty()

    val content = runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    }.getOrNull() ?: return

    when {
        name.endsWith(".hocon", ignoreCase = true) -> actions.onImportOffsetsHocon(content)
        name.endsWith(".json", ignoreCase = true) -> actions.onImportOffsetsJson(content)
    }
}
