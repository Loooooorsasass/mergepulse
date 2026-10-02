package com.example.game.render

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.engine.GameWorld
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer {

    // Chamber & Background
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chamberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dangerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dangerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val aimLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Core Drawing
    private val coreFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Effects
    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val floatTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val floatTextStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textBounds = Rect()
    private val tempRectF = RectF()
    private val tempPath = Path()

    init {
        bgPaint.color = android.graphics.Color.WHITE
        bgPaint.style = Paint.Style.FILL

        chamberPaint.color = android.graphics.Color.WHITE
        chamberPaint.style = Paint.Style.FILL

        // Prominent, clear danger line
        dangerLinePaint.color = android.graphics.Color.parseColor("#B91C1C")
        dangerLinePaint.style = Paint.Style.STROKE
        dangerLinePaint.strokeWidth = 4.5f

        dangerTextPaint.color = android.graphics.Color.parseColor("#B91C1C")
        dangerTextPaint.textSize = 28f
        dangerTextPaint.textAlign = Paint.Align.RIGHT
        dangerTextPaint.isFakeBoldText = true

        aimLinePaint.color = android.graphics.Color.parseColor("#90A4AE")
        aimLinePaint.style = Paint.Style.STROKE
        aimLinePaint.strokeWidth = 2.5f
        aimLinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)

        coreFillPaint.style = Paint.Style.FILL

        coreBorderPaint.style = Paint.Style.STROKE
        coreBorderPaint.strokeWidth = 3f

        coreDetailPaint.style = Paint.Style.STROKE
        coreDetailPaint.strokeWidth = 2f

        symbolPaint.textAlign = Paint.Align.CENTER
        symbolPaint.isFakeBoldText = true

        floatTextPaint.textAlign = Paint.Align.CENTER
        floatTextPaint.isFakeBoldText = true

        floatTextStrokePaint.textAlign = Paint.Align.CENTER
        floatTextStrokePaint.style = Paint.Style.STROKE
        floatTextStrokePaint.strokeWidth = 4f
        floatTextStrokePaint.color = android.graphics.Color.WHITE
        floatTextStrokePaint.isFakeBoldText = true
    }

    fun render(
        canvas: NativeCanvas,
        world: GameWorld,
        screenWidth: Float,
        screenHeight: Float,
        dangerLabel: String = "DANGER"
    ) {
        val now = System.currentTimeMillis() / 1000f

        // 1. Clean, minimalist white background
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, bgPaint)

        canvas.save()
        // Very subtle camera shake only on heavy impact
        if (world.screenShakeIntensity > 0f) {
            val shakeX = (kotlin.random.Random.nextFloat() - 0.5f) * world.screenShakeIntensity * 0.25f
            val shakeY = (kotlin.random.Random.nextFloat() - 0.5f) * world.screenShakeIntensity * 0.25f
            canvas.translate(shakeX, shakeY)
        }

        // 2. Bold, high-contrast Danger Line (Section 5)
        val overflowTime = world.overflowWatcher.currentOverflowTime
        when {
            overflowTime > 1.5f -> {
                // Critical danger: stronger rapid pulse and bright warning line
                val pulseDash = (sin(now * 16f) * 6f).coerceAtLeast(0f)
                dangerLinePaint.pathEffect = DashPathEffect(floatArrayOf(12f + pulseDash, 8f), now * 35f)
                dangerLinePaint.strokeWidth = 6.5f
                dangerLinePaint.color = android.graphics.Color.parseColor("#DC2626")
                dangerTextPaint.color = android.graphics.Color.parseColor("#DC2626")
            }
            world.isDangerActive || overflowTime > 0f -> {
                // Warning danger: subtle pulse
                val pulseDash = (sin(now * 8f) * 3f).coerceAtLeast(0f)
                dangerLinePaint.pathEffect = DashPathEffect(floatArrayOf(14f + pulseDash, 10f), now * 20f)
                dangerLinePaint.strokeWidth = 5.2f
                dangerLinePaint.color = android.graphics.Color.parseColor("#B91C1C")
                dangerTextPaint.color = android.graphics.Color.parseColor("#B91C1C")
            }
            else -> {
                // Normal: clean, solid, static line
                dangerLinePaint.pathEffect = null
                dangerLinePaint.strokeWidth = 4.5f
                dangerLinePaint.color = android.graphics.Color.parseColor("#B91C1C")
                dangerTextPaint.color = android.graphics.Color.parseColor("#B91C1C")
            }
        }

        canvas.drawLine(world.chamberLeft, world.dangerLineY, world.chamberRight, world.dangerLineY, dangerLinePaint)
        canvas.drawText(dangerLabel, world.chamberRight - 6f, world.dangerLineY - 8f, dangerTextPaint)

        // 3. Aim Trajectory (Subtle dashed line)
        world.currentCore?.let { core ->
            if (!core.isSpawned) {
                canvas.drawLine(core.position.x, core.position.y, core.position.x, world.chamberFloorY - core.radius, aimLinePaint)
                drawCore(canvas, core, now)
            }
        }

        // 4. Active Cores in Chamber
        for (core in world.activeCores) {
            if (core.active) {
                drawCore(canvas, core, now)
            }
        }

        // 5. Clean Shockwaves (Soft expanding rings)
        for (wave in world.shockwaves) {
            shockwavePaint.color = wave.color.toArgb()
            shockwavePaint.style = Paint.Style.STROKE
            shockwavePaint.strokeWidth = 3.5f * wave.alpha
            shockwavePaint.alpha = (wave.alpha * 150).toInt().coerceIn(0, 255)
            canvas.drawCircle(wave.x, wave.y, wave.currentRadius, shockwavePaint)
        }

        // 6. Subtle Particles
        for (p in world.particles) {
            particlePaint.color = p.color.toArgb()
            particlePaint.alpha = (p.alpha * 180).toInt().coerceIn(0, 255)
            particlePaint.style = Paint.Style.FILL
            canvas.drawCircle(p.x, p.y, p.radius * 0.7f, particlePaint)
        }

        // 7. Floating Score Text (Clean with white outline)
        for (ft in world.floatingTexts) {
            val textSize = if (ft.isCombo) 44f * ft.scale else 36f
            floatTextPaint.textSize = textSize
            floatTextStrokePaint.textSize = textSize

            val textAlpha = (ft.alpha * 255).toInt().coerceIn(0, 255)
            floatTextPaint.color = ft.color.toArgb()
            floatTextPaint.alpha = textAlpha
            floatTextStrokePaint.alpha = textAlpha

            canvas.drawText(ft.text, ft.x, ft.y, floatTextStrokePaint)
            canvas.drawText(ft.text, ft.x, ft.y, floatTextPaint)
        }

        // Note: Section 11 - Removed "containment chamber" label completely!

        canvas.restore()
    }

    private fun drawCore(canvas: NativeCanvas, core: Core, now: Float) {
        val cx = core.position.x
        val cy = core.position.y
        val r = core.radius
        val phase = (core.id % 1000) / 100f

        // Section 5: Limited reaction rule!
        // If core has reacted 2 times, it stays completely still (no continuous oscillation)
        val idleScale = when {
            !core.isSpawned -> 1f + sin(now * 2.2f + phase) * 0.02f // Ready preview breathes slightly
            core.reactionCount >= 2 -> 1f                           // Grounded/settled core stays still
            else -> 1f + sin(now * 2.0f + phase) * 0.008f           // Very tiny subtle life before settling
        }

        // Merge Pop Ease
        val mergeProgress = (1f - core.mergeAnimTime / 0.25f).coerceIn(0f, 1f)
        val mergeEase = if (core.mergeAnimTime > 0f) {
            1f + 0.22f * sin(mergeProgress * Math.PI).toFloat()
        } else {
            1f
        }

        // Subtle falling stretch based on vertical speed
        val speed = abs(core.velocity.y)
        val stretch = if (core.isSpawned) (speed / 2000f).coerceIn(0f, 0.045f) else 0f

        val scaleX = idleScale * mergeEase * (1f - stretch * 0.35f)
        val scaleY = idleScale * mergeEase * (1f + stretch)

        canvas.save()
        canvas.scale(scaleX, scaleY, cx, cy)

        val levelInfo = CoreLevelRegistry.getInfo(core.level)
        val fillColor = if (core.charge == Charge.POSITIVE) levelInfo.positiveColor else levelInfo.negativeColor
        val borderColor = darkOutlineColor(fillColor.toArgb())
        val detailColor = semiDarkOutlineColor(fillColor.toArgb())
        val symbolColor = symbolTextColor(fillColor.toArgb())

        // Base Core Fill
        coreFillPaint.color = fillColor.toArgb()
        canvas.drawCircle(cx, cy, r, coreFillPaint)

        // Section 9: Unique Procedural Geometric Identity per Tier
        coreDetailPaint.color = detailColor
        drawTierProceduralGeometry(canvas, cx, cy, r, core.level)

        // Outer Perimeter Border
        coreBorderPaint.color = borderColor
        canvas.drawCircle(cx, cy, r, coreBorderPaint)

        // Section 10: VERY LARGE + / - CHARGE SYMBOL (scalable to radius)
        val symbol = core.charge.symbol
        symbolPaint.color = symbolColor
        val targetTextSize = r * 1.15f
        symbolPaint.textSize = targetTextSize
        symbolPaint.getTextBounds(symbol, 0, symbol.length, textBounds)
        val textY = cy + textBounds.height() / 2f - 2f
        canvas.drawText(symbol, cx, textY, symbolPaint)

        canvas.restore()
    }

    private fun drawTierProceduralGeometry(canvas: NativeCanvas, cx: Float, cy: Float, r: Float, level: Int) {
        when (level) {
            1 -> {
                // Tier 1: Clean sphere, tiny center accent dot
                canvas.drawCircle(cx, cy, r * 0.16f, coreDetailPaint)
            }
            2 -> {
                // Tier 2: Single delicate inner concentric ring
                canvas.drawCircle(cx, cy, r * 0.72f, coreDetailPaint)
            }
            3 -> {
                // Tier 3: Double concentric inner rings
                canvas.drawCircle(cx, cy, r * 0.80f, coreDetailPaint)
                canvas.drawCircle(cx, cy, r * 0.60f, coreDetailPaint)
            }
            4 -> {
                // Tier 4: Segmented 4-notch orbital ring
                tempRectF.set(cx - r * 0.75f, cy - r * 0.75f, cx + r * 0.75f, cy + r * 0.75f)
                canvas.drawArc(tempRectF, 15f, 60f, false, coreDetailPaint)
                canvas.drawArc(tempRectF, 105f, 60f, false, coreDetailPaint)
                canvas.drawArc(tempRectF, 195f, 60f, false, coreDetailPaint)
                canvas.drawArc(tempRectF, 285f, 60f, false, coreDetailPaint)
            }
            5 -> {
                // Tier 5: Inner diamond geometry
                tempPath.reset()
                val d = r * 0.72f
                tempPath.moveTo(cx, cy - d)
                tempPath.lineTo(cx + d, cy)
                tempPath.lineTo(cx, cy + d)
                tempPath.lineTo(cx - d, cy)
                tempPath.close()
                canvas.drawPath(tempPath, coreDetailPaint)
            }
            6 -> {
                // Tier 6: 6-segment radar ring
                tempRectF.set(cx - r * 0.78f, cy - r * 0.78f, cx + r * 0.78f, cy + r * 0.78f)
                for (i in 0 until 6) {
                    canvas.drawArc(tempRectF, i * 60f + 10f, 40f, false, coreDetailPaint)
                }
            }
            7 -> {
                // Tier 7: Concentric ring + rounded quad contour
                canvas.drawCircle(cx, cy, r * 0.82f, coreDetailPaint)
                val q = r * 0.52f
                tempRectF.set(cx - q, cy - q, cx + q, cy + q)
                canvas.drawRoundRect(tempRectF, 12f, 12f, coreDetailPaint)
            }
            8 -> {
                // Tier 8: 8 tick marks around perimeter
                for (i in 0 until 8) {
                    val angle = (i * Math.PI / 4.0).toFloat()
                    val x1 = cx + cos(angle) * (r * 0.84f)
                    val y1 = cy + sin(angle) * (r * 0.84f)
                    val x2 = cx + cos(angle) * (r * 0.96f)
                    val y2 = cy + sin(angle) * (r * 0.96f)
                    canvas.drawLine(x1, y1, x2, y2, coreDetailPaint)
                }
            }
            9 -> {
                // Tier 9: 4-pointed soft star contour inside
                tempPath.reset()
                val outer = r * 0.82f
                val inner = r * 0.40f
                for (i in 0 until 8) {
                    val rad = if (i % 2 == 0) outer else inner
                    val angle = (i * Math.PI / 4.0 - Math.PI / 2.0).toFloat()
                    val px = cx + cos(angle) * rad
                    val py = cy + sin(angle) * rad
                    if (i == 0) tempPath.moveTo(px, py) else tempPath.lineTo(px, py)
                }
                tempPath.close()
                canvas.drawPath(tempPath, coreDetailPaint)
            }
            10 -> {
                // Tier 10 (Singularity): Multi-layered nested cosmic rings
                canvas.drawCircle(cx, cy, r * 0.86f, coreDetailPaint)
                canvas.drawCircle(cx, cy, r * 0.68f, coreDetailPaint)
                canvas.drawCircle(cx, cy, r * 0.50f, coreDetailPaint)
            }
        }
    }

    private fun darkOutlineColor(argb: Int): Int {
        val a = android.graphics.Color.alpha(argb)
        val r = (android.graphics.Color.red(argb) * 0.68f).toInt()
        val g = (android.graphics.Color.green(argb) * 0.68f).toInt()
        val b = (android.graphics.Color.blue(argb) * 0.68f).toInt()
        return android.graphics.Color.argb(a, r, g, b)
    }

    private fun semiDarkOutlineColor(argb: Int): Int {
        val a = (android.graphics.Color.alpha(argb) * 0.45f).toInt()
        val r = (android.graphics.Color.red(argb) * 0.65f).toInt()
        val g = (android.graphics.Color.green(argb) * 0.65f).toInt()
        val b = (android.graphics.Color.blue(argb) * 0.65f).toInt()
        return android.graphics.Color.argb(a, r, g, b)
    }

    private fun symbolTextColor(argb: Int): Int {
        val a = android.graphics.Color.alpha(argb)
        val r = (android.graphics.Color.red(argb) * 0.35f).toInt()
        val g = (android.graphics.Color.green(argb) * 0.35f).toInt()
        val b = (android.graphics.Color.blue(argb) * 0.35f).toInt()
        return android.graphics.Color.argb(a, r, g, b)
    }
}
