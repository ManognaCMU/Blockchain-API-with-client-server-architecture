/**
 * Author: Sai Manogna Pentyala
 * Last Modified: March 07, 2020
 * Andrew: spentyal
 *
 * This program demonstrates a very simple TCP client.
 * The request from the client is sent through the
 * printWriter object to the server. The request is
 * entered by the user, which includes a clientID,operation,
 * e, n, sign, difficulty, transaction, blockID, blockData
 * The program then blocks waiting for the server to perform
 * the requested operation. When the response arrives,
 * the reply is displayed (based on the operation chosen)
 * If the client chooses exit option the menu is no longer
 * shown to the client and the client stops running.
 */

package com.spentyal.andrew;

// imports for TCP client server communication
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// marks the client in client server architecture
public class BlockChainTCPClient {

    public static void main(String args[]) {

        try {

            // marks the starting of client
            System.out.println("Client Running");
            System.out.println();

            // Each public and private key consists of an exponent and a modulus
            BigInteger n; // n is the modulus for both the private and public keys
            BigInteger e; // e is the exponent of the public key
            BigInteger d; // d is the exponent of the private key

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

            //System.out.println(" RSA Public Key = " + e);  // Step 6: (e,n) is the RSA public key
            //System.out.println(" RSA Private Key = " + d);  // Step 7: (d,n) is the RSA private key
            //System.out.println(" Modulus for both the RSA keys = " + n);  // Modulus for both keys

            // captures the combined public key, e and n
            String combinedPublicKey = String.valueOf(e).concat(String.valueOf(n));
            // captures the client ID by hashing the combined public key
            String clientID = ComputeSHA_256_as_Hex_String(combinedPublicKey);

            // forever, until exited by the user
            // menu is provided to the user to choose
            /** On the whole the process time it takes to add a block and repair the chain (1000-18000ms) is comparatively
             * higher than verifying the chain (0-4ms).
             * All the process times also varies depending on the machine and the applications running on it
             */
            while (true) {

                System.out.println("0. View basic blockchain status.");
                System.out.println("1. Add a transaction to the blockchain.");
                System.out.println("2. Verify the blockchain.");
                System.out.println("3. View the blockchain.");
                System.out.println("4. Corrupt the chain.");
                System.out.println("5. Hide the corruption by repairing the chain.");
                System.out.println("6. Exit.");
                System.out.println();

                // read the input from the client
                Scanner sc = new Scanner(System.in);
                // captures the operation chosen
                int operation = sc.nextInt();
                // captures the request to be signed
                String combinedRequest = "";
                // captures the difficulty of the block
                int difficulty = -1;
                // captures the transaction data of the block
                String transaction = "";
                // captures the index of the block
                int blockID = -1;
                // captures the data to be added to the block
                String blockData = "";

                // captures whether the user wants to exit
                boolean exitFlag = false;

                // based on the operation chosen by the user
                switch (operation) {

                    // indicates that the user wants to view the basic block chain status
                    case 0:
                        // client ID, combinedPublicKey, operation concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation));
                        System.out.println();
                        break;

                    // indicates that the user wants to add a new transaction to the block chain
                    /** For adding a block, the process time it takes varies based on the difficulty level.
                     *  The process time increases as the difficulty level increases.
                     *  It takes around 100-3000ms for difficulty 4,
                     *  around 1500ms - 6000ms for difficulty 5,
                     *  around 10000ms - 150000ms for difficulty 6
                     */
                    case 1:
                        // prompt for and then read the difficulty level for this block
                        difficulty = getDifficultyFromUser();
                        // prompt for and then read the transaction data for this block
                        transaction = getTransactionToBeAdded();
                        // client ID, combinedPublicKey, operation, difficulty, transaction concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation)).concat(String.valueOf(difficulty).concat(transaction));
                        System.out.println();
                        break;

                    // indicates that the user wants to verify if the block chain is valid
                    /** For verifying a block, the process time it takes is very less (Around 1ms to 4ms)
                     * irrespective of the difficulty level.
                     */
                    case 2:
                        System.out.println();
                        // tells that it started verifying the entire chain
                        System.out.println("Verifying entire chain");
                        // client ID, combinedPublicKey, operation concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation));
                        break;


                    // indicates that the user wants to view the blockchain
                    case 3:
                        System.out.println();
                        System.out.println("View the Blockchain");
                        // client ID, combinedPublicKey, operation concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation));
                        System.out.println();
                        break;


                    // indicates that the user wants to corrupt the block in the blockchain
                    case 4:
                        System.out.println();
                        System.out.println("Corrupt the Blockchain");
                        // prompt for and then read the blockID
                        blockID = getBlockIDToCorrupt();
                        // prompt for and then read the data to be entered
                        blockData = getNewDataForBlock(blockID);
                        // client ID, combinedPublicKey, operation, blockID, blockData concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation)).concat(String.valueOf(blockID)).concat(blockData);
                        break;

                    // indicates that the user wants to repair the blockchain
                    /** For repairing a block, the process time it takes varies based on the difficulty level.
                     */
                    case 5:
                        System.out.println();
                        // begins the repair of the block chain
                        System.out.println("Repairing the entire chain");
                        // client ID, combinedPublicKey, operation concatenated as the combined request to be signed
                        combinedRequest = clientID.concat(combinedPublicKey).concat(String.valueOf(operation));
                        System.out.println();
                        break;

                    // indicates the user wants to exit from the menu
                    case 6:
                        exitFlag = true;
                        break;

                    // the user has to enter only a number from 0 to 6
                    default:
                        System.out.println("Please select only one of the below options");
                        System.out.println();
                        System.out.println();
                        break;
                }

                // if user chose exit option
                if (exitFlag) {
                    break;
                }

                //the combined request is signed using d and n
                String signedVal = sign(combinedRequest, d, n);

                // once the user enters all the input values, the server is called to pass the inputs as a request
                String result = callServerToPerformOperation(clientID, operation, e, n, signedVal, difficulty, transaction, blockID, blockData);

                // if the response is returned from the client
                if(result != null && !result.isEmpty()) {
                    // if the sign is not verified, throw the below error
                    if(result.contains("\"Error\" :")) {
                        System.out.println("Error In Request");
                        // if the operation is 3 or 4
                    } else if(operation == 3 || operation == 4) {
                        // display the JSON representation of the blockchain to the client
                        displayJSONToTheClient(operation, result, blockID, blockData);
                    } else {
                        // display the response to the client
                        displayTheResponseToTheClient(result);
                    }
                    System.out.println();
                }
            }
            //handle exception
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    /** prompt for and then read the data for the block */
    public static String getNewDataForBlock(int blockID) {

        // ask the user to enter the new data for the block to be corrupted
        System.out.println("Enter new data for block " + blockID);
        // read the input from the client
        Scanner sc = new Scanner(System.in);
        // return the new data for the block
        return sc.nextLine();
    }

    /** prompt for and then read the blockID */
    public static int getBlockIDToCorrupt() {

        // ask the user to enter the blockID to corrupt
        System.out.println("Enter block ID of block to Corrupt");
        // read the input from the client
        Scanner sc = new Scanner(System.in);
        // return the block ID of the block to be corrupted
        return sc.nextInt();
    }


    /** display the JSON representation of the blockchain to the client */
    private static void displayJSONToTheClient(int operation, String result, int blockID, String blockData) {

       if(operation == 3) {
           // modifying the response such that each block in the block chain comes on a seperate line
           result = result.replaceAll("},", "},\n");
           result = result.replaceAll(" ]", "\n ]");
           // show the JSON representation of the blockchain to the client
           System.out.println(result);
           // if the operation is 4
       } else if(operation == 4) {
           // show it as an output to the client
           System.out.println("Block " + blockID + " now holds " + blockData);
           System.out.println();
       }
    }


    /** prompt for and then read the transaction data for the block */
    public static String getTransactionToBeAdded() {

        // ask the user to enter transaction
        System.out.println("Enter transaction");
        // read the input from the client
        Scanner sc = new Scanner(System.in);
        //return the transaction data
        return sc.nextLine();

    }

    /** prompt for and then read the difficulty level for the block */
    public static int getDifficultyFromUser() {

        // ask the user to enter difficulty
        System.out.println("Enter difficulty > 0");
        // read the input from the client
        Scanner sc = new Scanner(System.in);
        // return the difficulty input
        return sc.nextInt();

    }


    /** display the response to the client */
    private static void displayTheResponseToTheClient(String result) {

        // determine the substring of the response to get only the main response
        result = result.substring(result.indexOf("{") + "{".length());
        result = result.substring(0, result.indexOf("}"));

        // split the response received from server on comma
        StringTokenizer st = new StringTokenizer(result, ",");
        // captures the response from the server
        Map<String, String> responseMap = new HashMap<>();

        // if the response is split
        if(st != null) {

            // while there is more response to read
            while (st.hasMoreTokens()) {
                // capture each token
                String entry = st.nextToken();
                // split the entry on :
                StringTokenizer str = new StringTokenizer(entry, ":");
                // while there is more response to read
                while (str.hasMoreTokens()) {
                    // capture each token
                    String key = str.nextToken();
                    // trim to remove spaces
                    key = key.trim();
                    // capture each token
                    String value = str.nextToken();
                    // trim to remove spaces
                    value = value.trim();
                    // put in the response map, to capture the response from the server
                    responseMap.put(key, value);
                }
            }

            // captures the operation
            int operation = -1;

            // if Operation exists in the response
            if (responseMap.get("\"Operation\"") != null) {
                // capture the operation
                operation = Integer.parseInt(responseMap.get("\"Operation\""));
            }

            // captures the noOfBlocksOnChain
            int noOfBlocksOnChain = 0;
            // captures the currentHashesPerSecond
            int currentHashesPerSecond = 0;
            // captures the difficultOfMostRecentBlock
            int difficultOfMostRecentBlock = 0;
            // captures the nonceOfMostRecentBlock
            String nonceOfMostRecentBlock = "";
            // captures the chainHash
            String chainHash = "";
            // captures the executionTime
            String executionTime = "";
            // captures the invalidBlockIndex
            int invalidBlockIndex = 0;
            // captures the hashTarget
            String hashTarget = "";
            // captures the isValid
            String isValid = "";

            // for each key in the response map
            for (Map.Entry<String, String> entry : responseMap.entrySet()) {

                // if Current size of chain exists in the response
                if (entry.getKey().equals("\"Current size of chain\"")) {
                    if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                        // captures the noOfBlocksOnChain
                        noOfBlocksOnChain = Integer.parseInt(entry.getValue());
                    }
                    // if Current hashes per second by this machine exists in the response
                } else if (entry.getKey().equals("\"Current hashes per second by this machine\"")) {
                    if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                        // captures the currentHashesPerSecond
                        currentHashesPerSecond = Integer.parseInt(entry.getValue());
                    }
                    // if difficultOfMostRecentBlock exists in the response
                } else if (entry.getKey().equals("\"Difficulty of most recent block\"")) {
                    if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                        // captures the difficultOfMostRecentBlock
                        difficultOfMostRecentBlock = Integer.parseInt(entry.getValue());
                    }
                    // if Nonce for most recent block exists in the response
                } else if (entry.getKey().equals("\"Nonce for most recent block\"")) {
                    // captures the nonceOfMostRecentBlock
                    nonceOfMostRecentBlock = entry.getValue();
                    // if Chain hash exists in the response
                } else if (entry.getKey().equals("\"Chain hash\"")) {
                    // captures the chainHash
                    chainHash = entry.getValue();
                    // if Execution Time exists in the response
                } else if(entry.getKey().equals("\"Execution Time\"")) {
                    // captures the executionTime
                    executionTime = entry.getValue();
                    // if invalidBlockIndex exists in the response
                } else if(entry.getKey().equals("\"invalidBlockIndex\"")) {
                    if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                        // captures the invalidBlockIndex
                        invalidBlockIndex = Integer.parseInt(entry.getValue());
                    }
                    // if hashTarget exists in the response
                } else if(entry.getKey().equals("\"hashTarget\"")) {
                    // captures the hashTarget
                    hashTarget = entry.getValue();
                    // if isValid exists in the response
                } else if(entry.getKey().equals("\"isValid\"")) {
                    // captures the isValid
                    isValid = entry.getValue();
                }
            }

