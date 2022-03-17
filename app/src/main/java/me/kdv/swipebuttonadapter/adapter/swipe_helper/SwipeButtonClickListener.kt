package me.kdv.swipebuttonadapter.adapter.swipe_helper

import android.view.View

interface SwipeButtonClickListener {
    fun onClick(view: View, pos: Int)
}