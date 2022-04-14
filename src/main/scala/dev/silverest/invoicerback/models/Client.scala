package dev.silverest.invoicerback.models

enum Client (address: Address, email: Email, phoneNumber: PhoneNumber, userId: String):
  case Person(id: String, firstName: String, lastName: String, address: Address,
              email: Email, phoneNumber: PhoneNumber, userId: String) extends Client(address, email, phoneNumber, userId)

  case Company(name: String, email: Email, phoneNumber: PhoneNumber,
               address: Address, iceNumber: ICENumber, userId: String) extends Client (address, email, phoneNumber, userId)

object Person:
  def apply(id: String, firstName: String, lastName: String, address: Address,
            email: Email, phone: PhoneNumber, userId: String): Client.Person =
    new Client.Person(id: String, firstName, lastName, address, email, phone, userId)

object Company:
  def apply(name: String, email: Email, phone: PhoneNumber,
            address: Address, iceNumber: ICENumber, userId: String): Client.Company =
    new Client.Company(name, email, phone, address, iceNumber, userId)
