package dev.silverest.invoicerback.entities

case class Address (
    street: String,
    city: String,
    zip: String)

case class PhoneNumber (
    number: String,
    countryCode: String)

case class Email(email: String)

case class ICENum(num: String)

sealed class Client (id: Long, address: Address, email: Email, phone: PhoneNumber)

case class Person(id: Long,
                  firstName: String,
                  lastName: String,
                  address: Address,
                  email: Email,
                  phone: PhoneNumber
                 ) extends Client(id, address, email, phone)

case class Company(id: Long,
                   name: String,
                   email: Email,
                   phone: PhoneNumber,
                   address: Address,
                   iceNumber: ICENum
                  ) extends Client (id, address, email, phone)