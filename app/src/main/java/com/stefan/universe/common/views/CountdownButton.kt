package com.stefan.universe.common.views

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.stefan.universe.R
import com.stefan.universe.databinding.CountdownButtonBinding

class CountdownButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: CountdownButtonBinding
    private var countDownTimer: CountDownTimer? = null

    init {
        setupView(context)
    }

    private fun setupView(context: Context) {
        binding = CountdownButtonBinding.inflate(LayoutInflater.from(context), this, true)

        binding.startButton.setOnClickListener {
            startCountdownTimer()
        }
    }

    private fun startCountdownTimer() {
        binding.startButton.apply {
            isEnabled = false
            alpha = 0.3f
            setBackgroundColor(resources.getColor(R.color.primary, null))
        }
        binding.countdownLayout.apply {
            visibility = VISIBLE
        }
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.countdownText.text = "$secondsRemaining"
            }

            override fun onFinish() {
                binding.startButton.apply {
                    isEnabled = true
                    alpha = 1f
                    setBackgroundColor(resources.getColor(R.color.secondary, null))
                }
                binding.countdownText.apply {
                    text = "60"
                }
                binding.countdownLayout.apply {
                    visibility = GONE
                }
            }
        }.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cancel the countdown timer to avoid memory leaks
        countDownTimer?.cancel()
    }
}
