package pt.charactor

import pt.charactor.MoverArbiter.{Cmnd, CurrentWorldMap, Square}
import akka.actor._
import pt.charactor.Mover.{MoverTransfer, PositionChanged}
import pt.charactor.MoverArbiter.CurrentWorldMap
import akka.actor.RootActorPath

object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
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
    worldMap.find(addr => (addr.port.get % worldMap.size) == fragment).get
  }

  def receive = {

     case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map
      context.become(active)
  }


  def active:Receive = {
    case MoverTransfer(name) =>
      context.system.actorOf(Props(classOf[Mover], Vector2D(50,50), Vector2D(1,1)), name)
      log.info(s"Transfered actor $name to arbiter: $id")
    case PositionChanged(actor, position) =>
      log.info("position of actor: "+actor.path.name+ " changed to "+position)
      val fragment = chooseMapFragment(position)
      if(fragment != thisFragment) {
        val nodeAddress = chooseTargetNode(fragment)
        val selection = context.actorSelection(RootActorPath(nodeAddress) / "raft-member-mover-arbiter")
        actor ! Kill
        selection ! MoverTransfer(actor.path.name)
        log.info("Killed actor: "+actor.path.name)
      }

  }
}