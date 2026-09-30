package com.example.cattodo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)
}

class Factory(private val c: Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks = listOf<Task>()

    override fun onCreate() {}
    override fun onDataSetChanged() { tasks = Store.load(c) }
    override fun onDestroy() {}
    override fun getCount() = tasks.size
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = tasks[position].id
    override fun hasStableIds() = true

    override fun getViewAt(pos: Int): RemoteViews {
        val t = tasks[pos]
        val rv = RemoteViews(c.packageName, R.layout.widget_item)
        val color = when (t.priority) {
            0 -> 0xFFFF8FA3.toInt()
            1 -> 0xFFFFD166.toInt()
            else -> 0xFFA8E6CF.toInt()
        }
        rv.setInt(R.id.bar, "setBackgroundColor", color)
        rv.setImageViewResource(R.id.sticker, c.resources.getIdentifier(t.sticker, "drawable", c.packageName))
        rv.setImageViewBitmap(R.id.text, textBitmap(t))
        rv.setOnClickFillInIntent(R.id.row, Intent().putExtra("id", t.id))
        return rv
    }

    private fun textBitmap(t: Task): Bitmap {
        val tf = Store.typeface(c)

        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = 46f
            this.color = if (t.done) 0xFF999999.toInt() else 0xFF4A3B47.toInt()
            isStrikeThruText = t.done
        }
        val shown = TextUtils.ellipsize(t.text, paint, 510f, TextUtils.TruncateAt.END).toString()
        val bmp = Bitmap.createBitmap(520, 80, Bitmap.Config.ARGB_8888)
        Canvas(bmp).drawText(shown, 0f, 54f, paint)
        return bmp
    }
}