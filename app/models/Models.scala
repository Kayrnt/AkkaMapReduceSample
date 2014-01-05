package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._

import scala.language.postfixOps

case class Website(views: Long)

object Website {


  def count() = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          select count(*) as c from website
          """
        ).apply().head[Long]("c")
    }
  }

  def select(range: (Long,Long)) : List[Website] = {
    val from = range._1
    val to = range._2
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          select views from website
          LIMIT {from}, {to}
          """
        ).on("from" -> from,
            "to" -> to)
          .apply().map(row =>
          Website(row[Long]("views"))
          ).toList
    }
  }

  def insertList(list: List[Website]) = {
    DB.withConnection { implicit connection =>
      val insertQuery = SQL("insert into website(views) values ({views})")
      val batchInsert = (insertQuery.asBatch /: list)(
        (sql, elem) => sql.addBatchParams(elem.views)
      )
      batchInsert.execute()
    }
  }

  def deleteAll() = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from website ").executeUpdate()
    }
  }

}