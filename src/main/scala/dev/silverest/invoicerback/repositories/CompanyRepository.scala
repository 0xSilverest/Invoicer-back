package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.Client.Company

import java.io.Closeable
import javax.sql.DataSource

import io.getquill.context.ZioJdbc._
import io.getquill.util.LoadConfig
import io.getquill.context.qzio._
import io.getquill._

import zio._

object CompanyRepository:
  
  import io.getquill.context.qzio.ImplicitSyntax._

  val impDs: DataSource with Closeable = JdbcContextConfig(LoadConfig("ctx")).dataSource
  implicit val env: Implicit[Has[DataSource]] = Implicit(Has(impDs))

  trait Service:
    def insert(company: Company): Task[Long]
    def update(company: Company): Task[Long]
    def delete(name: String, userId: String): Task[Long]
    def all(userId: String): Task[List[Company]]
    def byName(name: String, userId: String): Task[List[Company]]

  type Env = Has[Service]

  inline def companies = quote { querySchema[Company]("Company") }

  val live: ZLayer[ZEnv, Nothing, Env] = ZLayer.succeed {
    import PostgresContext._
    new Service:
      override def insert(company: Company) =
        inline def insertQuery = quote { companies.insertValue(lift(company)) }
        for {
          id <- run(insertQuery).implicitDS
        } yield id

      override def all(userId: String) = 
        inline def allQuery = quote { companies.filter(c => c.userId == lift(userId)) }  
        for {
          cs <- run(companies).implicitDS
        } yield cs

      override def byName(name: String, userId: String) =
        inline def byNameQuery = quote {
          companies
            .filter(c => c.name == lift(name)
                      && c.userId == lift(userId))
        }
        for {
          cs <- run(byNameQuery).implicitDS
        } yield cs

      override def delete(name: String, userId: String) =
        inline def deleteQuery() = quote {
          companies
            .filter(c => c.name == lift(name)
                      && c.userId == lift(userId))
            .delete
        }
        for {
          l <- run(deleteQuery()).implicitDS
        } yield l

      override def update(company: Company) =
        inline def updateQuery() =
          quote {
            companies
              .filter(_.name == lift(company.name))
              .updateValue(lift(company))
          }
        for {
          l <- run(updateQuery()).implicitDS
        } yield l
  }

  def insert(company: Company): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.insert(company))

  def getAll(userId: String): ZIO[Env, Throwable, List[Company]] =
    ZIO.accessM(_.get.all(userId))

  def findByName(userId: String)(name: String): ZIO[Env, Throwable, List[Company]] =
    ZIO.accessM(_.get.byName(name, userId))

  def delete(userId: String)(name: String): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.delete(name, userId))

  def update(company: Company): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.update(company))
