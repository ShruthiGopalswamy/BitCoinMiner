package main.scala.local

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import scala.io.StdIn.{ readInt, readBoolean }
import java.security.SecureRandom
import com.typesafe.config.ConfigFactory

sealed trait message
case class StartMiners(start: Int, range: Int, target: Int) extends message
case class WorkRequest() extends message
case class FindBitCoin(start: Int, nrOfElements: Int, target: Int) extends message
case class CoinFound(inputString: String, hashValue: String, status: Int) extends message

object LocalClient extends App {

  val config = ConfigFactory.parseString(
    """akka{
          actor{
            provider = "akka.remote.RemoteActorRefProvider"
          }
          remote{
                   enabled-transports = ["akka.remote.netty.tcp"]
            netty.tcp{
            hostname = "127.0.0.1"
            port = 0
          }
        }     
      }""")
      
  implicit val system = ActorSystem("LocalSystem", config, getClass.getClassLoader)
  val localActor = system.actorOf(Props(new BitCoinServer(5, 100)),
    name = "LocalClient") // the local actor

  localActor ! "START"

  class BitCoinServer(numWorkers: Int, numberOfChunks: Int)
      extends Actor {

    val remote = context.actorSelection("akka.tcp://BitCoinMining@127.0.0.1:5150/user/BitCoinServer")

    val clientMiners = context.actorOf(
      Props[Miners].withRouter(RoundRobinRouter(numWorkers)), name = "Miners")

    def receive = {
      case "START" =>
        remote ! WorkRequest
      case StartMiners(beginValue, range, target) ⇒
        for (i ← beginValue until beginValue + range) clientMiners ! FindBitCoin(i * numberOfChunks, numberOfChunks, target)

      case CoinFound(inputString, hashValue, status) ⇒
        if (status == 1) //0 for failure and 1 for success
          println(inputString + " " + hashValue);
      
      case _ =>
        println("Unknown message received")
    }
  }

  //Client
  class Miners extends Actor {

    def receive = {
      case FindBitCoin(start, numberOfChunks, target) ⇒
        for (nonce ← start until (start + numberOfChunks)) {
          var nonceString: String = nonce.toString();
          var inputString: String = "ssisodia" + nonceString
          var hashByte = java.security.MessageDigest.getInstance("SHA-256").digest(inputString.getBytes("UTF-8"))
          var hashString: String = hashByte.map("%02x".format(_)).mkString;
          var targetStr: String = "0" * target
          if (hashString.substring(0, target).equals(targetStr)) {
            // println(nonce)
            println("Success by  %s".format(self.path.name))
            println(hashString + " " + nonce)
            sender ! CoinFound(inputString, hashString, 1) // perform the work

          }
        }

    }

  }

}