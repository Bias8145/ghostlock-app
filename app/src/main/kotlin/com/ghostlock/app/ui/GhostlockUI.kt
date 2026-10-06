package com.ghostlock.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostlock.app.R
import com.ghostlock.app.domain.model.CpuPair
import com.ghostlock.app.domain.model.ExecutionFieldValue
import com.ghostlock.app.domain.model.ProfileFieldNode
import com.ghostlock.app.domain.model.ShizukuStatus
import com.ghostlock.app.domain.model.UserProfileFile
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.navBackStackOf
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

data class GhostlockUiState(
    val deviceName: String = "",
    val kernelRelease: String = "",
    val socName: String = "",
    val kernelSupported: Boolean = false,
    val shizukuEnabled: Boolean = false,
    val shizukuStatus: ShizukuStatus = ShizukuStatus.NOT_REQUIRED,
    val manager: ManagerDetection = ManagerDetection(),
    val running: Boolean = false,
    val cpuPairLabels: List<String> = emptyList(),
    val cpuPairIndex: Int = 0,
    /** CPU pair from the resolved profile when it differs from the device pick. */
    val customCpuPair: CpuPair? = null,
    val safeModeEnabled: Boolean = false,
    val forceAttackTestEnabled: Boolean = false,
    val tcpRouteEnabled: Boolean = true,
    val compact: Boolean = false,
    val executionSheetVisible: Boolean = false,
    val executionSheetDismissible: Boolean = false,
    val dialogVisible: Boolean = false,
    val dialogType: DialogType = DialogType.NONE,
    val dialogTitleRes: Int = 0,
    val dialogMessage: String = "",
    val dialogMessageRes: Int = 0,
    val dialogItems: List<String> = emptyList(),
    val dialogItemResIds: List<Int> = emptyList(),
    val dialogCurrentItemIndex: Int = -1,
    val dialogInput: String = "",
    val dialogConfirmLabelRes: Int = R.string.parse_start,
    /** Documentation URL shown as an extra button on a NOTICE dialog. */
    val dialogDocUrl: String? = null,
    val overwriteDialogVisible: Boolean = false,
    val overwriteMessage: String = "",
    val logLines: List<GhostlockLogLine> = emptyList(),
    val executionRelease: String = "",
    val executionHasProfile: Boolean = false,
    val executionFields: List<ExecutionFieldValue> = emptyList(),
    val executionEditing: Map<String, String> = emptyMap(),
    val advancedScreenVisible: Boolean = false,
    val debugExportEnabled: Boolean = true,
    val debugExportLocation: String = "",
    val debugKernelLogEnabled: Boolean = true,
    val aboutVisible: Boolean = false,
    val parametersVisible: Boolean = false,
    val profileOverrideVisible: Boolean = false,
    val advancedOverrideVisible: Boolean = false,
    /** Stored document edited by the open session; null for the builtin. */
    val editTargetName: String? = null,
    val profileOverrideRelease: String = "",
    val profileOverrideRoots: List<ProfileFieldNode> = emptyList(),
    val profileOverrideEditing: Map<String, String> = emptyMap(),
    /** Controller-reported geometry violations, dotted paths. */
    val profileInvalidPaths: Set<String> = emptySet(),
    /** Explicit route from the profile; null means geometry inference. */
    val profileRoute: String? = null,
    /** Declared fallback route; null/"none" means disabled. */
    val profileFallback: String? = null,
    /** Manually selected builtin source; null means automatic matching. */
    val activeBuiltinProfile: String? = null,
    val builtinScreenVisible: Boolean = false,
    /** Unfilled reference templates, listed separately on the builtin picker. */
    val builtinTemplates: List<String> = emptyList(),
    /** Builtin releases sorted by similarity to the device kernel. */
    val builtinProfiles: List<String> = emptyList(),
    val loadConfigVisible: Boolean = false,
    /** Verbatim documents in the user profile folder, newest first. */
    val userProfiles: List<UserProfileFile> = emptyList(),
    /** Loaded user document feeding the imported layer; null means none. */
    val activeUserProfile: String? = null,
    /** File name of the open user-profile detail screen, null when closed. */
    val userProfileDetail: String? = null,
    val userProfileRenameTarget: String? = null,
    val userProfileDeleteTarget: String? = null,
)

