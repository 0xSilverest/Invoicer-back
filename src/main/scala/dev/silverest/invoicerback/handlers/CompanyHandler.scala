package dev.silverest.invoicerback.handlers

import zhttp.http._
import zio._

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._

import dev.silverest.invoicerback.models.Company
import dev.silverest.invoicerback.models.Client.Company
import dev.silverest.invoicerback.repositories.CompanyRepository
import javax.sql.DataSource
import java.util.UUID

class CompanyHandler:
  val companyBackendLayer: ZLayer[ZEnv, Nothing, CompanyRepository.CompanyRepositoryEnv] = CompanyRepository.live

  val endpoints =
    Http.collectZIO[Request] {
      case Method.GET -> _ / "clients" / "all" =>
        for {
          companies <- CompanyRepository.getAll
        } yield Response.json(companies.asJson.toString)

      case Method.GET -> _ / "clients" / id =>
        for {
          company <- CompanyRepository.getById(id)
        } yield Response.json(company.asJson.toString)
        
      case request @ Method.POST -> _ / "clients" / "add" =>
          for {
            company <- request.bodyAsString.map(decode[Company])
            insComp <- company match {
                case Right(c) => CompanyRepository.insert(c)
                case Left(e) => ZIO.fail(e)
              }
          } yield Response.json(insComp.asJson.toString)
      //case Method.DELETE -> _ / "clients" / "delete" / id => Response.json("")
      //case Method.PUT -> _ / "clients" / "update" / id => Response.json("")
    }
