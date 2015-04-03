package pt.charactor

import akka.persistence.{SnapshotOffer, PersistentActor}
import akka.actor.{ActorLogging, ActorIdentity, Identify, ActorRef}
import pt.charactor.Mover.{Act, ElapsedTime, TakeSnapshot, PositionChanged}
import scala.concurrent.duration.FiniteDuration
import akka.persistence.journal.leveldb.SharedLeveldbJournal

object Mover {
  case class MoverTransfer(name:String)
  case class PositionChanged(actor:ActorRef, position:Vector2D)
  case object TakeSnapshot
  case class ElapsedTime(duration:FiniteDuration)
  case object Act
}
import scala.concurrent.duration._
class Mover(initPosition:Vector2D, initDirection:Vector2D) extends PersistentActor with ActorLogging{

//  override def preStart(): Unit = {
//    context.actorSelection("akka.tcp://example@127.0.0.1:2550/user/store") ! Identify(1)
//  }
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
//    case ActorIdentity(1, Some(store)) =>
//      SharedLeveldbJournal.setStore(store, context.system)

    case ElapsedTime(duration) =>
   //   log.info("Received: "+duration)
      val newpos = position + direction * distancePerSec * duration.toMillis / 1000d
      val newPosition = newpos % mapDimensions
      val newdirection = direction.rotateRadians(dirChange)
      persist((newPosition, newdirection)) { id =>
      //  log.info("Persisted: "+newPosition+", "+ newdirection)
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