/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program verify the message and send back the sum
 */
package ds;

import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class VerifyingServerTCP {

    static MessageDigest md;
    static BigInteger e;
    static BigInteger n;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

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
        ServerSocket listenSocket = new ServerSocket(serverPort); // Create a new server socket
        String signature;

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

                    int index = data.lastIndexOf(";");
                    String priorTokens = data.substring(0, index);

                    id = data.split(";")[0];
                    e = new BigInteger(data.split(";")[1]);
                    n = new BigInteger(data.split(";")[2]);
                    op = data.split(";")[3];
                    numToBeOperated = Integer.parseInt(data.split(";")[4]);
                    signature = data.split(";")[5];

                    System.out.println("---------New Request---------");
                    System.out.println("e = " + e);  // Step 6: (e,n) is the RSA public key
                    System.out.println("n = " + n);  // Modulus for both keys

                    if (verify(id, e, n, priorTokens, signature)) {
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
                        System.out.println("Error in request");
                    }
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

    /**
     * verify signature and public key
     * @param id
     * @param e
     * @param n
     * @param priorTokens
     * @param signature
     * @return
     * @throws UnsupportedEncodingException
     */
    static boolean verify(String id, BigInteger e, BigInteger n, String priorTokens, String signature) throws UnsupportedEncodingException {
        //code below are referenced from ShortMessageSign
        boolean idCheckFlag = false;
        boolean sigCheckFlag = false;

        // check if public key hashes to id
        String publicKey = e.toString() + n.toString();

        byte[] bytesOfPublicKey = publicKey.getBytes("UTF-8");
        byte[] hashedPublicKey = md.digest(bytesOfPublicKey);
        byte[] addedExtraByte = new byte[hashedPublicKey.length+1]; // add extra zero in front
        addedExtraByte[0] = 0;
        System.arraycopy(hashedPublicKey,0,addedExtraByte,1,hashedPublicKey.length);
        BigInteger m = new BigInteger(addedExtraByte); // conver to Big Integer
        String idGenerated = m.toString();

        if (idGenerated.equals(id)) { idCheckFlag = true; }

        // check if priorTokens is equal to the decryption of signature, using public key
        BigInteger encryptedSignature = new BigInteger(signature); //convert signature to Big Integer and decrypt
        BigInteger decryptedSignature = encryptedSignature.modPow(e, n);

        byte[] bytesOfPriorTokens = priorTokens.getBytes("UTF-8"); //hash prior tokens
        byte[] priorTokensDigest = md.digest(bytesOfPriorTokens);

        byte[] extraByte = new byte[priorTokensDigest.length+1]; //add 0 to hash of prior tokens
        extraByte[0] = 0;
        System.arraycopy(priorTokensDigest,0,extraByte,1,priorTokensDigest.length);
        BigInteger priorTokensCheck = new BigInteger(extraByte); //convert to Big Integer

        if(priorTokensCheck.compareTo(decryptedSignature) == 0) { sigCheckFlag = true; }

        if (sigCheckFlag) { System.out.println("Valid signature"); }
        else { System.out.println("Invalid signature"); }

        return (idCheckFlag && sigCheckFlag);
    }
}