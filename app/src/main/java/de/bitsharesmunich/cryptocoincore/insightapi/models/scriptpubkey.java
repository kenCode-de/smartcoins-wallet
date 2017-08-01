package de.bitsharesmunich.cryptocoincore.insightapi.models;

/**
 * The transasction Script public keym is used to validate
 */

public class ScriptPubKey {
    /**
     * The code to validate in hex
     */
    public String hex;
    /**
     * the code to validate this transaction
     */
    public String asm;
    /**
     * the acoin address involved
     */
    public String[] addresses;
    /**
     * The type of the hash
     */
    public String type;
}
