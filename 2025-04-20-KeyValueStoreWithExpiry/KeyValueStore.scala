import scala.io.StdIn.readLine
import scala.util._


object KeyValueStore extends App {

  iterate(KV.empty)

  def iterate(kv: KV): Unit = {
    print("> ")
    val tryCmd = Parser.parseInput(readLine())

    val modifiedKv = tryCmd match {
      case Failure(_) =>
        println("Unable to parse Instruction.")
        kv
      case Success(command) =>
        command.process(kv.updateExpired)
    }
    iterate(modifiedKv)
  }
}

object Parser {
  def parseInput(input: String): Try[Command] = {
    Try {
      input.split(" ").toList match {
        case CmdId.GET :: key :: Nil => Get(key)

        case CmdId.SET :: key :: value :: ttl :: Nil => Set(key, value, ttl.toInt)
        case CmdId.SET :: key :: value :: Nil => Set(key, value, -1)

        case CmdId.DEL :: key :: Nil => Del(key)

        case CmdId.QUIT :: Nil => QUIT
        case _ => throw new RuntimeException("Unable to parse Input")
      }
    }
  }
}

object KV {
  val empty: KV = KV(Map.empty)
}
case class KV(map: Map[String, Value]) {
  def +(tup: (String, Value)): KV = KV(map + tup)
  def -(key: String): KV = KV(map - key)
  def get(key: String): Option[Value] = map.get(key)

  def updateExpired: KV = KV(map.map(kv => kv._1 -> kv._2.updateIfExpired))
}

sealed trait Value {
  val expired: Boolean
  def updateIfExpired: Value
}
case class NonExpired(v: String, ttl: Int, createdTimestamp: Long) extends Value {
  override val expired: Boolean = false

  private def checkExpired: Boolean = {
    if (ttl < 0) false
    else (System.currentTimeMillis() - (ttl * 1000) > createdTimestamp)
  }

  override def updateIfExpired: Value = {
    if (checkExpired) Expired
    else this
  }
}
case object Expired extends Value {
  override val expired: Boolean = true
  override def updateIfExpired: Value = Expired
}

object CmdId {
  val SET = "SET"
  val DEL = "DEL"
  val GET = "GET"
  val QUIT = "QUIT"
}
sealed trait Command {
  def process(kv: KV): KV
}
case class Set(key: String, value: String, ttl: Int) extends Command {
  override def process(kv: KV): KV = kv.get(key) match {
    case Some(_) =>
      println(s"'$key' already exists.")
      kv
    case None =>
      println("OK")
      kv + (key -> NonExpired(value, ttl, System.currentTimeMillis()))
  }
}
case class Del(key: String) extends Command {
  override def process(kv: KV): KV = kv.get(key) match {
    case Some(value) =>
      println(s"OK")
      kv - key
    case None =>
      println(s"'$key' not found.")
      kv
  }
}
case class Get(key: String) extends Command {
  override def process(kv: KV): KV = {
    kv.get(key) match {
      case Some(v) => v match {
        case NonExpired(value, ttl, createdTimestamp) =>
          println(s"$value")
        case Expired =>
          println(s"'$key' is expired.")
      }
      case None => println(s"'$key' doesn't exist.")
    }
    kv
  }
}

case object QUIT extends Command {
  override def process(kv: KV): KV = {
    println("Exiting. Thanks!")
    System.exit(1)
    kv
  }
}
