package dev.lildua.oddly.ui.screens.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.components.GlowOrb
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.TextAction
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * S04 — reminder opt-in. Declining never blocks onboarding, per the spec; the
 * screen explains the value rather than demanding the permission.
 */
@Composable
fun NotificationPermissionScreen(
    onAllow: (String) -> Unit,
    onSkip: () -> Unit,
) {
    val palette = OddlyTheme.palette
    val times = listOf("08:00", "09:00", "12:00", "18:00")
    var selectedTime by remember { mutableStateOf("09:00") }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 50, seed = 17)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            Box(contentAlignment = Alignment.Center) {
                GlowOrb(OddlyColors.Purple, Modifier.size(180.dp), alpha = 0.35f)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(palette.surfaceElevated)
                        .border(1.dp, OddlyColors.Purple.copy(alpha = 0.4f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    OddlyIcon(OddlyIcon.Bell, size = 42.dp, tint = OddlyColors.Purple, strokeWidth = 2.dp)
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = "Nhắc bạn mỗi ngày nhé?",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Một lời nhắc nhẹ nhàng vào giờ bạn chọn, để thử thách hôm nay không bị bỏ lỡ. Bạn có thể tắt bất cứ lúc nào.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Chọn giờ nhắc",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textTertiary,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                times.forEach { time ->
                    val active = time == selectedTime
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (active) OddlyColors.Purple.copy(alpha = 0.18f)
                                else palette.surfaceElevated,
                            )
                            .border(
                                1.dp,
                                if (active) OddlyColors.Purple else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .clickableNoRipple { selectedTime = time }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) OddlyColors.Purple else palette.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(text = "Bật nhắc nhở", onClick = { onAllow(selectedTime) })

            Spacer(Modifier.height(8.dp))

            TextAction("Để sau", onSkip)
        }
    }
}
