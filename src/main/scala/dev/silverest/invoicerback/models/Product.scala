package dev.silverest.invoicerback.models

case class Product(id: String,
                   name: String,
                   description: String,
                   price: Double,
                   tax: Percent,
                   discount: Percent,
                   userId: String)

case class OrderedProduct(product: Product, quantity: Int, totalPrice: Double, totalTax: Double)