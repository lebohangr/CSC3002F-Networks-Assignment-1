import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    public static void main(String [] args){

        try{

            DatagramSocket socket = new DatagramSocket(5000);
        

        while(true){

            byte[] buffer = new byte[50];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            System.out.println("Message received: "+ new String(buffer, 0, packet.getLength()));

            String returnMessage = "message: "+ new String(buffer, 0 ,packet.getLength());
            byte[] buffer2 = returnMessage.getBytes();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer2, buffer2.length, address, port);
            socket.send(packet);
            }
        }

        catch(SocketException e){
            System.out.println("SocketException: "+e.getMessage());

        }
        catch(IOException e){
            System.out.print("IOException: "+e.getMessage());
        }

    }
    
}
