package scromium.thrift

import org.apache.thrift.transport.TFramedTransport
import org.apache.thrift.transport.TTransport
import org.apache.thrift.transport.{ TSocket, TTransportException }
import java.net.InetAddress
import java.net.Socket

class ThriftSocketFactory {    

    @throws(classOf[TTransportException])
    def make(host: String, port: Int, framed: Boolean): TTransport = {
        val addy = InetAddress.getByName(host)
        val socket = new Socket(addy, port)
        if(framed)
        	new TFramedTransport(new TSocket(socket))        	
        else
        	new TSocket(socket)
    }
}
