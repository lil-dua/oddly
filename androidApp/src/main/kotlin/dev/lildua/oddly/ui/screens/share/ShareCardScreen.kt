package dev.lildua.oddly.ui.screens.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.domain.model.Challenge
import dev.lildua.oddly.ui.components.GradientButton
import dev.lildua.oddly.ui.components.GradientText
import dev.lildua.oddly.ui.components.OddlyIcon
import dev.lildua.oddly.ui.components.Planet
import dev.lildua.oddly.ui.components.StarField
import dev.lildua.oddly.ui.components.clickableNoRipple
import dev.lildua.oddly.ui.state.OddlyAppState
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyTheme
import kotlinx.coroutines.launch

private enum class ShareLayout(val label: String, val ratio: Float) {
    STORY("Dọc 9:16", 9f / 16f),
    SQUARE("Vuông 1:1", 1f),
}

/**
 * S18 — the shareable achievement card. Renders entirely locally and shows only
 * aggregate achievements, never notes or private data (spec §16).
 */
@Composable
fun ShareCardScreen(
    state: OddlyAppState,
    challenge: Challenge?,
    onBack: () -> Unit,
) {
    val palette = OddlyTheme.palette
    var layout by remember { mutableStateOf(ShareLayout.STORY) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // The card is recorded into this layer as it draws, so exporting is a
    // replay of what is already on screen rather than a second composition.
    val cardLayer = rememberGraphicsLayer()
    var exportFailed by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.clickableNoRipple(onBack)) {
                OddlyIcon(OddlyIcon.Close, size = 22.dp, tint = palette.textSecondary)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Chia sẻ thành quả",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.padding(11.dp))
        }

        Column(Modifier.padding(horizontal = 24.dp)) {
            // Layout switcher
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShareLayout.entries.forEach { entry ->
                    val active = entry == layout
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (active) OddlyColors.Purple.copy(alpha = 0.2f)
                                else palette.surfaceElevated,
                            )
                            .clickableNoRipple { layout = entry }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) OddlyColors.Purple else palette.textTertiary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                Modifier.drawWithContent {
                    cardLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(cardLayer)
                },
            ) {
                SharePreview(
                    state = state,
                    challenge = challenge,
                    ratio = layout.ratio,
                )
            }

            Spacer(Modifier.height(24.dp))

            // One action: the system chooser is also where "save to photos"
            // lives on Android, so a separate save button would duplicate it.
            GradientButton(
                text = "Chia sẻ ngay",
                onClick = {
                    scope.launch {
                        val bitmap = cardLayer.toImageBitmap().asAndroidBitmap()
                        val intent = ShareImageExport.chooserIntent(
                            context = context,
                            bitmap = bitmap,
                            caption = "1% HUMAN · ${state.totalCompleted} thử thách",
                        )
                        if (intent == null) {
                            exportFailed = true
                        } else {
                            exportFailed = false
                            context.startActivity(intent)
                        }
                    }
                },
                leadingIcon = OddlyIcon.Share,
            )

            Spacer(Modifier.height(16.dp))

            if (exportFailed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(OddlyColors.Warning.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OddlyIcon(OddlyIcon.Info, size = 18.dp, tint = OddlyColors.Warning)
                    Spacer(Modifier.height(0.dp))
                    Text(
                        text = "  Không tạo được ảnh chia sẻ. Hãy kiểm tra dung lượng trống rồi thử lại.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OddlyColors.Warning,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = "Ảnh chỉ hiển thị thành tích của bạn, không kèm ghi chú hay dữ liệu cá nhân.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/** The card itself — this composition is what the image generator will render. */
@Composable
private fun SharePreview(
    state: OddlyAppState,
    challenge: Challenge?,
    ratio: Float,
) {
    val palette = OddlyTheme.palette

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(24.dp))
            .background(
                // Resolved to an opaque colour rather than left translucent:
                // the exported PNG has nothing behind it, so a 25%-alpha stop
                // would come out washed out wherever the card is shared.
                Brush.verticalGradient(
                    listOf(
                        OddlyColors.Background,
                        OddlyColors.Purple.copy(alpha = 0.25f).compositeOver(OddlyColors.Background),
                        OddlyColors.Background,
                    ),
                ),
            )
            .border(1.dp, OddlyColors.Purple.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
    ) {
        StarField(Modifier.fillMaxSize(), starCount = 70, seed = 53)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GradientText(text = "1% HUMAN", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.weight(1f))

            Text(
                text = "Tôi đã hoàn thành",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            GradientText(
                text = "${state.totalCompleted} thử thách",
                style = MaterialTheme.typography.displaySmall,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "và duy trì chuỗi ngày\n${state.streak.current} ngày liên tiếp!",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )

            challenge?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "“${it.title}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Planet(size = 96.dp)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "#1PercentHuman",
                style = MaterialTheme.typography.labelMedium,
                color = OddlyColors.Purple,
            )
        }
    }
}
