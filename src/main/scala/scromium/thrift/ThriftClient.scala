package scromium.thrift

import scromium.serializers.BufferConverter
import org.apache.cassandra.thrift.CfDef
import org.apache.cassandra.thrift.KsDef
import java.nio.ByteBuffer
import scromium._
import scromium.meta._
import scromium.client._
import scromium.util.{DefaultHashMap, ArrayKeyedHashMap, Log}
import org.apache.cassandra.thrift
import org.apache.cassandra.db.marshal._
import org.apache.thrift.transport.{TTransport, TTransportException}
import java.util.{Map => JMap, List => JList, HashMap => HMap}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import Thrift._

class ThriftConnection(socket : TTransport, client : thrift.Cassandra.Client) extends ThriftClient(client) {

  def isOpen() : Boolean = {socket.isOpen()}

  def ensureOpen {
    if (!socket.isOpen) {
      socket.open
    }
  }

  def close {
    socket.close
  }
}

class ThriftClient(cass : thrift.Cassandra.Iface) extends Client with Log with BufferConverter {
  type MuteMap = JMap[String, JList[thrift.Mutation]]

  def put(keyspace : String, writes : List[Write[Column]], c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    writes.foreach { write =>
      val mutes = rowMap(write.key)(write.cf)
      mutes ++= write.columns.map(columnMutation(_))
    }
    logger.debug("batch_mutate(" + keyspace + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }

  def superPut(keyspace : String, writes : List[Write[SuperColumn]], c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    writes.foreach { write =>
      val mutes = rowMap(write.key)(write.cf)
      mutes ++= write.columns.map(superColumnMutation(_))
    }
    logger.debug("batch_mutate(" + keyspace + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }

  def delete(keyspace : String, delete : Delete, c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    val mutation = deleteMutation(delete)
    delete.keys.foreach { key =>
      rowMap(key)(delete.cf) += mutation
    }
    logger.debug("batch_mutate(" + rowMap + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }

  def get(keyspace : String, read : Read, c : ReadConsistency) : RowIterator[Column] = {
    cass.set_keyspace(keyspace)
    val parent = readToColumnParent(read)
    val predicate = readToPredicate(read)
    logger.debug("multiget_slice(" + read.keys + "," + parent + "," + predicate + "," + c.thrift + ")")
    val results = cass.multiget_slice(read.keys.map(ByteBuffer.wrap(_)), parent, predicate, c.thrift)
    new MGColumnRowIterator(results)
  }

  def superGet(keyspace : String, read : Read, c : ReadConsistency) : RowIterator[SuperColumn] = {
    cass.set_keyspace(keyspace)
    val parent = readToColumnParent(read)
    val predicate = readToPredicate(read)
    logger.debug("multiget_slice(" + read.keys + "," + parent + "," + predicate + "," + c.thrift + ")")
    val results = cass.multiget_slice(read.keys.map(ByteBuffer.wrap(_)), parent, predicate, c.thrift)
    new MGSuperColumnRowIterator(results)
  }

  def createKeyspace(keyspace : KeyspaceDef) {
    logger.debug("system_add_keyspace(" + ksDef(keyspace) + ")")
    cass.system_add_keyspace(ksDef(keyspace))
  }

  def createColumnFamily(cf : ColumnFamilyDef) {
    logger.debug("system_add_column_family(" + cfDef(cf) + ")")
    cass.system_add_column_family(cfDef(cf))
  }

  def dropKeyspace(name : String) {
    logger.debug("system_drop_keyspace(" + name + ")")
    cass.system_drop_keyspace(name)
  }

  def renameKeyspace(from : String, to : String) {
    logger.debug("system_rename_keyspace(" + from + "," + to + ")")
    cass.set_keyspace(from)
    val kDef = new KsDef
    kDef.name = to
    cass.system_update_keyspace(kDef)
  }

  def dropColumnFamily(name : String) {
    logger.debug("system_drop_column_family(" + name + ")")
    cass.system_drop_column_family(name)
  }

  def renameColumnFamily(from : String, to : String) {
    logger.debug("system_rename_column_family(" + from + "," + to + ")")
    val cDef = new CfDef
    cDef.keyspace = ""
    cDef.name = to
    cass.system_update_column_family(cDef)
  }

  def listKeyspaces : Set[String] = {
    logger.debug("describe_keyspaces()")
    cass.describe_keyspaces.flatMap(ks => List(ks.getName)).toSet
  }

  /*def scan(scanner : Scanner[Column], c : ReadConsistency) : RowIterator[Column]
  def superScan(scanner : Scanner[SuperColumn], c : ReadConsistency) : RowIterator[SuperColumn]*/
  private def createRowMap = new DefaultHashMap[ByteBuffer, MuteMap]({ key =>
    new JMapWrapper(new DefaultHashMap[String, JList[thrift.Mutation]]({ cf =>
      new JListWrapper(new ListBuffer[thrift.Mutation])
    }))
  })
}
