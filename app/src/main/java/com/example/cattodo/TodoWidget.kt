package com.example.cattodo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TodoWidget : AppWidgetProvider() {

    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val rv = RemoteViews(c.packageName, R.layout.widget)
            rv.setRemoteAdapter(R.id.list, Intent(c, WidgetService::class.java))

            val toggle = Intent(c, TodoWidget::class.java).setAction("TOGGLE")
            rv.setPendingIntentTemplate(
                R.id.list,
                PendingIntent.getBroadcast(c, 0, toggle, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            )
            val open = PendingIntent.getActivity(c, 1, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            rv.setOnClickPendingIntent(R.id.addBtn, open)

            m.updateAppWidget(id, rv)
        }
    }

    override fun onReceive(c: Context, i: Intent) {
        super.onReceive(c, i)
        if (i.action == "TOGGLE") {
            val tid = i.getLongExtra("id", -1L)
            val list = Store.load(c)
            list.find { it.id == tid }?.let { it.done = !it.done }
            Store.save(c, list)
        }
    }
}