import java.io.File;
import java.net.DatagramPacket;

public class Logger {
    DatagramPacket packet;
    File f;
    String filename;
    public Logger(DatagramPacket packet){
        this.packet = packet;
    }
}
