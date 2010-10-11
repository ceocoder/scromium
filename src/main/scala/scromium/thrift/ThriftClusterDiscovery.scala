package scromium.thrift

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.{TSocket, TTransportException}
import org.apache.cassandra.thrift
import scromium.util.JSON
import scala.collection.JavaConversions._

class ThriftClusterDiscovery {
  @throws(classOf[TTransportException])
  def hosts(seedHost : String, seedPort : Int) : List[String] = {
    val socket = new TSocket(seedHost, seedPort)
    socket.open
    val client = new thrift.Cassandra.Client(new TBinaryProtocol(socket))
    val keyspaces = client.describe_keyspaces.flatMap(x => List(x.getName)).filter { x => x != "system" }    
    if (keyspaces.isEmpty) {
      return List(seedHost)
    }
    val ks = keyspaces.head
    val ranges = client.describe_ring(ks)
    socket.close
    val seq = for (range <- ranges; endpoint <- range.endpoints) yield(endpoint)
    seq.toList.distinct
  }
}
