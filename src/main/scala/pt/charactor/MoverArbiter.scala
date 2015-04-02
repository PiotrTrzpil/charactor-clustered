package pt.charactor

import pt.charactor.MoverArbiter.{Cmnd, CurrentWorldMap, Square}
import akka.actor.{ActorLogging, Actor}
import pt.charactor.Mover.PositionChanged

object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
  case class CurrentWorldMap(map:List[String])
}

class MoverArbiter(id:Int, mapDimensions :Vector2D) extends Actor with ActorLogging {
  type Command = Cmnd


  var worldMap = List[String]()
  context.system.eventStream.subscribe(self, classOf[PositionChanged])

  def chooseMapFragment(position:Vector2D) = {
    val thisPortion = id % worldMap.size
    val step = mapDimensions.x / worldMap.size
    val margin = step/3
    val min = thisPortion * step
    val max = min + step

    if(position.x >= min - margin && position.x <= max + margin) {
      thisPortion
    } else {
      (position.x / step).toInt
    }
  }

  def receive = {
    case PositionChanged(actor, position) =>
      log.info("position of actor: "+actor.path.name+ " changed to "+position)

     case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map
  }

}