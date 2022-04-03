package dev.silverest.invoicerback.models

case class Invoice (idClient: Long, products: List[OrderedProduct], idBonDeCommande: Option[String])
