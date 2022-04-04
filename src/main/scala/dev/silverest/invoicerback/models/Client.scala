package dev.silverest.invoicerback.models

enum Client (id: String, address: Address, email: Email, phoneNumber: PhoneNumber):
  case Person(id: String, firstName: String, lastName: String, address: Address,
              email: Email, phoneNumber: PhoneNumber) extends Client(id, address, email, phoneNumber)

  case Company(id: String, name: String, email: Email, phoneNumber: PhoneNumber,
               address: Address, iceNumber: ICENumber) extends Client (id, address, email, phoneNumber)

object Person:
  def apply(id: String, firstName: String, lastName: String, address: Address,
            email: Email, phone: PhoneNumber): Client.Person =
    new Client.Person(id, firstName, lastName, address, email, phone)

object Company:
  def apply(id: String, name: String, email: Email, phone: PhoneNumber,
            address: Address, iceNumber: ICENumber): Client.Company =
    new Client.Company(id, name, email, phone, address, iceNumber)
