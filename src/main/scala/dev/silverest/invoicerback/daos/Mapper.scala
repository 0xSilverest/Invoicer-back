package dev.silverest.invoicerback.daos

trait Mapper[A, B]:
  def modelMapper(a: A, userId: String): B
  extension (a: A) def mapToModel(userId: String): B = modelMapper(a, userId)
