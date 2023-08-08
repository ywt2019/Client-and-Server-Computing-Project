/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program serves as a UDP server (local host).
 * It continuously receives numbers+op+id from the client
 * and performs addition/subtraction/get to an id-corresponding sum.
 * It then returns the sum in a reply to the client.
 *
 */
package ds;

// imports and exceptions required for UDP/IP, scanner for console inputs
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

public class RemoteVariableServerUDP {
    public static Map<String, Integer> treeMap = new TreeMap<>(); // treeMap for storing id,sum pairs

    /**
     * no args was to be inputted in a command line argument,
     * @param args
     */
    public static void main(String args[]){

        System.out.println("Server started");

        // initialize local variables
        String op;
        String id;
        int numToBeOperated;
        int result = 0;

        // initialize a socket
        DatagramSocket aSocket = null;

        // prepare a buffer(with custom size) to contain byte array messages
        byte[] buffer = new byte[1000];

        try{
            // set the socket for connection
            aSocket = new DatagramSocket(6789);

            // attach buffer to a datagram packet to get the server reply
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);

            while(true){
                aSocket.receive(request);

                // put request data into a string of appropriate size
                // http://underpop.online.fr/j/java/help/example-udp-echo-server-client-java-game.html.gz
                String requestString = new String(request.getData(), 0, request.getLength());

                // split request String to relevant id, op, num parts
                id = requestString.split(";")[0];
                op = requestString.split(";")[1];
                numToBeOperated = Integer.parseInt(requestString.split(";")[2]);

                // if treeMap does not contain the key, a new key pair (with sum = 0) is created
                if (!treeMap.containsKey(id)){
                    treeMap.put(id,0);
                }

                // choose different operation (and thus different functions) based on op in request
                switch(op){
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

                // package into bytes and send reply to client
                // https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
                byte[] m = ByteBuffer.allocate(4).putInt(result).array();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
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
