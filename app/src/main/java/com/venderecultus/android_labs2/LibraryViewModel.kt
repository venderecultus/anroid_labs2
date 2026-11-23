package com.venderecultus.android_labs2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(BookDatabase.get(application).bookDao())
    val books = repository.books.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun book(id: Int) = repository.book(id)
    fun addBook(title: String, author: String, year: String) = viewModelScope.launch { repository.add(title, author, year) }
    fun updateBook(book: Book) = viewModelScope.launch { repository.update(book) }
    fun deleteBook(book: Book) = viewModelScope.launch { repository.delete(book) }
}