enum class DialogType { NONE, LIST, INPUT, CONFIRM, NOTICE }

data class GhostlockLogLine(val text: String, val color: Int)

interface GhostlockActions {
    fun onRun()
    fun onProfileInvalid()
    fun onStatusClick()
    fun onCloseExecutionSheet()
    fun onCopyLogs()
    fun onImportOffsetsHocon()
    fun onImportOffsetsJson()
    fun onDocumentsResult(request: DocumentRequest, uris: List<String>)
    fun onParseOta()
    fun onParseImage()
    fun onCpuPairSelected(index: Int)
    fun onSafeModeChanged(enabled: Boolean)
    fun onForceAttackTestChanged(enabled: Boolean)
    fun onShizukuChanged(enabled: Boolean)
    fun onDialogItemSelected(index: Int)
    fun onDialogInputChange(value: String)
    fun onDialogConfirm(value: String)
    fun onDialogDismiss()
    fun onDialogDismissFinished()
    fun onOverwriteConfirm()
    fun onOverwriteDismiss()
    fun onExecutionFieldChanged(path: String, value: String)
    fun onRouteChanged(index: Int)
    fun onFallbackChanged(index: Int)
    fun onExportProfile()
    fun onSaveProfileEdits()
    fun onSaveProfileAs()
    fun onExportProfileEdits()
    fun onRevertProfileEdits()
    fun onOpenAdvanced()
    fun onCloseAdvanced()
    fun onShowAbout()
    fun onCloseAbout()
    fun onDebugExportChanged(enabled: Boolean)
    fun onDebugExportLocationPick()
    fun onDebugKernelLogChanged(enabled: Boolean)
    fun onOpenParameters()
    fun onCloseParameters()
    fun onOpenLoadConfig()
    fun onCloseLoadConfig()
    fun onOpenUserProfileDetail(name: String)
    fun onCloseUserProfileDetail()
    fun onLoadUserProfile(name: String)
    fun onUnloadUserProfile()
    fun onEditUserProfile(name: String)
    fun onUserProfileRename(name: String)
    fun onUserProfileExport(name: String)
    fun onConvertUserProfile(name: String)
    fun onUserProfileDelete(name: String)
    fun onUserProfileDeleteConfirm()
    fun onUserProfileDeleteDismiss()
    fun onOpenBuiltinProfiles()
    fun onCloseBuiltinProfiles()
    fun onSelectBuiltinProfile(release: String?)
    fun onOpenProfileOverrides()
    fun onCloseProfileOverrides()
    fun onOpenAdvancedOverrides()
    fun onCloseAdvancedOverrides()
    fun onProfileOverrideChanged(path: String, value: String)
}

internal sealed interface GhostlockScreen : NavKey {
    data object Main : GhostlockScreen
    data object Advanced : GhostlockScreen
    data object About : GhostlockScreen
    data object Parameters : GhostlockScreen
    data object LoadConfig : GhostlockScreen
    data object Builtin : GhostlockScreen
    data class UserProfileDetail(val name: String) : GhostlockScreen
    data object ProfileOverride : GhostlockScreen
    data object AdvancedOverride : GhostlockScreen
}

internal fun navigationPath(state: GhostlockUiState): List<GhostlockScreen> {
    val path = mutableListOf<GhostlockScreen>(GhostlockScreen.Main)
    if (!state.advancedScreenVisible) return path
    path += GhostlockScreen.Advanced
    if (state.aboutVisible) {
        path += GhostlockScreen.About
        return path
    }
    if (!state.parametersVisible) return path
    path += GhostlockScreen.Parameters
    if (state.loadConfigVisible) {
        path += GhostlockScreen.LoadConfig
        when {
            state.builtinScreenVisible -> path += GhostlockScreen.Builtin
            state.userProfileDetail != null ->
                path += GhostlockScreen.UserProfileDetail(state.userProfileDetail)
        }
    }
    if (state.profileOverrideVisible) {
        path += GhostlockScreen.ProfileOverride
        if (state.advancedOverrideVisible) path += GhostlockScreen.AdvancedOverride
    }
    return path
}

