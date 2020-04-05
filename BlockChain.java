/**
 * Author: Sai Manogna Pentyala
 * Last Modified: March 06, 2020
 * Andrew: spentyal
 *
 * This program demonstrates a very simple blockchain.
 * It will begin by creating a BlockChain object and
 * then adding the Genesis block to the chain.
 * All blocks added to the Blockchain will have a difficulty.
 * It is menu driven and will continously provide the user
 * with seven options. Based on the option selected by
 * the user the corresponding action is performed.
 */
package com.spentyal.andrew;

//imports for Blockchain processing
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** This class represents a simple BlockChain.*/
public class BlockChain {

    // an ArrayList to hold Blocks
    List<Block> chain;
    // chain hash to hold a SHA256 hash of the most recently added Block
    String chainHash;
    // index of the invalid block
    static int invalidBlockIndex;
    // string that represents the number of leftmost hex digits to be present for the invali block
    static String hashTargetforInvalidBlock;

    // Blockchain constructor
    public BlockChain() {
        // an empty ArrayList for Block storage
        chain = new ArrayList<Block>();
        // sets the chain hash to the empty string
        chainHash = "";
    }

    /** repairs the blockchain */
    public void repairChain() {

        // for every block in the blockchain
        for(int i=0; i<getChainSize(); i++) {
            // each block in the blockchain
            Block currentBlock = chain.get(i);
            // hash that has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field
            String hashTarget = new String(new char[currentBlock.getDifficulty()]).replace('\0', '0');

            // if the block is invalid
            if(!currentBlock.calculateHash().substring( 0, currentBlock.getDifficulty()).equals(hashTarget)) {
                // determine the actual hash by computing the proof of work
                String actualHash = currentBlock.proofOfWork();
                // if a block next to the current block exists
                if(i < getChainSize()-1 && chain.get(i+1) != null) {
                    // set the computed hash as the previous hash to the next block
                    chain.get(i+1).setPreviousHash(actualHash);
                    // if its the last block in the blockchain
                } else {
                    // set the computed hash as the chainhash
                    chainHash = actualHash;
                }
            }
        }
    }

    /** changes the transaction data for the existing block */
    public void corruptBlockChain(int blockID, String blockData) {

        // corrupt the block by modifying its transaction data
        chain.get(blockID).setData(blockData);
    }

    /** display the entire Blockchain contents as a correctly formed JSON document */
    public String viewBlockChain() {

        // JSON string for each block in the chain
        String blockchainJson = "";
        // complete JSON string for the entire blockchain
        String finalBlockchainJson = "";

        // for each block in the block chain
        for(Block eachBlock : chain) {
            // captures the index of the block
            blockchainJson = blockchainJson.concat("{\"index\" : ");
            // captures the timestamp of the block
            blockchainJson = blockchainJson.concat(String.valueOf(eachBlock.getIndex())).concat(",\"time stamp \" : \"");
            // captures the TX of the block
            blockchainJson = blockchainJson.concat(eachBlock.getTimeStamp().toString()).concat("\",\"Tx \": \"");
            // captures the prevhash of the block
            blockchainJson = blockchainJson.concat(eachBlock.getData()).concat("\",\"PrevHash\" : \"");
            // captures the nonce of the block
            blockchainJson = blockchainJson.concat(eachBlock.getPreviousHash()).concat("\",\"nonce\" : ");
            // captures the difficulty of the block
            blockchainJson = blockchainJson.concat(String.valueOf(eachBlock.getNonce())).concat(",\"difficulty\": ");
            blockchainJson = blockchainJson.concat(String.valueOf(eachBlock.getDifficulty())).concat("},");
        }

        // to omit the last comma
        blockchainJson  = blockchainJson.substring(0, blockchainJson.length()-1);

        // beginning of the block chain
        finalBlockchainJson = finalBlockchainJson.concat("{\"ds_chain\" : [ ");
        // contents of each block in the blockchain
        finalBlockchainJson = finalBlockchainJson.concat(blockchainJson);
        // captures the chainhash of the block chain
        finalBlockchainJson = finalBlockchainJson.concat(" ], \"chainHash\":\"" + chainHash + "\"}");

        // returns the JSON representation of the block chain
        return finalBlockchainJson;

    }

