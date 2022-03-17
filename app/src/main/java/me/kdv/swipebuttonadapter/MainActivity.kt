package me.kdv.swipebuttonadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import me.kdv.swipebuttonadapter.adapter.RecyclerViewAdapder
import me.kdv.swipebuttonadapter.adapter.RecyclerViewAdapder.Companion.MAX_POOL_SIZE
import me.kdv.swipebuttonadapter.adapter.swipe_helper.SwipeButton
import me.kdv.swipebuttonadapter.adapter.swipe_helper.SwipeHelper
import me.kdv.swipebuttonadapter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val itemList = mutableListOf<Item>()
    private var itemAdapter: RecyclerViewAdapder? = null

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        for (i in 1..20) {
            val item = Item(i, "Name $i")
            itemList.add(item)
        }

        itemAdapter = RecyclerViewAdapder(this)
        binding.recyclerView.adapter = itemAdapter
        binding.recyclerView.recycledViewPool.setMaxRecycledViews(0, MAX_POOL_SIZE)
        itemAdapter?.submitList(itemList)

        itemAdapter?.onItemClickListener = {
            Toast.makeText(this, "Item name: ${it.name}", Toast.LENGTH_SHORT).show()
        }

        val swipeHelper = object : SwipeHelper(this) {
            override fun createRightButton(
                viewHolder: RecyclerView.ViewHolder?,
                swipeButtons: MutableList<SwipeButton>
            ) {
                val deleteButton = SwipeButton.build(applicationContext) {
                    text = "Delete"
                    buttonTextSize = 14
                    imageResId = R.drawable.ic_baseline_delete_48
                    buttonBackgroundColor = ContextCompat.getColor(applicationContext, R.color.grey_300)
                    textColor = ContextCompat.getColor(applicationContext, R.color.red_600)
                    imageColor = ContextCompat.getColor(applicationContext, R.color.red_600)
                }
                deleteButton.onSwipeButtonClickListener = { position ->
                    val data = itemAdapter?.let {
                        it.currentList[position] as Item
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Delete item with id = ${data?.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val likeButton = SwipeButton.build(applicationContext) {
                    text = "Like it"
                    buttonTextSize = 18
                    buttonBackgroundColor = ContextCompat.getColor(applicationContext, R.color.red_500)
                    textColor = ContextCompat.getColor(applicationContext, R.color.white)
                    imageColor = ContextCompat.getColor(applicationContext, R.color.white)
                }

                likeButton.onSwipeButtonClickListener = { position ->
                    val data = itemAdapter?.let {
                        it.currentList[position] as Item
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Like item with id = ${data?.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                swipeButtons.apply {
                    add(deleteButton)
                    add(likeButton)
                }
            }

            override fun createLeftButton(
                viewHolder: RecyclerView.ViewHolder?,
                swipeButtons: MutableList<SwipeButton>
            ) {
                val shareButton = SwipeButton.build(applicationContext) {
                    text = "Share it"
                    buttonTextSize = 10
                    imageResId = R.drawable.ic_baseline_share_24
                    buttonCornerRadius = 90
                    buttonBackgroundColor = ContextCompat.getColor(applicationContext, R.color.blue_700)
                    textColor = ContextCompat.getColor(applicationContext, R.color.white)
                    imageColor = ContextCompat.getColor(applicationContext, R.color.white)
                }
                shareButton.onSwipeButtonClickListener = { position ->
                    val data = itemAdapter?.let {
                        it.currentList[position] as Item
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Share item with id = ${data?.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                swipeButtons.add(shareButton)

                val secondButton = SwipeButton.build(applicationContext) {
                    text = "second"
                    buttonTextSize = 10
                    imageResId = R.drawable.ic_baseline_thumb_up_alt_48
                    buttonCornerRadius = 16
                    buttonBackgroundColor = ContextCompat.getColor(applicationContext, R.color.amber_600)
                    textColor = ContextCompat.getColor(applicationContext, R.color.white)
                    imageColor = ContextCompat.getColor(applicationContext, R.color.black)
                }
                swipeButtons.add(secondButton)
            }
        }

        swipeHelper.setButtonMargin(10)
        swipeHelper.setButtonWidth(200)
        swipeHelper.attachToRecyclerView(binding.recyclerView)
    }
}