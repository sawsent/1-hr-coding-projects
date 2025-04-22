import java.nio.file.{Files, Paths} 
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.io.StdIn.readLine
import Syntax._
import Settings.PersistenceFileName
import java.nio.file.Path

object Settings {
  val PersistenceFileName = ".persistence"
}

object PersistenceManager {
  val NEWLINE = "\n"
  def read(file: String = Settings.PersistenceFileName): Try[List[ListItem]] = 
    Try(Source.fromFile(file).getLines().toList)
      .map(lines => lines.map(line => TaskProtocol.parseItem(line)).filter(t => t.isSuccess).map(t => t.get))

  def write(items: List[ListItem], file: String = PersistenceFileName): Try[Path] = { 

    val writeString = items.map(item => TaskProtocol.toLine(item)).mkString(NEWLINE)

    Try(Files.write(Paths.get(file), writeString.getBytes(StandardCharsets.UTF_8)))
  }
}


object TaskManager extends App {

  val lines = PersistenceManager.read(PersistenceFileName).getOrElse(List.empty)
  iteration(lines)

  def iteration(state: List[ListItem]): Unit = {
    print("> ")

    val maybeCmd = InputParser.parseInput(readLine())

    val newState = maybeCmd match {
    case Failure(_) => 
      println("Unable to parse Input")
      state
    case Success(cmd) =>
      processCommand(state, cmd)
    }

    iteration(newState)
  }

  def processCommand(state: List[ListItem], cmd: Command): List[ListItem] = {
    val stateMap = state.toMapp

    cmd match {
      case Add(description) => 
        println("OK")
        state :+ ListItem(nextId(state), description, false)

      case Del(id) => state.findById(id) match {
        case None =>
          println(s"'$id' not found")
          state
        case Some(li) =>
          println("OK")
          (stateMap - id).toListt
      }

      case Done(id) => state.findById(id) match {
        case None => 
          println(s"'$id' not found")
          state
        case Some(value) =>
          println("OK")
          (stateMap + (id -> value.copy(done = true))).toListt
      }

      case Undo(id) => state.findById(id) match {
        case None => 
          println(s"'$id' not found")
          state
        case Some(value) =>
          println("OK")
          (stateMap + (id -> value.copy(done = false))).toListt
      }

      case ListItems =>
        println("id |don| description")
        state.foreach(li => println(li.format))
        state

      case Clear =>
        println("OK")
        List.empty

      case Save(file) => 
        println("Saving...")
        PersistenceManager.write(state, file) match {
          case Failure(exception) => println(s"An error occured! - ${exception.getMessage()}")
          case Success(_) => println("Save Successful.")
        }
        state
        
      case Read(file) =>
        PersistenceManager.read(file) match {
          case Failure(exception) => 
            println(s"Error loading file $file - $exception") 
            state
          case Success(value) => 
            println("OK")
            value
        }
        
        

      case Quit => 
        println("Quitting")
        System.exit(1)
        throw new IllegalStateException()
    }
  }

  def nextId(state: List[ListItem]): Int = state.sortWith((i1, i2) => i1.id > i2.id) match {
    case Nil => 0
    case head :: _ => head.id + 1
  }

}

object Syntax {
  implicit class ToMapSyntax(l: List[ListItem]) {
    def toMapp: Map[Int, ListItem] = l.map(li => li.id -> li).toMap
    def findById(id: Int): Option[ListItem] = Try(l.filter(_.id == id).head).toOption
  }

  implicit class ToListSyntax(m: Map[Int, ListItem]) {
    def toListt: List[ListItem] = m.values.toList
  }
}

object InputParser {

  def parseInput(input: String): Try[Command] = Try(input.split(" ").toList match {
    case Add.descriptor :: tail         => Add(tail.mkString(" "))
    case Del.descriptor :: id :: Nil    => Del(id.toInt)
    case Done.descriptor :: id :: Nil   => Done(id.toInt)
    case Undo.descriptor :: id :: Nil   => Undo(id.toInt)
    case Clear.descriptor :: Nil        => Clear
    case ListItems.descriptor :: Nil    => ListItems
    case Save.descriptor :: Nil         => Save()
    case Save.descriptor :: file :: Nil => Save(file)
    case Read.descriptor :: file :: Nil => Read(file)
    case Quit.descriptor :: Nil         => Quit
    case _ => throw new IllegalArgumentException()
  })

}

sealed trait Command
object Add {
  val descriptor = "ADD"
}
case class Add(description: String) extends Command
object Del {
  val descriptor = "DEL"
}
case class Del(id: Int) extends Command
object Done {
  val descriptor = "DONE"
}
case class Done(id: Int) extends Command
object Undo {
  val descriptor = "UNDO"
}
case class Undo(id: Int) extends Command
case object ListItems extends Command {
  val descriptor = "ls"
}
case object Clear extends Command {
  val descriptor = "CLEAR"
}
object Save {
  val descriptor = "SAVE"
}
case class Save(file: String = PersistenceFileName) extends Command
object Read {
  val descriptor = "LOAD"
}
case class Read(file: String) extends Command
case object Quit extends Command {
  val descriptor = "QUIT"
}

object TaskProtocol {
  val TRUE = "true"
  val FALSE = "false"
  val SPLITTER = "%"

  private def asBoolean(str: String): Boolean = str match {
    case TRUE   => true
    case FALSE  => false
  }

  def parseItem(line: String): Try[ListItem] = Try(line.split(SPLITTER).toList match {
    case id :: description :: done :: Nil => ListItem(id.toInt, description, asBoolean(done))
    case _ => throw new IllegalArgumentException()
  })

  def toLine(listItem: ListItem): String = s"${listItem.id}$SPLITTER${listItem.description}$SPLITTER${listItem.done}"

}

case class ListItem(id: Int, description: String, done: Boolean) {
  val format = s"[$id] [${if (done) "x" else " "}] - $description"
}

