package example

import java.io.File

import annotation.Main
import org.apache.avro.Schema
import avro._

//@Main("schema/example.avsc")
object Hello {
  val schema = new Schema.Parser().parse(new File("schema/example.avsc"))
  def main(args: Array[String]): Unit = {
    println(schema.schemas)
  }
}

trait Greeting {
  lazy val greeting: String = "hello"
}

class bb {

}

@Main("/example.avsc")
object aa {

}