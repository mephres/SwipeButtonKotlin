package me.kdv.swipebuttonadapter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import me.kdv.swipebuttonadapter.Item
import me.kdv.swipebuttonadapter.databinding.ItemBinding

class RecyclerViewAdapder (private var context: Context) : ListAdapter<Item, ItemViewHolder>(
    ItemDiffCallback()
) {
    var onItemClickListener: ((Item) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {

            itemNameTextView.text = item.name

            root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    companion object {
        const val MAX_POOL_SIZE = 15
    }
}
