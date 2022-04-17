package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.Product

import java.io.Closeable
import javax.sql.DataSource

import io.getquill._
import context.ZioJdbc._
import util.LoadConfig

import zio._

object ProductRepository:

  import context.qzio.ImplicitSyntax._

  val impDs: DataSource with Closeable = JdbcContextConfig(LoadConfig("ctx")).dataSource
  implicit val env: Implicit[Has[DataSource]] = Implicit(Has(impDs))

  trait Service:
    def insert(product: Product): Task[Long]
    def update(product: Product): Task[Long]
    def delete(id: String, userId: String): Task[Long]
    def byName(pattern: String): Task[List[Product]]
    def all(userId: String): Task[List[Product]]
    def byId(id: String): Task[Option[Product]]

  type Env = Has[Service]

  inline def products = quote { querySchema[Product]("Product") }

  val live: ZLayer[ZEnv, Nothing, Env] = ZLayer.succeed {
    import PostgresContext._
    new Service:
      implicit inline def productInsertMeta: InsertMeta[Product] = insertMeta[Product](_.id)

      override def insert(product: Product) =
        inline def insertQuery = quote(products.insertValue(lift(product)))
        for {
          id <- run(insertQuery).implicitDS
        } yield id

      override def update(product: Product) =
        inline def updateQuery = quote {
          products
            .filter(_.id == lift(product.id))
            .updateValue(lift(product))
        }
        for {
          id <- run(updateQuery).implicitDS
        } yield id

      override def all(userId: String) =
        inline def allQuery = quote {
          products
            .filter(_.userId == lift(userId))
        }
        for {
          products <- run(allQuery).implicitDS
        } yield products

      override def byName(pattern: String) =
        inline def byName = quote {
          products
            .filter(_.name == lift(pattern))
        }
        for {
          products <- run(byName).implicitDS
        } yield products

      override def byId(id: String) =
        inline def byIdQuery = quote {
          products.filter(_.id == lift(id))
        }
        for {
          products <- run(byIdQuery).implicitDS
        } yield products.headOption

      override def delete(id: String, userId: String) =
        inline def deleteQuery = quote {
          products
            .filter(p => p.id == lift(id) && p.userId == lift(userId))
            .delete
        }
        for {
          id <- run(deleteQuery).implicitDS
        } yield id
  }

  def insert(product: Product): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.insert(product))

  def update(product: Product): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.update(product))

  def delete(id: String, userId: String): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.delete(id, userId))

  def findById(id: String, userId: String): ZIO[Env, Throwable, Option[Product]] =
    ZIO.accessM(_.get.byId(id))

  def all(userId: String): ZIO[Env, Throwable, List[Product]] =
    ZIO.accessM(_.get.all(userId))

  def hasNameContaining(userId: String, pattern: String): ZIO[Env, Throwable, List[Product]] =
    all(userId).map(_.filter(_.name contains pattern))

