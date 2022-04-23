package dev.silverest.invoicerback.models

enum Client (id: Int, address: Address, email: Email, phoneNumber: PhoneNumber, userId: String):
  case Person(id: Int, firstName: String, lastName: String, address: Address,
              email: Email, phoneNumber: PhoneNumber, userId: String) extends Client(id, address, email, phoneNumber, userId)

  case Company(id: Int, name: String, email: Email, phoneNumber: PhoneNumber,
               address: Address, iceNumber: ICENumber, userId: String) extends Client (id, address, email, phoneNumber, userId)

object Person:
  def apply(id: Int, firstName: String, lastName: String, address: Address,
            email: Email, phone: PhoneNumber, userId: String): Client.Person =
    new Client.Person(id, firstName, lastName, address, email, phone, userId)

  case class PersonWithoutId(firstName: String, lastName: String, address: Address,
              email: Email, phoneNumber: PhoneNumber, userId: String)

object Company:
  def apply(id: Int, name: String, email: Email, phone: PhoneNumber,
            address: Address, iceNumber: ICENumber, userId: String): Client.Company =
    new Client.Company(id, name, email, phone, address, iceNumber, userId)
