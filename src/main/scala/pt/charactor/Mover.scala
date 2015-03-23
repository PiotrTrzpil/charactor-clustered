package com.example.pt.charactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import pt.charactor.{Act, ElapsedTime}
import akka.actor.ActorRef
import com.example.pt.charactor.Mover.{TakeSnapshot, PositionChanged}

object Mover {
  case class PositionChanged(actor:ActorRef, position:Vector2D)
  case object TakeSnapshot
}
import scala.concurrent.duration._
class Mover extends PersistentActor {

  var position = Vector2D(1,1)
  var direction = Vector2D(1,1)
  val dirChange = 2
  val distancePerSec = 2

  import context.dispatcher

  context.system.scheduler.schedule(1.minute, 1.minute, self, TakeSnapshot)
  def receiveRecover = {
    case SnapshotOffer(meta, (pos:Vector2D, dir:Vector2D)) =>
      position = pos
      direction = dir
  }

  def receiveCommand = {
    case ElapsedTime(duration) =>

      val newposition = position + direction * distancePerSec * duration.toSeconds
      val newdirection = direction.rotateRadians(dirChange)
      persist((newposition, newdirection)) { id =>
        position = newposition
        direction = newdirection
        context.system.eventStream.publish(PositionChanged(self, position))
      }
    case Act =>
    case TakeSnapshot =>
      saveSnapshot((position, direction))
  }

  def persistenceId = self.path.name
}