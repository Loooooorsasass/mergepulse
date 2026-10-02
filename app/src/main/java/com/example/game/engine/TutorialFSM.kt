package com.example.game.engine

enum class TutorialState {
    INTRO_DROP,
    TEACH_MERGE,
    MERGE_CONFIRM,
    TEACH_FLIP,
    TEACH_SHOCKWAVE,
    TEACH_DANGER_LINE,
    COMPLETE
}

enum class HighlightTarget {
    NONE,
    CHAMBER,
    FLIP_BUTTON,
    DROP_BUTTON,
    DANGER_LINE
}

class TutorialFSM(private val world: GameWorld) {

    var state: TutorialState = TutorialState.INTRO_DROP
        private set

    var canSkip: Boolean = false
        private set

    var overlayMessage: String = ""
        private set

    var highlightTarget: HighlightTarget = HighlightTarget.NONE
        private set

    var isFlipAllowed: Boolean = true
        private set

    var isDropAllowed: Boolean = true
        private set

    var requireFlipBeforeDrop: Boolean = false
        private set

    var hasFlippedInStep: Boolean = false

    private var stateTimer: Float = 0f

    fun start() {
        transition(TutorialState.INTRO_DROP)
    }

    fun update(dt: Float) {
        if (state == TutorialState.COMPLETE) return

        if (stateTimer > 0f) {
            stateTimer -= dt
            if (stateTimer <= 0f) {
                when (state) {
                    TutorialState.MERGE_CONFIRM -> transition(TutorialState.TEACH_FLIP)
                    TutorialState.TEACH_SHOCKWAVE -> transition(TutorialState.TEACH_DANGER_LINE)
                    TutorialState.TEACH_DANGER_LINE -> transition(TutorialState.COMPLETE)
                    else -> {}
                }
            }
        }
    }

    fun onEvent(eventName: String, payload: Map<String, Any> = emptyMap()) {
        when (state) {
            TutorialState.INTRO_DROP -> {
                if (eventName == "core_dropped") {
                    transition(TutorialState.TEACH_MERGE)
                }
            }
            TutorialState.TEACH_MERGE -> {
                if (eventName == "merge_success") {
                    transition(TutorialState.MERGE_CONFIRM)
                }
            }
            TutorialState.TEACH_FLIP -> {
                if (eventName == "flip_used") {
                    hasFlippedInStep = true
                }
                if (eventName == "merge_success" && hasFlippedInStep) {
                    transition(TutorialState.TEACH_SHOCKWAVE)
                } else if (eventName == "core_dropped" && !hasFlippedInStep) {
                    // Re-prompt if dropped without flip
                    overlayMessage = "Hãy nhấn nút [ FLIP ± ] trước để đổi dấu!"
                }
            }
            else -> {}
        }
    }

    fun transition(nextState: TutorialState) {
        state = nextState
        enterState(nextState)
    }

    fun skip() {
        if (!canSkip) return
        transition(TutorialState.COMPLETE)
    }

    private fun enterState(nextState: TutorialState) {
        stateTimer = 0f
        when (nextState) {
            TutorialState.INTRO_DROP -> {
                overlayMessage = "Chạm để thả Energy Core vào buồng chứa"
                highlightTarget = HighlightTarget.CHAMBER
                isFlipAllowed = false
                isDropAllowed = true
                requireFlipBeforeDrop = false
                canSkip = false
                world.spawnFixedCore(level = 1, charge = com.example.game.model.Charge.POSITIVE)
            }

            TutorialState.TEACH_MERGE -> {
                overlayMessage = "Cùng Level, KHÁC DẤU (+ và -) sẽ MERGE!"
                highlightTarget = HighlightTarget.NONE
                isFlipAllowed = false
                isDropAllowed = true
                requireFlipBeforeDrop = false
                canSkip = false
                world.spawnFixedCore(level = 1, charge = com.example.game.model.Charge.NEGATIVE)
            }

            TutorialState.MERGE_CONFIRM -> {
                overlayMessage = "MERGE THÀNH CÔNG! +3 ĐIỂM!"
                highlightTarget = HighlightTarget.NONE
                isFlipAllowed = false
                isDropAllowed = false
                canSkip = false
                stateTimer = 1.0f
                world.onPlaySfx?.invoke(GameWorld.SfxType.MISSION_WIN, 1.2f, 1)
            }

            TutorialState.TEACH_FLIP -> {
                overlayMessage = "Thử nhấn nút [ FLIP ± ] để đổi dấu rồi thả!"
                highlightTarget = HighlightTarget.FLIP_BUTTON
                isFlipAllowed = true
                isDropAllowed = true
                requireFlipBeforeDrop = true
                hasFlippedInStep = false
                canSkip = true
                world.spawnFixedCore(level = 1, charge = com.example.game.model.Charge.POSITIVE)
            }

            TutorialState.TEACH_SHOCKWAVE -> {
                overlayMessage = "Merge tạo ra Shockwave – đẩy các Core xung quanh!"
                highlightTarget = HighlightTarget.NONE
                isFlipAllowed = true
                isDropAllowed = true
                requireFlipBeforeDrop = false
                canSkip = true
                stateTimer = 2.5f
            }

            TutorialState.TEACH_DANGER_LINE -> {
                overlayMessage = "Đừng để Core vượt vạch đỏ quá lâu!"
                highlightTarget = HighlightTarget.DANGER_LINE
                isFlipAllowed = true
                isDropAllowed = true
                requireFlipBeforeDrop = false
                canSkip = true
                stateTimer = 2.5f
            }

            TutorialState.COMPLETE -> {
                overlayMessage = ""
                highlightTarget = HighlightTarget.NONE
                isFlipAllowed = true
                isDropAllowed = true
                requireFlipBeforeDrop = false
                canSkip = false
            }
        }
    }
}
