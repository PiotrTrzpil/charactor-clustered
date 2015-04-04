package pt.charactor

import pt.charactor.MoverArbiter._
import akka.actor._
import pt.charactor.Mover._
import akka.actor.RootActorPath
import pt.charactor.MoverArbiter.CurrentWorldMap
import scala.Some
import akka.actor.RootActorPath
import scala.concurrent.duration._
import pt.charactor.MoverArbiter.CurrentWorldMap
import pt.charactor.Mover.MoverTransfer
import scala.Some
import pt.charactor.Mover.ElapsedTime
import pt.charactor.MoverArbiter.SpawnMover
import akka.actor.RootActorPath
import pt.charactor.Mover.PositionChanged
import scala.concurrent.ExecutionContext.Implicits.global
object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
  case class SpawnMover(position :Vector2D, name:String)
  case class CurrentWorldMap(map:List[Address])
}

class MoverArbiter(id:Int, mapDimensions :Vector2D) extends Actor with ActorLogging {
  type Command = Cmnd

  var worldMap = List[Address]()
  context.system.eventStream.subscribe(self, classOf[PositionChanged])

  def thisFragment = id % worldMap.size

  def chooseMapFragment(position:Vector2D) = {
    val step = mapDimensions.x / worldMap.size
    val margin = step/5
    val min = thisFragment * step
    val max = min + step

    if(position.x >= min - margin && position.x <= max + margin) {
      thisFragment
    } else {
      (position.x / step).toInt
    }
  }

  def chooseTargetNode(fragment: Int) = {
    worldMap.find(addr => (addr.port.get % worldMap.size) == fragment)
  }

  val sss: PartialFunction[Any, Unit] = initial orElse defaultBehavior
  def receive : Receive = sss

  def initial : Receive = {
    case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map
      val ss: PartialFunction[Any, Unit] = active orElse defaultBehavior
      context.become(ss)
  }
  def defaultBehavior : Receive = {
    case SpawnMover(position, name) =>
      val mover = context.system.actorOf(Props(classOf[Mover], name))
      mover ! SetPosition(position)
      context.system.scheduler.schedule(500.millis, 1500.milli, mover, ElapsedTime(1500.milli))
  }

  def active:Receive = {

    case MoverTransfer(name) =>
      log.info(s"Transfered actor $name to arbiter: $id")
      val mover = context.system.actorOf(Props(classOf[Mover], name))
      context.system.scheduler.schedule(500.millis, 1500.milli, mover, ElapsedTime(1500.milli))
    case PositionChanged(actor, position) =>
      log.info("position of actor: "+actor.path.name+ " changed to "+position)
      val fragment = chooseMapFragment(position)
      if(fragment != thisFragment) {
        val nodeAddress = chooseTargetNode(fragment) match {
          case None =>
            log.info("could not choose fragment for "+position)
            chooseTargetNode(0).get
          case Some(node) => node
        }
        val path = RootActorPath(nodeAddress) / "user" / "raft-member-mover-arbiter"
        val selection: ActorSelection = context.actorSelection(path)
        actor ! Kill
        selection ! MoverTransfer(actor.path.name)
        log.info("Killed actor: "+actor.path.name + " and transfered to : " +selection + " on node: "+nodeAddress)
      }

  }
}