package dev.lildua.oddly.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.BuildConfig
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.core.text.Brand
import dev.lildua.oddly.core.text.ExternalLinks
import dev.lildua.oddly.core.text.Strings
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.data.local.PersistedState
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.ThemeMode
import dev.lildua.oddly.domain.usecase.FeedbackRules
import dev.lildua.oddly.ui.feedback.rememberOddlyFeedback
import dev.lildua.oddly.notifications.rememberNotificationPermission
import dev.lildua.oddly.ui.components.Astronaut
import dev.lildua.oddly.ui.components.GradientProgressBar
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.SettingsRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import dev.lildua.oddly.ui.components.a11yHeading
import dev.lildua.oddly.ui.components.a11yHidden
import dev.lildua.oddly.ui.components.a11yLiveRegion
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.components.selectableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.LocalStrings
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

private enum class ImportNotice { FAILED, SUCCEEDED }

/** What the "couldn't open" dialog shows: an explanation and the address to copy. */
private data class LinkFallback(val body: String, val address: String)

/**
 * S15 — configuration and local-data controls. Reset is confirmation-gated per
 * spec §16.
 *
 * Theme, language and reminder time each open a picker rather than cycling on
 * tap: with three theme modes and a free-form time, a row that changes value on
 * every tap makes the user hunt for the option they wanted.
 */
