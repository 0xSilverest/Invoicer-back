package dev.silverest.invoicerback.entities

case class Invoice (
    idClient: Long,
    products: List[OrderedProduct],
    idBonDeCommande: Option[String])
