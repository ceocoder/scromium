package scromium

import java.nio.ByteBuffer
import serializers._

object Scanner {
  def apply[R](start : R, end : R)(implicit ser : Serializer[R]) =
    new Scanner(ser.serialize(start), ser.serialize(end))
}

class Scanner(val startKey : ByteBuffer, val endKey : ByteBuffer) {
  var limit = 1000
  var fetchLimit = 100
  
}