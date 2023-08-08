/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a client who continuously send requests (that contains
 * messages inputted through the program console) and receives replies from the
 * local host through a user-chosen port. It echos the reply message from the server.
 */
package org.example;

// imports required for UDP/IP
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoClientUDP{
    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     */
    public static void main(String args[]){

        System.out.println("The UDP client is running.");

        // initialize a socket
        DatagramSocket aSocket = null;

        try {
            // get host address
            InetAddress aHost = InetAddress.getByName("localhost");

            // set up scanner to take user inputs from the console, for port number
            // https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
            Scanner s = new Scanner(System.in);

            // set port number to use
            System.out.println("Server side port number:");
            int serverPort = s.nextInt();

            // set a socket for connection use later
            aSocket = new DatagramSocket();

            // read input from console, using a buffered reader
            String nextLine;
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

            // loop continues as long as there is valid user input but breaks when user inputs "halt!"
            while ((nextLine = typed.readLine()) != null) {

                // get byte representation of each line of the input
                byte [] m = nextLine.getBytes();

                // attach message(in byte array form) to a data packet for sending the request to server
                DatagramPacket request = new DatagramPacket(m,  m.length, aHost, serverPort);
                aSocket.send(request); //send data in bytes to the server

                // prepare a buffer(with custom size) to contain byte array messages
                byte[] buffer = new byte[1000];

                // attach such buffer to a datagram packet to get the server reply
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);

                // set replyString to the right size through creating a new string with request data
                // http://underpop.online.fr/j/java/help/example-udp-echo-server-client-java-game.html.gz
                String replyString = new String(request.getData(), 0, request.getLength());

                // echo server's reply
                System.out.println("Reply from server: " + replyString);

                // quit loop if user types "halt!" and the server has echoed such message
                if (replyString.equals("halt!")) break;
            }

        }catch (SocketException e) {System.out.println("Socket Exception: " + e.getMessage());
        }catch (IOException e){System.out.println("IO Exception: " + e.getMessage());
        }finally {
            // client quiting message
            System.out.println("UDP Client side quitting");

            // close the socket at last
            if(aSocket != null) aSocket.close();
        }
    }
}