package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.Client.Company

import java.sql.SQLException
import H2Context.lift

import io.getquill.util.LoadConfig
import io.getquill.context.qzio._
import io.getquill._
import zio._

import java.io.Closeable
import javax.sql.DataSource

import io.getquill.context.ZioJdbc._

object CompanyRepository:
  
  import io.getquill.context.qzio.ImplicitSyntax._

  val impDs: DataSource with Closeable = JdbcContextConfig(LoadConfig("ctx")).dataSource
  implicit val env: Implicit[Has[DataSource]] = Implicit(Has(impDs))

  inline def companies = quote {querySchema[Company]("Company")}

  trait Service {
    def insert(company: Company): Task[Long]
    def getAll: Task[List[Company]]
    def byName(name: String): Task[List[Company]]
    def byId(id: String): Task[Option[Company]]
  }

  type CompanyRepositoryEnv = Has[CompanyRepository.Service]

  val live: ZLayer[ZEnv, Nothing, CompanyRepositoryEnv] = ZLayer.succeed {
    import H2Context._
    new Service:
      override def insert(company: Company): Task[Long] = {
        inline def insertQuery = quote { companies.insertValue(lift(company)) }
        for {
          i <- run(insertQuery).implicitDS
        } yield i
      }

      override def getAll: Task[List[Company]] = for {
        cs <- run(companies).implicitDS
      } yield cs

      override def byName(name: String): Task[List[Company]] = {
        inline def filterQuery = quote {
          query[Company].filter(c => c.name == lift(name))
        }
        for {
          cs <- run(filterQuery).implicitDS
        } yield cs
      }

      override def byId(id: String): Task[Option[Company]] = {
        inline def filterQuery = quote {
          query[Company].filter(c => c.id == lift(id))
        }
        for {
          cs <- run(filterQuery).implicitDS
        } yield cs.headOption
      }
  }
 
  def insert(company: Company): ZIO[CompanyRepositoryEnv, Throwable, Long] =
    ZIO.accessM(_.get.insert(company))

  def getAll: ZIO[CompanyRepositoryEnv,Throwable,List[Company]] =
    ZIO.accessM(_.get.getAll)

  def getByName(name: String): ZIO[CompanyRepositoryEnv,Throwable,List[Company]] =
    ZIO.accessM(_.get.byName(name))
  
  def getById(id: String): ZIO[CompanyRepositoryEnv,Throwable,Option[Company]] =
    ZIO.accessM(_.get.byId(id))
