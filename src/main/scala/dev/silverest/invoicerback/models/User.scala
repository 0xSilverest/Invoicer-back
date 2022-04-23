package dev.silverest.invoicerback.models

import io.circe.Decoder
import io.circe.Encoder
import io.getquill.MappedEncoding
import io.github.nremond.*

import dev.silverest.invoicerback.daos.Unique

case class User(
  username: String,
  email: Email,
  password: HashedPassword,
  role: Role,
  firstName: String,
  lastName: String):
  def validatePassword(passwordToValidate: String) =
    SecureHash.validatePassword(passwordToValidate, password.digest)

enum Role:
  case Admin
  case User

sealed abstract case class HashedPassword private (digest: String)

case class UserJwtDecode(username: String)

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
    MappedEncoding(_.toString)

  given quillRoleDecoder: MappedEncoding[String, Role] =
    MappedEncoding(Role.fromString(_).getOrElse(Role.User))

object HashedPassword:

  def empty: HashedPassword =
    new HashedPassword("") {}

  def fromString(password: String): Either[String, HashedPassword] =
    if password matches "(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}" then
      Right(new HashedPassword(SecureHash.createHash(password)) {})
    else Left("Password doesn't match the given pattern")

  given quillPasswordDecoder: MappedEncoding[String, HashedPassword] =
    MappedEncoding(new HashedPassword(_) {})

  given quillPasswordEncoder: MappedEncoding[HashedPassword, String] =
    MappedEncoding(_.digest)

  given circePasswordDecoder: Decoder[HashedPassword] =
    Decoder.decodeString.emap(HashedPassword.fromString)

  given circePasswordEncoder: Encoder[HashedPassword] =
    Encoder.encodeString.contramap(_.digest)

object User:

  given userUniqueId: Unique[User, String] with
    def uniquesExtractor(u: User) = u.username
