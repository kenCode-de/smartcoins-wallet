package de.bitsharesmunich.cryptocoincore.base;

import com.google.gson.JsonObject;

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
    protected long id;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
                temp = new byte[encKey.length];
                System.arraycopy(encKey, 0, temp, 0, temp.length);

                byte[] encBrain = new BigInteger(jsonObject.get("encrypted_mnemonic").getAsString(), 16).toByteArray();
                while (encBrain[0] == 0) {
                    byte[] temp2 = new byte[encBrain.length - 1];
                    System.arraycopy(encBrain, 1, temp2, 0, temp2.length);
                    encBrain = temp2;
                }
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
            Logger.getLogger(AccountSeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JsonObject toJson(String password) {
        try {
            JsonObject answer = new JsonObject();
            answer.addProperty("type", this.type.name());
            StringBuilder mnemonic = new StringBuilder();
            for (String word : this.mnemonicCode) {
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
            answer.addProperty("additional", this.additional);
            return answer;
        } catch (UnsupportedEncodingException ex) {

        }
        return null;
    }
}
