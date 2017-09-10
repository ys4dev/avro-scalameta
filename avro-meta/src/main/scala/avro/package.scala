import org.apache.avro.Schema

/**
  *
  */
package object avro {

  import collection.JavaConverters._

  implicit class RichSchema(private val schema: Schema) extends AnyVal {
    def fields: Seq[Schema.Field] = {
      schema.getFields.asScala
    }

    def schemas: Map[String, Schema] = {
      import collection.mutable
      def go(schema: Schema, result: mutable.Map[String, Schema]): Unit = {
        result.get(schema.getFullName) match {
          case None =>
            result(schema.getFullName) = schema
            for (field <- schema.fields) {
              field.schema().getType match {
                case Schema.Type.RECORD =>
                  go(field.schema, result)
                case Schema.Type.ARRAY =>
                  go(field.schema().getElementType, result)
                case _ =>
              }
            }

          case _ =>
        }
      }

      val result = mutable.Map[String, Schema]()
      go(schema, result)
      Map() ++ result
    }

    def toScalaType: String = {
      schema.getType match {
        case Schema.Type.BOOLEAN => "Boolean"
        case Schema.Type.INT => "Int"
        case Schema.Type.LONG => "Long"
        case Schema.Type.FLOAT => "Float"
        case Schema.Type.DOUBLE => "Double"
        case Schema.Type.STRING => "String"
        case Schema.Type.ARRAY => s"Array[${schema.getElementType.getFullName}]"
        case Schema.Type.RECORD => schema.getFullName
        case Schema.Type.UNION =>
          val types = schema.getTypes.asScala
          if (types.size == 2 && types.exists(_.getType == Schema.Type.NULL)) {
            val elemType = types.filter(_.getType != Schema.Type.NULL).head.toScalaType
            s"Option[$elemType]"
          } else if (types.size == 2 && types.exists(_.getType == Schema.Type.FLOAT) && types.exists(_.getType == Schema.Type.DOUBLE)) {
            "Double"
          } else {
            schema.getTypes.asScala.mkString(", ")
          }
        case _ => schema.getType.toString
      }
    }
  }

  implicit class RichField(private val field: Schema.Field) extends AnyVal {
  }
}
