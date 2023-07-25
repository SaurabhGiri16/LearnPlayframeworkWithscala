package controllers

import models.{Stock, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.{existentials, postfixOps}

@Singleton
class DAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def addNewUser(user: User): Unit = {
    val query = sql"insert into users values(${user.id},${user.name},${user.about});".as[String]
    db.run(query)
  }

  def findAllUser(): List[User] = {
    val query = sql"select * from users ORDER BY id ASC;".as[(Int, String, String)]
    val queryResult: Future[Vector[(Int, String, String)]] = db.run(query)

    val vectorOfTuples: Vector[(Int, String, String)] = Await.result(queryResult, 5 seconds)
    vectorOfTuples.map {
      case (id, name, about) => User(id, name, about)
    }.toList
  }


  def findUserByUserId(id: Int): User = {
    val query = sql"select * from users where id = ${id};".as[(Int, String, String)]
    val queryResult: Future[Vector[(Int, String, String)]] = db.run(query)

    Await.result(queryResult, 5 seconds).map {
      case (id, name, about) => User(id, name, about)
    }.head
  }

  def deleteUserByUserId(id: Int): Unit = {
    val query = sql"DELETE from users where id = ${id};".as[String]
    db.run(query)
  }

  def updateUserByUserId(id: Int, user: User): Unit = {
    val query = sql"UPDATE users SET id= ${user.id}, name= ${user.name}, about= ${user.about} where id= ${id};".as[String]
    db.run(query)
  }

  def findById(id: Int): User = {
    val query = sql"select * from users where id = ${id};".as[(Int, String, String)]
    val user: Future[User] = db.run(query).map(_.head).map {
      case (id, name, about) => User(id, name, about)
    }
    Await.result(user, 5 seconds)
  }

  // slick database configure in stocks table
  private class StocksTable(tag: Tag) extends Table[Stock](tag, "stocks") {
    def id = column[Long]("ID", O.PrimaryKey)

    def symbol = column[String]("SYMBOL")

    def company = column[String]("COMPANY")

    def * = (id, symbol, company) <> (Stock.tupled, Stock.unapply)
  }

  //query generator
  private val Stocks = TableQuery[StocksTable]

  // create table if not exists
  db.run(Stocks.schema.createIfNotExists)

  //slick function calling
  def all(): Future[Seq[Stock]] = db.run(Stocks.result)
  def byId(id: Long): Future[Seq[Stock]] = db.run(Stocks.filter(_.id === id).result)
  def add(stock: Stock) = db.run(Stocks += stock)

}