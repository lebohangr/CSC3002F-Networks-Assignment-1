/***
 * Author: Lebo Ramachela - RMCLEB001
 * Sample code acquired from @johnscanlon (daniweb.com)
 */

import java.io.*;  // Imported because we need the InputStream and OuputStream classes
import java.net.*; // Imported because the Socket class is needed
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Client {
    public static void main(String args[]) throws Exception {

        // The default port
        int clientport = 8188;
        String host = "localhost";

        if (args.length < 1) {
            System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);
        }
        // Get the port number to use from the command line
        else {
            //host = args[0];
            clientport = Integer.parseInt(args[0]);
            System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);
        }

        // Get the IP address of the local machine - we will use this as the address to send the data to
        InetAddress ia = InetAddress.getByName(host);

        //start sender and receiver threads
        SenderThread sender = new SenderThread(ia, clientport);
        sender.start();
        ReceiverThread receiver = new ReceiverThread(sender.getSocket());
        receiver.start();
    }
}

class SenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int serverport;
    private String username;

    public SenderThread(InetAddress address, int serverport) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }

    //method for performing checksum on data sent
    public static String getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        //  return crc32.getValue();
        //return checksum value formatted to hexadecimals to always have 8 bits.
        return String.format(Locale.US, "%08X", crc32.getValue());
    }

    public void run() {
        try {
            //send blank message
            byte[] data = new byte[1024];

            //get username from client
            System.out.println("Enter a username to identify with:");
            Scanner sn = new Scanner(System.in);
            username = sn.nextLine();
            //add trailing spaces to username
            String paddedUsername = String.format("%-10s", username);


            data = ("S"+username + " joined the chat").getBytes();
            DatagramPacket blankPacket = new DatagramPacket(data,data.length , serverIPAddress, serverport);
            udpClientSocket.send(blankPacket);




            // Create input stream
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                if (stopped)
                    return;

                // Message to send
                String clientMessage = inFromUser.readLine();

                if (clientMessage.equals("."))
                    break;

                // Create byte buffer to hold the message to send
                byte[] sendData = new byte[1024];

                //create checksum
                byte[] bytes = clientMessage.getBytes();
                String checksum = getCRC32Checksum(bytes);

                // Put this message into our empty buffer/array of bytes
                sendData = ("C"+checksum + paddedUsername + clientMessage).getBytes();

                // Create a DatagramPacket with the data, IP address and port number
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);

                // Send the UDP packet to server
                System.out.println(username + " (Me)"+ ": "+clientMessage);
                udpClientSocket.send(sendPacket);

                Thread.yield();
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
}

class ReceiverThread extends Thread {

    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int msgsReceived = 0;

    public ReceiverThread(DatagramSocket ds) throws SocketException {
        this.udpClientSocket = ds;
    }
    public void halt() {
        this.stopped = true;
    }

    //method for performing checksum on data received
    public static String getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        //  return crc32.getValue();
        return String.format(Locale.US, "%08X", crc32.getValue());
    }

    public void run() {

        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[1024];

        while (true) {
            if (stopped)
                return;

            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // Receive a packet from the server (blocks until the packets are received)
                udpClientSocket.receive(receivePacket);

                //have the client interpret the packet differently depending on the type of data received
                //C- client message, S- server response, A - user authentication, E - checksum
                switch (new String(receivePacket.getData(), 0, 1)){
                    case "C":
                        // Extract the reply from the DatagramPacket
                        String checksum = new String(receivePacket.getData(), 1, 8);
                        String username2 = new String(receivePacket.getData(), 9, 10);
                        String serverReply = new String(receivePacket.getData(), 19, receivePacket.getLength() - 19);

                        //remove trailing whitespace from username
                        username2 = username2.trim();

                        //perform checksum on packet received
                        byte[] bytes = serverReply.getBytes();
                        String checksum2 = getCRC32Checksum(bytes);

                        // print to the screen if checksum succeeds
                        if (checksum.equals(checksum2)) {
                            System.out.println("Checksum succeeded!");
                            System.out.println(username2 + ": " + serverReply);
                        }
                        break;


                    case "S":
                        serverReply = new String(receivePacket.getData(), 1, receivePacket.getLength());
                        System.out.println(serverReply);
                        break;
                }
                Thread.yield();
            }
            catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}

