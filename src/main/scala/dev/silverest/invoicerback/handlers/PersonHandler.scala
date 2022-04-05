package dev.silverest.invoicerback.handlers

import dev.silverest.invoicerback.models.Person
import dev.silverest.invoicerback.models.Client.Person
import dev.silverest.invoicerback.repositories.PersonRepository
import dev.silverest.invoicerback.handlers.utils.HandlerUtils

import zhttp.http._
import zio._

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._

class PersonHandler:
  val backendLayer: ZLayer[ZEnv, Nothing, PersonRepository.Env] = PersonRepository.live

  val endpoints =
    Http.collectZIO[Request] {
      case Method.GET -> _ / "persons" =>
        for {
          persons <- PersonRepository.getAll
        } yield Response.json(persons.asJson.toString)
    
      case Method.GET -> _ / "person" / id =>
        for {
          person <- PersonRepository.findById(id)
        } yield Response.json(person.asJson.toString)

      case Method.GET -> _ / "person" / firstName / lastName =>
        for {
          person <- PersonRepository.findByName(firstName, lastName)
        } yield Response.json(person.asJson.toString)

      case request @ Method.POST -> _ / "person" / "add" =>
        HandlerUtils.successActionRequest(PersonRepository.insert)(request)

      case Method.DELETE -> _ / "person" / id =>
        for {
          _ <- PersonRepository.delete(id)
        } yield Response.text(s"$id deleted")

      case request @ Method.PUT -> _ / "person" / "update" =>
        HandlerUtils.successActionRequest(PersonRepository.update)(request)
    }