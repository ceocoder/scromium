package scromium.serializers

import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

object Serializers {

    implicit object MapSerializer extends Serializer[Map[String, String]] with Deserializer[Map[String, String]] {

        def serialize(map: Map[String, String]) = {

        	val baos = new ByteArrayOutputStream(1024)
            val o = new ObjectOutputStream(baos)
            o.writeObject(map)
            baos.toByteArray
        }

        def deserialize(ary: Array[Byte]): Option[Map[String, String]] = {

            val bis = new ByteArrayInputStream(ary)
            val ois = new ObjectInputStream(bis)
            val obj = ois.readObject
            ois.close()
            Some(obj.asInstanceOf[Map[String, String]])
        }

    }

    implicit object ListStringSerializer extends Serializer[List[String]] with Deserializer[List[String]] {

        def serialize(address: List[String]) = {

            val baos = new ByteArrayOutputStream(1024)
            val o = new ObjectOutputStream(baos)
            o.writeObject(address)
            baos.toByteArray
        }

        def deserialize(ary: Array[Byte]): Option[List[String]] = {

            val bis = new ByteArrayInputStream(ary)
            val ois = new ObjectInputStream(bis)
            val obj = ois.readObject
            ois.close()
            Some(obj.asInstanceOf[List[String]])
        }
    }

    implicit object StringSerializer extends Serializer[String] with Deserializer[String] {
        def serialize(str: String) = str.asInstanceOf[String].getBytes
        def deserialize(ary: Array[Byte]) = Some(new String(ary))
    }

    implicit object ByteArraySerializer extends Serializer[Array[Byte]] with Deserializer[Array[Byte]] {
        def serialize(ary: Array[Byte]) = ary
        def deserialize(ary: Array[Byte]) = Some(ary)
    }

    implicit object ByteSeqSerializer extends Serializer[Seq[Byte]] with Deserializer[Seq[Byte]] {
        def serialize(seq: Seq[Byte]) = seq.toArray
        def deserialize(ary: Array[Byte]) = Some(ary.toSeq)
    }

    implicit object LongSerializer extends Serializer[Long] with Deserializer[Long] with IntegralSerializer with IntegralDeserializer {
        def serialize(n: Long) = longToBytes(n)
        def deserialize(ary: Array[Byte]) = Some(bytesToLong(ary))
    }

    implicit object IntSerializer extends Serializer[Int] with Deserializer[Int] with IntegralSerializer with IntegralDeserializer {
        def serialize(n: Int) = longToBytes(n)
        def deserialize(ary: Array[Byte]) = Some(bytesToLong(ary).toInt)
    }
}

trait Serializer[-T] {
    def serialize(obj: T): Array[Byte]
}

trait Deserializer[+T] {
    def deserialize(ary: Array[Byte]): Option[T]
}