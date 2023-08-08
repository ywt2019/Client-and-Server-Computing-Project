/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a client who continuously send requests (that contains
 * numbers inputted through the program console) and receives the sum after addition
 * of such numbers from the server.
 */
package ds;

// imports required for UDP/IP
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class AddingClientUDP {

    //global variables are declared here for use in functions outside main

    // store user-inputted server port number
    public static int serverPort;

    // initialize a socket
    public static DatagramSocket aSocket = null;

    // initialize a host address variable
    public static InetAddress aHost;

    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     */
    public static void main(String args[]){

        System.out.println("The UDP client is running.");

        try {

            // set up scanner to take user inputs from the console, for port number
            // https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
            Scanner s = new Scanner(System.in);
            System.out.println("Please enter server port:");
            serverPort = s.nextInt();

            // read input from console through a buffered reader
            String nextLine;
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

            // loop continues as long as there is valid user input but breaks when user inputs "halt!"
            while ((nextLine = typed.readLine()) != null) {

                // check for "halt!" input
                if (!nextLine.equals("halt!")) {

                    // collect sum replied by the server through the add function
                    int serverReplySum = add(Integer.parseInt(nextLine));

                    System.out.println("The server returned " + serverReplySum + ".");
                }
                else { break; }
            }
        }catch (IOException e){System.out.println("IO Exception: " + e.getMessage());
        }finally {
            // client quiting message
            System.out.println("UDP Client side quitting");

            // close the socket at last
            if(aSocket != null) aSocket.close();
        }
    }

    /**
     * This function sends the number i in a request to the server,
     * receives server's reply with a sum, and outputs the sum.
     * @param i
     * @return result of addition
     * @throws IOException
     */
    public static int add(int i) throws IOException {

        // put the adding number in a 4-byte array
        //https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
        byte[] m = ByteBuffer.allocate(4).putInt(i).array();

        // set host address
        aHost = InetAddress.getByName("localhost");

        // set socket
        aSocket = new DatagramSocket();

        // attach message(in byte array form) to a data packet for sending the request to server
        DatagramPacket request = new DatagramPacket(m,  m.length, aHost, serverPort);
        aSocket.send(request);

        // prepare a buffer(with custom size) to contain byte array messages received from the server
        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        aSocket.receive(reply);

        // the sum returned by the server is wrapped into an int and returned
        return java.nio.ByteBuffer.wrap(reply.getData()).getInt();
    }
}
