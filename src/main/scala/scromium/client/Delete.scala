package scromium.client

import java.nio.ByteBuffer
import scromium._
import scromium.clocks._

case class Delete(val keys : List[ByteBuffer],
  cf : String,
  columns : Option[List[ByteBuffer]] = None,
  subColumns : Option[List[ByteBuffer]] = None,
  slice : Option[Slice] = None,
  clock : Clock)
  
