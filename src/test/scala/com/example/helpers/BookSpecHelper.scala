package com.example.helpers

import java.sql.Date

import com.example.models.{Book, Category}
import com.example.repository.{BookRepository, CategoryRepository}

import scala.concurrent.{ExecutionContext, Future}

class BookSpecHelper(categoryRepository: CategoryRepository)(bookRepository: BookRepository)(implicit executionContext: ExecutionContext) {

  val category = Category(None, "Sci-Fi")

  def book(categoryId: Long,
           title: String = "Murder in Ganymede",
           releaseDate: Date = Date.valueOf("1998-01-20"),
           author: String = "John Doe") =
    Book(None, title, releaseDate, categoryId, 3, author)

  def createAndDelete[T]()(assertion: Book => Future[T]): Future[T] = {
    categoryRepository.create(category) flatMap { c =>
      bookRepository.create(book(c.id.get)) flatMap { b =>
        val assertions = assertion(b)
        bookRepository.delete(b.id.get) flatMap { _ =>
          categoryRepository.delete(c.id.get) flatMap { _ => assertions }
        }
      }
    }
  }

}
