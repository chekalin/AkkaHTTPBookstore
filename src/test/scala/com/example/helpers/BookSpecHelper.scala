package com.example.helpers

import java.sql.Date

import com.example.models.{Book, Category}
import com.example.repository.{BookRepository, CategoryRepository}

import scala.concurrent.{ExecutionContext, Future}

class BookSpecHelper(categoryRepository: CategoryRepository)(bookRepository: BookRepository)(implicit executionContext: ExecutionContext) {

  val category = Category(None, "Sci-Fi")

  def book(categoryId: Long) = Book(None, "Murder in Ganymede", Date.valueOf("1998-01-20"), categoryId, 3, "John Doe")

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
