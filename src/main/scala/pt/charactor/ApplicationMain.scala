package pt.charactor

import akka.actor._
import pl.project13.scala.akka.raft.ClusterConfiguration
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import pt.charactor.Mover.ElapsedTime
import akka.persistence.journal.leveldb.SharedLeveldbStore
import akka.persistence.inmem.journal.{SharedInMemoryMessageStore, SharedInMemoryJournal}
import pt.charactor.MoverArbiter.SpawnMover

object ApplicationMain extends App {

  val port = if (args.isEmpty) "0" else args(0)
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    withFallback(ConfigFactory.parseString("akka.cluster.roles = [raft]")).
    withFallback(ConfigFactory.load())

  val system = ActorSystem("MyActorSystem", config)
  if (port.toInt == 2550) {
    val store = system.actorOf(
      Props[SharedInMemoryMessageStore], "store")
   // SharedInMemoryJournal.setStore(, system)
  }
  system.actorOf(Props[SharedStoreUsage])

  //else if (port.toInt == 2551 {
  //  SharedInMemoryJournal.setStore

//    (getActorRef(
//      node(node1) / "user" / "journalStore").get, system)
//  }
  //  system.actorOf(Props(classOf[SharedLeveldbStore]), "store")

  import system.dispatcher
  val setter = system.actorOf(Props[MapSetter], "map-setter")
  val arbiter = system.actorOf(Props(classOf[MoverArbiter], port.toInt, Vector2D(100, 100)),
    s"raft-member-mover-arbiter")

  val xPos = 11+(port.toInt % 3)*33
  arbiter ! SpawnMover(Vector2D(xPos,50), s"mover"+(port.toInt % 3))

  system.awaitTermination()
}