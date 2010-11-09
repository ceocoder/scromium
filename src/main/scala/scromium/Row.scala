package scromium

import java.nio.ByteBuffer
import serializers._

abstract class Row[T <: Columnar](val key : ByteBuffer) {
  def keyAs[K](implicit ser : Deserializer[K]) = ser.deserialize(key)
  
  def columns : Iterator[T]
}