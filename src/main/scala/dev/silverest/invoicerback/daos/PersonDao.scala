package dev.silverest.invoicerback.daos

import dev.silverest.invoicerback.models.*

case class PersonDao(
  id: Option[Int],
  firstName: String,
  lastName: String,
  address: Address,
  email: Email,
  phoneNumber: PhoneNumber)

object PersonDao:
  given personDaoMapper: Mapper[PersonDao, Client.Person] with
    def modelMapper(p: PersonDao, userId: String): Client.Person =
      Person(p.id.getOrElse(-1), p.firstName, p.lastName, p.address,
             p.email, p.phoneNumber, userId)

  given personDaoUniques: Unique[PersonDao, (String, String)] with
    def uniquesExtractor(p: PersonDao): (String, String) = 
      (p.firstName, p.lastName)

  given personDaoId: Unique[PersonDao, Int] with
    def uniquesExtractor(p: PersonDao): Int = 
      p.id.getOrElse(-1)
