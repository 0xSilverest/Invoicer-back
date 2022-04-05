package dev.silverest.invoicerback.handlers

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import dev.silverest.invoicerback.handlers.utils.HandlerUtils
import dev.silverest.invoicerback.models.Company
import dev.silverest.invoicerback.models.Client.Company
import dev.silverest.invoicerback.repositories.CompanyRepository

import javax.sql.DataSource
import java.util.UUID

class CompanyHandler:
  val backendLayer: ZLayer[ZEnv, Nothing, CompanyRepository.Env] = CompanyRepository.live

  val endpoints =
    Http.collectZIO[Request] {
      case Method.GET -> _ / "companies" =>
        for {
          companies <- CompanyRepository.getAll
        } yield Response.json(companies.asJson.toString)

      case Method.GET -> _ / "company" / id =>
        HandlerUtils.genericGetRequest(CompanyRepository.findById)(id)

      case Method.GET -> _ / "company" / "name" / name =>
        HandlerUtils.genericGetRequest(CompanyRepository.findByName)(name)

      case request @ Method.POST -> _ / "company" / "add" =>
        HandlerUtils.successActionRequest(CompanyRepository.insert)(request)

      case Method.DELETE -> _ / "company" / "delete" / id =>
        for {
          _ <- CompanyRepository.delete(id)
        } yield Response.text(s"$id deleted")

      case request @ Method.PUT -> _ / "company" / "update" =>
        HandlerUtils.successActionRequest(CompanyRepository.update)(request)
    }
