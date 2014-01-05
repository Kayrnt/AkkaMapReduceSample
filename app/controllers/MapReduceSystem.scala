package controllers

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.{ Await, Promise, Future }
import scala.concurrent.duration._
import akka.routing.RoundRobinRouter
import org.slf4j.{ LoggerFactory, Logger }

/**
 * User: kayrnt
 * Date: 05/01/2014
 * Time: 16:23
 */

class MapReduceSystem[T, U, V] {

  case class StatsQuery(key: T)

  case class StatsProcess(item: U)

  case class TotalProcessResult(result: V)

  case class ExpectedResultModifier(count: Int)

  case object StatsRetrievalTimeout

  /**
   * *****************
   * MAP ACTOR
   *
   *
   * @param reduceActor
   * @param resultActor
   */

  class MapActor(val reduceActor: ActorRef, val resultActor: ActorRef, map: T ⇒ U) extends Actor {

    def receive = LoggingReceive {
      case StatsQuery(key) ⇒ {
        reduceActor ! StatsProcess(map(key))
        resultActor ! ExpectedResultModifier(1)
        self ! PoisonPill
      }
    }
  }

  object MapActor {

    def props(reduceActor: ActorRef, resultActor: ActorRef, map: T ⇒ U): Props = {
      Props(new MapActor(reduceActor, resultActor, map))
    }

  }

  /**
   * *****************
   * REDUCE ACTOR
   *
   *
   * @param resultActor
   */

  class ReduceActor(val resultActor: ActorRef, reduce: U ⇒ V) extends Actor {

    import context.dispatcher

    def receive = LoggingReceive {
      case StatsProcess(item) ⇒ {
        Future {
          resultActor ! TotalProcessResult(reduce(item))
        }
      }
    }

    val timeoutMessager = context.system.scheduler.
      scheduleOnce(1.hours) {
        self ! PoisonPill
      }
  }

  object ReduceActor {
    def props(resultActor: ActorRef, reduce: U ⇒ V): Props = {
      Props(new ReduceActor(resultActor, reduce))
    }
  }

  class ResponseActor(var expectedMapNumber: Int, promise: Promise[List[V]]) extends Actor with ActorLogging {

    var fullTotalStats: List[V] = List()
    var expectedReduceNumber = 0

    def receive = LoggingReceive {

      case TotalProcessResult(result) ⇒
        expectedReduceNumber -= 1
        fullTotalStats = fullTotalStats ++ Seq[V](result)
        collectResults()

      case ExpectedResultModifier(count) ⇒
        expectedReduceNumber += count
        expectedMapNumber -= 1
        collectResults()

    }

    def collectResults() = {
      if (expectedReduceNumber == 0 && expectedMapNumber == 0) {
        timeoutMessager.cancel()
        promise.success(fullTotalStats)
        self ! PoisonPill
      }
    }

    import context.dispatcher

    val timeoutMessager = context.system.scheduler.
      scheduleOnce(1.hours) {
        promise.failure(new Throwable("Failed map reduce"))
        self ! PoisonPill
      }

    if (expectedReduceNumber == 0 && expectedMapNumber == 0) {
      timeoutMessager.cancel()
      promise.success(fullTotalStats)
      self ! PoisonPill
    }
  }

  object ResponseActor {

    val log: Logger = LoggerFactory.getLogger(this.getClass)

    def props(expectedQueryNumber: Int, promise: Promise[List[V]]): Props = {
      Props(new ResponseActor(expectedQueryNumber, promise))
    }

  }

  def mapReduce(keys: List[T], map: T ⇒ U, reduce: U ⇒ V)(implicit system: ActorSystem): List[V] = {

    val p = Promise[List[V]]()

    val resultActor = system.actorOf(ResponseActor.props(keys.length, p))

    val reduceActors = system.actorOf(ReduceActor.props(resultActor, reduce).withRouter(RoundRobinRouter(10)))

    val mapActors = system.actorOf(MapActor.props(reduceActors, resultActor, map).withRouter(RoundRobinRouter(100)))

    keys.map {
      key ⇒
        mapActors ! StatsQuery(key)
    }

    Await.result(p.future, scala.concurrent.duration.Duration.Inf)
  }

}