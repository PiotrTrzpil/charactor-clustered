package pt.charactor

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import scala.concurrent.duration.FiniteDuration
import akka.persistence.PersistentActor
import pl.project13.scala.akka.raft.{ClusterConfiguration, RaftClientActor, RaftActor}
import com.typesafe.config.ConfigFactory
import pt.charactor.MoverArbiter._
import pl.project13.scala.akka.raft.cluster.ClusterRaftActor

object ApplicationMain extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [raft]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("MyActorSystem", config)

import pl.project13.scala.akka.raft.protocol._

/*
  val arbiters = (1 to 3).map(i=>system.actorOf(Props[MoverArbiter], s"raft-member-mover-arbiter-$port-$i"))

  arbiters.foreach(_ ! ChangeConfiguration(ClusterConfiguration(arbiters)))
  val client = system.actorOf(RaftClientActor.props(arbiters.map(_.path):_*))
  client ! CurrentWorldMap(Map())
  */


  //val setter3 = system.actorOf(Props[MapSetterLite], "map-setter3")
   val arbiter = system.actorOf(Props[MoverArbiter], s"raft-member-mover-arbiter-$port")

 // client ! CurrentWorldMap(Map())
  //client ! CurrentWorldMap(Map())




 // val setter2 = system.actorOf(Props[MapSetterLite], "map-setter2")
  //val setter = system.actorOf(Props[MapSetter], "map-setter")
  system.actorOf(ClusterRaftActor.props(arbiter, 2), "clustered-raft")

  val addresses = List(2551, 2552).map(p =>
    RootActorPath(arbiter.path.address.copy(port=Some(p), protocol = "akka.tcp", host=Some("127.0.0.1"))) / "user" / ("raft-member-mover-arbiter-"+p)).toList
  val client = system.actorOf(RaftClientActor.props(addresses:_*))
  client ! CurrentWorldMap(Map())

  system.awaitTermination()



}




case class ElapsedTime(duration:FiniteDuration)
case object Act




class MapSetterLite extends Actor with ActorLogging {

  log.info("Received state: ")
  val map = (0 until 3)
    .zipWithIndex
    .map { case (address, index) => Square(index, 0) -> address }
    .toMap
  val addresses = (0 until 3).map(m => self.path.root / "user" / "mover-arbiter*").toList
  val client = context.actorOf(RaftClientActor.props(addresses:_*))
  client ! CurrentWorldMap(map)

  def receive = {
    case _ =>
  }
}


class MapSetter extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  var members = List[Member]()
    override def preStart() = {
      cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
        classOf[MemberEvent], classOf[UnreachableMember])
    }
  override def receive = {
    case MemberUp(member) =>
      context.children.foreach(_ ! Kill)
      log.info("Member is Up: {}", member.address)
      members = members :+ member
      val map = members.map(m => m.address)
        .zipWithIndex
        .map { case (address, index) => Square(index, 0) -> address }
        .toMap
      val addresses = members.map(m => RootActorPath(m.address) / "user" / ("raft-member-mover-arbiter-"+m.address.port.get)).toList
      val client = context.actorOf(RaftClientActor.props(addresses:_*))
      client ! CurrentWorldMap(map)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case a: MemberEvent =>
      log.info("event: {}", a)
    case s:CurrentClusterState =>
      log.info("Received state: "+s)

  }
}