package pt.charactor

import akka.actor._
import pl.project13.scala.akka.raft.ClusterConfiguration
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import pt.charactor.Mover.ElapsedTime

object ApplicationMain extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [raft]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("MyActorSystem", config)

  import system.dispatcher
  val setter = system.actorOf(Props[MapSetter], "map-setter")
  val arbiter = system.actorOf(Props[MoverArbiter], s"raft-member-mover-arbiter-$port")

  val mover = system.actorOf(Props[Mover], s"mover")
  system.scheduler.schedule(500.millis, 40.milli, mover, ElapsedTime(40.milli))


  system.awaitTermination()
}