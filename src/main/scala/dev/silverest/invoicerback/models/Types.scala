package dev.silverest.invoicerback.models

type Percent = Double

case class Address (street: String, city: String, zip: String)

case class PhoneNumber (phoneNumber: String, countryCode: String)

sealed abstract case class Email private (email: String)

case class ICENum(iceNumber: String)

object Address:
    def empty: Address =
        Address(
          street = "",
          city = "",
          zip = ""
        )


object ICENum:
    def empty: ICENum =
        ICENum(
          iceNumber = ""
        )


object PhoneNumber:
    def empty: PhoneNumber =
        PhoneNumber(
          phoneNumber = "",
          countryCode = ""
        )

object Email:

  def fromString (email: String): Option[Email] =
    if email.matches("(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)") then
      Some(new Email(email) {})
    else None