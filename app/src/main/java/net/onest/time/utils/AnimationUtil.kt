package net.onest.time.utils

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object AnimationUtil {
    fun alphaAnimation(
        fromAlpha: Float = .3f,
        toAlpha: Float = 1f,
        duration: Long = 500
    ): AlphaAnimation {
        val alphaAnimation = AlphaAnimation(.3f, 1f)
        alphaAnimation.setDuration(500)
        return alphaAnimation;
    }
}

fun View.withCustomAlphaAnimation(): View {
    this.animation = AnimationUtil.alphaAnimation()
    return this
}

fun View.withCustomTranslateAnimation(): View {
    val translateAnimation = TranslateAnimation(0f, 100f, 0f, 100f)
    translateAnimation.duration = 5000
    this.animation = translateAnimation
    return this
}

fun View.doShakeAnimation(rotation: Float = 15f, duration: Long = 500) {
    var animator: ViewPropertyAnimator? = null

    animator = this.animate()
        .rotation(rotation)
    animator.duration = duration / 4
    animator.withEndAction {
        animator = this.animate()
            .rotationBy(-rotation * 2)
        animator!!.duration = duration / 2
        animator!!.withEndAction {
            animator = this.animate()
                .rotationBy(rotation)
            animator!!.duration = duration / 4
        }
    }
}

fun View.doSpinCircleAnimation(rotation: Float = 90f, duration: Long = 500) {
    var animator: ViewPropertyAnimator? = null

    animator = this.animate()
        .rotation(rotation)
    animator.duration = duration
    animator.withEndAction {
        animator.rotation(0f)
        animator.duration = 0
    }
}

fun View.doFadeInAnimation(duration: Long = 300) {
    var animator: ViewPropertyAnimator? = null

    this.alpha = 0f
    animator = this.animate()
        .alpha(1f)
        .setDuration(duration)
}

fun View.doFadeOutAnimation(duration: Long = 300) {
    var animator: ViewPropertyAnimator? = null

    animator = this.animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            this.visibility = View.INVISIBLE
        }
}