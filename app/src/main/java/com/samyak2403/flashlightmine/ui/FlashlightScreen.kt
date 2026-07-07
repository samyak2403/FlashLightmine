package com.samyak2403.flashlightmine.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samyak2403.flashlightmine.R
import com.samyak2403.flashlightmine.ui.theme.BackgroundDark
import com.samyak2403.flashlightmine.ui.theme.BackgroundOn
import com.samyak2403.flashlightmine.ui.theme.ChipTextDark
import com.samyak2403.flashlightmine.ui.theme.ChipWhite
import com.samyak2403.flashlightmine.ui.theme.OrangeGlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main flashlight screen: a vertical rope with pull-to-toggle animation, glowing
 * concentric rings around a bulb, and an On/Off chip button at the bottom.
 */
@Composable
fun FlashlightScreen(
    isFlashlightOn: Boolean,
    onToggle: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isFlashlightOn) BackgroundOn else BackgroundDark,
        label = "backgroundColor",
    )
    // Matches the original ellipse tint colors: orange when on, background when off
    // so the dot pattern effectively disappears.
    val ringTint by animateColorAsState(
        targetValue = if (isFlashlightOn) Color(0xFFFFA500) else BackgroundDark,
        label = "ringTint",
    )

    val density = LocalDensity.current
    val lineHeightDp = 336.dp
    val lineHeightPx = with(density) { lineHeightDp.toPx() }
    val pullThresholdPx = with(density) { 100.dp.toPx() }

    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val bulbPulse = remember { Animatable(1f) }

    // Derived transforms based on drag distance
    val stretchFactor = 1f + (dragOffset.value / lineHeightPx) * 0.5f
    val bottomOffsetPx = dragOffset.value * 0.5f

    val triggerPull: () -> Unit = {
        onToggle()
        scope.launch {
            bulbPulse.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
            bulbPulse.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        // Scene artwork: night when OFF, morning when ON. Both are wide landscape
        // illustrations, so fit to the screen width and pin to the bottom to preserve
        // aspect ratio.
        Image(
            painter = painterResource(
                id = if (isFlashlightOn) R.drawable.ic_morning else R.drawable.ic_night,
            ),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Rope / vertical line — original 5dp width with the textured "lines" drawable.
            // Pinned at top, stretches down when pulled.
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(lineHeightDp)
                    .graphicsLayer {
                        scaleY = stretchFactor
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .pullRopeGesture(
                        dragOffset = dragOffset,
                        pullThresholdPx = pullThresholdPx,
                        scope = scope,
                        onPullComplete = triggerPull,
                    ),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lines),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Bulb + concentric glow rings. The Box is 300dp tall (largest ring) with
            // the bulb centered inside. Shift the whole group UP by half the ring height
            // minus half the bulb height so the bulb's TOP visually attaches to the
            // bottom of the rope. Group also translates down with the drag.
            val bulbGroupOffsetDp = -((300 - 93) / 2).dp
            Box(
                modifier = Modifier
                    .offset(y = bulbGroupOffsetDp)
                    .graphicsLayer { translationY = bottomOffsetPx },
                contentAlignment = Alignment.Center,
            ) {
                // Concentric dot patterns — sizes and alphas mirror the original XML ellipses.
                DotRing(size = 300.dp, tint = ringTint, alpha = 0.10f)
                DotRing(size = 250.dp, tint = ringTint, alpha = 0.05f)
                DotRing(size = 200.dp, tint = ringTint, alpha = 0.10f)
                DotRing(size = 150.dp, tint = ringTint, alpha = 1.0f)

                Image(
                    painter = painterResource(
                        id = if (isFlashlightOn) R.drawable.bulb_on else R.drawable.bulb_off,
                    ),
                    contentDescription = stringResource(R.string.flashlight_bulb),
                    modifier = Modifier
                        .size(width = 76.dp, height = 93.dp)
                        .graphicsLayer {
                            scaleX = bulbPulse.value
                            scaleY = bulbPulse.value
                        }
                        .pullRopeGesture(
                            dragOffset = dragOffset,
                            pullThresholdPx = pullThresholdPx,
                            scope = scope,
                            onPullComplete = triggerPull,
                        ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ShakeToggleChip(
                isOn = isFlashlightOn,
                onClick = onToggle,
                modifier = Modifier.padding(bottom = 32.dp),
            )
        }

        // Telegram group shortcut — tapped to open the invite link in the Telegram
        // app (or the browser as a fallback).
        val context = LocalContext.current
        Image(
            painter = painterResource(id = R.drawable.ic_telegram),
            contentDescription = "Join Telegram group",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 16.dp)
                .size(36.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_GROUP_URL))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                },
        )
    }
}

private const val TELEGRAM_GROUP_URL = "https://t.me/+VlBPxKRBMiA5YjU1"

@Composable
private fun DotRing(
    size: Dp,
    tint: Color,
    alpha: Float,
) {
    Image(
        painter = painterResource(id = R.drawable.one),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier
            .size(size)
            .graphicsLayer { this.alpha = alpha },
    )
}

@Composable
private fun ShakeToggleChip(
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .width(136.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, OrangeGlow, RoundedCornerShape(18.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Shake",
                color = ChipWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .width(54.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ChipWhite)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onClick() })
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isOn) "On" else "Off",
                color = ChipTextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Applies the pull-rope drag gesture. Only downward drag is captured. On release,
 * the offset springs back to zero with a bouncy spring. If the total drag exceeded
 * [pullThresholdPx], [onPullComplete] is invoked once.
 */
private fun Modifier.pullRopeGesture(
    dragOffset: Animatable<Float, *>,
    pullThresholdPx: Float,
    scope: CoroutineScope,
    onPullComplete: () -> Unit,
): Modifier = pointerInput(Unit) {
    var totalDrag = 0f
    detectVerticalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            val triggered = totalDrag > pullThresholdPx
            scope.launch {
                dragOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
            if (triggered) onPullComplete()
        },
        onDragCancel = {
            scope.launch {
                dragOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
        },
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            val next = (dragOffset.value + dragAmount).coerceAtLeast(0f)
            totalDrag = next
            scope.launch { dragOffset.snapTo(next) }
        },
    )
}
