package dev.silverest.invoicerback.daos

import dev.silverest.invoicerback.models.*

case class PersonDao(
  id: String,
  firstName: String,
  lastName: String,
  address: Address,
  email: Email,
  phoneNumber: PhoneNumber):

  def toPerson(userId: String) =
    Person(id, firstName, lastName,
      address, email, phoneNumber, userId)
