package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.Client.Person

import java.io.Closeable
import javax.sql.DataSource
import io.getquill.context.ZioJdbc.*
import io.getquill.util.LoadConfig
import io.getquill.context.qzio.*
import io.getquill.*
import zio.*

object PersonRepository:

  import io.getquill.context.qzio.ImplicitSyntax._

  val impDs: DataSource with Closeable = JdbcContextConfig(LoadConfig("ctx")).dataSource
  implicit val env: Implicit[Has[DataSource]] = Implicit(Has(impDs))

  trait Service:
    def insert(person: Person): Task[Long]
    def update(person: Person): Task[Long]
    def delete(id: String): Task[Long]
    def all(userId: String): Task[List[Person]]
    def byName(firstName: String, lastName: String): Task[List[Person]]
    def byId(id: String): Task[Option[Person]]

  type Env = Has[Service]

  inline def persons = quote { querySchema[Person]("Person") }

  val live: ZLayer[ZEnv, Nothing, Env] = ZLayer.succeed {
    import H2Context._
    new Service:
      override def insert(person: Person) =
        inline def insertQuery = quote(persons.insertValue(lift(person)))
        for {
          id <- run(insertQuery).implicitDS
        } yield id

      override def all(id: String) =
        inline def allQuery = quote(persons.filter(_.userId == lift(id)))
        for {
          ps <- run(allQuery).implicitDS
        } yield ps

      override def byName(firstName: String, lastName: String) =
        inline def byNameQuery = quote {
          persons.filter(p =>
            p.firstName == lift(firstName)
              && p.lastName == lift(lastName)
          )
        }
        for {
          ps <- run(byNameQuery).implicitDS
        } yield ps

      override def byId(id: String) =
        inline def byIdQuery = quote {
          persons.filter(p => p.id == lift(id))
        }
        for {
          ps <- run(byIdQuery).implicitDS
        } yield ps.headOption

      override def update(person: Person) =
        inline def updateQuery() = quote {
          persons
            .filter(_.id == lift(person.id))
            .updateValue(lift(person))
        }
        for {
          id <- run(updateQuery()).implicitDS
        } yield id

      override def delete(id: String) =
        inline def deleteQuery() = quote {
          persons
            .filter(_.id == lift(id))
            .delete
        }
        for {
          id <- run(deleteQuery()).implicitDS
        } yield id
  }

  def insert(person: Person): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.insert(person))

  def getAll(userId: String): ZIO[Env, Throwable, List[Person]] =
    ZIO.accessM(_.get.all(userId))

  def findByName(firstName: String, lastName: String): ZIO[Env, Throwable, List[Person]] =
    ZIO.accessM(_.get.byName(firstName, lastName))

  def findById(id: String): ZIO[Env, Throwable, Option[Person]] =
    ZIO.accessM(_.get.byId(id))

  def update(person: Person): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.update(person))

  def delete(id: String): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.delete(id))