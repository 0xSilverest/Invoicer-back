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
  val backendLayer: ZLayer[ZEnv, Nothing, CompanyRepository.Env] = CompanyRepository.live

  val endpoints =
    Http.collectZIO[Request] {
      case Method.GET -> _ / "companies" =>
        for {
          companies <- CompanyRepository.getAll
        } yield Response.json(companies.asJson.toString)

      case Method.GET -> _ / "company" / id =>
        for {
          company <- CompanyRepository.findById(id)
        } yield Response.json(company.asJson.toString)

      case Method.GET -> _ / "company" / name =>
        for {
          company <- CompanyRepository.findByName(name)
        } yield Response.json(company.asJson.toString)
        
      case request @ Method.POST -> _ / "company" / "add" =>
          for {
            eitherCompany <- request.bodyAsString.map(decode[Company])
            insComp <- eitherCompany match
                case Right(c) => CompanyRepository.insert(c)
                case Left(_) => ZIO.fail(new Exception("Invalid company"))
          } yield Response.json(insComp.asJson.toString)

      case Method.DELETE -> _ / "company" / "delete" / id =>
        for {
          _ <- CompanyRepository.delete(id)
        } yield Response.text("Deleted successfully")
        
      case request @ Method.PUT -> _ / "company" / "update" =>
        for {
          eitherCompany <- request.bodyAsString.map(decode[Company])
          updComp <- eitherCompany match {
              case Right(c) => CompanyRepository.update(c)
              case Left(e) => ZIO.fail(e)
            }
        } yield Response.json(updComp.asJson.toString)
    }
