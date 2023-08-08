/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a TCP client who continuously send requests (that contains
 * numbers + id/id inputted through the program console) and receives the
 * id-corresponding sum after addition/subtraction/get from the server.
 */
import java.net.*;
import java.io.*;

public class RemoteVariableClientTCP {

    //global variables are declared here for use in functions outside main
    static BufferedReader in;
    static PrintWriter out;
    static Socket clientSocket;

    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        // initialize variables
        int num = 0;
        int result = 0;
        String ID = null;

        System.out.println("The UDP client is running.");

        // set port number to use
        System.out.println("Please enter server port:");
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
        int serverPort = Integer.parseInt(typed.readLine());

        try {
            // set socket and create in/out stream serving as input to and output from the server
            clientSocket = new Socket("localhost", serverPort);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            while (true) {

                // menu options
                System.out.println("1. Add a value to your sum.");
                System.out.println("2. Subtract a value from your sum.");
                System.out.println("3. Get your sum.");
                System.out.println("4. Exit client");

                int choice = Integer.parseInt(typed.readLine());

                // different cases based on input choice
                switch(choice){
                    case 1:
                        System.out.println("Enter value to add:");
                        num = Integer.parseInt(typed.readLine());
                        System.out.println("Enter your ID:");
                        ID = typed.readLine();
                        result = add(num,ID);
                        break;
                    case 2:
                        System.out.println("Enter value to subtract:");
                        num = Integer.parseInt(typed.readLine());
                        System.out.println("Enter your ID:");
                        ID = typed.readLine();
                        result = subtract(num,ID);
                        break;
                    case 3:
                        System.out.println("Enter your ID:");
                        ID = typed.readLine();
                        result = get(ID);
                }
                if (choice == 4) {
                    System.out.println("Client side quitting. The remote variable server is still running.");
                    break;
                }
                else { System.out.println("The result is " + result + "."); }
            }
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
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
     * This function sends the number i + op +id in a request to the
     * server for addition to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param i
     * @param ID
     * @return
     * @throws IOException
     */
    public static int add(int i, String ID) throws IOException {

        String m = ID + ";" + "add" + ";" + i;
        return sendToReceiveFromServer(m);
    }

    /**
     * This function sends the number i + op + id in a request to the
     * server for subtraction to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param i
     * @param ID
     * @return
     * @throws IOException
     */
    public static int subtract(int i, String ID) throws IOException {

        String m = ID + ";" + "subtract" + ";" + i;
        return sendToReceiveFromServer(m);
    }

    /**
     * This function sends an id + op in a request to the
     * server for the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param ID
     * @return
     * @throws IOException
     */
    public static int get(String ID) throws IOException {

        String m = ID + ";" + "get" + ";" + "1";
        return sendToReceiveFromServer(m);
    }

    /**
     * This function is a helper function to store the procedure
     * of connecting to a server. It returns server reply to add,
     * subtract, and get functions.
     * @param m
     * @return
     * @throws IOException
     */
    static int sendToReceiveFromServer(String m) throws IOException {

        // print to server
        out.println(m);
        out.flush();

        // read a line of data from the server stream
        String data = in.readLine();

        return Integer.parseInt(data);
    }

}