package remote

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import scala.io.StdIn.{readInt,readBoolean}
import java.security.SecureRandom
import com.sun.management.OperatingSystemMXBean;

 sealed trait message
  case class StartMiners(start: Int, range: Int, target :Int) extends message
  case class WorkRequest(str:String) extends message
  case class FindBitCoin(start: Int, nrOfElements: Int,target :Int) extends message
  case class CoinFound(inputString : String ,hashValue :String ,status : Int) extends message
  
  object MainServer extends App {
  var startValue:Int=0
  var range :Int =10000
  var requestNum :Int =1
  val start: Long = System.currentTimeMillis
  
  println("Enter the number")
  val target :Int= readInt()
  startServer(numWorkers = 5, numberOfChunks = 1000)
 
 
   
  
  class BitCoinServer(numWorkers: Int, numberOfChunks: Int) 
    extends Actor {
    
    
    //var nrOfResults: Int = _
   // val start: Long = System.currentTimeMillis
 
    val clientMiners = context.actorOf(
      Props[Miners].withRouter(RoundRobinRouter(numWorkers)), name = "Miners")
 
    def receive = {
      case StartMiners( beginValue,range,target) ⇒
        for (i ← beginValue until beginValue+range) clientMiners ! FindBitCoin(i * numberOfChunks, numberOfChunks,target)
      case  WorkRequest(str) ⇒
            print("Work request")
           // startValue = startValue *requestNum
           // sender ! StartMiners(startValue,range,target)
            //requestNum +=requestNum
            
      case CoinFound(inputString,hashValue,status) ⇒
        if(status == 1) //0 for failure and 1 for success
          //code for displaying the output
          println(inputString + " "+ hashValue);
          println((System.currentTimeMillis - start).millis)
          context.system.shutdown()
          context.stop(self)
          
        }
    }
  
  
  
  
  //Client
  class Miners extends Actor {
    
 
      def receive = {
      case FindBitCoin(start, numberOfChunks,target) ⇒
              for (nonce ← start until (start + numberOfChunks)){
          var nonceString:String =nonce.toString();
          var inputString:String ="ssisodia"+nonceString
          var hashByte = java.security.MessageDigest.getInstance("SHA-256").digest(inputString.getBytes("UTF-8"))
          var hashString :String=hashByte.map("%02x".format(_)).mkString;
          var targetStr :String = "0" *target
          //println(nonce)
          //println(hashString)
          if(hashString.substring(0, target).equals(targetStr)){
         // println(nonce)
         println("Success by  %s" .format(self.path.name) )
         println( hashString + " " +nonce )
          sender ! CoinFound(inputString,hashString,1) // perform the work
          context.stop(self)
          
        }
          }
      case _ => 
        println("Unknown message received")
      
        
    }

  }
    def startServer(numWorkers: Int, numberOfChunks: Int) {
     val system = ActorSystem("BitCoinMining")
 
 
    // create the master
    val serverInstance = system.actorOf(Props(new BitCoinServer(numWorkers, numberOfChunks)),
      name = "BitCoinServer")

      serverInstance ! StartMiners(startValue,range,target)
 
  }
 
 
 
 
 
}