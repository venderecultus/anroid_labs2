package com.venderecultus.android_labs2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.venderecultus.android_labs2.ui.theme.Android_labs2Theme

data class Book(val id: Int, val title: String, val author: String, val year: String, val rating: Float = 3f)

class LibraryViewModel : ViewModel() {
    val books = mutableStateListOf(
        Book(1, "1984", "George Orwell", "1949", 5f),
        Book(2, "Pride and Prejudice", "Jane Austen", "1813", 4f),
        Book(3, "The Great Gatsby", "F. Scott Fitzgerald", "1925", 5f),
        Book(4, "To Kill a Mockingbird", "Harper Lee", "1960", 5f),
        Book(5, "The Little Prince", "Antoine de Saint-Exupéry", "1943", 5f)
    )
    private var nextId = 6
    fun addBook(title: String, author: String, year: String) { books.add(Book(nextId++, title, author, year)) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Android_labs2Theme { LibraryApp() } }
    }
}

@Composable
private fun LibraryApp() {
    val nav = rememberNavController()
    val viewModel: LibraryViewModel = viewModel()
    val books = viewModel.books

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        NavHost(navController = nav, startDestination = "books", modifier = Modifier.padding(padding)) {
            composable("books") {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Text("My Library", style = MaterialTheme.typography.headlineMedium)
                    Text("${books.size} books", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        items(books, key = { it.id }) { book ->
                            Card(Modifier.fillMaxWidth().clickable { nav.navigate("details/${book.id}") }) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(book.title, style = MaterialTheme.typography.titleMedium)
                                    Text(book.author, style = MaterialTheme.typography.bodyMedium)
                                    Text("${book.year}  ·  ${"★".repeat(book.rating.toInt())}", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Button(onClick = { nav.navigate("add") }, modifier = Modifier.fillMaxWidth()) { Text("＋  Add a book") }
                }
            }
            composable("add") {
                BookForm("Add a book", null, onSave = { title, author, year ->
                    viewModel.addBook(title, author, year); nav.popBackStack()
                }, onCancel = { nav.popBackStack() })
            }
            composable("edit/{bookId}", arguments = listOf(navArgument("bookId") { type = NavType.IntType })) { entry ->
                val book = books.find { it.id == entry.arguments?.getInt("bookId") }
                if (book != null) BookForm("Edit book", book, onSave = { title, author, year ->
                    val index = books.indexOfFirst { it.id == book.id }
                    if (index >= 0) books[index] = book.copy(title = title, author = author, year = year)
                    nav.popBackStack()
                }, onCancel = { nav.popBackStack() }) else nav.popBackStack()
            }
            composable("details/{bookId}", arguments = listOf(navArgument("bookId") { type = NavType.IntType })) { entry ->
                val book = books.find { it.id == entry.arguments?.getInt("bookId") }
                if (book != null) {
                    var rating by remember(book.id) { mutableFloatStateOf(book.rating) }
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
                        OutlinedButton(onClick = { nav.popBackStack() }) { Text("← Back") }
                        Spacer(Modifier.height(24.dp))
                        Text(book.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Author: ${book.author}")
                        Text("Publication year: ${book.year}")
                        Spacer(Modifier.height(24.dp))
                        Text("Rating: ${rating.toInt()} out of 5", style = MaterialTheme.typography.titleMedium)
                        Slider(value = rating, onValueChange = { rating = it }, onValueChangeFinished = {
                            val index = books.indexOfFirst { it.id == book.id }
                            if (index >= 0) books[index] = books[index].copy(rating = rating)
                        }, valueRange = 1f..5f, steps = 3)
                        Text("${"★".repeat(rating.toInt())}${"☆".repeat(5 - rating.toInt())}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { nav.navigate("edit/${book.id}") }, modifier = Modifier.fillMaxWidth()) { Text("Edit") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { books.removeAll { it.id == book.id }; nav.popBackStack("books", false) }, modifier = Modifier.fillMaxWidth()) { Text("Delete") }
                    }
                } else nav.popBackStack("books", false)
            }
        }
    }
}

@Composable
private fun BookForm(title: String, book: Book?, onSave: (String, String, String) -> Unit, onCancel: () -> Unit) {
    var name by remember(book?.id) { mutableStateOf(book?.title.orEmpty()) }
    var author by remember(book?.id) { mutableStateOf(book?.author.orEmpty()) }
    var year by remember(book?.id) { mutableStateOf(book?.year.orEmpty()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Book title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(author, { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(year, { year = it }, label = { Text("Publication year") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onSave(name.trim(), author.trim(), year.trim()) }, enabled = name.isNotBlank() && author.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}
