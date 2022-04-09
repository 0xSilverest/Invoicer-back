package dev.silverest.invoicerback.models

case class Invoice (idClient: String,
                    products: List[OrderedProduct],
                    idBonDeCommande: Option[String])
