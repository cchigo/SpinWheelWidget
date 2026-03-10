package com.chigo.tappgospinwheel.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.chigo.tappgospinwheel.R

internal fun setCooldownMessage(
    context: Context,
    manager: AppWidgetManager,
    widgetId: Int,
    spinCount: Int,
    showButton: Boolean,
    buttonText: String = ""
) {
    val views = RemoteViews(context.packageName, R.layout.widget_spin_wheel)
    views.setTextViewText(R.id.spin_count_text, "$spinCount")
    views.setViewVisibility(R.id.btn_spin, if (showButton) View.VISIBLE else View.GONE)
    if (showButton) {
        views.setTextViewText(R.id.btn_spin, buttonText)
    }
    views.setInt(R.id.img_spin_btn, "setAlpha", 128)
    manager.updateAppWidget(widgetId, views)
}