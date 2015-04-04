package pt.charactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import akka.actor.{ActorLogging, ActorIdentity, Identify, ActorRef}
import pt.charactor.Mover._
import scala.concurrent.duration.FiniteDuration
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import scala.util.Random
import akka.persistence.SnapshotOffer
import pt.charactor.Mover.ElapsedTime
import pt.charactor.Mover.PositionChanged

object Mover {
  case class MoverTransfer(name:String)
  case class PositionChanged(actor:ActorRef, position:Vector2D)
  case class SetPosition(position:Vector2D)
  case object TakeSnapshot
  case class ElapsedTime(duration:FiniteDuration)
  case object Act
}
import scala.concurrent.duration._
class Mover(id:String) extends PersistentActor with ActorLogging{

  val mapDimensions = Vector2D(100,100)
  var position = Vector2D(0,0)
  var direction = Vector2D(1,1)
  val dirChange = 2d
  val distancePerSec = 8d
  import context.dispatcher

  context.system.scheduler.schedule(1.minute, 1.minute, self, TakeSnapshot)

  def receiveRecover = {
    case SnapshotOffer(meta, (pos:Vector2D, dir:Vector2D)) =>
      position = pos
      direction = dir
  }

  def receiveCommand = {
    case SetPosition(pos) =>
      persist((pos, direction)) { id =>
        position = pos
      }
    case ElapsedTime(duration) =>
      val newpos = position + direction * distancePerSec * duration.toMillis / 1000d
      val newPosition = newpos.bounded(mapDimensions)
      val newdirection = direction.rotateRadians(dirChange*Random.nextInt(100).toDouble/100d)
      persist((newPosition, newdirection)) { id =>
        position = newPosition
        direction = newdirection
        context.system.eventStream.publish(PositionChanged(self, position))
      }
    case Act =>
    case TakeSnapshot =>
      saveSnapshot((position, direction))
  }

  def persistenceId = id
}