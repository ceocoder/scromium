package scromium.client

import org.apache.cassandra.thrift.IndexExpression
import scromium._

case class Read(val keys : List[Array[Byte]],
  val columnFamily : String,
  val columns : Option[List[Array[Byte]]] = None,
  val subColumns : Option[List[Array[Byte]]] = None,
  val slice : Option[Slice] = None, 
  val expressions : Option[List[Expression]] = None)
