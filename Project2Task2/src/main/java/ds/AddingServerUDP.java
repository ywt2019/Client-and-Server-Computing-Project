/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a server (local host).
 * It continuously receives numbers from the client
 * and adds such numbers to a global variable sum.
 * It then returns the sum in a reply to the client.
 */
package ds;

// imports required for UDP/IP
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class AddingServerUDP {

    // variable to accumulate value sent from client
    public static int sum = 0;

    /**
     * no args was to be inputted in a command line argument,
     * @param args
     */
    public static void main(String args[]){

        System.out.println("Server started");

        // initialize a socket
        DatagramSocket aSocket = null;

        // prepare a buffer(with custom size) to contain byte array messages
        byte[] buffer = new byte[1000];

        try{
            // construct a socket with user-inputted port for connection
            aSocket = new DatagramSocket(6789);

            // attach buffer to a datagram packet to get the server reply
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);

            // loop continues forever
            while(true){

                // receive request from client
                aSocket.receive(request);

                // wrap request message into a number
                // https://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
                int numToBeAdded = java.nio.ByteBuffer.wrap(request.getData()).getInt();

                // add such number
                add(numToBeAdded);

                // put the sum in a 4-byte array
                // https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
                byte[] m = ByteBuffer.allocate(4).putInt(sum).array();

                // package the sum in the reply
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());

                System.out.println("Returning sum of " + sum + " to client");

                //send reply to client
                aSocket.send(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }

    /**
     * add i to the global variable sum
     * @param i
     */
    static void add(int i){
        System.out.println("Adding: " + i + " to " + sum);
        sum+=i;
    }
}