internal fun syncNavigationPath(backStack: NavBackStack, desired: List<GhostlockScreen>) {
    var common = 0
    while (common < backStack.size && common < desired.size &&
        backStack[common] == desired[common]
    ) {
        common++
    }
    while (backStack.size > common) backStack.removeAt(backStack.lastIndex)
    backStack.addAll(desired.drop(common))
}

private fun closeScreen(screen: GhostlockScreen, actions: GhostlockActions) {
    when (screen) {
        GhostlockScreen.Main -> Unit
        GhostlockScreen.Advanced -> actions.onCloseAdvanced()
        GhostlockScreen.About -> actions.onCloseAbout()
        GhostlockScreen.Parameters -> actions.onCloseParameters()
        GhostlockScreen.LoadConfig -> actions.onCloseLoadConfig()
        GhostlockScreen.Builtin -> actions.onCloseBuiltinProfiles()
        is GhostlockScreen.UserProfileDetail -> actions.onCloseUserProfileDetail()
        GhostlockScreen.ProfileOverride -> actions.onCloseProfileOverrides()
        GhostlockScreen.AdvancedOverride -> actions.onCloseAdvancedOverrides()
    }
}

private fun ghostlockLightColors() = lightColorScheme(
    primary = Color(0xFFDCC58A),
    onPrimary = Color(0xFF3C321D),
    primaryContainer = Color(0xFFF3E8C8),
    onPrimaryContainer = Color(0xFF514323),
    secondary = Color(0xFF8A784B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9E1D0),
    onSecondaryContainer = Color(0xFF29261F),
    background = Color(0xFFF9F6EE),
    onBackground = Color(0xFF29261F),
    surface = Color(0xFFFFFCF7),
    onSurface = Color(0xFF29261F),
    surfaceVariant = Color(0xFFF1ECE2),
    error = Color(0xFF9A5E68),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF5DDE1),
    onErrorContainer = Color(0xFF5C2832),
)

private fun ghostlockDarkColors() = darkColorScheme(
    primary = Color(0xFFE3C982),
    onPrimary = Color(0xFF3B3017),
    primaryContainer = Color(0xFF574A2F),
    onPrimaryContainer = Color(0xFFF8E9B7),
    secondary = Color(0xFFE6D29A),
    onSecondary = Color(0xFF3B3017),
    secondaryContainer = Color(0xFF3B362A),
    onSecondaryContainer = Color(0xFFF5F0E5),
    background = Color(0xFF201F1B),
    onBackground = Color(0xFFF5F0E5),
    surface = Color(0xFF292820),
    onSurface = Color(0xFFF5F0E5),
    surfaceVariant = Color(0xFF322F25),
    error = Color(0xFFFFB5BF),
    onError = Color(0xFF5A343D),
    errorContainer = Color(0xFF5A343D),
    onErrorContainer = Color(0xFFFFDADF),
)

