package com.example

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import scala.concurrent.duration.{Duration, FiniteDuration}
import akka.persistence.PersistentActor
import scala.concurrent.Await
import pl.project13.scala.akka.raft.{RaftClientActor, RaftActor}
import pl.project13.scala.akka.raft.cluster.ClusterRaftActor
import com.example.MoverArbiter.{Cmnd, Square, CurrentWorldMap}
import com.typesafe.config.ConfigFactory

object ApplicationMain extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [raft]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("MyActorSystem", config)

  val setter = system.actorOf(Props[MapSetter], "map-setter")
  val arbiter = system.actorOf(Props[MoverArbiter], "raft-member-mover-arbiter")
  system.actorOf(ClusterRaftActor.props(arbiter, 3))


  Await.result(system.whenTerminated, Duration.Inf)



}

case class ElapsedTime(duration:FiniteDuration)
case object Act

class Mover extends PersistentActor {



  var position = Vector2D(1,1)
  var direction = Vector2D(1,1)
  val dirChange = 2
  val movePerSecond = 2



  override def receive = {

    case ElapsedTime(duration) =>
      position = position + direction * movePerSecond * duration.toSeconds
    case Act =>
  }

  def receiveRecover = ???

  def receiveCommand = ???

  def persistenceId = ???
}

object MoverArbiter {
  trait Cmnd
  case class Square(x:Int, y:Int)
  case class CurrentWorldMap(map:Map[Square, Address])
}

class MoverArbiter extends RaftActor {
  type Command = Cmnd

  var worldMap: Map[Square, Address] = Map()

  /** Called when a command is determined by Raft to be safe to apply */
  def apply = {
    case CurrentWorldMap(map) =>
      log.info("Received world map: "+map)
      worldMap = map

      worldMap

  }

}


class MapSetter extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

    override def preStart() = {
      cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot,
        classOf[MemberEvent], classOf[UnreachableMember])
    }
  override def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case a: MemberEvent =>
      log.info("event: {}", a)
    case s:CurrentClusterState =>
      log.info("Received state: "+s)
      val map = s.members.map(m => m.address)
        .zipWithIndex
        .map { case (address, index) => Square(index, 0) -> address }
        .toMap
      val addresses = s.members.map(m => RootActorPath(m.address) / "user" / "mover-arbiter").toList
      val client = context.actorOf(RaftClientActor.props(addresses:_*))
      client ! CurrentWorldMap(map)
  }
}