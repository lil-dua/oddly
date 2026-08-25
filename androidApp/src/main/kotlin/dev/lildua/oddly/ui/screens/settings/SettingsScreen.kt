package dev.lildua.oddly.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.core.time.DateFormat
import dev.lildua.oddly.domain.model.AppLanguage
import dev.lildua.oddly.domain.model.ThemeMode
import dev.lildua.oddly.ui.components.Astronaut
import dev.lildua.oddly.ui.components.GradientProgressBar
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.SectionLabel
import dev.lildua.oddly.ui.components.SettingsRow
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S15 — configuration and local-data controls. Reset is confirmation-gated per
 * spec §16.
 */
@Composable
fun SettingsScreen(
    state: OddlyAppState,
    onThemeChange: (ThemeMode) -> Unit,
) {
    val palette = OddlyTheme.palette
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val settings = state.settings

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
            text = "Cài đặt",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.textPrimary,
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
                    text = "Level ${state.profile.level} · ${state.profile.xpInLevel}/${state.profile.xpForNextLevel} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textTertiary,
                )
                Spacer(Modifier.height(8.dp))
                GradientProgressBar(progress = state.profile.levelProgress, height = 5.dp)
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionLabel("Giao diện")
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Palette,
                title = "Chủ đề",
                value = settings.themeMode.title,
                onClick = {
                    // Cycle through the three modes.
                    val next = when (settings.themeMode) {
                        ThemeMode.DARK -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.SYSTEM
                        ThemeMode.SYSTEM -> ThemeMode.DARK
                    }
                    state.settings = settings.copy(themeMode = next)
                    onThemeChange(next)
                },
            )
            SettingsRow(
                icon = OddlyIcon.Globe,
                title = "Ngôn ngữ",
                value = settings.language.title,
                onClick = {
                    val next = if (settings.language == AppLanguage.VIETNAMESE) {
                        AppLanguage.ENGLISH
                    } else {
                        AppLanguage.VIETNAMESE
                    }
                    state.settings = settings.copy(language = next)
                },
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("Nhắc nhở")
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Bell,
                title = "Lời nhắc hằng ngày",
                showChevron = false,
                trailing = {
                    Switch(
                        checked = settings.reminderEnabled,
                        onCheckedChange = {
                            state.settings = settings.copy(reminderEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                },
            )
            SettingsRow(
                icon = OddlyIcon.Clock,
                title = "Giờ nhắc",
                value = DateFormat.time(settings.reminderTime),
                onClick = { },
            )
            SettingsRow(
                icon = OddlyIcon.Volume,
                title = "Âm thanh & rung",
                showChevron = false,
                trailing = {
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            state.settings = settings.copy(soundEnabled = it, hapticsEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = OddlyColors.Success,
                        ),
                    )
                },
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionLabel("Dữ liệu")
        Spacer(Modifier.height(8.dp))
        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Download,
                title = "Sao lưu dữ liệu",
                onClick = { },
            )
            SettingsRow(
                icon = OddlyIcon.Share,
                title = "Xuất dữ liệu (JSON)",
                onClick = { },
            )
            SettingsRow(
                icon = OddlyIcon.Info,
                title = "Giới thiệu",
                onClick = { showAboutDialog = true },
            )
        }

        Spacer(Modifier.height(20.dp))

        SettingsGroup {
            SettingsRow(
                icon = OddlyIcon.Trash,
                title = "Xóa tất cả dữ liệu",
                tint = OddlyColors.Danger,
                showChevron = false,
                onClick = { showResetDialog = true },
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "1% HUMAN · phiên bản 1.0\nDữ liệu của bạn được lưu an toàn trên thiết bị.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = palette.surfaceElevated,
            title = {
                Text(
                    "Xóa tất cả dữ liệu?",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary,
                )
            },
            text = {
                Text(
                    "Toàn bộ lịch sử, streak và cấp độ sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    state.resetAllData()
                    showResetDialog = false
                }) {
                    Text("Xóa", color = OddlyColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Hủy", color = palette.textSecondary)
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
                    "Phiên bản 1.0\n\n" +
                        "Mỗi ngày một điều nhỏ. Một phiên bản tốt hơn.\n\n" +
                        "Ứng dụng hoạt động hoàn toàn offline. Không tài khoản, " +
                        "không thu thập vị trí, không gửi dữ liệu lên máy chủ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Đóng", color = OddlyColors.Purple)
                }
            },
        )
    }
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
