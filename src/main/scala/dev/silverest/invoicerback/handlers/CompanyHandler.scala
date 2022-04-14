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
import dev.silverest.invoicerback.daos.CompanyDao
import dev.silverest.invoicerback.models.UserJwtDecode
import dev.silverest.invoicerback.repositories.CompanyRepository
import dev.silverest.invoicerback.services.Authenticator
import pdi.jwt.JwtClaim

import javax.sql.DataSource
import java.util.UUID

class CompanyHandler:
  private type Env = CompanyRepository.Env

  val backendLayer: ZLayer[ZEnv, Nothing, Env] = CompanyRepository.live

  private val authenticator = Authenticator()

  def endpoints(jwtDecoded: UserJwtDecode) =
    Http.collectZIO[Request] {
      case Method.GET -> _ / "companies" =>
        for {
          companies <- CompanyRepository.getAll(jwtDecoded.username)
        } yield Response.json(companies.asJson.toString)

      case Method.GET -> _ / "company" / name =>
        HandlerUtils.genericGetRequest(CompanyRepository.findByName(jwtDecoded.username))(name)

      case request @ Method.POST -> _ / "company" / "add" =>
        HandlerUtils.successActionRequest[CompanyDao, Company, Env](CompanyRepository.insert)(request, jwtDecoded.username)

      case Method.DELETE -> _ / "company" / "delete" / name =>
        for {
          _ <- CompanyRepository.delete(jwtDecoded.username)(name)
        } yield Response.text("")

      case request @ Method.PUT -> _ / "company" / "update" =>
        HandlerUtils.successActionRequest[CompanyDao, Company, Env](CompanyRepository.update)(request, jwtDecoded.username)
    }
