package com.example.cattodo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val stickers = listOf("cat1", "cat2", "cat3")   // add more names here
    private var sticker = "cat1"
    private var prio = 0
    private var draft = ""
    private lateinit var root: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(48), dp(20), dp(20))
        }
        setContentView(ScrollView(this).apply { addView(root) })
        build()
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun build() {
        root.removeAllViews()

        val input = EditText(this).apply { hint = "Add a task..."; setText(draft) }
        root.addView(input)

        val sRow = LinearLayout(this)
        for (s in stickers) {
            sRow.addView(ImageView(this).apply {
                setImageResource(resources.getIdentifier(s, "drawable", packageName))
                layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
                alpha = if (s == sticker) 1f else 0.35f
                setOnClickListener { draft = input.text.toString(); sticker = s; build() }
            })
        }
        root.addView(sRow)

        val pRow = LinearLayout(this)
        listOf("High", "Medium", "Low").forEachIndexed { i, name ->
            pRow.addView(Button(this).apply {
                text = name
                alpha = if (i == prio) 1f else 0.4f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { draft = input.text.toString(); prio = i; build() }
            })
        }
        root.addView(pRow)

        val fonts = (assets.list("fonts") ?: emptyArray()).map { it.removeSuffix(".ttf") }
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fonts)
        spinner.setSelection(fonts.indexOf(Store.font(this)).coerceAtLeast(0))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                Store.setFont(this@MainActivity, fonts[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        root.addView(spinner)

        root.addView(Button(this).apply {
            text = "Add task"
            setOnClickListener {
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) {
                    val list = Store.load(this@MainActivity)
                    list.add(Task(System.currentTimeMillis(), t, prio, sticker, false))
                    Store.save(this@MainActivity, list)
                    draft = ""
                    build()
                }
            }
        })

        val colors = listOf("#FFB3C1", "#FFE29A", "#C7F0DF")
        for (t in Store.load(this)) {
            root.addView(TextView(this).apply {
                text = (if (t.done) "✓  " else "•  ") + t.text
                textSize = 18f
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setBackgroundColor(Color.parseColor(colors[t.priority]))
                setOnClickListener {   // tap = done / undone
                    val l = Store.load(this@MainActivity)
                    l.find { it.id == t.id }?.let { it.done = !it.done }
                    Store.save(this@MainActivity, l); build()
                }
                setOnLongClickListener {   // long-press = delete
                    Store.save(this@MainActivity, Store.load(this@MainActivity).filter { it.id != t.id })
                    build(); true
                }
            })
        }
    }
}