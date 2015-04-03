package pt.charactor

import akka.actor._
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.persistence.inmem.journal.SharedInMemoryJournal

class SharedStoreUsage extends Actor {
  override def preStart(): Unit = {
    context.actorSelection("akka.tcp://MyActorSystem@127.0.0.1:2550/user/store") ! Identify(1)
  }

  def receive = {
    case ActorIdentity(1, Some(store)) =>
      SharedInMemoryJournal.setStore(store, context.system)
   //   SharedLeveldbJournal.setStore(store, context.system)
  }
}
