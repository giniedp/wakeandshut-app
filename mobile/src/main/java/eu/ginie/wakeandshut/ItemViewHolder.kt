package eu.ginie.wakeandshut

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun inflate(parent: ViewGroup): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            return ItemViewHolder(view)
        }
    }


    private val iconView = itemView.findViewById<ImageView>(R.id.icon)
    private val text1View = itemView.findViewById<TextView>(R.id.text1)
    private val text2View = itemView.findViewById<TextView>(R.id.text2)

    private val stateLiveView = itemView.findViewById<ImageView>(R.id.state_alive)
    private val stateShieldView = itemView.findViewById<ImageView>(R.id.state_shield)
    private val stateKeyView = itemView.findViewById<ImageView>(R.id.state_key)
    private val progressView = itemView.findViewById<ProgressBar>(R.id.progress)

    private val mainContainer = itemView.findViewById<View>(R.id.main_content)

    fun update(record: Item, onClickListener: OnClickListener) {
        when (record.os) {
            "windows" -> iconView.setImageResource(R.drawable.ic_windows_grey600_48dp)
            "linux" -> iconView.setImageResource(R.drawable.ic_linux_grey600_48dp)
            "osx" -> iconView.setImageResource(R.drawable.ic_apple_grey600_48dp)
            else -> iconView.setImageResource(R.drawable.ic_laptop_grey600_48dp)
        }
        text1View.text = record.name
        text2View.text = record.host

        if (record.loading) {
            progressView.visibility = VISIBLE
            stateLiveView.visibility = GONE
        } else {
            progressView.visibility = GONE
            stateLiveView.visibility = VISIBLE
        }
        when (record.alive) {
            true -> stateLiveView.setImageResource(R.drawable.ic_heart_grey600_18dp)
            false -> stateLiveView.setImageResource(R.drawable.ic_heart_off_grey600_18dp)
        }
        stateShieldView.visibility = if (record.ssl) VISIBLE else INVISIBLE
        stateKeyView.visibility = if (record.protected) VISIBLE else INVISIBLE

        mainContainer.setOnClickListener(onClickListener)
    }
}