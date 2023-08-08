/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a TCP server (local host).
 * It continuously receives numbers+op+id from the client
 * and performs addition/subtraction/get to an id-corresponding sum.
 * It then returns the sum in a reply to the client.
 */
import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class RemoteVariableServerTCP {

    public static Map<String, Integer> treeMap = new TreeMap<>();

    /**
     * no args was to be inputted in a command line argument,
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        String op;
        String id;
        int numToBeOperated;
        int result = 0;
        int serverPort = 6789; // the server port we are using
        ServerSocket listenSocket = new ServerSocket(serverPort); // create a new server socket

        System.out.println("Server started");
        Socket clientSocket = null;
        try {

            /*
             * Block waiting for a new connection request from a client.
             * When the request is received, "accept" it, and the rest
             * the tcp protocol handshake will then take place, making
             * the socket ready for reading and writing.
             */
            clientSocket = listenSocket.accept();
            // If we get here, then we are now connected to a client.

            // Set up "in" to read from the client socket
            Scanner in;
            in = new Scanner(clientSocket.getInputStream());

            // Set up "out" to write to the client socket
            PrintWriter out;
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            /*
             * Forever,
             *   read a line from the socket
             *   print it to the console
             *   echo it (i.e. write it) back to the client
             */
            while (true) {

                if (in.hasNext()) {
                    String data = in.nextLine();

                    // split data from client with ";" to corresponding id, op, and num
                    id = data.split(";")[0];
                    op = data.split(";")[1];
                    numToBeOperated = Integer.parseInt(data.split(";")[2]);

                    if (!treeMap.containsKey(id)) {
                        treeMap.put(id, 0);
                    }

                    switch (op) {
                        case "add":
                            result = add(numToBeOperated, id);
                            break;
                        case "subtract":
                            result = subtract(numToBeOperated, id);
                            break;
                        case "get":
                            result = get(id);
                    }

                    System.out.println("Visitor: " + id + ", Operation requested: " + op + ", Value returned: " + result);

                    out.println(result);
                    out.flush();
                }
                else {
                    clientSocket = listenSocket.accept();
                    in = new Scanner(clientSocket.getInputStream());
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
                }
                // Handle exceptions
            }
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());

            // If quitting (typically by you sending quit signal) clean up sockets
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /**
     * add i to the id-corresponding sum and return the sum
     * @param i
     * @param id
     * @return
     */
    static int add(int i, String id){
        treeMap.replace(id, treeMap.get(id) + i);
        return treeMap.get(id);
    }

    /**
     * subtract i from the id-corresponding sum and return the sum
     * @param i
     * @param id
     * @return
     */
    static int subtract(int i, String id){
        treeMap.replace(id, treeMap.get(id)-i);
        return treeMap.get(id);
    }

    /**
     * return the id-corresponding sum
     * @param id
     * @return
     */
    static int get(String id){
        return treeMap.get(id);
    }
}