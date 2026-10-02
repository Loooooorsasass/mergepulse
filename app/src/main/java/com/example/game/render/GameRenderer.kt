package com.example.game.render

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.engine.GameWorld
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameRenderer {

    // Paints
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chamberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wallBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dangerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dangerZonePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val aimLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val coreBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coreRingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val symbolTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val symbolStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val levelBadgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val levelBadgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val floatTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val floatTextStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val tempRectF = RectF()
    private val textBounds = Rect()

    init {
        bgPaint.style = Paint.Style.FILL
        chamberPaint.style = Paint.Style.FILL

        wallBorderPaint.color = android.graphics.Color.parseColor("#00E5FF")
        wallBorderPaint.style = Paint.Style.STROKE
        wallBorderPaint.strokeWidth = 4f

        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = 1.5f

        dangerLinePaint.color = android.graphics.Color.parseColor("#FF1744")
        dangerLinePaint.style = Paint.Style.STROKE
        dangerLinePaint.strokeWidth = 3.5f
        dangerLinePaint.pathEffect = DashPathEffect(floatArrayOf(16f, 12f), 0f)

        dangerZonePaint.style = Paint.Style.FILL

        aimLinePaint.color = android.graphics.Color.parseColor("#8000E5FF")
        aimLinePaint.style = Paint.Style.STROKE
        aimLinePaint.strokeWidth = 2.5f
        aimLinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)

        symbolTextPaint.textAlign = Paint.Align.CENTER
        symbolTextPaint.isFakeBoldText = true

        symbolStrokePaint.textAlign = Paint.Align.CENTER
        symbolStrokePaint.style = Paint.Style.STROKE
        symbolStrokePaint.strokeWidth = 4f
        symbolStrokePaint.color = android.graphics.Color.parseColor("#0A0D14")

        levelBadgeBgPaint.color = android.graphics.Color.parseColor("#D90A0D14")
        levelBadgeBgPaint.style = Paint.Style.FILL

        levelBadgeTextPaint.color = android.graphics.Color.WHITE
        levelBadgeTextPaint.textAlign = Paint.Align.CENTER
        levelBadgeTextPaint.isFakeBoldText = true

        floatTextPaint.textAlign = Paint.Align.CENTER
        floatTextPaint.isFakeBoldText = true

        floatTextStrokePaint.textAlign = Paint.Align.CENTER
        floatTextStrokePaint.style = Paint.Style.STROKE
        floatTextStrokePaint.strokeWidth = 4.5f
        floatTextStrokePaint.color = android.graphics.Color.BLACK
        floatTextStrokePaint.isFakeBoldText = true
    }

    private fun applyThemeColors(themeId: String) {
        when (themeId) {
            "cyber_neon" -> {
                bgPaint.color = android.graphics.Color.parseColor("#10051A")
                chamberPaint.color = android.graphics.Color.parseColor("#1B0A2E")
                wallBorderPaint.color = android.graphics.Color.parseColor("#FF007F")
                gridPaint.color = android.graphics.Color.parseColor("#2E104D")
            }
            "plasma_void" -> {
                bgPaint.color = android.graphics.Color.parseColor("#090A1A")
                chamberPaint.color = android.graphics.Color.parseColor("#0F122B")
                wallBorderPaint.color = android.graphics.Color.parseColor("#7C4DFF")
                gridPaint.color = android.graphics.Color.parseColor("#1B214D")
            }
            "golden_core" -> {
                bgPaint.color = android.graphics.Color.parseColor("#141008")
                chamberPaint.color = android.graphics.Color.parseColor("#1F180B")
                wallBorderPaint.color = android.graphics.Color.parseColor("#FFD600")
                gridPaint.color = android.graphics.Color.parseColor("#382D12")
            }
            else -> {
                bgPaint.color = android.graphics.Color.parseColor("#0A0D14")
                chamberPaint.color = android.graphics.Color.parseColor("#0D111A")
                wallBorderPaint.color = android.graphics.Color.parseColor("#00E5FF")
                gridPaint.color = android.graphics.Color.parseColor("#152033")
            }
        }
    }

    fun render(canvas: NativeCanvas, world: GameWorld, screenWidth: Float, screenHeight: Float) {
        applyThemeColors(world.currentThemeId)

        // Screen Shake Juice
        canvas.save()
        if (world.screenShakeIntensity > 0f) {
            val shakeX = (Random.nextFloat() - 0.5f) * world.screenShakeIntensity
            val shakeY = (Random.nextFloat() - 0.5f) * world.screenShakeIntensity
            canvas.translate(shakeX, shakeY)
        }

        // 1. Draw Overall Background
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, bgPaint)

        // 2. Draw Chamber Containment Area
        tempRectF.set(world.chamberLeft, world.spawnLineY - 40f, world.chamberRight, world.chamberFloorY)
        canvas.drawRoundRect(tempRectF, 24f, 24f, chamberPaint)

        // Chamber Grid Accent
        val gridStep = 60f
        var gx = world.chamberLeft + gridStep
        while (gx < world.chamberRight) {
            canvas.drawLine(gx, world.spawnLineY - 40f, gx, world.chamberFloorY, gridPaint)
            gx += gridStep
        }

        // Chamber Outer Border
        canvas.drawRoundRect(tempRectF, 24f, 24f, wallBorderPaint)

        // 3. Draw Danger Warning Zone & Line
        if (world.isDangerActive) {
            val pulsingAlpha = (110 + (sin(System.currentTimeMillis() * 0.018) * 75)).toInt().coerceIn(50, 185)
            dangerZonePaint.color = android.graphics.Color.argb(pulsingAlpha, 255, 23, 68)
            canvas.drawRect(world.chamberLeft, world.spawnLineY, world.chamberRight, world.dangerLineY, dangerZonePaint)
        }

        // Danger line: calm by default, gently pulses only when the chamber is actually unsafe.
        val dangerTime = System.nanoTime() / 1_000_000_000f
        if (world.isDangerActive) {
            val pulse = (0.72f + 0.28f * ((sin(dangerTime * 5.0f) + 1f) * 0.5f))
            dangerLinePaint.alpha = (255f * pulse).toInt()
            dangerLinePaint.pathEffect = DashPathEffect(floatArrayOf(16f, 12f), -dangerTime * 28f)
        } else {
            dangerLinePaint.alpha = 185
            dangerLinePaint.pathEffect = DashPathEffect(floatArrayOf(16f, 12f), 0f)
        }
        canvas.drawLine(world.chamberLeft, world.dangerLineY, world.chamberRight, world.dangerLineY, dangerLinePaint)

        // 4. Draw Aim Trajectory & Aiming Core Preview
        world.currentCore?.let { core ->
            if (!core.isSpawned) {
                canvas.drawLine(core.position.x, core.position.y, core.position.x, world.chamberFloorY - core.radius, aimLinePaint)
                drawCore(canvas, core)
            }
        }

        // 5. Draw Active Cores
        for (core in world.activeCores) {
            if (core.active) {
                drawCore(canvas, core)
            }
        }

        // 6. Draw Shockwaves
        for (wave in world.shockwaves) {
            shockwavePaint.color = wave.color.toArgb()
            shockwavePaint.style = Paint.Style.STROKE
            shockwavePaint.strokeWidth = 6.5f * wave.alpha
            shockwavePaint.alpha = (wave.alpha * 230).toInt().coerceIn(0, 255)
            canvas.drawCircle(wave.x, wave.y, wave.currentRadius, shockwavePaint)
        }

        // 7. Draw Particles
        for (p in world.particles) {
            particlePaint.color = p.color.toArgb()
            particlePaint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
            particlePaint.style = Paint.Style.FILL
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
        }

        // 8. Draw Floating Score Text
        for (ft in world.floatingTexts) {
            val textSize = if (ft.isCombo) 48f * ft.scale else 38f
            floatTextPaint.textSize = textSize
            floatTextStrokePaint.textSize = textSize

            val textAlpha = (ft.alpha * 255).toInt().coerceIn(0, 255)
            floatTextPaint.color = ft.color.toArgb()
            floatTextPaint.alpha = textAlpha
            floatTextStrokePaint.alpha = textAlpha

            canvas.drawText(ft.text, ft.x, ft.y, floatTextStrokePaint)
            canvas.drawText(ft.text, ft.x, ft.y, floatTextPaint)
        }

        canvas.restore()
    }

    fun drawCore(canvas: NativeCanvas, core: Core) {
        val levelInfo = CoreLevelRegistry.getInfo(core.level)
        val mainColor = if (core.charge == Charge.POSITIVE) levelInfo.positiveColor else levelInfo.negativeColor
        val glowColor = levelInfo.glowColor

        val cx = core.position.x
        val cy = core.position.y

        /*
         * Visual-only motion. Physics never uses these values.
         * The aim is "alive, but calm": tiny idle breathing, soft preview float,
         * a quick merge pop, and a small squash/stretch while a core is moving.
         */
        val now = System.nanoTime() / 1_000_000_000f
        val phase = (core.id % 31L).toFloat() * 0.37f

        val idleAmount = if (!core.isSpawned) 0.025f else 0.012f
        val idleScale = 1f + sin(now * 2.2f + phase) * idleAmount

        val mergeProgress = (1f - core.mergeAnimTime / 0.3f).coerceIn(0f, 1f)
        val mergeEase = if (core.mergeAnimTime > 0f) {
            1f + 0.28f * sin(mergeProgress * Math.PI).toFloat()
        } else {
            1f
        }

        val speed = kotlin.math.abs(core.velocity.y)
        val stretch = if (core.isSpawned) (speed / 1800f).coerceIn(0f, 0.055f) else 0f

        val scaleX = idleScale * mergeEase * (1f - stretch * 0.35f)
        val scaleY = idleScale * mergeEase * (1f + stretch)

        val r = core.radius

        canvas.save()
        canvas.scale(scaleX, scaleY, cx, cy)

        // 1. Outer Glow Pulse Aura
        coreGlowPaint.style = Paint.Style.FILL
        val glowRadius = r * (1.32f + if (core.level >= 5) 0.15f else 0f)
        val radialGlow = android.graphics.RadialGradient(
            cx, cy, glowRadius,
            intArrayOf(glowColor.copy(alpha = 0.45f).toArgb(), android.graphics.Color.TRANSPARENT),
            floatArrayOf(0.4f, 1.0f),
            android.graphics.Shader.TileMode.CLAMP
        )
        coreGlowPaint.shader = radialGlow
        canvas.drawCircle(cx, cy, glowRadius, coreGlowPaint)
        coreGlowPaint.shader = null

        // High Level Orbital Particle Aura (Singularity / Star / Nova / Plasma)
        if (core.level >= 7) {
            val orbitCount = core.level - 4
            val orbitStep = (2f * Math.PI / orbitCount).toFloat()
            for (k in 0 until orbitCount) {
                val orbitAngle = core.rotationAngle * 2f + k * orbitStep
                val ox = cx + cos(orbitAngle) * (r * 1.22f)
                val oy = cy + sin(orbitAngle) * (r * 1.22f)
                particlePaint.color = glowColor.toArgb()
                canvas.drawCircle(ox, oy, 4f, particlePaint)
            }
        }

        // 2. Core Main Radial Body Sphere
        val mainShader = android.graphics.RadialGradient(
            cx - r * 0.3f, cy - r * 0.3f, r * 1.1f,
            intArrayOf(android.graphics.Color.WHITE, mainColor.toArgb(), darkShade(mainColor.toArgb())),
            floatArrayOf(0f, 0.55f, 1.0f),
            android.graphics.Shader.TileMode.CLAMP
        )
        coreBodyPaint.style = Paint.Style.FILL
        coreBodyPaint.shader = mainShader
        canvas.drawCircle(cx, cy, r, coreBodyPaint)
        coreBodyPaint.shader = null

        // 3. Orbiting Energy Ring
        coreRingPaint.style = Paint.Style.STROKE
        coreRingPaint.strokeWidth = (r * 0.08f).coerceAtLeast(2.5f)
        coreRingPaint.color = mainColor.toArgb()
        canvas.drawCircle(cx, cy, r * 0.82f, coreRingPaint)

        val tickCount = (4 + core.level).coerceAtMost(12)
        val angleStep = (2f * Math.PI / tickCount).toFloat()
        for (i in 0 until tickCount) {
            val tickAngle = core.rotationAngle + i * angleStep
            val tx1 = cx + cos(tickAngle) * (r * 0.72f)
            val ty1 = cy + sin(tickAngle) * (r * 0.72f)
            val tx2 = cx + cos(tickAngle) * (r * 0.88f)
            val ty2 = cy + sin(tickAngle) * (r * 0.88f)
            canvas.drawLine(tx1, ty1, tx2, ty2, coreRingPaint)
        }

        // 4. Center Charge Symbol (+ or -) with Outline
        val symbolSize = (r * 0.9f).coerceIn(24f, 80f)
        symbolTextPaint.textSize = symbolSize
        symbolStrokePaint.textSize = symbolSize
        symbolTextPaint.color = android.graphics.Color.WHITE

        val symbolStr = core.charge.symbol
        symbolTextPaint.getTextBounds(symbolStr, 0, symbolStr.length, textBounds)
        val symbolY = cy + textBounds.height() / 2f - 3f

        canvas.drawText(symbolStr, cx, symbolY, symbolStrokePaint)
        canvas.drawText(symbolStr, cx, symbolY, symbolTextPaint)

        // 5. Core Level Badge
        val badgeW = (r * 0.75f).coerceIn(24f, 50f)
        val badgeH = (r * 0.36f).coerceIn(14f, 24f)
        val badgeY = cy + r * 0.55f

        tempRectF.set(cx - badgeW / 2f, badgeY - badgeH / 2f, cx + badgeW / 2f, badgeY + badgeH / 2f)
        canvas.drawRoundRect(tempRectF, badgeH / 2f, badgeH / 2f, levelBadgeBgPaint)

        levelBadgeTextPaint.textSize = (badgeH * 0.75f).coerceAtLeast(10f)
        val lvlText = "L${core.level}"
        levelBadgeTextPaint.getTextBounds(lvlText, 0, lvlText.length, textBounds)
        canvas.drawText(lvlText, cx, badgeY + textBounds.height() / 2f - 1.5f, levelBadgeTextPaint)

        canvas.restore()
    }

    private fun darkShade(colorInt: Int): Int {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(colorInt, hsv)
        hsv[2] *= 0.35f
        return android.graphics.Color.HSVToColor(hsv)
    }
}
