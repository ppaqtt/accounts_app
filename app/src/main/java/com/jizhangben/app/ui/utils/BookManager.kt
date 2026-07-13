package com.jizhangben.app.ui.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray

class BookManager(context: Context) {
    private val prefs = context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE)

    private val defaultBooks = listOf("日常账本", "工作账本", "旅行账本")

    val bookListState = mutableStateOf(loadBookList())
    val currentBookState = mutableStateOf(
        prefs.getString("current_book", "日常账本") ?: "日常账本"
    )

    var currentBook: String
        get() = currentBookState.value
        set(value) {
            currentBookState.value = value
            prefs.edit().putString("current_book", value).apply()
        }

    var bookList: List<String>
        get() = bookListState.value
        set(value) {
            bookListState.value = value
            saveBookList(value)
        }

    private fun loadBookList(): List<String> {
        val json = prefs.getString("book_list", null)
        return if (json != null) {
            val list = mutableListOf<String>()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } else {
            defaultBooks
        }
    }

    private fun saveBookList(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString("book_list", arr.toString()).apply()
    }

    fun addBook(name: String): Boolean {
        if (name.isBlank() || bookList.contains(name)) return false
        bookList = bookList + name
        return true
    }

    fun deleteBook(name: String): Boolean {
        if (bookList.size <= 1) return false
        if (name == currentBook) return false
        bookList = bookList - name
        return true
    }

    fun renameBook(oldName: String, newName: String): Boolean {
        if (newName.isBlank() || bookList.contains(newName)) return false
        val idx = bookList.indexOf(oldName)
        if (idx == -1) return false
        val newList = bookList.toMutableList()
        newList[idx] = newName
        bookList = newList
        if (currentBook == oldName) {
            currentBook = newName
        }
        return true
    }
}

val LocalBookManager = compositionLocalOf<BookManager> {
    error("No BookManager provided")
}

@Composable
fun ProvideBookManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val bookManager = remember { BookManager(context) }
    CompositionLocalProvider(LocalBookManager provides bookManager) {
        content()
    }
}
