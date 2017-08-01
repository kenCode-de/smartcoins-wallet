package de.bitsharesmunich.cryptocoincore.base;

import com.google.gson.JsonObject;

import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.base.seed.Brainkey;
import de.bitsharesmunich.graphenej.Util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the account Seed class.
 *
 * With this class we can obtain a seed to generate keys for generated addresses
 */

public abstract class AccountSeed {
    /**
     * The id on the database
     */
    protected long mId = -1;
    /**
     * The seed type
     */
    protected SeedType mType;
    /**
     * the list of words
     */
    protected List<String> mMnemonicCode;
    /**
     * An additional value, it depends on the seed type
     */
    protected String mAdditional;

    /**
     * Gets the seed generated
     * @return the seed to be used directly on the creation of keys
     */
    public abstract byte[] getSeed();

    /**
     * Gets the seed type
     * @return The seed type
     */
    public SeedType getType() {
        return this.mType;
    }

    /**
     * Get the list of words
     * @return A list with each word
     */
    public List<String> getMnemonicCode() {
        return this.mMnemonicCode;
    }

    /**
     * Gets the list of words
     * @return The words as Strign separated with space
     */
    public String getMnemonicCodeString(){
        StringBuilder answer = new StringBuilder();
        for(String word : this.mMnemonicCode){
            answer.append(word);
            answer.append(" ");
        }
        answer.deleteCharAt(answer.length()-1);
        return answer.toString();
    }

    /**
     * Get the additional value
     * @return The additional value
     */
    public String getAdditional() {
        return this.mAdditional;
    }

    /**
     * GEts the id of the database of this seed
     * @return The id of the database
     */
    public long getId() {
        return this.mId;
    }

    /**
     * Changes the id of the database
     * @param id The id of the database
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * TRansform a json object into a seed object
     * @param jsonObject The json object
     * @param password the password to be used
     * @return Teh Account Seed created
     */
    public static AccountSeed fromJson(JsonObject jsonObject, String password) {
        try {
            String typeString = jsonObject.get("type").getAsString();
            String mnemonic;
            if (jsonObject.has("mnemonic")) {
                mnemonic = jsonObject.get("mnemonic").getAsString();
            } else {

                byte[] encKey_enc = new BigInteger(jsonObject.get("encryption_key").getAsString(), 16).toByteArray();
                byte[] temp = new byte[encKey_enc.length - (encKey_enc[0] == 0 ? 1 : 0)];
                System.arraycopy(encKey_enc, (encKey_enc[0] == 0 ? 1 : 0), temp, 0, temp.length);
                byte[] encKey = Util.decryptAES(temp, password.getBytes("UTF-8"));
                assert encKey != null;
                temp = new byte[encKey.length];
                System.arraycopy(encKey, 0, temp, 0, temp.length);

                byte[] encBrain = new BigInteger(jsonObject.get("encrypted_mnemonic").getAsString(), 16).toByteArray();
                while (encBrain[0] == 0) {
                    byte[] temp2 = new byte[encBrain.length - 1];
                    System.arraycopy(encBrain, 1, temp2, 0, temp2.length);
                    encBrain = temp2;
                }
                assert (Util.decryptAES(encBrain, temp)) != null;
                mnemonic = new String((Util.decryptAES(encBrain, temp)), "UTF-8");

            }
            String additional = jsonObject.get("additional").getAsString();

            switch (typeString) {
                case "BIP39":
                    return new BIP39(mnemonic, additional);
                case "BrainKey":
                    return new Brainkey(mnemonic, Integer.parseInt(additional));
                default:
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            //TODO handler best this exception
            Logger.getLogger(AccountSeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Transform this seed to a json object, to be saved in the bin file
     * @param password The password to be used to encrypt the json object
     * @return The Json Object
     */
    public JsonObject toJson(String password) {
        try {
            JsonObject answer = new JsonObject();
            answer.addProperty("type", this.mType.name());
            StringBuilder mnemonic = new StringBuilder();
            for (String word : this.mMnemonicCode) {
                mnemonic.append(word);
                mnemonic.append(" ");
            }
            mnemonic.deleteCharAt(mnemonic.length() - 1);
            if (password.isEmpty()) {
                answer.addProperty("mnemonic", getMnemonicCodeString());
            } else {
                byte[] encKey = new byte[32];
                new SecureRandom().nextBytes(encKey);
                byte[] encKey_enc = Util.encryptAES(encKey, password.getBytes("UTF-8"));
                byte[] encMnem = Util.encryptAES(mnemonic.toString().getBytes("ASCII"), encKey);
                answer.addProperty("encryption_key", Util.bytesToHex(encKey_enc));
                answer.addProperty("encrypted_mnemonic", Util.bytesToHex(encMnem));
            }
            answer.addProperty("additional", this.mAdditional);
            return answer;
        } catch (UnsupportedEncodingException ex) {
            //TODO this exception never rises
        }
        return null;
    }
}
