package dev.silverest.invoicerback.handlers

import zhttp.http.*
import zio.*
import io.circe.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import pdi.jwt.JwtClaim

import dev.silverest.invoicerback.repositories.ProductRepository
import dev.silverest.invoicerback.models._
import dev.silverest.invoicerback.daos.ProductDao

import utils.*

class ProjectHandler:
  private type RepEnv = ProductRepository.Env

  val backendLayer: ZLayer[ZEnv, Nothing, RepEnv] = ProductRepository.live

  def endpoints (userDecoded: UserJwtDecode) = 
    val userId = userDecoded.username
    Http.collectZIO[Request] {
      case Method.GET -> _ / "products" => getAll(userId)

      case Method.GET -> _ / "product" / id =>
        id.toLongOption
          .fold(ZIO.succeed(HttpError.BadRequest("")))(get (userId, _))

      case request @ Method.POST -> _ / "product" => insertProduct(userId, request)

      case request @ Method.PUT -> _ / "product" => updateProduct(userId, request)

      case Method.DELETE -> _ / "product" / id => 
        id.toLongOption
          .fold(ZIO.succeed(HttpError.BadRequest("")))(delete (userId, _))
    }

  def getAll (userId: String) = 
    for {
      products <- ProductRepository.getAll(userId)
    } yield Response.json(products.asJson.toString)

  def get (userId: String, id: Long) =
    for {
      product <- ProductRepository.findById(id, userId)
    } yield Response.json(product.asJson.toString)

  def delete(userId: String, id: Long) =
    for {
      _ <- ProductRepository.delete(id, userId)
    } yield Response.ok

  def insertProduct = nonExistenceCheckerPartial(ProductRepository.insert)

  def updateProduct = existenceCheckerPartial(ProductRepository.update)

  private def nonExistenceCheckerPartial(repositoryAction: Product => ZIO[RepEnv, Throwable, Long])
    (userId: String, request: Request) =
      nonExistenceChecker(request, userId)
        .flatMap {
          case p: Product => HandlerUtils.successActionHandler(repositoryAction)(p)
          case e: HttpError => ZIO.succeed(Response.fromHttpError(e))
        }

  private def nonExistenceChecker = 
    HandlerUtils.genericExistenceChecker[ProductDao, Product, String, RepEnv](
      ProductRepository.containsNot,
      HttpError.Conflict("Product already exists")
    )

  private def existenceCheckerPartial(repositoryAction: Product => ZIO[RepEnv, Throwable, Long])
                          (userId: String, request: Request) =
    existenceChecker(request, userId)
      .flatMap {
        case p: Product => HandlerUtils.successActionHandler(repositoryAction)(p)
        case e: HttpError => ZIO.succeed(Response.fromHttpError(e))
      }

  private def existenceChecker =
    HandlerUtils.genericExistenceChecker[ProductDao, Product, String, RepEnv](
      ProductRepository.contains,
      HttpError.BadRequest("Product not found")
    )
