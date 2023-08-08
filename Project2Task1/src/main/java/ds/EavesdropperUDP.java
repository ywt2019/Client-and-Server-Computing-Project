/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a "server" to client
 * and a "client" to the actual server. It listens
 * to messages from client, who mistakenly connect
 *  to its port 6798, and append a "!" to the message,
 *  and finally send the changed message to the actual
 *  server.
 */
package ds;

// imports required for UDP/IP & exception imports
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class EavesdropperUDP {
    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     */
    public static void main(String args[]){

        System.out.println("The UDP Eavesdropper is running.");

        // set up scanner to take user inputs from the console, for listen port number
        // https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
        Scanner s = new Scanner(System.in);

        // set port number to listen to
        System.out.println("Listen on port number:");
        int listenPort = s.nextInt();

        // port number to mask as (in this case: 6798)
        System.out.println("Masquerading as port number:");
        int maskPort = s.nextInt();

        // initialize a socket for connection use later
        DatagramSocket aSocket = null;

        // prepare a buffer(with custom size) to contain byte array messages
        byte[] buffer = new byte[1000];

        try{

            aSocket = new DatagramSocket(maskPort);

            while(true){

                // receive and message sent by client
                DatagramPacket fromClientRequest = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(fromClientRequest);

                // set replyString to the right size through putting request in an array
                // http://underpop.online.fr/j/java/help/example-udp-echo-server-client-java-game.html.gz
                String requestString = new String(fromClientRequest.getData(), 0, fromClientRequest.getLength());

                System.out.println("Received client request: "+ requestString);
                System.out.println("Reply to client with normal message: "+ requestString);

                // change client message
                if (!requestString.equals("halt!")) requestString += "!";

                //reply to client with original message content
                DatagramPacket toClientReply = new DatagramPacket(fromClientRequest.getData(),
                        fromClientRequest.getLength(), fromClientRequest.getAddress(), fromClientRequest.getPort());
                aSocket.send(toClientReply);

                // request to server with changed message
                InetAddress aHost = InetAddress.getLocalHost();
                byte [] m = requestString.getBytes();
                DatagramPacket toServerRequest = new DatagramPacket(m,  m.length, aHost, listenPort);
                aSocket.send(toServerRequest);

                System.out.println("Request to server with masked message: "+ requestString);

                // get reply from server
                DatagramPacket fromServerReply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(fromServerReply);

                System.out.println("Reply from server: "+ requestString);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {
            if(aSocket != null) aSocket.close();
        }

    }
}
