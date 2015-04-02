package pt.charactor

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.{TestActorRef, TestActors, TestKit, ImplicitSender}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import pt.charactor.Vector2D
import pt.charactor.MoverArbiter.CurrentWorldMap

class MoverArbiterSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("MySpec"))
 
  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "A Ping actor" must {
    "choose target while being in the first fragment" in {
      val pingActor = TestActorRef[MoverArbiter](Props(classOf[MoverArbiter], 0, Vector2D(100, 100) ))
      pingActor ! CurrentWorldMap(List("", "", ""))
      pingActor.underlyingActor.chooseMapFragment(Vector2D(0, 0)) shouldEqual 0
      pingActor.underlyingActor.chooseMapFragment(Vector2D(33, 0)) shouldEqual 0
      pingActor.underlyingActor.chooseMapFragment(Vector2D(44, 0)) shouldEqual 0
      pingActor.underlyingActor.chooseMapFragment(Vector2D(45, 0)) shouldEqual 1
      pingActor.underlyingActor.chooseMapFragment(Vector2D(66, 0)) shouldEqual 1
      pingActor.underlyingActor.chooseMapFragment(Vector2D(67, 0)) shouldEqual 2
    }
    "choose target while being in the second fragment" in {
      val pingActor = TestActorRef[MoverArbiter](Props(classOf[MoverArbiter], 1, Vector2D(100, 100) ))
      pingActor ! CurrentWorldMap(List("", "", ""))
      pingActor.underlyingActor.chooseMapFragment(Vector2D(0, 0)) shouldEqual 0
      pingActor.underlyingActor.chooseMapFragment(Vector2D(22, 0)) shouldEqual 0
      pingActor.underlyingActor.chooseMapFragment(Vector2D(23, 0)) shouldEqual 1
      pingActor.underlyingActor.chooseMapFragment(Vector2D(77, 0)) shouldEqual 1
      pingActor.underlyingActor.chooseMapFragment(Vector2D(78, 0)) shouldEqual 2
    }
  }

//  "A Pong actor" must {
//    "send back a pong on a ping" in {
//      val pongActor = system.actorOf(PongActor.props)
//      pongActor ! PingActor.PingMessage("ping")
//      expectMsg(PongActor.PongMessage("pong"))
//    }
//  }

}