@Composable
internal fun GhostlockApp(
    state: GhostlockUiState,
    actions: GhostlockActions,
) {
    val colors = if (isSystemInDarkTheme()) ghostlockDarkColors() else ghostlockLightColors()
    MiuixTheme(colors = colors) {
        val desiredPath = navigationPath(state)
        val backStack = remember {
            navBackStackOf(GhostlockScreen.Main).apply { addAll(desiredPath.drop(1)) }
        }
        LaunchedEffect(desiredPath) { syncNavigationPath(backStack, desiredPath) }
        val swipeBack = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
            NavSwipeDirection.RightToLeft
        } else {
            NavSwipeDirection.LeftToRight
        }
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    effects = NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius()),
                    onBack = {
                        when {
                            state.executionSheetVisible -> {
                                if (state.executionSheetDismissible) actions.onCloseExecutionSheet()
                            }

                            state.overwriteDialogVisible -> actions.onOverwriteDismiss()
                            state.dialogVisible -> actions.onDialogDismiss()
                            state.userProfileDeleteTarget != null -> actions.onUserProfileDeleteDismiss()
                            else -> {
                                val screen = backStack.lastOrNull() as? GhostlockScreen
                                if (screen != null && screen != GhostlockScreen.Main) {
                                    closeScreen(screen, actions)
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            }
                        }
                    },
                ) {
                    entry<GhostlockScreen.Main>(swipeDismiss = NavSwipeDirection.None) {
                        MainScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.Advanced>(swipeDismiss = swipeBack) {
                        AdvancedScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.About>(swipeDismiss = swipeBack) {
                        AboutScreen(onBack = actions::onCloseAbout)
                    }
                    entry<GhostlockScreen.Parameters>(swipeDismiss = swipeBack) {
                        ParameterScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.LoadConfig>(swipeDismiss = swipeBack) {
                        LoadConfigScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.Builtin>(swipeDismiss = swipeBack) {
                        BuiltinProfileScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.UserProfileDetail>(swipeDismiss = swipeBack) { screen ->
                        UserProfileDetailScreen(state = state, actions = actions, name = screen.name)
                    }
                    entry<GhostlockScreen.ProfileOverride>(swipeDismiss = swipeBack) {
                        ProfileOverrideScreen(state = state, actions = actions)
                    }
                    entry<GhostlockScreen.AdvancedOverride>(swipeDismiss = swipeBack) {
                        AdvancedOverrideScreen(state = state, actions = actions)
                    }
                }
                GhostlockDialog(state = state, actions = actions)
                GhostlockOverwriteDialog(state = state, actions = actions)
                GhostlockExecutionSheet(state = state, actions = actions)
            }
        }
    }
}

@Composable
private fun MainScreen(
    state: GhostlockUiState,
    actions: GhostlockActions,
) {
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "GhostLock",
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        MainContent(
            state = state,
            actions = actions,
            scrollBehavior = scrollBehavior,
            scaffoldPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun pageContentPadding(
    scaffoldPadding: PaddingValues,
    top: Dp = 6.dp,
    bottom: Dp = 10.dp,
    horizontalMin: Dp = 12.dp,
): PaddingValues {
    val windowWidth = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val horizontal = ((windowWidth - 800.dp) / 2).coerceAtLeast(horizontalMin)
    return PaddingValues(
        start = horizontal,
        end = horizontal,
        top = scaffoldPadding.calculateTopPadding() + top,
        bottom = scaffoldPadding.calculateBottomPadding() + bottom,
    )
}

@Composable
private fun GhostlockExecutionSheet(
    state: GhostlockUiState,
    actions: GhostlockActions,
) {
    OverlayBottomSheet(
        show = state.executionSheetVisible,
        title = stringResource(R.string.log_title),
        allowDismiss = state.executionSheetDismissible,
        onDismissRequest = actions::onCloseExecutionSheet,
        startAction = {
            IconButton(onClick = actions::onCopyLogs) {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = stringResource(R.string.action_copy),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
        endAction = {
            IconButton(
                enabled = state.executionSheetDismissible,
                onClick = actions::onCloseExecutionSheet,
            ) {
                Icon(
                    imageVector = MiuixIcons.Close,
                    contentDescription = stringResource(R.string.action_close),
                )
            }
        },
        content = {
            LogPanel(
                lines = state.logLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp, max = 520.dp)
                    .navigationBarsPadding(),
            )
        },
    )
}

@Composable
private fun GhostlockDialog(
    state: GhostlockUiState,
    actions: GhostlockActions,
) {
    OverlayDialog(
        show = state.dialogVisible,
        title = if (state.dialogType == DialogType.NONE) null else stringResource(state.dialogTitleRes),
        onDismissRequest = actions::onDialogDismiss,
        onDismissFinished = actions::onDialogDismissFinished,
        content = {
            when (state.dialogType) {
                DialogType.LIST -> {
                    val items = state.dialogItems.ifEmpty { state.dialogItemResIds.map { stringResource(it) } }
                    items.forEachIndexed { index, item ->
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            text = if (index == state.dialogCurrentItemIndex) {
                                stringResource(R.string.export_current_marker, item)
                            } else {
                                item
                            },
                            onClick = { actions.onDialogItemSelected(index) },
                        )
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.cancel),
                        onClick = actions::onDialogDismiss,
                    )
                }

                DialogType.INPUT -> {
                    TextField(
                        value = state.dialogInput,
                        onValueChange = actions::onDialogInputChange,
                        label = stringResource(state.dialogMessageRes),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.cancel),
                            onClick = actions::onDialogDismiss,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(state.dialogConfirmLabelRes),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            onClick = { actions.onDialogConfirm(state.dialogInput) },
                        )
                    }
                }

                DialogType.CONFIRM -> {
                    Text(
                        text = stringResource(state.dialogMessageRes),
                        modifier = Modifier.fillMaxWidth(),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f)Summary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.cancel),
                            onClick = actions::onDialogDismiss,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.w3_shizuku_hint_enable),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            onClick = { actions.onDialogConfirm("") },
                        )
                    }
                }

                DialogType.NOTICE -> {
                    Text(
                        text = stringResource(state.dialogMessageRes),
                        modifier = Modifier.fillMaxWidth(),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f)Summary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    ) {
                        state.dialogDocUrl?.let { docUrl ->
                            val uriHandler = LocalUriHandler.current
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.dialog_open_guide),
                                onClick = { uriHandler.openUri(docUrl) },
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.dialog_dismiss),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            onClick = actions::onDialogDismiss,
                        )
                    }
                }

                DialogType.NONE -> Unit
            }
        },
    )
}

