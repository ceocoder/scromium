package scromium.serializers

import java.nio.ByteBuffer
import java.io.{ByteArrayOutputStream, ObjectOutputStream, ByteArrayInputStream, ObjectInputStream}

object Serializers {
    implicit object MapSerializer extends Serializer[Map[String, String]] with Deserializer[Map[String, String]] {
    def serialize(map : Map[String, String]) = {
      val baos = new ByteArrayOutputStream(1024)
      val o = new ObjectOutputStream(baos)
      o.writeObject(map)
      ByteBuffer.wrap(baos.toByteArray)
    }
    
    def deserialize(ary : ByteBuffer) : Option[Map[String, String]] = {
      val bis = new ByteArrayInputStream(ary.array)
      val ois = new ObjectInputStream(bis)
      val obj = ois.readObject
      ois.close()
      Some(obj.asInstanceOf[Map[String, String]])
    }
  }

  implicit object ListStringSerializer extends Serializer[List[String]] with Deserializer[List[String]] {
    def serialize(address : List[String]) = {
      val baos = new ByteArrayOutputStream(1024)
      val o = new ObjectOutputStream(baos)
      o.writeObject(address)
      ByteBuffer.wrap(baos.toByteArray)
    }

    def deserialize(ary : ByteBuffer) : Option[List[String]] = {
      val bis = new ByteArrayInputStream(ary.array)
      val ois = new ObjectInputStream(bis)
      val obj = ois.readObject
      ois.close()
      Some(obj.asInstanceOf[List[String]])
    }
  }

  implicit object StringSerializer extends Serializer[String] with Deserializer[String] {
    def serialize(str : String) = ByteBuffer.wrap(str.asInstanceOf[String].getBytes)
    def deserialize(ary : ByteBuffer) = Some(new String(ary.array))
  }
  
  implicit object ByteBufferSerializer extends Serializer[ByteBuffer] with Deserializer[ByteBuffer] {
    def serialize(ary : ByteBuffer) = ary
    def deserialize(ary : ByteBuffer) = Some(ary)
  }  
  
  implicit object ByteArraySerializer extends Serializer[Array[Byte]] with Deserializer[Array[Byte]] {
    def serialize(ary : Array[Byte]) = ByteBuffer.wrap(ary)
    def deserialize(ary : ByteBuffer) = Some(ary.array)
  }
  
  implicit object ByteSeqSerializer extends Serializer[Seq[Byte]] with Deserializer[Seq[Byte]] {
    def serialize(seq : Seq[Byte]) = ByteBuffer.wrap(seq.toArray)
    def deserialize(ary : ByteBuffer) = Some(ary.array.toSeq)
  }
  
  implicit object LongSerializer extends Serializer[Long] with Deserializer[Long] with 
      IntegralSerializer with IntegralDeserializer {
    def serialize(n : Long) = ByteBuffer.wrap(longToBytes(n))
    def deserialize(ary : ByteBuffer) = Some(bytesToLong(ary.array))
  }
  
  implicit object IntSerializer extends Serializer[Int] with Deserializer[Int] with
      IntegralSerializer with IntegralDeserializer {
    def serialize(n : Int) = ByteBuffer.wrap(longToBytes(n))
    def deserialize(ary : ByteBuffer) = Some(bytesToLong(ary.array).toInt)
  }
}

trait Serializer[-T] {
  def serialize(obj : T) : ByteBuffer
}

trait Deserializer[+T] {
  def deserialize(ary : ByteBuffer) : Option[T]
}
