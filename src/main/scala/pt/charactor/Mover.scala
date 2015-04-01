package pt.charactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import akka.actor.ActorRef
import pt.charactor.Mover.{Act, ElapsedTime, TakeSnapshot, PositionChanged}
import scala.concurrent.duration.FiniteDuration

object Mover {
  case class PositionChanged(actor:ActorRef, position:Vector2D)
  case object TakeSnapshot
  case class ElapsedTime(duration:FiniteDuration)
  case object Act
}
import scala.concurrent.duration._
class Mover(initPosition:Vector2D, initDirection:Vector2D) extends PersistentActor {

  val mapDimensions = Vector2D(100,100)
  var position = initPosition
  var direction = initDirection
  val dirChange = 2d
  val distancePerSec = 2d

  import context.dispatcher

  context.system.scheduler.schedule(1.minute, 1.minute, self, TakeSnapshot)

  def receiveRecover = {
    case SnapshotOffer(meta, (pos:Vector2D, dir:Vector2D)) =>
      position = pos
      direction = dir
  }

  def receiveCommand = {
    case ElapsedTime(duration) =>

      val newpos = position + direction * distancePerSec * duration.toMillis / 1000d
      val newPosition = newpos % mapDimensions
      val newdirection = direction.rotateRadians(dirChange)
      persist((newPosition, newdirection)) { id =>
        position = newPosition
        direction = newdirection
        context.system.eventStream.publish(PositionChanged(self, position))
      }
    case Act =>
    case TakeSnapshot =>
      saveSnapshot((position, direction))
  }

  def persistenceId = self.path.name
}