            // if operation is 0
            if (operation == 0) {
                // shows the number of blocks in the chain
                System.out.println("Current size of chain: " + noOfBlocksOnChain);
                // shows the number of hashes per second
                System.out.println("Current hashes per second by this machine: " + currentHashesPerSecond);
                // shows the difficulty of the most recently added block
                System.out.println("Difficulty of most recent block: " + difficultOfMostRecentBlock);
                // shows the nonce of the most recently added block
                System.out.println("Nonce for most recent block: " + nonceOfMostRecentBlock);
                // shows the chain hash of the chain
                System.out.println("Chain hash: " + chainHash);
                // if operation is 1
            } else if (operation == 1) {
                // captures the total time of the process
                System.out.println("Total execution time to add this block was " + executionTime + " milliseconds");
                // if operation is 2
            } else if (operation == 2) {
                // if the chain is invalid
                if(isValid != null && isValid.equals("false")) {
                    // show the node which is invalid
                    System.out.println("..Improper hash on node " + invalidBlockIndex + " Does not begin with " + hashTarget);
                }
                // specifies whether chain is valid or invalid
                System.out.println("Chain verification: " + isValid);
                // captures the total time of the process
                System.out.println("Total execution time required to verify the chain was " + executionTime + " milliseconds");
                // if operation is 5
            } else if(operation == 5) {
                // captures the total time of the process
                System.out.println("Total execution time required to repair the chain was " + executionTime + " milliseconds");
            }
        }
    }


    /**
     * Signing proceeds as follows:
     * 1) Get the bytes from the string to be signed.
     * 2) Compute a SHA-256 digest of these bytes.
     * 3) Copy these bytes into a byte array that is one byte longer than needed.
     *    The resulting byte array has its extra byte set to zero. This is because
     *    RSA works only on positive numbers. The most significant byte (in the
     *    new byte array) is the 0'th byte. It must be set to zero.
     * 4) Create a BigInteger from the byte array.
     * 5) Encrypt the BigInteger with RSA d and n.
     * 6) Return to the caller a String representation of this BigInteger.
     */
    private static String sign(String hashCombinedRequest, BigInteger d, BigInteger n) throws Exception{

        // compute the digest with SHA-256
        byte[] bytesOfMessage = hashCombinedRequest.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // computes the digest
        byte[] bigDigest = md.digest(bytesOfMessage);

        // we add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[bigDigest.length + 1];
        messageDigest[0] = 0;   // most significant set to 0

        for(int i=1; i<messageDigest.length; i++) {
            messageDigest[i] = bigDigest[i-1]; // take other bytes from SHA-256
        }

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this as a big integer string
        return c.toString();
    }

    /** sends the (clientID, operation, e, n, sign, difficulty, transaction, blockID, blockData) as a request to the
     *  server, so that the server performs the operations and returns
     *  a response back to the client
     */
    private static String callServerToPerformOperation(String clientID, int operation, BigInteger e, BigInteger n, String sign, int difficulty, String transaction, int blockID, String blockData) {

        // arguments supply hostname
        Socket clientSocket = null;
        try {

            // create an InetAddress object from a DNS name for "localhost"
            InetAddress aHost = InetAddress.getByName("localhost");

            // port used for communication is 7777
            int serverPort = 7777;
            clientSocket = new Socket(aHost, serverPort);

            // to read the user input
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // to send the data to the server
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            // determines the JSON string representation to be sent as a request to the client
            String jsonString  = designJSONString(clientID, operation, e, n, sign, difficulty, transaction, blockID, blockData);

            // sending the request (clientID, operation, e, n, sign, difficulty, transaction, blockID, blockData)  to the server
            out.println(jsonString);
            out.flush();

            // read the response from the server
            String response = in.readLine();
            return response;

            // handles IO Exceptions
        } catch (IOException ex) {
            System.out.println("IO Exception:" + ex.getMessage());
            return "";
        } finally {
            try {
                if (clientSocket != null) {
                    // closes the clientsocket
                    clientSocket.close();
                }
            } catch (IOException ex) {
                // ignore exception on close
            }
        }
    }

    /**  determines the JSON string representation to be sent as a request to the client */
    private static String designJSONString(String clientID, int operation, BigInteger e, BigInteger n, String sign, int difficulty, String transaction, int blockID, String blockData) {

        // captures the request JSON string representation
        String blockchainJson = "";
        // captures the request JSON string representation
        String finalJsonString = "";

        // captures the clientID
        blockchainJson = blockchainJson.concat("{\"clientID\" : ");
        // captures the operation
        blockchainJson = blockchainJson.concat(String.valueOf(clientID)).concat(",\"operation\" : ");
        // captures the e
        blockchainJson = blockchainJson.concat(String.valueOf(operation)).concat(",\"e\" : ");
        // captures the n
        blockchainJson = blockchainJson.concat(String.valueOf(e)).concat(",\"n\": ");
        // captures the sign
        blockchainJson = blockchainJson.concat(String.valueOf(n)).concat(",\"sign\" : ").concat(sign);
        // if operation is 1
        if(operation == 1) {
            // captures the difficulty
            blockchainJson = blockchainJson.concat(",\"difficulty\" : ").concat(String.valueOf(difficulty));
            // captures the transaction
            blockchainJson = blockchainJson.concat(",\"transaction\" : ").concat(transaction);
        }
        if(operation == 4) {
            // captures the blockID
            blockchainJson = blockchainJson.concat(",\"blockID\" : ").concat(String.valueOf(blockID));
            // captures the blockData
            blockchainJson = blockchainJson.concat(",\"blockData\" : ").concat(blockData);
        }
        blockchainJson = blockchainJson.concat("}");

        // beginning of the request to the server
        finalJsonString = finalJsonString.concat("{\"Request_From_Client\" : [ ");
        // complete request
        finalJsonString = finalJsonString.concat(blockchainJson);
        // ending of the request to the server
        finalJsonString = finalJsonString.concat(" ] }");

        // returns the JSON string representation of the request to the server
        return finalJsonString;
    }


    /** computes SHA 256 hash value for the text string **/
    public static String ComputeSHA_256_as_Hex_String(String text) {

        try {
            // Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            // allocate room for the result of the hash
            byte[] hashBytes;
            // perform the hash
            digest.update(text.getBytes("UTF-8"), 0, text.length());
            // collect result
            hashBytes = digest.digest();

            // create another array with 20 bytes to get only the least significant 20 bytes
            byte[] finalHashBytes = new byte[20];
            int count = 0;

            // take the least significant 20 bytes from hashBytes to finalHashBytes
            for(int i=hashBytes.length-20; i < hashBytes.length; i++) {
                finalHashBytes[count] = hashBytes[i];
                count++;
            }

            // convert to Hex string
            return convertToHex(finalHashBytes);
        }
        // handles NoSuchAlgorithmException
        catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        }
        // handles UnsupportedEncodingException
        catch (UnsupportedEncodingException uee ) {
            System.out.println("Unsupported encoding exception thrown " + uee);
        }
        return null;
    }

    // code from Stack overflow
    // converts a byte array to a string.
    // each nibble (4 bits) of the byte array is represented
    // by a hex characer (0,1,2,3,...,9,a,b,c,d,e,f)
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

}
