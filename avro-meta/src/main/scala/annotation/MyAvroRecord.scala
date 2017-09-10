package annotation

import avro._
import org.apache.avro.Schema
import org.scalameta.logger

import scala.annotation.StaticAnnotation
import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.meta._

/**
  *
  */
class Main(file: String) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    logger.debug(this)

    val file = this match {
      case q"""new Main($name)""" =>
        logger.debug(name.tokens.mkString("\n"))
        val quoted = name.syntax
        quoted.substring(1, quoted.length - 1)
      case _ =>
        logger.debug("not match")
        ???
    }
    logger.debug(file)
    logger.debug(getClass.getResourceAsStream(file))

    defn match {
      case q"object $name { ..$stats }" =>
//        val schema: String = scala.io.Source.fromFile(file).mkString
        MainMacroImpl.expand(name, stats, file)
      case _ =>
        abort("@main must annotate an object.")
    }
  }
}

object MainMacroImpl {
  def expand(name: Term.Name, stats: Seq[Stat], schema: String): Defn.Object = {
    val s = new Schema.Parser().parse(getClass.getResourceAsStream(schema))
    val print: Stat = s"""println(\"\"\"$schema\"\"\")""".parse[Stat].get
    val newStats: Seq[Stat] = print +: stats
    val main = q"""def main(args: Array[String]): Unit = { ..$newStats }"""
    q"object $name { $main }"
  }
}

class MyAvroRecord(file: String, name: String) extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (file, name) = this match {
      case q"""new MyAvroRecord($file, $name)""" =>
        logger.debug(file.tokens.mkString("\n"))
        val quotedFile = file.syntax
        val quotedName = name.syntax
        (quotedFile.substring(1, quotedFile.length - 1), quotedName.substring(1, quotedName.length - 1))
      case _ =>
        logger.debug("not match")
        logger.debug(this)
        ???
    }
    logger.debug(file)
    val schema = new Schema.Parser().parse(getClass.getResourceAsStream(file))
    val schemas = schema.schemas
    val target = schemas(name)
    val schemaString = target.toString
    val content = Lit.String(schemaString)
    val fields = target.fields
    val schemaStats = Seq(
      q"""lazy val SCHEMA = new org.apache.avro.Schema.Parser().parse($content)""",
      q"""def getSchema = SCHEMA"""
    )
    val gets = scala.collection.immutable.Seq() ++ fields.zipWithIndex.map { case (field, index) =>
      val i = Lit.Int(index)
      val name = Term.Name(field.name())
      p"""case $i => $name """
    }
    val get = q"def get(key: Int): Object = (key match { ..case $gets }).asInstanceOf[Object]"
    val put = "def put(key: Int, value: Any): Unit = ???".parse[Stat].get

    defn match {
      case q"case class $name(..$params) { ..$stats }" =>
        logger.debug("match")
        logger.debug(name)
        logger.debug(stats)
        logger.debug(params)
        val fields = target.fields.map { field =>
          s"var ${field.name()}: ${field.schema().toScalaType}".parse[Term.Param].get
        }
        val params1: Seq[Term.Param] = params ++ fields
        logger.debug(params1)
        val newStats = stats ++ schemaStats :+ get :+ put
        q"case class $name(..$params1) extends org.apache.avro.specific.SpecificRecordBase { ..$newStats }"
      case _ =>
        defn
    }
  }
}