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
    def delete(id: Int, userId: String): Task[Long]
    def all(userId: String): Task[List[Person]]
    //def byId(id: String, userId: String): Task[Long]
    def byName(firstName: String, lastName: String, userId: String): Task[List[Person]]
    def contains(firstName: String, lastName: String, userId: String): Task[Boolean]
    def containsId(id: Int, userId: String): Task[Boolean]
    def containsNot(firstName: String, lastName: String, userId: String): Task[Boolean]

  type Env = Has[Service]

  inline def persons = quote { querySchema[Person]("Person") }

  val live: ZLayer[ZEnv, Nothing, Env] = ZLayer.succeed {
    import PostgresContext._
    new Service:
      implicit inline def personInsertMeta: InsertMeta[Person] = insertMeta[Person](_.id)

      override def insert(person: Person) =
        inline def insertQuery = quote(persons.insertValue(lift(person)))
        for {
          id <- run(insertQuery).implicitDS
        } yield id

      override def all(userId: String) =
        inline def allQuery = quote(persons.filter(_.userId == lift(userId)))
        for {
          ps <- run(allQuery).implicitDS
        } yield ps

      override def byName(firstName: String, lastName: String, userId: String) =
        inline def byNameQuery = quote {
          persons.filter(p =>
            p.firstName == lift(firstName)
              && p.lastName == lift(lastName)
              && p.userId == lift(userId)
          )
        }
        for {
          ps <- run(byNameQuery).implicitDS
        } yield ps

      override def update(person: Person) =
        inline def updateQuery() = quote {
          persons
            .filter(_.id == lift(person.id)) 
            .filter(_.userId == lift(person.userId))
            .updateValue(lift(person))
        }
        for {
          id <- run(updateQuery()).implicitDS
        } yield id

      override def delete(id: Int, userId: String) =
        inline def deleteQuery() = quote {
          persons
            .filter(_.id == lift(id)) 
            .filter(_.userId == lift(userId))
            .delete
        }
        for {
          id <- run(deleteQuery()).implicitDS
        } yield id

      override def contains(firstName: String, lastName: String, userId: String) =
        inline def containsQuery() = quote {
          persons
            .filter(_.firstName == lift(firstName))
            .filter(_.lastName == lift(lastName)) 
            .filter(_.userId == lift(userId))
            .nonEmpty
        }
        for {
          b <- run(containsQuery()).implicitDS
        } yield b
        
      override def containsNot(firstName: String, lastName: String, userId: String) =
        inline def containsNotQuery() = quote {
          persons
            .filter(_.firstName == lift(firstName))
            .filter(_.lastName == lift(lastName)) 
            .filter(_.userId == lift(userId))
            .isEmpty
        }
        for {
          b <- run(containsNotQuery()).implicitDS
        } yield b

      override def containsId(id: Int, userId: String) =
        inline def containsQuery() = quote {
          persons
            .filter(_.id == lift(id))
            .filter(_.userId == lift(userId))
            .nonEmpty
        }
        for {
          b <- run(containsQuery()).implicitDS
        } yield b
  }

  def insert(person: Person): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.insert(person))

  def getAll(userId: String): ZIO[Env, Throwable, List[Person]] =
    ZIO.accessM(_.get.all(userId))

  def findByName(userId: String)(name: (String, String)): ZIO[Env, Throwable, List[Person]] =
    ZIO.accessM(_.get.byName(name._1, name._2, userId))

  def update(person: Person): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.update(person))

  def delete(id: Int, userId: String): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.delete(id, userId))

  def contains(params: (String, String), userId: String): ZIO[Env, Throwable, Boolean] =
    val (firstName, lastName) = params
    ZIO.accessM(_.get.contains(firstName, lastName, userId))

  def containsNot(params: (String, String), userId: String): ZIO[Env, Throwable, Boolean] =
    val (firstName, lastName) = params
    ZIO.accessM(_.get.containsNot(firstName, lastName, userId))

  def containsId(id: Int, userId: String): ZIO[Env, Throwable, Boolean] = 
    ZIO.accessM(_.get.containsId(id, userId))
