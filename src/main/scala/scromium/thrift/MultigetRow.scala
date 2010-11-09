package scromium.thrift

import java.nio.ByteBuffer
import scromium._

case class MultigetRow[T <: Columnar](override val key : ByteBuffer, cols : List[T]) extends Row[T](key) {
  def columns = cols.iterator
}