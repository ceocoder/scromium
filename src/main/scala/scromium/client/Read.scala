package scromium.client

import java.nio.ByteBuffer
import scromium._

case class Read(val keys : List[ByteBuffer],
  val columnFamily : String,
  val columns : Option[List[ByteBuffer]] = None,
  val subColumns : Option[List[ByteBuffer]] = None,
  val slice : Option[Slice] = None)
