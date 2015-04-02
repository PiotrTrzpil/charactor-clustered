package pt.charactor

import akka.actor._
import pl.project13.scala.akka.raft.ClusterConfiguration
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import pt.charactor.Mover.ElapsedTime
import akka.persistence.journal.leveldb.SharedLeveldbStore

object ApplicationMain extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [raft]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("MyActorSystem", config)
  //if (port.toInt == 2550)
  //  system.actorOf(Props(classOf[SharedLeveldbStore]), "store")

  import system.dispatcher
  val setter = system.actorOf(Props[MapSetter], "map-setter")
  val arbiter = system.actorOf(Props(classOf[MoverArbiter], port.toInt, Vector2D(100, 100)),
    s"raft-member-mover-arbiter")

  val xPos = 11+(port.toInt % 3)*33
  val mover = system.actorOf(Props(classOf[Mover], Vector2D(xPos,50), Vector2D(1,1)), s"mover"+(port.toInt % 3))
  system.scheduler.schedule(500.millis, 500.milli, mover, ElapsedTime(500.milli))

  system.awaitTermination()
}