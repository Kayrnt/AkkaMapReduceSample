package controllers

import play.api.mvc._

import anorm._

import views._
import models._
import scala.util.Random
import akka.actor.ActorSystem

object Application extends Controller {


  implicit val system: ActorSystem = ActorSystem("default")

  /**
   * This result directly redirect to the application home.
   */

  def Home = {
    val (websitesCount, totalViews) = mapReduceTotalViews
    Ok(html.index(websitesCount, totalViews))
  }

  // -- Actions

  /**
   * Handle default path requests
   */
  def index = Action {
    Home
  }

  def addRandom() = Action {

    def listWebsite(l: List[Website], r: Random, i: Int): List[Website] =
      if (i > 0) listWebsite(Website(r.nextInt(100000)) :: l, r, i - 1) else l

    val r: Random = new Random()
    val i = 10000
    Website.insertList(listWebsite(Nil, r, i))
    //redirect to home
    Home
  }

  def deleteAll() = Action {
    Website.deleteAll()
    Home
  }

  def mapReduceTotalViews: (Long, Long) = {

    val numberOfWebsites = Website.count()
    def keys(current: List[(Long, Long)], min: Long, max: Long, width: Long): List[(Long, Long)] =
      if (max - min > width) keys((min, min + width) :: current, min + width, max, width) else current

    (numberOfWebsites,
      new MapReduceSystem[(Long, Long), List[Website], Long]().mapReduce(
        //we do read rows, 100 by 100
        keys = keys(Nil, 0, numberOfWebsites, 100),
        map = Website.select,
        reduce = (l: List[Website]) => l.foldLeft(0L)((total, w) => total + w.views)
      ).sum)

  }

}
            
