package dev.silverest.invoicerback.daos

import dev.silverest.invoicerback.models.*

case class ProductDao(
  id: Option[Int],
  name: String,
  description: String,
  price: Double,
  tax: Percent,
  discount: Percent,
  userId: String)

object ProductDao:
  given ProductDaoMapper: Mapper[ProductDao, Product] with
    def modelMapper(p: ProductDao, userId: String): Product = 
      Product(
        id = p.id.getOrElse(-1),
        name = p.name,
        description = p.description,
        price = p.price,
        tax = p.tax,
        discount = p.discount,
        userId = userId)

  given productDaoUniques: Unique[ProductDao, String] with
    def uniquesExtractor(p: ProductDao): String = p.name

