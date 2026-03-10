package com.chigo.tappgospinwheel

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chigo.tappgospinwheel.ui.CooldownState
import com.chigo.tappgospinwheel.ui.SpinWheelView
import com.chigo.tappgospinwheel.ui.SpinWheelViewModel
import com.chigo.tappgospinwheel.ui.WheelState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: SpinWheelViewModel by viewModels()

    private lateinit var spinWheelView: SpinWheelView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSpinCount: TextView
    private lateinit var tvCooldown: TextView

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinWheelView = findViewById(R.id.spinWheelView)
        progressBar   = findViewById(R.id.progressBar)
        tvSpinCount   = findViewById(R.id.tv_spin_count)
        tvCooldown    = findViewById(R.id.tv_cooldown)

        spinWheelView.onSpinComplete = { degrees ->
            viewModel.onSpinComplete(degrees)
        }

        observeWheelState()
        observeCooldownState()
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCooldown()
    }

    // ─── Observers ───────────────────────────────────────────────────────────

    private fun observeWheelState() {
        lifecycleScope.launch {
            viewModel.wheelState.collect { state ->
                when (state) {
                    is WheelState.Loading -> {
                        progressBar.visibility  = View.VISIBLE
                        spinWheelView.visibility = View.GONE
                    }
                    is WheelState.Ready -> {
                        progressBar.visibility  = View.GONE
                        spinWheelView.visibility = View.VISIBLE
                        spinWheelView.setAssets(
                            bg      = state.bg,
                            wheel   = state.wheel,
                            frame   = state.wheelFrame,
                            spinBtn = state.wheelSpin
                        )
                        spinWheelView.setRotationConfig(state.rotationConfig)
                        updateSpinCount()
                    }
                    is WheelState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.loadWheel()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun observeCooldownState() {
        lifecycleScope.launch {
            viewModel.cooldownState.collect { state ->
                when (state) {
                    is CooldownState.Ready -> {
                        countDownTimer?.cancel()
                        tvCooldown.visibility = View.GONE
                        spinWheelView.setSpinEnabled(true)
                        updateSpinCount()
                    }
                    is CooldownState.Active -> {
                        spinWheelView.setSpinEnabled(false)
                        startCountdown(state.timeLeft)
                    }
                }
            }
        }
    }

    //Countdown

    private fun startCountdown(timeLeft: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                tvCooldown.visibility = View.VISIBLE
                tvCooldown.text = "Try again in: ${String.format("%02d:%02d", minutes, seconds)}"
            }

            override fun onFinish() {
                tvCooldown.visibility = View.GONE
                viewModel.checkCooldown()
            }
        }.start()
    }

    // Helpers

    private fun updateSpinCount() {
        tvSpinCount.visibility = View.VISIBLE
        tvSpinCount.text = "Total spins: ${viewModel.spinCount}"
    }
}