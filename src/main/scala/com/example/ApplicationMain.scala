package com.example

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.cluster._
import akka.cluster.ClusterEvent._
import scala.concurrent.duration.{Duration, FiniteDuration}
import akka.persistence.PersistentActor
import scala.concurrent.Await

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")

  Await.result(system.whenTerminated, Duration.Inf)

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

class MoverArbiter extends Actor {
  val cluster = Cluster(context.system)

  override def preStart() = {
    cluster.subscribe(self, initialStateMode = InitialStateAsSnapshot,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  def receive = {
    case s:CurrentClusterState => //s.members.foreach(m => m.)
  }
  def nerbyMovers : List[ActorRef] = {
    //context.system.actorSelection("").anchor
  }
}