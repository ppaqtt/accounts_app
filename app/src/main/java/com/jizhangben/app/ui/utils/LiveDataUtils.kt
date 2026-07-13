package com.jizhangben.app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData

@Composable
fun <T> LiveData<T>.observeAsStateInitial(initial: T): T {
    val state = remember { mutableStateOf(initial) }
    LaunchedEffect(this) {
        observeForever { value ->
            state.value = value ?: initial
        }
    }
    return state.value
}
