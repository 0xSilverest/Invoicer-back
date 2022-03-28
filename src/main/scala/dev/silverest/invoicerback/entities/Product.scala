package dev.silverest.invoicerback.entities

case class Product(
    id: Long,
    name: String,
    description: String,
    price: Double,
    tax: Percent,
    discount: Percent)

case class OrderedProduct(
    product: Product,
    quantity: Int,
    totalPrice: Double,
    totalTax: Double)