package com.example.helpers

import com.example.models.Category
import com.example.repository.CategoryRepository
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

class CategorySpecHelper(categoryRepository: CategoryRepository)(implicit executionContext: ExecutionContext) {

  val category = Category(None, "Test Category")

  def createAndDelete(category: Category = category)(assertion: Category => Future[Assertion]): Future[Assertion] = {
    categoryRepository.create(category) flatMap {
      c =>
        val assertions = assertion(c)
        categoryRepository.delete(c.id.get) flatMap { _ => assertions }
    }
  }
}
