package de.bitsharesmunich.cryptocoincore.base;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bitsharesmunich.cryptocoincore.base.seed.BIP39;
import de.bitsharesmunich.cryptocoincore.base.seed.Brainkey;
import de.bitsharesmunich.graphenej.Util;

/**
 * Created by henry on 05/02/2017.
 */

public abstract class AccountSeed {
    protected String id;
    protected SeedType type;
    protected List<String> mnemonicCode;
    protected String additional;

    public abstract byte[] getSeed();

    public SeedType getType() {
        return type;
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }

    public String getMnemonicCodeString(){
        StringBuilder answer = new StringBuilder();
        for(String word : mnemonicCode){
            answer.append(word);
            answer.append(" ");
        }
        answer.deleteCharAt(answer.length()-1);
        return answer.toString();
    }

    public String getAdditional() {
        return additional;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*public static AccountSeed loadFromJsonString(String jsonString, String password) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String typeString = jsonObject.getString("type");
            String mnemonic;
            if (jsonObject.has("mnemonic")) {
                mnemonic = jsonObject.getString("mnemonic");
            } else {
                byte[] encKey_enc = new BigInteger(jsonObject.getString("encryption_key"), 16).toByteArray();
                byte[] temp = new byte[encKey_enc.length - (encKey_enc[0] == 0 ? 1 : 0)];
                System.arraycopy(encKey_enc, (encKey_enc[0] == 0 ? 1 : 0), temp, 0, temp.length);
                byte[] encKey = Util.decryptAES(temp, password.getBytes("UTF-8"));
                temp = new byte[encKey.length];
                System.arraycopy(encKey, 0, temp, 0, temp.length);

                byte[] encBrain = new BigInteger(jsonObject.getString("encrypted_brainkey"), 16).toByteArray();
                while (encBrain[0] == 0) {
                    byte[] temp2 = new byte[encBrain.length - 1];
                    System.arraycopy(encBrain, 1, temp2, 0, temp2.length);
                    encBrain = temp2;
                }
                mnemonic = new String((Util.decryptAES(encBrain, temp)), "UTF-8");

            }
            String additional = jsonObject.getString("additional");

            switch (typeString) {
                case "BIP39":
                    return new BIP39(mnemonic, additional);
                case "BrainKey":
                    return new Brainkey(mnemonic, Integer.parseInt(additional));
                default:
                    break;
            }
        } catch (JSONException | UnsupportedEncodingException | NumberFormatException e) {
        }
        return null;
    }*/
}
