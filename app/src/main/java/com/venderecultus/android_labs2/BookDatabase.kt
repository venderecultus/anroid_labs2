package com.venderecultus.android_labs2

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val year: String,
    val rating: Float = 3f
)

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE") fun observeAll(): Flow<List<Book>>
    @Query("SELECT * FROM books WHERE id = :id") fun observeById(id: Int): Flow<Book?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(book: Book): Long
    @Update suspend fun update(book: Book)
    @Delete suspend fun delete(book: Book)
}

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var instance: BookDatabase? = null

        fun get(context: Context): BookDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, BookDatabase::class.java, "library.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        db.execSQL("INSERT INTO books (title, author, year, rating) VALUES ('1984', 'George Orwell', '1949', 5), ('Pride and Prejudice', 'Jane Austen', '1813', 4), ('The Great Gatsby', 'F. Scott Fitzgerald', '1925', 5), ('To Kill a Mockingbird', 'Harper Lee', '1960', 5), ('The Little Prince', 'Antoine de Saint-Exupéry', '1943', 5)")
                    }
                }).build().also { instance = it }
        }
    }
}
