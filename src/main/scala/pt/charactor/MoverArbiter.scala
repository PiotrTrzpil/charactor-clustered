package pt.charactor

import pl.project13.scala.akka.raft.RaftActor
import pt.charactor.MoverArbiter.{Cmnd, CurrentWorldMap, Square}

object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
  case class CurrentWorldMap(map:Map[Square, Any])
}

class MoverArbiter extends RaftActor {
  type Command = Cmnd

  var worldMap: Map[Square, Any] = Map()

  /** Called when a command is determined by Raft to be safe to apply */
  def apply = {
    case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map

      worldMap

  }

}