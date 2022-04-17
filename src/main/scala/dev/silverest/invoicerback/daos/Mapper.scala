package dev.silverest.invoicerback.daos

trait Mapper[A, B]:
  def modelMapper(a: A, userId: String): B
  extension (a: A) def mapToModel(userId: String): B = modelMapper(a, userId)

trait Unique[A, K]:
  def uniquesExtractor(a: A): K
  extension (a: A) def extractKeys: K = uniquesExtractor(a)
