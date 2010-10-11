package scromium

import serializers._

object Expression {
  def apply[C](columnName: C, op: Operator, value: C)(implicit cSer: Serializer[C]) =
    new Expression(cSer.serialize(columnName), op, cSer.serialize(value))
}

class Expression(val columnName: Array[Byte], val op: Operator, val value: Array[Byte])

abstract class Operator
case object EQ extends Operator
case object GTE extends Operator
case object GT extends Operator
case object LTE extends Operator
case object LT extends Operator

  
