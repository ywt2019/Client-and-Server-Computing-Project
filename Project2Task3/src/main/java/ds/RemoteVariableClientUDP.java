/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a UDP client who continuously send requests (that contains
 * numbers + id/id inputted through the program console) and receives the
 * id-corresponding sum after addition/subtraction/get from the server.
 */
package ds;

// imports required for UDP/IP, scanner for console inputs
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class RemoteVariableClientUDP {

    //global variables are declared here for use in functions outside main
    public static int serverPort; // store user-inputted server port number
    public static DatagramSocket aSocket = null; // initialize a socket
    public static InetAddress aHost; // initialize a host address variable

    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     */
    public static void main(String args[]){

        // initialize variables
        int num = 0;
        int result = 0;
        String ID = null;

        System.out.println("The UDP client is running.");

        try {

            // set up scanner to take user inputs from the console, for port number
            // https://www.geeksforgeeks.org/ways-to-read-input-from-console-in-java/
            Scanner s = new Scanner(System.in);
            System.out.println("Please enter server port:");
            serverPort = s.nextInt();

            // loops continues until user inputs choice 4
            while (true) {

                System.out.println("1. Add a value to your sum.");
                System.out.println("2. Subtract a value from your sum.");
                System.out.println("3. Get your sum.");
                System.out.println("4. Exit client");

                // set user input
                int choice = s.nextInt();

                // switch lead to different cases based on user input
                switch(choice){
                    case 1: // add
                        // store user inputs for value and id
                        System.out.println("Enter value to add:");
                        num = s.nextInt();
                        System.out.println("Enter your ID:");
                        s.nextLine();
                        ID = s.nextLine();

                        // use add function to send num+id to server and get resulting value
                        result = add(num,ID);
                        break;
                    case 2: // subtract
                        // store user inputs for value and id
                        System.out.println("Enter value to subtract:");
                        num = s.nextInt();
                        System.out.println("Enter your ID:");
                        s.nextLine();
                        ID = s.nextLine();

                        // use subtract function to send num+id to server and get resulting value
                        result = subtract(num,ID);
                        break;
                    case 3: // get
                        // store user inputs for id
                        System.out.println("Enter your ID:");
                        s.nextLine();
                        ID = s.nextLine();

                        // use get function to send id to server and get resulting value
                        result = get(ID);
                }

                // exit program, else output resulting value sent by server
                if (choice == 4) {break;}
                else { System.out.println("The result is " + result + "."); }
            }
        }catch (IOException e){System.out.println("IO Exception: " + e.getMessage());
        }finally {
            System.out.println("Client side quitting. The remote variable server is still running.");
            if(aSocket != null) aSocket.close();
        }
    }

    /**
     * This function sends the number i + id in a request to the
     * server for addition to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param i
     * @param ID
     * @return
     * @throws IOException
     */
    public static int add(int i, String ID) throws IOException {

        // concatenate id, operation, and number in a message
        // get byte array of the message
        byte[] m = (ID + ";" + "add" + ";" + Integer.toString(i)).getBytes();

        // use connectToSocket function to send such message and get reply from it
        DatagramPacket reply = connectToSocket(m);

        // return resulting value received from the server
        return java.nio.ByteBuffer.wrap(reply.getData()).getInt();
    }

    /**
     * This function sends the number i + id in a request to the
     * server for subtraction to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param i
     * @param ID
     * @return
     * @throws IOException
     */
    public static int subtract(int i, String ID) throws IOException {

        // concatenate id, operation, and number in a message
        // get byte array of the message
        byte[] m = (ID + ";" + "subtract" + ";" + Integer.toString(i)).getBytes();

        // use connectToSocket function to send such message and get reply from it
        DatagramPacket reply = connectToSocket(m);

        // return resulting value received from the server
        return java.nio.ByteBuffer.wrap(reply.getData()).getInt();
    }

    /**
     * This function sends an id in a request to the
     * server for the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param ID
     * @return
     * @throws IOException
     */
    public static int get(String ID) throws IOException {

        // concatenate id, operation, and number in a message
        // (get operation does not require a number, "1" here
        // is just a filler value for simpler splitting on the
        // server side)
        // get byte array of the message
        byte[] m = (ID + ";" + "get" + ";" + "1").getBytes();

        // use connectToSocket function to send such message and get reply from it
        DatagramPacket reply = connectToSocket(m);

        // return resulting value received from the server
        return java.nio.ByteBuffer.wrap(reply.getData()).getInt();
    }

    /**
     * This function is a helper function to store the procedure
     * of connecting to a server. It returns server reply to add,
     * subtract, and get functions.
     * @param m
     * @return reply
     * @throws IOException
     */
    public static DatagramPacket connectToSocket(byte[] m) throws IOException {

        // get host address
        aHost = InetAddress.getByName("localhost");

        // send int in bytes to the server
        aSocket = new DatagramSocket();
        DatagramPacket request = new DatagramPacket(m,  m.length, aHost, serverPort);
        aSocket.send(request);

        // prepare a buffer to receive data from server
        byte[] buffer = new byte[1000];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        aSocket.receive(reply);

        return reply;
    }
}
