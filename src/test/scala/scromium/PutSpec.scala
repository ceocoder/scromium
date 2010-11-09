package scromium

import java.nio.ByteBuffer
import serializers.Serializers._
import scromium.client._
import org.specs._
import org.specs.mock.Mockito
import org.mockito.Matchers._

class PutSpec extends Specification with Mockito {
  val clock = mock[Clock]
  clock.timestamp returns 1
  val row = ByteBuffer.wrap("1".getBytes)
  val column = ByteBuffer.wrap("2".getBytes)
  val value = ByteBuffer.wrap("3".getBytes)
  
  val column2 = ByteBuffer.wrap("4".getBytes)
  val value2 = ByteBuffer.wrap("5".getBytes)
  
  val row2 = ByteBuffer.wrap("6".getBytes)
  
  "Put" should {
    "single column write" in {
      val put = new Put(clock)
      val rb = put.row(row)
      rb.insert(column, value)
      val writes = put.toWrites("cf")
      writes must ==(List(Write(row, "cf", 
        List(Column(column, value, 1, None)))))
    }
    
    "multi column write" in {
      val put = new Put(clock)
      val rb = put.row(row)
      rb.insert(column, value)
      rb.insert(column2, value2)
      val writes = put.toWrites("cf")
      writes must ==(List(Write(row, "cf",
        List(Column(column, value, 1, None),
             Column(column2, value2, 1, None)))))
    }
    
    "multirow write" in {
      val put = new Put(clock)
      put.row(row).insert(column, value)
      put.row(row2).insert(column2, value2)
      val writes = put.toWrites("cf")
      writes must ==(List(
        Write(row, "cf", List(Column(column, value, 1, None))),
        Write(row2, "cf", List(Column(column2, value2, 1, None)))))
    }
    
  }
  
  "SuperPut" should {
    val sc = ByteBuffer.wrap("7".getBytes)
    val sc2 = ByteBuffer.wrap("8".getBytes)
    
    "single subcolumn write" in {
      val put = new SuperPut(clock)
      put.row(row).superColumn(sc).insert(column, value)
      val writes = put.toWrites("cf")
      writes must ==(List(
        Write(row, "cf", List(
          SuperColumn(sc, List(Column(column, value, 1, None)))))))
    }
    
    "multi subcolumn write" in {
      val put = new SuperPut(clock)
      val scb = put.row(row).superColumn(sc)
      scb.insert(column, value)
      scb.insert(column2, value2)
      val writes = put.toWrites("cf")
      writes must ==(List(
        Write(row, "cf", List(
          SuperColumn(sc, List(
            Column(column, value, 1, None),
            Column(column2, value2, 1, None)))))))
    }
    
    "multi supercolumn write" in {
      val put = new SuperPut(clock)
      val rb = put.row(row)
      rb.superColumn(sc).insert(column, value)
      rb.superColumn(sc2).insert(column2, value2)
      val writes = put.toWrites("cf")
      writes must ==(List(
        Write(row, "cf", List(
          SuperColumn(sc, List(Column(column, value, 1, None))),
          SuperColumn(sc2, List(Column(column2, value2, 1, None)))))))
    }
    
    "multirow supercolumn write" in {
      val put = new SuperPut(clock)
      put.row(row).superColumn(sc).insert(column, value)
      put.row(row2).superColumn(sc2).insert(column2, value2)
      val writes = put.toWrites("cf")
      writes must ==(List(
        Write(row, "cf", List(
          SuperColumn(sc, List(Column(column, value, 1, None))))),
        Write(row2, "cf", List(
          SuperColumn(sc2, List(Column(column2, value2, 1, None)))))))
    }
  }
}