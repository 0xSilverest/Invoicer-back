package dev.silverest.invoicerback.models

import io.circe.Decoder
import io.getquill.MappedEncoding
import io.circe.Encoder

type Percent = Double

case class Address (street: String, city: String, zip: String)

sealed abstract case class PhoneNumber private (phoneNumber: String):
  override def toString: String = phoneNumber

sealed abstract case class Email private (email: String):
  override def toString: String = email

sealed abstract case class ICENumber private (iceNumber: String):
  override def toString: String = iceNumber

object Address:
    def empty: Address =
        Address(
          street = "",
          city = "",
          zip = ""
        )


object ICENumber:
  def empty: ICENumber =
    new ICENumber ("") {}

  private inline def iceNumberRegex(iceNumber: String): Boolean =
    iceNumber matches "[0-9]{15}"

  def fromString (iceNumber: String): Either[String, ICENumber] =
    if iceNumberRegex(iceNumber) then
      Right(new ICENumber(iceNumber) {})
    else
      Left("ICE number must be 15 characters long")

  given circeICENumberEncoder: Encoder[ICENumber] =
    Encoder.encodeString.contramap[ICENumber](_.iceNumber)

  given circeICENumberDecoder: Decoder[ICENumber] =
    Decoder.decodeString.emap[ICENumber](ICENumber.fromString)

  given quillICENumberEncoder: MappedEncoding[ICENumber, String] =
    MappedEncoding[ICENumber, String](_.toString)

  given quillICENumberDecoder: MappedEncoding[String, ICENumber] =
    MappedEncoding[String, ICENumber](
      ICENumber.fromString(_)
        .getOrElse(ICENumber.empty))


object PhoneNumber:
  def empty: PhoneNumber =
    new PhoneNumber("") {}

  private inline def phoneNumberRegex(phoneNumber: String): Boolean =
    phoneNumber matches "0(6|7)[0-9]{8}"

  def fromString (phoneNumber: String): Either[String, PhoneNumber] =
    if phoneNumberRegex(phoneNumber)  then
      Right(new PhoneNumber(phoneNumber) {})
    else
      Left("Phone number must be 10 numbers starting with 06 or 07")

  given circePhoneNumberEncoder: Encoder[PhoneNumber] =
    Encoder.encodeString.contramap[PhoneNumber](_.phoneNumber)

  given circePhoneNumberDecoder: Decoder[PhoneNumber] =
    Decoder.decodeString.emap[PhoneNumber](PhoneNumber.fromString)

  given quillPhoneNumberEncoder: MappedEncoding[PhoneNumber, String] =
    MappedEncoding[PhoneNumber, String](_.toString)

  given quillPhoneNumberDecoder: MappedEncoding[String, PhoneNumber] =
    MappedEncoding[String, PhoneNumber](
      PhoneNumber.fromString(_)
        .getOrElse(PhoneNumber.empty))


object Email:
  def empty: Email =
    new Email("") {}

  private inline def emailRegex(email: String): Boolean =
    email matches "(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)"

  def fromString (email: String): Either[String, Email] =
    if emailRegex(email) then
      Right(new Email(email) {})
    else Left(s"Email $email is not valid")

  given circeEmailEncoder: Encoder[Email] =
    Encoder.encodeString.contramap[Email](_.email)

  given circeEmailDecoder: Decoder[Email] = 
    Decoder.decodeString.emap[Email](Email.fromString)

  given quillEmailEncoder: MappedEncoding[Email, String] =
     MappedEncoding[Email, String](_.toString)

  given quillEmailDecoder: MappedEncoding[String, Email] = 
    MappedEncoding[String, Email](Email.fromString(_).getOrElse(Email.empty))
