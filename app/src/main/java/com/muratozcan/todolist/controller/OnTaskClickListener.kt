package com.muratozcan.todolist.controller

import android.widget.EditText

interface OnTaskClickListener {

    fun OnTaskClick(name: EditText, notes: EditText)
}