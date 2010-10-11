package scromium.thrift

import scromium._
import scromium.meta._
import scromium.client._
import scromium.util.{DefaultHashMap, ArrayKeyedHashMap, Log, HexString}
import org.apache.cassandra.thrift
import org.apache.thrift.transport.{TTransport, TTransportException}
import java.util.{Map => JMap, List => JList}
import scala.collection.mutable.{ListBuffer, StringBuilder}
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

class ThriftClient(cass : thrift.Cassandra.Iface) extends Client with Log {
  type MuteMap = JMap[String, JList[thrift.Mutation]]
  
  def put(keyspace : String, writes : List[Write[Column]], c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    writes.foreach { write =>
      val mutes = rowMap(write.key)(write.cf)
      mutes ++= write.columns.map(columnMutation(_))
    }
    debug("batch_mutate(" + stringify(rowMap) + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }
  
  def superPut(keyspace : String, writes : List[Write[SuperColumn]], c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    writes.foreach { write =>
      val mutes = rowMap(write.key)(write.cf)
      mutes ++= write.columns.map(superColumnMutation(_))
    }
    debug("batch_mutate(" + rowMap + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }
  
  def delete(keyspace : String, delete : Delete, c : WriteConsistency) {
    cass.set_keyspace(keyspace)
    val rowMap = createRowMap
    val mutation = deleteMutation(delete)
    delete.keys.foreach { key =>
      rowMap(key)(delete.cf) += mutation
    }
    debug("batch_mutate(" + rowMap + "," + c.thrift + ")")
    cass.batch_mutate(rowMap, c.thrift)
  }
  
  def get(keyspace : String, read : Read, c : ReadConsistency) : RowIterator[Column] = {
    cass.set_keyspace(keyspace)
    val parent = readToColumnParent(read)
    val predicate = readToPredicate(read)
    debug("multiget_slice(" + read.keys + "," + parent + "," + predicate + "," + c.thrift + ")")
    val results = cass.multiget_slice(read.keys, parent, predicate, c.thrift)
    new MGColumnRowIterator(results)
  }
  
  def getIndexed(keyspace : String, read : Read, c : ReadConsistency) : RowIterator[Column] = {
    cass.set_keyspace(keyspace)
    val parent = readToColumnParent(read)
    val predicate = readToPredicate(read)
    val index = readToIndexClause(read)
    debug("get_indexed_slices(" + read.keys + "," + parent + "," + index + "," + predicate + "," + c.thrift + ")")
    val results = Map(cass.get_indexed_slices(parent, index, predicate, c.thrift).map(a => { (a.key, a.columns) }) : _*)
    new MGColumnRowIterator(results)
  }
  
  def superGet(keyspace : String, read : Read, c : ReadConsistency) : RowIterator[SuperColumn] = {
    cass.set_keyspace(keyspace)
    val parent = readToColumnParent(read)
    val predicate = readToPredicate(read)
    debug("multiget_slice(" + read.keys + "," + parent + "," + predicate + "," + c.thrift + ")")
    val results = cass.multiget_slice(read.keys, parent, predicate, c.thrift)
    new MGSuperColumnRowIterator(results)
  }
  
  def createKeyspace(keyspace : KeyspaceDef) {
    debug("system_add_keyspace(" + ksDef(keyspace) + ")")
    cass.system_add_keyspace(ksDef(keyspace))
  }
  
  def createColumnFamily(cf : ColumnFamilyDef) {
    debug("system_add_column_family(" + cfDef(cf) + ")")
    cass.system_add_column_family(cfDef(cf))
  }
  
  def dropKeyspace(name : String) {
    debug("system_drop_keyspace(" + name + ")")
    cass.system_drop_keyspace(name)
  }
  
  def renameKeyspace(from : String, to : String) {
    debug("system_rename_keyspace(" + from + "," + to + ")")
    cass.system_rename_keyspace(from, to)
  }
  
  def dropColumnFamily(name : String) {
    debug("system_drop_column_family(" + name + ")")
    cass.system_drop_column_family(name)
  }
  
  def renameColumnFamily(from : String, to : String) {
    debug("system_rename_column_family(" + from + "," + to + ")")
    cass.system_rename_column_family(from, to)
  }
  
  def listKeyspaces : Set[String] = {
    debug("describe_keyspaces()")    
    cass.describe_keyspaces.flatMap(ks => List(ks.getName)).toSet
  }
  
/*  def scan(scanner : Scanner[Column], c : ReadConsistency) : RowIterator[Column]
  def superScan(scanner : Scanner[SuperColumn], c : ReadConsistency) : RowIterator[SuperColumn]*/
  
  private def createRowMap = new ArrayKeyedHashMap[Byte, MuteMap]({ key =>
    new JMapWrapper(new DefaultHashMap[String, JList[thrift.Mutation]]({ cf =>
      new JListWrapper(new ListBuffer[thrift.Mutation])
    }))
  })
  
  private def stringify(map : ArrayKeyedHashMap[Byte,MuteMap]) : String = {
    val buffer = new StringBuilder
    buffer ++= "Map("
    for((key,value) <- map) {
      buffer ++= HexString.toHexString(key) + " -> " + value.toString + ","
    }
    buffer.deleteCharAt(buffer.length-1)
    buffer ++= ")"
    buffer.toString
  }
}
