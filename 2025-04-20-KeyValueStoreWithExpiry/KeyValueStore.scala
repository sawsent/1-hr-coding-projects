import scala.io.StdIn.readLine
import scala.util._
import Common.KV

object Common {
  type KV = Map[String, Value]
  val DefaultSaveFile: String = "save"
}
object KeyValueStore extends App {
  def startup: KV = {
    Map.empty
  }

  def checkExpired(kv: KV): KV = {
    kv.map(kv => kv._1 -> kv._2.updateIfExpired)
  }

  def parseInput(input: String): Try[Command] = {
    def parseGetCommand(input: List[String]): Get = input match {
      case "GET" :: key :: Nil => Get(key)
      case _ => throw new RuntimeException("Unable to parse Input")
    }

    def parseSetCommand(input: List[String]): Set = input match {
      case "SET" :: key :: value :: ttl :: Nil => Set(key, value, ttl.toInt)
      case "SET" :: key :: value :: Nil => Set(key, value, -1)
      case _ => throw new RuntimeException("Unable to parse Input")
    }

    def parseDelCommand(input: List[String]): Del = input match {
      case "DEL" :: key :: Nil => Del(key)
      case _ => throw new RuntimeException("Unable to parse Input")
    }

    Try {
      val parts = input.split(" ").toList
      parts(0) match {
        case Command.GET_IDENTIFIER => parseGetCommand(parts)
        case Command.SET_IDENTIFIER => parseSetCommand(parts)
        case Command.DEL_IDENTIFIER => parseDelCommand(parts)
        case Command.QUIT_IDENTIFIER => QUIT
      }
    }
  }


  def run(): Unit = {
    def iterate(kv: KV): Unit = {

      print("> ")
      val input = readLine()
      val tryCmd = parseInput(input)

      val modifiedKv = tryCmd match {
        case Failure(_) =>
          println("Unable to parse Instruction.")
          kv
        case Success(command) =>
          command.process(checkExpired(kv))
      }

      iterate(modifiedKv)
    }

    iterate(startup)
  }
  run()

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

object Command {
  val SET_IDENTIFIER = "SET"
  val DEL_IDENTIFIER = "DEL"
  val GET_IDENTIFIER = "GET"
  val QUIT_IDENTIFIER = "QUIT"
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