@Composable
private fun GhostlockOverwriteDialog(
    state: GhostlockUiState,
    actions: GhostlockActions,
) {
    OverlayDialog(
        show = state.overwriteDialogVisible,
        title = stringResource(R.string.overwrite_title),
        summary = stringResource(R.string.overwrite_message, state.overwriteMessage),
        onDismissRequest = actions::onOverwriteDismiss,
        content = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.cancel),
                    onClick = actions::onOverwriteDismiss,
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.overwrite_yes),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = actions::onOverwriteConfirm,
                )
            }
        },
    )
}

@Composable
private fun MainContent(
    state: GhostlockUiState,
    actions: GhostlockActions,
    scrollBehavior: ScrollBehavior,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .scrollEndHaptic()
            .overScrollVertical()
            .scrollEndHaptic()
            .fillMaxHeight()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding(),
        contentPadding = pageContentPadding(scaffoldPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "controls") {
            SectionLabel(text = "KERNEL & PARAMETERS")
            ControlPanel(
                state = state,
                actions = actions,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "manager") {
            SectionLabel(text = "RUNTIME")
            ManagerStatusCard(manager = state.manager, modifier = Modifier.fillMaxWidth())
        }
        item(key = "run") {
            SectionLabel(text = "EXECUTION")
            RunButton(
                running = state.running,
                supported = state.kernelSupported &&
                        state.executionHasProfile &&
                        (!state.shizukuEnabled ||
                                state.shizukuStatus == ShizukuStatus.READY),
                profileValid = state.profileInvalidPaths.isEmpty(),
                labelRes = R.string.action_run,
                onClick = actions::onRun,
                onBlockedClick = actions::onProfileInvalid,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(start = 2.dp, bottom = 2.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f),
    )
}

@Composable
private fun ControlPanel(
    state: GhostlockUiState,
    actions: GhostlockActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ActivationStatusCard(
            supported = state.kernelSupported,
            profileAvailable = state.executionHasProfile,
            profileValid = state.profileInvalidPaths.isEmpty(),
            shizukuEnabled = state.shizukuEnabled,
            shizukuStatus = state.shizukuStatus,
            onParametersClick = actions::onOpenParameters,
            onShizukuClick = actions::onStatusClick,
            modifier = Modifier.fillMaxWidth(),
        )
        DeviceInfoCard(
            deviceName = state.deviceName,
            socName = state.socName,
            kernelRelease = state.kernelRelease,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
        Card(modifier = modifier.padding(top = 12.dp)) {
            if (state.cpuPairLabels.isNotEmpty()) {
                val customPair = state.customCpuPair
                val customSummary = customPair?.let {
                    stringResource(
                        R.string.cpu_pair_custom,
                        "${it.primary}, ${it.consumer}",
                    )
                }
                OverlaySpinnerPreference(
                    title = stringResource(R.string.cpu_pair_label),
                    items = state.cpuPairLabels.map { DropdownItem(icon = null, title = it) },
                    selectedIndex = if (customPair == null) state.cpuPairIndex else -1,
                    summary = customSummary,
                    showValue = customPair == null,
                    onSelectedIndexChange = actions::onCpuPairSelected,
                )
            }
            SwitchPreference(
                checked = state.safeModeEnabled,
                onCheckedChange = actions::onSafeModeChanged,
                title = stringResource(R.string.safe_mode_label),
                summary = stringResource(R.string.safe_mode_summary),
            )
            /* PROFILE-SUGGEST-01: the profile suggestion seeds the toggle but no
             * longer hides it; an explicit user choice overrides either way. */
            SwitchPreference(
                checked = state.shizukuEnabled,
                onCheckedChange = actions::onShizukuChanged,
                title = stringResource(R.string.shizuku_label),
                summary = stringResource(
                    when {
                        !state.shizukuEnabled -> R.string.shizuku_summary
                        state.shizukuStatus == ShizukuStatus.READY -> R.string.shizuku_status_ready
                        state.shizukuStatus == ShizukuStatus.PERMISSION_REQUIRED ->
                            R.string.shizuku_status_permission_required

                        state.shizukuStatus == ShizukuStatus.NOT_RUNNING ->
                            R.string.shizuku_status_not_running

                        else -> R.string.shizuku_status_checking
                    },
                ),
            )
        }
        Card(modifier = modifier.padding(top = 12.dp)) {
            ArrowPreference(
                title = stringResource(R.string.advanced_settings),
                summary = stringResource(R.string.advanced_settings_summary),
                onClick = actions::onOpenAdvanced,
            )
        }
    }
}

@Composable
private fun ActivationStatusCard(
    supported: Boolean,
    profileAvailable: Boolean,
    profileValid: Boolean,
    shizukuEnabled: Boolean,
    shizukuStatus: ShizukuStatus,
    onParametersClick: () -> Unit,
    onShizukuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accessReady = !shizukuEnabled || shizukuStatus == ShizukuStatus.READY
    val ready = supported && profileAvailable && profileValid && accessReady
    val missing = !supported || !profileAvailable
    val (backgroundColor, title, icon) = when {
        ready -> Triple(
            when {
                MiuixTheme.isDynamicColor -> MiuixTheme.colorScheme.secondaryContainer
                isSystemInDarkTheme() -> Color(0xFF1A3825)
                else -> Color(0xFFDFFAE4)
            },
            R.string.kernel_supported,
            Icons.Rounded.CheckCircleOutline,
        )

        missing -> Triple(
            MiuixTheme.colorScheme.errorContainer,
            if (!supported) R.string.kernel_unsupported else R.string.kernel_profile_required,
            Icons.Rounded.RemoveCircleOutline,
        )

        else -> Triple(
            when {
                MiuixTheme.isDynamicColor -> MiuixTheme.colorScheme.tertiaryContainer
                isSystemInDarkTheme() -> Color(0xFF3E2F1B)
                else -> Color(0xFFFFF0DB)
            },
            if (!profileValid) R.string.kernel_profile_invalid else R.string.shizuku_label,
            Icons.Rounded.ErrorOutline,
        )
    }
    val summary = when {
        ready && shizukuEnabled -> R.string.kernel_profile_ready_shizuku
        ready -> R.string.kernel_profile_ready
        !supported -> R.string.kernel_unsupported_summary
        !profileAvailable -> R.string.kernel_profile_required_summary
        !profileValid -> R.string.kernel_profile_invalid_summary
        shizukuStatus == ShizukuStatus.PERMISSION_REQUIRED ->
            R.string.shizuku_status_permission_required

        else -> R.string.shizuku_status_not_running
    }
    val action = if (supported && profileAvailable && profileValid && !accessReady) {
        onShizukuClick
    } else {
        onParametersClick
    }
    Card(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.defaultColors(color = backgroundColor),
        onClick = action,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 104.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (MiuixTheme.isDynamicColor) {
                    when (icon) {
                        Icons.Rounded.CheckCircleOutline -> MiuixTheme.colorScheme.primary.copy(alpha = 0.8f)
                        Icons.Rounded.RemoveCircleOutline ->
                            MiuixTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)

                        else -> MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    }
                } else {
                    when (icon) {
                        Icons.Rounded.CheckCircleOutline -> Color(0xFF36D167)
                        Icons.Rounded.RemoveCircleOutline -> Color(0xFFF5A623)
                        else -> Color(0xFFF72727)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 27.dp, y = 31.dp)
                    .size(96.dp),
            )
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 110.dp, bottom = 14.dp),
            ) {
                Text(
                    text = stringResource(title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = stringResource(summary),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(
    deviceName: String,
    socName: String,
    kernelRelease: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        insideMargin = PaddingValues(14.dp),
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DeviceInfoItem(
                    title = stringResource(R.string.device_label),
                    value = deviceName,
                )
                DeviceInfoItem(
                    title = stringResource(R.string.soc_label),
                    value = socName,
                )
                DeviceInfoItem(
                    title = stringResource(R.string.kernel_label),
                    value = kernelRelease,
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MiuixTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 2.dp),
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
    }
}

/* profile-ui: resolved execution view with auto-saved sparse overrides. */
@Composable
internal fun ExecutionEditor(
    state: GhostlockUiState,
    actions: GhostlockActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (field in state.executionFields) {
            val text = state.executionEditing[field.path] ?: field.value.toString()
            val invalid = isFieldInputInvalid(text) || field.path in state.profileInvalidPaths
            TextField(
                value = text,
                onValueChange = { value -> actions.onExecutionFieldChanged(field.path, value) },
                label = fieldLabel(field.path, field.path.substringAfterLast('.')),
                colors = when {
                    invalid ->
                        TextFieldDefaults.textFieldColors(labelColor = FieldErrorHighlight)

                    field.overridden ->
                        TextFieldDefaults.textFieldColors(labelColor = OverrideHighlight)

                    else -> TextFieldDefaults.textFieldColors()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

internal val OverrideHighlight = Color(0xFFF5A623)

/** Red marks an unfilled or non-numeric field in the parameter editors. */
internal val FieldErrorHighlight = Color(0xFFE53935)

internal fun isFieldInputInvalid(text: String): Boolean = text.trim().toLongOrNull() == null

@Composable
private fun ManagerStatusCard(
    manager: ManagerDetection,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val statusColor = when {
        !manager.installed -> MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        manager.spoofed -> MiuixTheme.colorScheme.error
        manager.identityVerified -> Color(0xFF4D9B67)
        else -> MiuixTheme.colorScheme.primary
    }
    val surface = when {
        manager.spoofed -> if (dark) Color(0xFF3A2928) else Color(0xFFF5E4E1)
        manager.installed -> if (dark) Color(0xFF302D25) else Color(0xFFF4EFE3)
        else -> if (dark) Color(0xFF292820) else Color(0xFFF1ECE2)
    }
    Card(
        modifier = modifier.clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.defaultColors(color = surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "RUNTIME MANAGER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (manager.installed) manager.name else "No Manager",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            !manager.installed -> "Not installed"
                            manager.spoofed -> "Identity Mismatch"
                            manager.identityVerified -> "Verified"
                            manager.recognized -> "Recognized"
                            else -> "Unknown"
                        },
                        fontSize = 12.sp,
                        color = statusColor,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ManagerMeta(label = "PACKAGE", value = manager.packageName.ifEmpty { "—" }, modifier = Modifier.weight(1.2f))
                ManagerMeta(label = "SIGNATURE", value = if (manager.identityVerified) "Verified" else "Not Verified", modifier = Modifier.weight(1f))
                ManagerMeta(label = "INTEGRITY", value = if (manager.installed) {
                    if (manager.spoofed) "Mismatch" else "Recognized"
                } else "Not available", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ManagerMeta(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        Text(value, modifier = Modifier.padding(top = 3.dp), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RunButton(
    running: Boolean,
    supported: Boolean,
    profileValid: Boolean,
    labelRes: Int,
    onClick: () -> Unit,
    onBlockedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        TextButton(
            text = stringResource(if (running) R.string.action_running else labelRes),
            enabled = supported && profileValid && !running,
            colors = ButtonDefaults.textButtonColorsPrimary(),
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
        )
        if (running) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.32f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(MiuixTheme.colorScheme.primary)
                )
            }
        }
        /* A disabled TextButton consumes no pointer input, so this overlay
         * explains why the run is blocked. */
        if (!running && (!supported || !profileValid)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onBlockedClick() },