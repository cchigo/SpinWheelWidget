package com.chigo.tappgospinwheel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import com.chigo.tappgospinwheel.MainActivity
import com.chigo.tappgospinwheel.R
import com.chigo.tappgospinwheel.data.repository.WheelRepository
import com.chigo.tappgospinwheel.model.ImageKey
import com.chigo.tappgospinwheel.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SpinWheelWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != Constants.ACTION_SPIN) return
        if (isSpinning) return

        val repo = getRepository(context)
        val manager = AppWidgetManager.getInstance(context)
        val widgetId = manager.getAppWidgetIds(
            ComponentName(context, SpinWheelWidget::class.java)
        ).firstOrNull() ?: return

        if (repo.isCooldownActive()) {
            showCooldownToast(context, manager, widgetId)
            return
        }

        spinWheel(context, manager, widgetId)
    }

    //  Widget UI

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        val repo = getRepository(context)
        val views = RemoteViews(context.packageName, R.layout.widget_spin_wheel)

        views.setTextViewText(R.id.spin_count_text, "${repo.getSpinCount()}")
        views.setViewVisibility(R.id.btn_spin, View.GONE)
        views.setInt(R.id.img_spin_btn, "setAlpha", if (repo.isCooldownActive()) 128 else 255)

        views.setOnClickPendingIntent(R.id.img_spin_btn, buildSpinPendingIntent(context, widgetId))
        views.setOnClickPendingIntent(R.id.img_bg, buildOpenAppPendingIntent(context))
        views.setOnClickPendingIntent(R.id.img_frame, buildOpenAppPendingIntent(context))

        loadCachedAssets(repo, views)
        manager.updateAppWidget(widgetId, views)

        fetchConfigIfNeeded(context, manager, widgetId)
    }

    private fun showCooldownToast(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val repo = getRepository(context)
        val spinCount = repo.getSpinCount()
        val cooldownText = context.getString(R.string.try_again_in, repo.getFormattedTimeUntilNextSpin())

        setCooldownMessage(context, manager, widgetId, spinCount, showButton = true, buttonText = cooldownText)

        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            setCooldownMessage(context, manager, widgetId, spinCount, showButton = false)
        }
    }

    private fun loadCachedAssets(repo: WheelRepository, views: RemoteViews) {
        repo.loadWidgetBitmaps().forEach { (key, bitmap) ->
            bitmap?.let { views.setImageViewBitmap(key.widgetViewId, it) }
        }
    }

    private fun loadOtherAssets(repo: WheelRepository, views: RemoteViews) {
        repo.loadWidgetBitmaps().forEach { (key, bitmap) ->
            bitmap?.let {
                when (key) {
                    ImageKey.BG         -> views.setImageViewBitmap(R.id.img_bg, it)
                    ImageKey.WHEEL_FRAME -> views.setImageViewBitmap(R.id.img_frame, it)
                    ImageKey.WHEEL_SPIN -> views.setImageViewBitmap(R.id.img_spin_btn, it)
                    else -> {}
                }
            }
        }
    }

    // Spiner
    private fun spinWheel(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        val repo = getRepository(context)
        val wheelBitmap = repo.loadWidgetBitmaps()[ImageKey.WHEEL] ?: return
        val config = repo.getRotationConfig()

        val duration  = config?.duration?.toLong() ?: 3000L
        val minSpins  = config?.minimumSpins ?: 3
        val maxSpins  = config?.maximumSpins ?: 5
        val spins     = Random.nextInt(minSpins, maxSpins + 1)
        val totalRotation = (spins * 360f) + Random.nextFloat() * 360f

        val startTime     = System.currentTimeMillis()
        val startRotation = currentRotation

        isSpinning = true

        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val elapsed  = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                val eased    = if (progress < 0.5f) 2f * progress * progress
                else -1f + (4f - 2f * progress) * progress

                currentRotation = (startRotation + (totalRotation * eased)) % 360f

                val rotated = createRotatedBitmap(wheelBitmap, currentRotation)
                val views   = RemoteViews(context.packageName, R.layout.widget_spin_wheel)
                views.setImageViewBitmap(ImageKey.WHEEL.widgetViewId, rotated)

                if (progress >= 1f) {
                    onSpinComplete(repo, context, views, manager, widgetId)
                    break
                } else {
                    manager.partiallyUpdateAppWidget(widgetId, views)
                    delay(16)
                }
            }
        }
    }

    private fun onSpinComplete(
        repo: WheelRepository,
        context: Context,
        views: RemoteViews,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        loadOtherAssets(repo, views)
        repo.incrementSpinCount()
        repo.saveSpinResult(currentRotation)
        repo.saveLastSpinTime()

        views.setTextViewText(R.id.spin_count_text, "${repo.getSpinCount()}")
        views.setViewVisibility(R.id.btn_spin, View.GONE)
        views.setInt(R.id.img_spin_btn, "setAlpha", 128)
        views.setOnClickPendingIntent(R.id.img_spin_btn, buildSpinPendingIntent(context, widgetId))

        manager.updateAppWidget(widgetId, views)
        isSpinning = false
    }

    // Bitmap
    private fun createRotatedBitmap(source: Bitmap, degrees: Float): Bitmap {
        val result = createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val matrix = Matrix().apply { postRotate(degrees, source.width / 2f, source.height / 2f) }
        canvas.drawBitmap(source, matrix, null)
        return result
    }

    // Intents
    private fun buildSpinPendingIntent(context: Context, widgetId: Int): PendingIntent {
        val intent = Intent(context, SpinWheelWidget::class.java).apply {
            action = Constants.ACTION_SPIN
        }
        return PendingIntent.getBroadcast(
            context, widgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildOpenAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Fetch config
    private fun fetchConfigIfNeeded(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = getRepository(context)
            val refreshed = repo.refreshIfExpired()
            if (refreshed) {
                currentRotation = 0f
                val views = RemoteViews(context.packageName, R.layout.widget_spin_wheel)
                loadCachedAssets(repo, views)
                manager.updateAppWidget(widgetId, views)
            }
        }
    }

    companion object {
        private var currentRotation = 0f
        private var isSpinning = false
        private var repository: WheelRepository? = null

        fun getRepository(context: Context): WheelRepository {
            return repository ?: WheelRepository(context.applicationContext).also {
                repository = it
            }
        }
    }
}