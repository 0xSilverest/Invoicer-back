package dev.silverest.invoicerback.models

import io.circe.Decoder
import io.getquill.MappedEncoding
import io.circe.Encoder

case class User (
    id: String,
    username: String,
    email: Email,
    password: String,
    role: Role,
    firstName: String,
    lastName: String)

enum Role:
  case Admin
  case User

object Role:
  def fromString (role: String): Either[String, Role] =
    role.toLowerCase match
      case "admin" => Right(Admin)
      case "user" => Right(User)
      case _ => Left("Invalid role")

  given circeRoleEncoder: Encoder[Role] =
    Encoder.encodeString.contramap(_.toString)

  given circeRoleDecoder: Decoder[Role] =
    Decoder.decodeString.emap(Role.fromString)

  given quillRoleEncoder: MappedEncoding[Role, String] =
    MappedEncoding(role => role.toString)

  given quillRoleDecoder: MappedEncoding[String, Role] =
    MappedEncoding(Role.fromString(_).getOrElse(Role.User))