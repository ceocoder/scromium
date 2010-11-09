package scromium

import java.nio.ByteBuffer
import serializers._

object Slice {
  def apply[C](start : C, end : C, reversed : Boolean = false, limit : Option[Int] = None)(implicit cSer : Serializer[C]) = 
    new Slice(cSer.serialize(start), cSer.serialize(end), reversed, limit)
}

class Slice(val start : ByteBuffer, val end : ByteBuffer, val reversed : Boolean, val limit : Option[Int])
