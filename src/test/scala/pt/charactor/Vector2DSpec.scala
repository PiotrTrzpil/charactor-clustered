package pt.charactor

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import pt.charactor.MoverArbiter.CurrentWorldMap

class Vector2DSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "A Vector2D" must {
    "bound another Vector inside" in {
      Vector2D(50, 50).bounded(Vector2D(100, 100)) shouldEqual Vector2D(50, 50)
      Vector2D(-10, 50).bounded(Vector2D(100, 100)) shouldEqual Vector2D(90, 50)
      Vector2D(10, -50).bounded(Vector2D(100, 100)) shouldEqual Vector2D(10, 50)
      Vector2D(110, 150).bounded(Vector2D(100, 100)) shouldEqual Vector2D(10, 50)
    }

  }
}
