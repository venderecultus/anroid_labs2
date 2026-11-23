package com.venderecultus.android_labs2

import kotlinx.coroutines.flow.Flow

class BookRepository(private val dao: BookDao) {
    val books: Flow<List<Book>> = dao.observeAll()
    fun book(id: Int): Flow<Book?> = dao.observeById(id)
    suspend fun add(title: String, author: String, year: String) = dao.insert(Book(title = title, author = author, year = year))
    suspend fun update(book: Book) = dao.update(book)
    suspend fun delete(book: Book) = dao.delete(book)
}