@Composable
fun SettingsScreen(state: OddlyAppState) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current
    val settings = state.settings
    val notifications = rememberNotificationPermission()
    val context = LocalContext.current
    val feedback = rememberOddlyFeedback()

    var showResetDialog by remember { mutableStateOf(false) }
    var exportFailed by remember { mutableStateOf(false) }
    // Raw text plus its decoded form, kept until the user confirms or cancels.
    var pendingImport by remember { mutableStateOf<Pair<String, PersistedState>?>(null) }
    var importNotice by remember { mutableStateOf<ImportNotice?>(null) }
    val scope = rememberCoroutineScope()
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        // Null means the picker was dismissed: nothing to report.
        if (uri != null) {
            scope.launch {
                val raw = DataImport.readText(context, uri)
                val decoded = raw?.let { state.readBackup(it) }
                if (raw == null || decoded == null) {
                    importNotice = ImportNotice.FAILED
                } else {
                    pendingImport = raw to decoded
                }
            }
        }
    }
    var showAboutDialog by remember { mutableStateOf(false) }
    var linkFallback by remember { mutableStateOf<LinkFallback?>(null) }
    var addressCopied by remember { mutableStateOf(false) }
    val resetNotices = {
        addressCopied = false
        importNotice = null
        exportFailed = false
    }
    var showThemePicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary,
            modifier = Modifier.a11yHeading(),
        )

        Spacer(Modifier.height(20.dp))

        // Profile card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(palette.surfaceElevated)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OddlyColors.Purple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Astronaut(size = 44.dp, animated = false)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.profile.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = strings.levelWithXp(
                        state.profile.level,
                        state.profile.xpInLevel,
                        state.profile.xpForNextLevel,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                )
                Spacer(Modifier.height(8.dp))
                GradientProgressBar(progress = state.profile.levelProgress, height = 5.dp)
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionLabel(strings.sectionAppearance)
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Palette,
                title = strings.theme,
                value = settings.themeMode.title.of(strings.language),
                onClick = { showThemePicker = true },
            )
            SettingsRow(
                icon = OddlyIcon.Globe,
                title = strings.languageRow,
                value = settings.language.title,
                onClick = { showLanguagePicker = true },
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel(strings.sectionReminders)
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Bell,
                title = strings.dailyReminder,
                showChevron = false,
                toggled = settings.reminderEnabled,
                onToggle = { enabled ->
                    state.settings = settings.copy(reminderEnabled = enabled)
                    if (enabled) notifications.request()
                },
                trailing = {
                    Switch(
                        checked = settings.reminderEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                },
            )
            SettingsRow(
                icon = OddlyIcon.Clock,
                title = strings.reminderTime,
                value = DateFormat.time(settings.reminderTime),
                onClick = { showTimePicker = true },
            )
            SettingsRow(
                icon = OddlyIcon.Volume,
                title = strings.soundAndHaptics,
                showChevron = false,
                // One switch for in-app sound/haptics and the reminder's sound
                // (ReminderReceiver reads soundEnabled).
                toggled = settings.soundEnabled,
                onToggle = {
                    state.settings = settings.copy(soundEnabled = it, hapticsEnabled = it)
                    if (it) feedback.play(FeedbackRules.preview(state.settings))
                },
                trailing = {
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                },
            )
        }

        if (settings.reminderEnabled && !notifications.granted) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OddlyColors.Warning.copy(alpha = 0.12f))
                    .clickableNoRipple { notifications.openSystemSettings() }
                    .a11yLiveRegion()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OddlyIcon(OddlyIcon.Info, size = 18.dp, tint = palette.warning)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = strings.notificationsDisabledNotice,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.warning,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = strings.openSystemSettings,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.warning,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel(strings.sectionData)
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Download,
                title = strings.exportData,
                onClick = {
                    addressCopied = false
                    importNotice = null
                    exportFailed = false
                    val intent = DataExport.chooserIntent(context, state.exportJson())
                    if (intent == null) exportFailed = true else context.startActivity(intent)
                },
            )
            SettingsRow(
                icon = OddlyIcon.Upload,
                title = strings.importData,
                onClick = {
                    addressCopied = false
                    importNotice = null
                    exportFailed = false
                    importLauncher.launch(DataImport.MIME_TYPES)
                },
            )
        }

        if (exportFailed) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = strings.exportFailed,
                style = MaterialTheme.typography.bodySmall,
                color = palette.warning,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(OddlyColors.Warning.copy(alpha = 0.12f))
                    .a11yLiveRegion()
                    .padding(14.dp),
            )
        }

        importNotice?.let { notice ->
            val color = if (notice == ImportNotice.SUCCEEDED) palette.success else palette.warning
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (notice == ImportNotice.SUCCEEDED) strings.importSucceeded else strings.importFailed,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f))
                    .a11yLiveRegion()
                    .padding(14.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel(strings.sectionSupport)
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Star,
                title = strings.rateApp,
                modifier = Modifier.testTag(UiTestTags.SETTINGS_RATE),
                onClickLabel = strings.a11yOpensStore,
                onClick = {
                    resetNotices()
                    if (!LinkLauncher.openPlayStore(context)) {
                        linkFallback = LinkFallback(
                            strings.browserFallbackBody,
                            ExternalLinks.playStoreWeb(context.packageName),
                        )
                    }
                },
            )
            SettingsRow(
                icon = OddlyIcon.Mail,
                title = strings.contactSupport,
                subtitle = Brand.SUPPORT_EMAIL,
                modifier = Modifier.testTag(UiTestTags.SETTINGS_CONTACT),
                onClickLabel = strings.a11yOpensEmail,
                onClick = {
                    resetNotices()
                    val subject = strings.supportEmailSubject(BuildConfig.VERSION_NAME, "Android")
                    if (!LinkLauncher.openEmail(context, Brand.SUPPORT_EMAIL, subject)) {
                        linkFallback = LinkFallback(strings.emailFallbackBody, Brand.SUPPORT_EMAIL)
                    }
                },
            )
            SettingsRow(
                icon = OddlyIcon.Shield,
                title = strings.privacyPolicy,
                modifier = Modifier.testTag(UiTestTags.SETTINGS_PRIVACY),
                onClickLabel = strings.a11yOpensBrowser,
                onClick = {
                    resetNotices()
                    if (!LinkLauncher.openUrl(context, Brand.PRIVACY_POLICY_URL)) {
                        linkFallback = LinkFallback(strings.browserFallbackBody, Brand.PRIVACY_POLICY_URL)
                    }
                },
            )
            SettingsRow(
                icon = OddlyIcon.Info,
                title = strings.about,
                modifier = Modifier.testTag(UiTestTags.SETTINGS_ABOUT),
                onClick = { showAboutDialog = true },
            )
        }

        if (addressCopied) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = strings.addressCopied,
                style = MaterialTheme.typography.bodySmall,
                color = palette.success,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.success.copy(alpha = 0.12f))
                    .a11yLiveRegion()
                    .padding(14.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Trash,
                title = strings.eraseAllData,
                tint = palette.danger,
                showChevron = false,
                onClick = { showResetDialog = true },
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = strings.settingsFooter(BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = palette.textTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
    }

    if (showThemePicker) {
        OptionPickerDialog(
            title = strings.theme,
            options = ThemeMode.entries,
            selected = settings.themeMode,
            label = { it.title.of(strings.language) },
            onSelect = {
                state.settings = settings.copy(themeMode = it)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false },
        )
    }

    if (showLanguagePicker) {
        OptionPickerDialog(
            title = strings.languageRow,
            options = AppLanguage.entries,
            selected = settings.language,
            label = { it.title },
            onSelect = {
                state.settings = settings.copy(language = it)
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false },
        )
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initial = settings.reminderTime,
            strings = strings,
            onConfirm = {
                state.settings = settings.copy(reminderTime = it, reminderEnabled = true)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    pendingImport?.let { (raw, decoded) ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            containerColor = palette.surfaceElevated,
            title = {
                Text(
                    strings.importConfirmTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
            },
            text = {
                Text(
                    strings.importConfirmBody(decoded.completions.size, decoded.profile.level),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    importNotice =
                        if (state.importBackup(raw)) ImportNotice.SUCCEEDED else ImportNotice.FAILED
                    pendingImport = null
                }) {
                    Text(strings.importReplace, color = palette.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) {
                    Text(strings.cancel, color = palette.textSecondary)
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = palette.surfaceElevated,
            title = {
                Text(
                    strings.eraseAllDataConfirm,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
            },
            text = {
                Text(
                    strings.eraseAllDataBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    state.resetAllData()
                    showResetDialog = false
                }) {
                    Text(strings.erase, color = palette.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel, color = palette.textSecondary)
                }
            },
        )
    }

    linkFallback?.let { fallback ->
        AlertDialog(
            onDismissRequest = { linkFallback = null },
            containerColor = palette.surfaceElevated,
            title = {
                Text(
                    strings.linkFallbackTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
            },
            text = {
                Column {
                    Text(
                        fallback.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    SelectionContainer {
                        Text(
                            fallback.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textPrimary,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    LinkLauncher.copy(context, fallback.address)
                    addressCopied = true
                    linkFallback = null
                }) {
                    Text(strings.copy, color = OddlyColors.Purple)
                }
            },
            dismissButton = {
                TextButton(onClick = { linkFallback = null }) {
                    Text(strings.close, color = palette.textSecondary)
                }
            },
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = palette.surfaceElevated,
            title = {
                Text(
                    "1% HUMAN",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
            },
            text = {
                Text(
                    strings.aboutBody(BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(strings.close, color = OddlyColors.Purple)
                }
            },
        )
    }
}

/**
 * A single-choice picker over a small, fixed option set.
 *
 * Generic over the option type so theme and language share one implementation —
 * they differ only in how an option is labelled.
 */
@Composable
private fun <T> OptionPickerDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val strings = LocalStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surfaceElevated,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    OddlyColors.Purple.copy(alpha = 0.16f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .selectableNoRipple(isSelected, Role.RadioButton) { onSelect(option) }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) OddlyColors.Purple else palette.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .a11yHidden()
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(OddlyColors.Purple),
                                contentAlignment = Alignment.Center,
                            ) {
                                dev.lildua.oddly.ui.components.OddlyIcon(
                                    OddlyIcon.Check,
                                    size = 14.dp,
                                    tint = Color(0xFF0B0B12),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = palette.textSecondary)
            }
        },
    )
}

/** Free-form reminder time, rather than a handful of preset hours. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initial: LocalTime,
    strings: Strings,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surfaceElevated,
        title = {
            Text(
                text = strings.reminderTime,
                style = MaterialTheme.typography.titleLarge,
                color = palette.textPrimary,
            )
        },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = pickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = palette.surfaceHighest,
                        selectorColor = OddlyColors.Purple,
                        clockDialSelectedContentColor = Color(0xFF0B0B12),
                        clockDialUnselectedContentColor = palette.textPrimary,
                        timeSelectorSelectedContainerColor = OddlyColors.Purple.copy(alpha = 0.22f),
                        timeSelectorSelectedContentColor = OddlyColors.Purple,
                        timeSelectorUnselectedContainerColor = palette.surfaceHighest,
                        timeSelectorUnselectedContentColor = palette.textPrimary,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(pickerState.hour, pickerState.minute)) }) {
                Text(strings.save, color = OddlyColors.Purple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = palette.textSecondary)
            }
        },
    )
}

/** Groups settings rows into one rounded surface with dividers between them. */
@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(OddlyTheme.palette.surfaceElevated),
    ) {
        content()
    }
}
