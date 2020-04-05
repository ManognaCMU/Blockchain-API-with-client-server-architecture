/**
 * Author: Sai Manogna Pentyala
 * Last Modified: March 07, 2020
 * Andrew: spentyal
 *
 * This program demonstrates a very simple TCP server.
 * The request from the client is received through the
 * client input stream. The request includes a client ID,
 * operation, combined public key, signature and the
 * difficulty, block id, transaction, block data which
 * is used to perform the operation. The server will
 * make two checks before servicing any client request.
 * First, does the public key (included with each request)
 * hash to the ID (also provided with each request)?
 * Second, is the request properly signed? If both of these
 * are true, the request is carried out on behalf of the client.
 * The server will perform the respective operation. Otherwise,
 * the server returns the message “Error in request”.
 * When the computation is done, the reply is displayed,
 * which could either be an "OK" message
 * or the final value computed by the server.
 */



package com.spentyal.andrew;

//imports required for TCP client server communication
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

// marks the server in client server architecture
public class BlockChainTCPServer {

    public static void main(String args[]) {
        Socket clientSocket = null;
        try {

            // marks the starting of server
            System.out.println("Server Running");

            int serverPort = 7777; // the server port we are using

            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);

            // to determine the blockchain that persists across all clients
            List<BlockChain> resultList = new ArrayList<BlockChain>();

            // captures the response JSON string representation
            String jsonResponseString = "";

            /*
             * Forever,
             *   read a line from the socket
             *   print it to the console
             *   echo it (i.e. write it) back to the client
             */
            /** On the whole the process time it takes to add a block and repair the chain (1000-18000ms) is comparatively
             * higher than verifying the chain (0-4ms).
             * All the process times also varies depending on the machine and the applications running on it
             */
            while (true) {
                /*
                 * Block waiting for a new connection request from a client.
                 * When the request is received, "accept" it, and the rest
                 * the tcp protocol handshake will then take place, making
                 * the socket ready for reading and writing.
                 */
                clientSocket = listenSocket.accept();

                // Set up "in" to read from the client socket
                Scanner in;
                in = new Scanner(clientSocket.getInputStream());

                // Set up "out" to write to the client socket
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                // read the request from the client
                String request = in.nextLine();

                // determine the substring of the request to get only the main request
                request = request.substring(request.indexOf("{\"Request_From_Client\" : [ {") + "{\"Request_From_Client\" : [ {".length());
                request = request.substring(0, request.indexOf(" ] }") - 1);

                // split the request received from client on comma
                StringTokenizer st = new StringTokenizer(request, ",");
                // captures the request from the client
                Map<String, String> requestMap = new HashMap<>();

                // captures the client ID
                String clientID = "";
                // captures the operation
                int operation = 0;
                //part of public key
                BigInteger e = null;
                // modulus
                BigInteger n = null;
                // captures the signature
                String sign= "";
                // captures the difficulty of the block
                int difficulty = -1;
                // captures the transaction data of the block
                String transaction = "";
                // captures the index of the block
                int blockID = -1;
                // captures the data to be added to the block
                String blockData = "";

                // if the request is split
                if(st != null) {

                    // while there is more request to read
                    while(st.hasMoreTokens()) {
                        // capture each token
                        String entry = st.nextToken();
                        // split the entry on :
                        StringTokenizer str = new StringTokenizer(entry, ":");
                        // while there is more request to read
                        while(str.hasMoreTokens()) {
                            // capture each token
                            String key = str.nextToken();
                            // trim to remove spaces
                            key = key.trim();
                            // capture each token
                            String value = str.nextToken();
                            // trim to remove spaces
                            value = value.trim();
                            // put in the request map, to capture the request from the client
                            requestMap.put(key, value);
                        }
                    }

                    // for each key in the request map
                    for(Map.Entry<String, String> entry : requestMap.entrySet()) {
                        // if client ID exists in the request
                        if(entry.getKey().equals("\"clientID\"")) {
                            // capture the client ID
                            clientID = entry.getValue();
                        }

                        // if operation exists in the request
                        if(entry.getKey().equals("\"operation\"")) {
                            if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                                // capture the operation
                                operation = Integer.parseInt(entry.getValue());
                            }
                        }

                        // if the public key (part of) exists in the request
                        if(entry.getKey().equals("\"e\"")) {
                            // capture the part of public key
                            e = new BigInteger(entry.getValue());
                        }

                        // if the moudlus exists in the request
                        if(entry.getKey().equals("\"n\"")) {
                            // capture the modulus
                            n = new BigInteger(entry.getValue());
                        }

                        // if the signed request exists in the request
                        if(entry.getKey().equals("\"sign\"")) {
                            // capture the signed request
                            sign = entry.getValue();
                        }

                        // if the difficulty of the block exists in the request
                        if(entry.getKey().equals("\"difficulty\"")) {
                            if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                                // captures the difficulty of the block
                                difficulty = Integer.parseInt(entry.getValue());
                            }
                        }

                        // if the transaction of the block exists in the request
                        if(entry.getKey().equals("\"transaction\"")) {
                            // captures the transaction of the block
                            transaction = entry.getValue();
                        }

                        // if the index of the block exists in the request
                        if(entry.getKey().equals("\"blockID\"")) {
                            if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                                // captures the index of the block
                                blockID = Integer.parseInt(entry.getValue());
                            }
                        }

                        // if the data to be entered on the block exists
                        if(entry.getKey().equals("\"blockData\"")) {
                            // captures the data to be entered on the block
                            blockData = entry.getValue();
                        }

                    }
                }

                // concatenation of n and e gives the public key
                String publicKey = String.valueOf(e).concat(String.valueOf(n));

                // captures the request to be signed
                String combinedRequest = "";

                // the combined public key and operation are concatenated for decrypting if the operation is 0, 2, 3 or 5
                if(operation == 0 || operation == 2 || operation == 3 || operation == 5) {
                    combinedRequest = clientID.concat(publicKey).concat(String.valueOf(operation));
                    // the combined public key, operation, difficulty, transaction are concatenated for decrypting if the operation is 1
                } else if(operation == 1) {
                    combinedRequest = clientID.concat(publicKey).concat(String.valueOf(operation)).concat(String.valueOf(difficulty).concat(transaction));
                    // the combined public key, operation, blockID, blockData are concatenated for decrypting if the operation is 4
                } else if(operation == 4) {
                    combinedRequest = clientID.concat(publicKey).concat(String.valueOf(operation)).concat(String.valueOf(blockID).concat(blockData));
                }

                // The server will make two checks before servicing any client request. First, does the
                // public key hash to the ID. Second, is the request properly signed.
                // If both of these are true, the request
                // is carried out on behalf of the client.
                if (publicKeyHashValid(publicKey, clientID) && signVerified(e, n, combinedRequest, sign)) {
                    // determines the blockchain
                    resultList  = selectTheBlockChain(clientID, resultList);
                    // performs the respective operation selected by the user
                    jsonResponseString = performRequestedOperation(operation, resultList.get(0), difficulty, transaction, blockID, blockData);

                    // OK is returned from the server if the operation is addition or subtraction
                    System.out.println("Response from Server: OK");

                    // sends the response to the client
                    out.println(jsonResponseString);
                    out.flush();
                } else {
                    // If the server does not validate the two conditions mentioned above,
                    // then below error is sent to the client
                    jsonResponseString = "{\"Error\" : " + "Error In Request }";

                    // sends the response to the client
                    out.println(jsonResponseString);
                    out.flush();
                }

            }

            // Handle IO Exception
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            // If quitting (typically by you sending quit signal) clean up sockets
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    // close client socket
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }


    /** to persist the blockchain **/
    private static List<BlockChain> selectTheBlockChain(String clientID, List<BlockChain> resultList) {

        // if the genesis block is not created
        if (resultList != null && resultList.isEmpty()) {

            // creating a BlockChain object
            BlockChain blockChain = new BlockChain();
            //  The Genesis block will be created with a difficulty of 2, index of 0.
            Block genesisBlock = new Block(0, new Timestamp(System.currentTimeMillis()), "Genesis", 2);
            // adding the block to the blockchain
            blockChain.addBlock(genesisBlock);
            // puts the block chain associated with the client ID in the resultMap
            resultList.add(blockChain);

        }

        // returns the blockchain
        return resultList;

    }

    /** performs computation based on the operation chosen **/
    private static String performRequestedOperation(int operation, BlockChain blockChain, int difficulty, String transaction, int blockID, String blockData) {

        // captures the response JSON string representation
        String responseJSONString = "";

        // if operation is 0
        if(operation == 0) {
            // view the block chain status
            responseJSONString = blockChain.viewBasicBlockChainStatus();
            // if operation is 1
        } else if(operation == 1) {
            // add transaction to the block chain
            responseJSONString = blockChain.addTransactionToBlockChain(difficulty, transaction);
            // if operation is 2
        } else if(operation == 2) {
            // captures the start time of the process
            long startTime = System.currentTimeMillis();
            // determines if the chain is valid
            boolean isValid = blockChain.isChainValid();
            // captures the end time of the process
            long endTime = System.currentTimeMillis();
            // captures the index of the invalid block
            int invalidBlockIndex = BlockChain.getInvalidBlockIndex();
            // captures a string that represents the number of leftmost hex digits to be present for the invali block
            String hashTarget = BlockChain.getHashTargetforInvalidBlock();
            // specifies the total time of the process
            long executionTime = endTime - startTime;

            // captures the response JSON string representation
            String blockChainString = "";

            blockChainString = blockChainString.concat("{");
            // captures the operation
            blockChainString = blockChainString.concat("\"Operation\" : ").concat("2");
            // captures the Execution Time
            blockChainString = blockChainString.concat(",\"Execution Time\" : ").concat(String.valueOf(executionTime));
            // captures the isValid
            blockChainString = blockChainString.concat(",\"isValid\" : ").concat(String.valueOf(isValid));
            // captures the invalidBlockIndex
            blockChainString = blockChainString.concat(",\"invalidBlockIndex\" : ").concat(String.valueOf(invalidBlockIndex));
            if(hashTarget != null && !hashTarget.isEmpty()) {
                // captures the hashTarget
                blockChainString = blockChainString.concat(",\"hashTarget\" : ").concat(hashTarget);
            }
            blockChainString = blockChainString.concat("}");
            responseJSONString = blockChainString;
            // if operation is 3
        } else if(operation == 3) {
            // view the JSON representation of the blockchain
            responseJSONString = blockChain.viewBlockChain();
            // if operation is 4
        } else if(operation == 4) {
            // corrupt the block chain
            blockChain.corruptBlockChain(blockID, blockData);

            // captures the response JSON string representation
            String blockChainString = "";
            blockChainString = blockChainString.concat("{");
            // captures the operation
            blockChainString = blockChainString.concat("\"Operation\" : ").concat("4");
            blockChainString = blockChainString.concat("}");
            responseJSONString = blockChainString;
            // if operation is 5
        } else if(operation == 5) {
            // captures the start time of the process
            long startRepairTime = System.currentTimeMillis();
            // repairs the corrupted blockchain
            blockChain.repairChain();
            // captures the end time of the process
            long endRepairTime = System.currentTimeMillis();
            // captures the total time of the process
            long executionTime = endRepairTime - startRepairTime;

            // captures the response JSON string representation
            String blockChainString = "";

            blockChainString = blockChainString.concat("{");
            // captures the operation
            blockChainString = blockChainString.concat("\"Operation\" : ").concat("5");
            // captures the Execution Time
            blockChainString = blockChainString.concat(",\"Execution Time\" : ").concat(String.valueOf(executionTime));
            blockChainString = blockChainString.concat("}");
            responseJSONString = blockChainString;
        }

        // returns the response JSON string representation
        return responseJSONString;
    }

    /** used to validate if the request is signed correctly by decrypting using private key **/
    private static boolean signVerified(BigInteger e, BigInteger n, String messageToCheck, String encryptedSign) throws Exception {
        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedSign);
        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Get the bytes from messageToCheck
        byte[] bytesOfMessageToCheck = messageToCheck.getBytes("UTF-8");

        // compute the digest of the message with SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] messageToCheckDigest = md.digest(bytesOfMessageToCheck);

        // messageToCheckDigest is a full SHA-256 digest
        // and add a zero byte in the beginning
        byte[] messageDigest = new byte[messageToCheckDigest.length + 1];
        messageDigest[0] = 0;   // most significant set to 0


        for(int i=1; i<messageDigest.length; i++) {
            messageDigest[i] = messageToCheckDigest[i-1]; // take all byte from SHA-256
        }

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(messageDigest);

        // inform the client on how the two compare
        if(bigIntegerToCheck.compareTo(decryptedHash) == 0) {
            return true;
        }
        else {
            return false;
        }

    }

    // check if the public key hash to the request ID
    private static boolean publicKeyHashValid(String publicKey, String reqID) {

        // compute the SHA 256 hash value
        String computedReqID = ComputeSHA_256_as_Hex_String(publicKey);

        // check if the public key hash to the request ID
        if(computedReqID.equalsIgnoreCase(reqID)) {
            return true;
        } else {
            return false;
        }
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
