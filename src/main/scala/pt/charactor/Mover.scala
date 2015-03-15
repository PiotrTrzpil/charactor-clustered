package com.example.pt.charactor

import akka.persistence.PersistentActor
import pt.charactor.{Act, ElapsedTime}


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