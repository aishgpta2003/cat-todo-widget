package com.example.cattodo

import android.graphics.Typeface
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Task(val id: Long, val text: String, val priority: Int, val sticker: String, var done: Boolean)

object Store {
    private fun prefs(c: Context) = c.getSharedPreferences("cat", Context.MODE_PRIVATE)

    fun load(c: Context): MutableList<Task> {
        val arr = JSONArray(prefs(c).getString("tasks", "[]"))
        val list = mutableListOf<Task>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(Task(o.getLong("id"), o.getString("text"), o.getInt("p"), o.getString("s"), o.getBoolean("d")))
        }
        return list.sortedWith(compareBy({ it.done }, { it.priority })).toMutableList()
    }

    fun save(c: Context, list: List<Task>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("text", it.text)
                .put("p", it.priority).put("s", it.sticker).put("d", it.done))
        }
        prefs(c).edit().putString("tasks", arr.toString()).apply()
        refresh(c)
    }

    fun font(c: Context): String = prefs(c).getString("font", "caveat") ?: "caveat"
    fun typeface(c: Context): Typeface = try {
    Typeface.createFromAsset(c.assets, "font/${font(c)}.ttf")
} catch (e: Exception) { 
    e.printStackTrace() // Check Logcat for the exact path Android is looking for
    Typeface.DEFAULT 
}
    fun setFont(c: Context, f: String) { prefs(c).edit().putString("font", f).apply(); refresh(c) }

    fun refresh(c: Context) {
        val m = AppWidgetManager.getInstance(c)
        val ids = m.getAppWidgetIds(ComponentName(c, TodoWidget::class.java))
        m.notifyAppWidgetViewDataChanged(ids, R.id.list)
    }
}