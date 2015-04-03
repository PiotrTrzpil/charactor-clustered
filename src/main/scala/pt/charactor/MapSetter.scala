package pt.charactor

import akka.actor._
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent._
import pt.charactor.MoverArbiter.CurrentWorldMap
import akka.actor.RootActorPath
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import pt.charactor.MoverArbiter.Square
import akka.cluster.ClusterEvent.UnreachableMember


class MapSetter extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  var members = List[Member]()

  override def preStart() = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  def arbiterPath = context.system / ("raft-member-mover-arbiter")
//RootActorPath(m.address)
  override def receive = {
    case MemberUp(member) =>
      context.children.foreach(_ ! Kill)
      log.info("Member is Up: {}", member.address)
      members = members :+ member
      val map = members.map(m => m.address)
        .zipWithIndex
        .map { case (address, index) => Square(index, 0) -> address}
        .toMap
      if(members.size == 3)
        context.actorSelection(arbiterPath) ! CurrentWorldMap(map.values.toList)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case a: MemberEvent =>
      log.info("event: {}", a)
    case s: CurrentClusterState =>
      log.info("Received state: " + s)

  }
}