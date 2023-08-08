/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a server (local host).
 * It receives requests and sends replies to the
 * client. It also echoes request messages from
 * the client.
 */
package org.example;

// imports required for UDP/IP
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoServerUDP{
    /**
     * no args was to be inputted in a command line argument,
     * @param args
     */
    public static void main(String args[]){

        System.out.println("The UDP server is running.");
        System.out.println("Server side port number:");

        // set up scanner to take user inputs from the console, for port number
        // https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
        Scanner s = new Scanner(System.in);

        //set port number to use
        int serverPort = s.nextInt();

        // initialize a socket for connection use later
        DatagramSocket aSocket = null;

        // prepare a buffer(with custom size) to contain byte array messages
        byte[] buffer = new byte[1000];

        try{
            // construct a socket with user-inputted port for connection
            aSocket = new DatagramSocket(serverPort);

            // attach buffer to a datagram packet to get the server reply
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);

            // loop continues forever until client sends "halt!"
            while(true){

                // receive request from client
                aSocket.receive(request);

                // construct reply datagram packet with the same data from request (as we are echoing the client here)
                DatagramPacket reply = new DatagramPacket(request.getData(),
                        request.getLength(), request.getAddress(), request.getPort());

                // set requestString to the right size through creating a new string with request data
                // http://underpop.online.fr/j/java/help/example-udp-echo-server-client-java-game.html.gz
                String requestString = new String(request.getData(), 0, request.getLength());

                // echo request message sent by client
                System.out.println("Echoing: "+requestString);

                //send reply to client
                aSocket.send(reply);

                // quit loop if client sends "halt!"
                if (requestString.equals("halt!")) {
                    System.out.println("UDP Server side quitting");
                    break;
                }
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}
