package pt.charactor

import pt.charactor.MoverArbiter.{Cmnd, CurrentWorldMap, Square}
import akka.actor.{ActorLogging, Actor}
import pt.charactor.Mover.PositionChanged

object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
  case class CurrentWorldMap(map:Map[Square, Any])
}

class MoverArbiter(id:Int) extends Actor with ActorLogging {
  type Command = Cmnd


  var worldMap: Map[Square, Any] = Map()
  val mapDimensions = Vector2D(100,100)
  context.system.eventStream.subscribe(self, classOf[PositionChanged])

  def chooseMapFragment(position:Vector2D) = {

  }

  def receive = {
    case PositionChanged(actor, position) =>
      log.info("position of actor: "+actor.path.name+ " changed to "+position)

     case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map
  }

}