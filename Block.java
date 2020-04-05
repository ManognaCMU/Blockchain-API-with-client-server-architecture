/**
 * Author: Sai Manogna Pentyala
 * Last Modified: March 06, 2020
 * Andrew: spentyal
 *
 * This program demonstrates a very simple Block.
 * Each block is defined by
 * an index - position of the block on the chain
 * previous hash - SHA256 hash of a block's parent
 * data - block's single transaction details
 * timestamp - time of the block's creation.
 * nonce - determined by a proof of work routine.
 * difficulty - exact number of left most hex digits
 * needed by a proper hash
 */

package com.spentyal.andrew;

// imports needed to define a block in a block chain
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;

/**  represents a simple Block */
public class Block {

    // captures the position of the block on the chain
    private int index;
    // captures the SHA256 hash of a block's parent
    private String previousHash;
    // captures the block's single transaction details
    private String data;
    // a Java Timestamp object, it holds the time of the block's creation.
    private Timestamp timeStamp;
    // captures a BigInteger value determined by a proof of work routine.
    private BigInteger nonce = new BigInteger("0");
    // captures the exact number of left most hex digits needed by a proper hash
    private int difficulty;

    /** Block Constructor */
    public Block(int index, Timestamp timestamp, String data, int difficulty) {

        // This is the position within the chain. Genesis is at 0.
        this.index = index;
        // This is the time this block was added.
        this.timeStamp = timestamp;
        // This is the transaction to be included on the blockchain.
        this.data = data;
        // This is the number of leftmost nibbles that need to be 0.
        this.difficulty = difficulty;
    }

    /** getter method for index */
    public int getIndex() {
        // returns the index of this block in the chain
        return index;
    }

    /** setter method for index */
    public void setIndex(int index) {
        // sets the index of this block in the chain
        this.index = index;
    }

    /** getter method for previous hash */
    public String getPreviousHash() {
        // returns a hashpointer to this block's parent
        return previousHash;
    }

    /** setter method for previous hash */
    public void setPreviousHash(String previousHash) {
        // sets s hashpointer to this block's parent
        this.previousHash = previousHash;
    }

    /** getter method for data */
    public String getData() {
        // returns the transaction held by this block
        return data;
    }

    /** setter method for data */
    public void setData(String data) {
        // sets the transaction held by this block
        this.data = data;
    }

    /** getter method for timestamp */
    public Timestamp getTimeStamp() {
        // returns the timestamp of when the block is created
        return timeStamp;
    }

    /** setter method for timestamp */
    public void setTimeStamp(Timestamp timeStamp) {
        // sets the timestamp of when the block is created
        this.timeStamp = timeStamp;
    }

    /** getter method for nonce */
    public BigInteger getNonce() {
        // returns the nonce of the block
        return nonce;
    }

    /** setter method for nonce */
    public void setNonce(BigInteger nonce) {
        // sets the nonce of the block
        this.nonce = nonce;
    }

    /** getter method for difficulty */
    public int getDifficulty() {
        // returns how much work is required to produce a proper hash
        return difficulty;
    }

    /** setter method for difficulty */
    public void setDifficulty(int difficulty) {
        // sets how much work is required to produce a proper hash
        this.difficulty = difficulty;
    }

    /** computes SHA 256 hash value for the text string **/
    public String calculateHash() {

        try {
            // hash of the concatenation of index, timestamp, data, previoushash, nonce and difficulty
            String input = String.valueOf(index).concat(timeStamp.toString()).concat(data).concat(previousHash).concat(String.valueOf(nonce)).concat(String.valueOf(difficulty));
            // Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            // allocate room for the result of the hash
            byte[] hashBytes;
            // perform the hash
            digest.update(input.getBytes("UTF-8"), 0, input.length());
            // collect result
            hashBytes = digest.digest();
            // convert to Hex string
            return convertToHex(hashBytes);
            // handles the exception
        } catch(Exception e) {
            e.printStackTrace();
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

    /** A JSON representation of all of this block's data is returned. */
    @Override
    public String toString() {

        // returns the JSON representation of the block
        return "Block{" +
                "index=" + index +
                ", previousHash='" + previousHash + '\'' +
                ", data='" + data + '\'' +
                ", timeStamp=" + timeStamp +
                ", nonce=" + nonce +
                ", difficulty=" + difficulty +
                '}';
    }


    /** method to find a good hash */
    public String proofOfWork() {

        // compute a hash of the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty
        String blockHash  = calculateHash();

        // hash that has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field
        String target = new String(new char[difficulty]).replace('\0', '0');
        // until the hash of the block has the appropriate number of leading hex zeroes
        while(!blockHash.substring( 0, difficulty).equals(target)) {
            // increment the nonce by 1
            nonce = nonce.add(new BigInteger("1"));
            // set the nonce
            setNonce(nonce);
            // compute a hash of the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty
            blockHash = calculateHash();
        }

        // return the hash that has the appropriate number of leading hex zeroes
        return blockHash;
    }

}
