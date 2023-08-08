/**
 * Author: Wenting Yu (wy2)
 * Last Modified: Feb 24, 2023
 *
 * This program signs the message and send it to the server
 * receive sum from the server
 */
package ds;

import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class SigningClientTCP {

    // RSA code below is referenced from RSAExample.java in Project2Task5 Information section
    // Each public and private key consists of an exponent and a modulus
    static BigInteger n; // n is the modulus for both the private and public keys
    static BigInteger e; // e is the exponent of the public key
    static BigInteger d; // d is the exponent of the private key
    static String ID;
    static String publicKey;
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    static BufferedReader in;
    static PrintWriter out;
    static Socket clientSocket;

    /**
     * args was to be inputted in a command line argument,
     * but is set to be "localhost" in this case
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {
        String num;
        String encryptedNum;
        int result = 0;

        System.out.println("The UDP client is running.");
        System.out.println("Please enter server port:");
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

        //set port number to use
        int serverPort = Integer.parseInt(typed.readLine());

        createRSAKeys();
        generateClientID();

        try {
            clientSocket = new Socket("localhost", serverPort);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            while (true) {

                System.out.println("1. Add a value to your sum.");
                System.out.println("2. Subtract a value from your sum.");
                System.out.println("3. Get your sum.");
                System.out.println("4. Exit client");

                int choice = Integer.parseInt(typed.readLine());
                switch(choice){
                    case 1:
                        System.out.println("Enter value to add:");
                        num = typed.readLine();
                        result = add(num);
                        break;
                    case 2:
                        System.out.println("Enter value to subtract:");
                        num = typed.readLine();
                        result = subtract(num);
                        break;
                    case 3:
                        result = get();
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
     * This function sends the number i + op + id + e + n in a request to the
     * server for addition to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param message
     * @return
     * @throws IOException
     */
    public static int add(String message) throws IOException {

        String mToBeSigned = ID + ";" + e + ";" + n + ";" + "add" + ";" + message;

        String signedMessage = encrypt(mToBeSigned);

        String m = mToBeSigned + ";" + signedMessage;

        return sendToReceiveFromServer(m);
    }

    /**
     * This function sends the number i + op + id + e + n in a request to the
     * server for subtraction to the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @param message
     * @return
     * @throws IOException
     */
    public static int subtract(String message) throws IOException {

        String mToBeSigned = ID + ";" + e + ";" + n + ";" + "subtract" + ";" + message;

        String signedMessage = encrypt(mToBeSigned);

        String m = mToBeSigned + ";" + signedMessage;

        return sendToReceiveFromServer(m);
    }

    /**
     * This function sends an id + op + e + n in a request to the
     * server for the id-corresponding sum, receives
     * server's reply with a sum, and outputs the sum.
     * @return
     * @throws IOException
     */
    public static int get() throws IOException {

        String mToBeSigned = ID + ";" + e + ";" + n + ";" + "get" + ";" + "1";

        String signedMessage = encrypt(mToBeSigned);

        String m = mToBeSigned + ";" + signedMessage;

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

        out.println(m);
        out.flush();

        // read a line of data from the stream
        String data = in.readLine();

        return Integer.parseInt(data);
    }

    /**
     * This function creates RSA keys e,n, and d
     */
    static void createRSAKeys(){
        //code below is referenced from RSAExample.java in Project2Task5 Information section

        Random rnd = new Random();

        // Step 1: Generate two large random primes.
        // We use 400 bits here, but best practice for security is 2048 bits.
        // Change 400 to 2048, recompile, and run the program again and you will
        // notice it takes much longer to do the math with that many bits.
        BigInteger p = new BigInteger(400, 100, rnd);
        BigInteger q = new BigInteger(400, 100, rnd);

        // Step 2: Compute n by the equation n = p * q.
        n = p.multiply(q);

        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Step 4: Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger("65537");

        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);

        System.out.println("e = " + e);  // Step 6: (e,n) is the RSA public key
        System.out.println("d = " + d);  // Step 7: (d,n) is the RSA private key
        System.out.println("n = " + n);  // Modulus for both keys
    }

    /**
     * This function generates client id from public key
     * @throws UnsupportedEncodingException
     */
    static void generateClientID() throws UnsupportedEncodingException {
        //code below are referenced from ShortMessageSign

        // concatenate e and n
        publicKey = e.toString() + n.toString();

        // get byte array of public key and hash it
        byte[] bytesOfPublicKey = publicKey.getBytes("UTF-8");
        byte[] hashedPublicKey = md.digest(bytesOfPublicKey);

        // add extra byte
        byte[] addedExtraByte = new byte[hashedPublicKey.length+1];
        addedExtraByte[0] = 0;
        System.arraycopy(hashedPublicKey,0,addedExtraByte,1,hashedPublicKey.length);

        BigInteger m = new BigInteger(addedExtraByte);

        ID = m.toString();
    }

    /**
     * This function encrypts messages with RSA algorithm
     * @param message
     * @return
     * @throws UnsupportedEncodingException
     */
    static String encrypt(String message) throws UnsupportedEncodingException {
        //cited from ShortMessageSign.java

        byte[] bytesOfMessage = message.getBytes("UTF-8");
        byte[] hashOfMessage = md.digest(bytesOfMessage);

        byte[] posHashOfMessage = new byte[hashOfMessage.length+1];

        // most significant set to 0
        posHashOfMessage[0] = 0;
        System.arraycopy(hashOfMessage,0,posHashOfMessage,1,hashOfMessage.length);

        BigInteger m = new BigInteger(posHashOfMessage);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this as a big integer string
        return c.toString();
    }
}