    /** A new Block is being added to the BlockChain */
    public void addBlock(Block newBlock) {

        // if genesis block to be added
        if(chain.size() == 0) {
            // previous hash for the genesis block is set as empty string
            newBlock.setPreviousHash("");
            // if genesis block already exists
        } else {
            // This new block's previous hash must hold the hash of the most recently added block
            newBlock.setPreviousHash(chain.get(getChainSize() - 1).proofOfWork());
        }

        // adding the block to the chain
        chain.add(newBlock);
        // chainHash contains the hash of the most recently added block
        chainHash = newBlock.proofOfWork();

    }


    /** If the chain only contains one block, the genesis block at position 0, this routine computes the hash of
     * the block and checks that the hash has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field.
     */
    public boolean isChainValid() {

        // If the chain only contains one block
        if(getChainSize() == 1) {

            // genesis block at the position 0
            Block genesisBlock = chain.get(0);
            // hash that has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field
            String hashTarget = new String(new char[genesisBlock.getDifficulty()]).replace('\0', '0');

            /** if  hash of the block has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field,
             * and, if chain hash is equal to this computed hash, return true. Else return false
             */
            if(genesisBlock.calculateHash().substring(0, genesisBlock.getDifficulty()).equals(hashTarget) && chainHash != null && chainHash.equalsIgnoreCase(genesisBlock.calculateHash())) {
                return true;
            } else {
                // captures the index of the invalid block
                invalidBlockIndex = 0;
                // captures the hash target of the invalid block
                hashTargetforInvalidBlock = hashTarget;
                return false;
            }

            // if the chain has more than one block
        } else if(getChainSize() > 1) {

            // genesis block at the position 0
            Block genesisBlock = chain.get(0);
            // hash that has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field
            String hashTarget = new String(new char[genesisBlock.getDifficulty()]).replace('\0', '0');

            /** if  hash of the block has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field,
             * and, if chain hash is equal to this computed hash, return true. Else return false
             */
            if(genesisBlock.calculateHash().substring(0, genesisBlock.getDifficulty()).equals(hashTarget)) {
                //nothing
            } else {
                // captures the index of the invalid block
                invalidBlockIndex = 0;
                // captures the hash target of the invalid block
                hashTargetforInvalidBlock = hashTarget;
                return false;
            }

            // for each block till the end of the chain
            for(int i=1; i < getChainSize(); i++) {
                // current block
                Block currentBlock = chain.get(i);
                // block previous to the current block
                Block previousBlock = chain.get(i-1);
                // hash that has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field
                hashTarget = new String(new char[currentBlock.getDifficulty()]).replace('\0', '0');

                /** The first check will involve a computation of a hash in Block 0 and a comparison with the hash pointer in Block 1.
                 * If not same return false */
                if(!previousBlock.calculateHash().equals(currentBlock.getPreviousHash())){
                    // captures the index of the invalid block
                    invalidBlockIndex = currentBlock.getIndex();
                    // captures the hash target of the invalid block
                    hashTargetforInvalidBlock = hashTarget;
                    return false;
                }

                /** if  hash of the block has the requisite number of leftmost 0's (proof of work) as specified in the difficulty field,
                 *  If not, return false
                 */
                if(!currentBlock.calculateHash().substring( 0, currentBlock.getDifficulty()).equals(hashTarget)) {
                    // captures the index of the invalid block
                    invalidBlockIndex = currentBlock.getIndex();
                    // captures the hash target of the invalid block
                    hashTargetforInvalidBlock = hashTarget;
                    return false;
                }
            }


            //  if chain hash is equal to the computed hash of the last block in the chain. If not return false;
            if(!chainHash.equals(chain.get(getChainSize() - 1).calculateHash())) {
                return false;
            }
        }
        // if chain is valid, return true
        return true;

    }


    /** add transaction to the block chain */
    public String addTransactionToBlockChain(int difficulty, String transaction) {

        // captures the start time of the process
        long startTime = System.currentTimeMillis();

        // create a block containing that transaction
        Block newBlock = new Block(getChainSize(), getTime(), transaction, difficulty);
        // add the block to the block chain
        addBlock(newBlock);

        // captures the end time of the process
        long endTime = System.currentTimeMillis();

        // captures the total time of the process
        long executionTime =  endTime - startTime;

        // captures the response JSON string
        String blockChainString = "";

        blockChainString = blockChainString.concat("{");
        // captures the operation
        blockChainString = blockChainString.concat("\"Operation\" : ").concat("1");
        // captures the execution time
        blockChainString = blockChainString.concat(",\"Execution Time\" : ").concat(String.valueOf(executionTime));
        blockChainString = blockChainString.concat("}");

        // returns the JSON representation of the JSON string
        return blockChainString;
    }

