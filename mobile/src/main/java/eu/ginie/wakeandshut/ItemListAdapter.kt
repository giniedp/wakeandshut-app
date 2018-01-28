package eu.ginie.wakeandshut

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

class ItemListAdapter : RecyclerView.Adapter<ItemViewHolder>() {

    private var items = ArrayList<Item>()
    private var onClick: (rec: Item) -> Unit = {}

    fun setData(items: ArrayList<Item>) {
        this.items = items
        this.notifyDataSetChanged()
    }

    fun setClickListener(l: (rec: Item) -> Unit) {
        this.onClick = l
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.update(item, View.OnClickListener { onClick(item) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.inflate(parent)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}