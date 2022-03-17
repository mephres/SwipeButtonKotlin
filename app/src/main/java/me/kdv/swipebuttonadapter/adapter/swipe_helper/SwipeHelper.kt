package me.kdv.swipebuttonadapter.adapter.swipe_helper

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class SwipeHelper(private val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var recyclerView: RecyclerView? = null
    private var rightButtons = mutableListOf<SwipeButton>()
    private var leftButtons = mutableListOf<SwipeButton>()
    private var gestureDetector: GestureDetector? = null

    private var swipedPos = -1
    private var swipeThreshold = 0.5f

    private var buttonsRightBuffer = mutableMapOf<Int, MutableList<SwipeButton>>()
    private var buttonsLeftBuffer = mutableMapOf<Int, MutableList<SwipeButton>>()

    lateinit var recoverQueue: Queue<Int>

    private var buttonMargin = 10
    private var buttonWidth = 200

    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in rightButtons) {
                if (button.onClick(e.x, e.y)) {
                    break
                }
            }
            for (button in leftButtons) {
                if (button.onClick(e.x, e.y)) {
                    break
                }
            }
            return true
        }
    }

    private val onTouchListener = OnTouchListener { view, e ->
        if (swipedPos < 0) return@OnTouchListener false
        val point = Point(
            e.rawX.toInt(),
            e.rawY.toInt()
        )
        val swipedViewHolder = recyclerView?.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder?.itemView
        val rect = Rect()
        swipedItem?.getGlobalVisibleRect(rect)
        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
            if (rect.top < point.y && rect.bottom > point.y) gestureDetector!!.onTouchEvent(e) else {
                recoverQueue.add(swipedPos)
                swipedPos = -1
                recoverSwipedItem()
            }
        }
        false
    }

    init {
        gestureDetector = GestureDetector(context, gestureListener)
        recoverQueue = object : LinkedList<Int>() {
            override fun add(element: Int): Boolean {
                return if (contains(element)) false else super.add(element)
            }
        }
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            recoverQueue.poll()?.let {
                if (it > -1) {
                    recyclerView?.adapter?.notifyItemChanged(it)
                }
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        val pos = viewHolder.adapterPosition

        if (swipedPos != pos) recoverQueue.add(swipedPos)

        swipedPos = pos

        if (buttonsRightBuffer.containsKey(swipedPos)) {
            buttonsRightBuffer[swipedPos]?.let {
                rightButtons = it
            }
        } else {
            rightButtons.clear()
        }

        if (buttonsLeftBuffer.containsKey(swipedPos)) {
            buttonsLeftBuffer[swipedPos]?.let {
                leftButtons = it
            }
        } else {
            leftButtons.clear()
        }

        setSwipeThreshold(buttonsLeftBuffer, leftButtons)
        setSwipeThreshold(buttonsRightBuffer, rightButtons)

        recoverSwipedItem()
    }

    private fun setSwipeThreshold(
        buttonsBuffer: MutableMap<Int, MutableList<SwipeButton>>,
        buttons: MutableList<SwipeButton>
    ) {
        buttonsBuffer.clear()
        if (buttons.isNotEmpty()) {
            swipeThreshold = 0.5f * buttons.size * buttonWidth
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView

        if (pos < 0) {
            swipedPos = pos
            return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer = mutableListOf<SwipeButton>()

                if (!buttonsRightBuffer.containsKey(pos)) {
                    createRightButton(viewHolder, buffer)
                    buttonsRightBuffer[pos] = buffer
                } else {
                    buttonsRightBuffer[pos]?.let {
                        buffer = it
                    }
                }
                translationX = dX * buffer.size * buttonWidth / itemView.width
                translationX =
                    if (buffer.size > 0) translationX - buttonMargin else translationX
                drawRightButtons(c, itemView, buffer, pos, translationX)
            } else if (dX > 0) {
                var buffer = mutableListOf<SwipeButton>()
                if (!buttonsLeftBuffer.containsKey(pos)) {
                    createLeftButton(viewHolder, buffer)
                    buttonsLeftBuffer[pos] = buffer
                } else {
                    buttonsLeftBuffer[pos]?.let {
                        buffer = it
                    }
                }
                translationX = dX * buffer.size * buttonWidth / itemView.width
                translationX =
                    if (buffer.size > 0) translationX + buttonMargin else translationX
                drawLeftButtons(c, itemView, buffer, pos, translationX)
            }
        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    private fun drawRightButtons(
        c: Canvas,
        itemView: View,
        buffer: List<SwipeButton>,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size
        for (button in buffer) {
            val left: Float = right - dButtonWidth + buttonMargin
            button.draw(
                c, RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ), pos
            )
            right = left - buttonMargin
        }
    }

    private fun drawLeftButtons(
        c: Canvas,
        itemView: View,
        buffer: List<SwipeButton>,
        pos: Int,
        dX: Float
    ) {
        var left = itemView.left.toFloat()
        val dButtonWidth = 1 * dX / buffer.size
        for (button in buffer) {
            val right: Float = left + dButtonWidth - buttonMargin
            button.draw(
                c, RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ), pos
            )
            left = right + buttonMargin
        }
    }

    open fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.setOnTouchListener(onTouchListener)
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    open fun getButtonMargin(): Int {
        return buttonMargin
    }

    open fun setButtonMargin(buttonMargin: Int) {
        this.buttonMargin = buttonMargin
    }

    open fun getButtonWidth(): Int {
        return buttonWidth
    }

    open fun setButtonWidth(buttonWidth: Int) {
        this.buttonWidth = buttonWidth
    }

    abstract fun createRightButton(
        viewHolder: RecyclerView.ViewHolder?,
        swipeButtons: MutableList<SwipeButton>
    )

    abstract fun createLeftButton(
        viewHolder: RecyclerView.ViewHolder?,
        swipeButtons: MutableList<SwipeButton>
    )
}