    /** view the basic block chain status */
    public String viewBasicBlockChainStatus() {

        // captures the number of blocks in the chain
        int noOfBlocksOnChain = getChainSize();
        // captures the number of hashes per second
        int currentHashesPerSecond = hashesPerSecond();
        // captures the difficulty of the most recently added block
        int difficultOfMostRecentBlock = getLatestBlock().getDifficulty();
        // captures the nonce of the most recently added block
        BigInteger nonceOfMostRecentBlock = getLatestBlock().getNonce();

        // captures the response JSON representation
        String blockChainString = "";

        blockChainString = blockChainString.concat("{");
        // captures the operation
        blockChainString = blockChainString.concat("\"Operation\" : ");
        // captures the Current size of chain
        blockChainString = blockChainString.concat("0").concat(",\"Current size of chain\" : ");
        // captures the Current hashes per second by this machine
        blockChainString = blockChainString.concat(String.valueOf(noOfBlocksOnChain)).concat(",\"Current hashes per second by this machine\" : ");
        // captures the Difficulty of most recent block
        blockChainString = blockChainString.concat(String.valueOf(currentHashesPerSecond)).concat(",\"Difficulty of most recent block\" : ");
        // captures the Nonce for most recent block
        blockChainString = blockChainString.concat(String.valueOf(difficultOfMostRecentBlock)).concat(",\"Nonce for most recent block\" : ");
        // captures the Chain Hash
        blockChainString = blockChainString.concat(String.valueOf(nonceOfMostRecentBlock)).concat(",\"Chain hash\" : ").concat(chainHash);
        blockChainString = blockChainString.concat("}");

        // returns the response JSON representation
        return blockChainString;
    }

    /** determines a reference to the most recently added Block */
    public Block getLatestBlock() {
        // returns the reference to the most recently added Block
        return chain.get(getChainSize() - 1);
    }

    /** determines hashes per second of the computer holding this chain */
    public int hashesPerSecond() {
        // determines the start time of the process
        long startTime = System.currentTimeMillis();
        // determines the end time of the process
        long endTime = 0;
        // captures the number of hashes per second
        int noOfHashesPerSecond  = 0;
        // within a second
        while((endTime - startTime)/1000 < 1) {
            // determines the hash of the "00000000"
            calculateHash("00000000");
            // determines the end time of the process
            endTime = System.currentTimeMillis();
            // increments the number of hashes till a second
            noOfHashesPerSecond ++;
        }

        // returns the number of hashes per second
        return noOfHashesPerSecond;
    }

    /** computes SHA 256 hash value for the text string **/
    public void calculateHash(String input) {

        try {
            // Create a SHA256 digest
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            // allocate room for the result of the hash
            byte[] hashBytes;
            // perform the hash
            digest.update(input.getBytes("UTF-8"), 0, input.length());
            // collect result
            hashBytes = digest.digest();
            // handles exception
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** determines the size of the chain in blocks */
    public int getChainSize() {
        // returns the size of the chain
        return chain.size();
    }

    /** String representation of the entire chain is returned */
    @Override
    public String toString() {

        //  String representation of the entire chain is returned
        return "BlockChain{" +
                "chain=" + chain +
                ", chainHash='" + chainHash + '\'' +
                '}';
    }

    /** determines the index of the invalid block */
    public static int getInvalidBlockIndex() {
        // returns the index of the invalid block
        return invalidBlockIndex;
    }

    /** determines the number of leftmost 0s to be present on the invalid block */
    public static String getHashTargetforInvalidBlock() {
        // returns the number of leftmost 0s to be present on the invalid block
        return hashTargetforInvalidBlock;
    }

    /** getter method for timestamp */
    public Timestamp getTime() {
        //returns the current time
        return new Timestamp(System.currentTimeMillis());
    }

}
