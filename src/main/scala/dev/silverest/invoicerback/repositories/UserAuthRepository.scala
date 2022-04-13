package dev.silverest.invoicerback.repositories

import dev.silverest.invoicerback.models.User
import dev.silverest.invoicerback.models.Email

import java.io.Closeable
import javax.sql.DataSource

import io.getquill.context.ZioJdbc._
import io.getquill.util.LoadConfig
import io.getquill.context.qzio._
import io.getquill._

import zio._

object UserAuthRepository:

  import io.getquill.context.qzio.ImplicitSyntax._

  val impDs: DataSource with Closeable = JdbcContextConfig(LoadConfig("ctx")).dataSource
  implicit val env: Implicit[Has[DataSource]] = Implicit(Has(impDs))

  trait Service:
    def insert(user: User): Task[Long]
    def update(user: User): Task[Long]
    def delete(username: String): Task[Long]
    def findById(Id: String): Task[Option[User]]
    def findByUsername(username: String): Task[Option[User]]
    def findByEmail(email: Email): Task[Option[User]]
    def all: Task[List[User]]

  type Env = Has[Service]

  inline def users = quote { querySchema[User]("Users") }

  val live: ZLayer[ZEnv, Nothing, Env] = ZLayer.succeed {
    import PostgresContext._
    new Service:
      override def insert(user: User) =
        inline def insertQuery = quote { users.insertValue(lift(user)) }
        for {
          id <- run(insertQuery).implicitDS
        } yield id

      override def update(user: User) =
        inline def updateQuery =
          quote {
            users
              .filter(_.id == lift(user.id))
              .updateValue(lift(user))
          }
        for {
          id <- run(updateQuery).implicitDS
        } yield id

      override def delete(username: String) =
        inline def deleteQuery = quote { users.filter(_.username == lift(username)).delete }
        for {
          id <- run(deleteQuery).implicitDS
        } yield id

      override def findById(id: String) =
        inline def findByIdQuery =
          quote { users.filter(_.id == lift(id)).take(1) }
        for {
          user <- run(findByIdQuery).implicitDS
        } yield user.headOption

      override def findByUsername(username: String) =
        inline def findByUsernameQuery =
          quote { users.filter(_.username == lift(username)) }
        for {
          user <- run(findByUsernameQuery).implicitDS
        } yield user.headOption

      override def findByEmail(email: Email) =
        inline def findByEmailQuery = quote { users.filter(_.email == lift(email)).take(1) }
        for {
          user <- run(findByEmailQuery).implicitDS
        } yield user.headOption

      override def all =
        for {
          users <- run(users).implicitDS
        } yield users
  }

  def insert(user: User): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.insert(user))

  def getAll: ZIO[Env, Throwable, List[User]] =
    ZIO.accessM(_.get.all)

  def update(user: User): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.update(user))

  def delete(username: String): ZIO[Env, Throwable, Long] =
    ZIO.accessM(_.get.delete(username))

  def findById(id: String): ZIO[Env, Throwable, Option[User]] =
    ZIO.accessM(_.get.findById(id))

  def findByUsername(username: String): ZIO[Env, Throwable, Option[User]] =
    ZIO.accessM(_.get.findByUsername(username))

  def findByEmail(email: Email): ZIO[Env, Throwable, Option[User]] =
    ZIO.accessM(_.get.findByEmail(email))
