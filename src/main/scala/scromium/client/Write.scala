package scromium.client

import java.nio.ByteBuffer
import scromium._

case class Write[C](val key : ByteBuffer,
  val cf : String,
  val columns : List[C])