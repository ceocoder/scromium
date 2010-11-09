package scromium

import java.nio.ByteBuffer
import scromium.serializers.Deserializer
import org.apache.commons.codec.binary.Hex

case class Column(val name : ByteBuffer, 
                  val value : ByteBuffer, 
                  val timestamp : Long, 
                  val ttl : Option[Int]) extends Columnar {
  def valueAs[T](implicit des : Deserializer[T]) = des.deserialize(value)
  def nameAs[T](implicit des : Deserializer[T]) = des.deserialize(name)
  
  override def toString() : String = {
    "Column(" + Hex.encodeHexString(name.array) + "," + Hex.encodeHexString(value.array) + "," + timestamp + ")"
  }
}