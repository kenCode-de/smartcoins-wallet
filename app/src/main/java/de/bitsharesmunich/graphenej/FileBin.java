package de.bitsharesmunich.graphenej;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.CryptoCoinFactory;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.graphenej.crypto.AndroidRandomSource;
import de.bitsharesmunich.graphenej.crypto.SecureRandomStrengthener;

import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;


/**
 * Class to manage the Bin Files
 *
 * @author Henry Varona
 */
public abstract class FileBin {
    private static final String TAG = "FileBin";

    private static final int COMPRESS_TYPE = Util.LZMA;

    /**
     * Method to get the brainkey fron an input of bytes
     *
     * @param input Array of bytes of the file to be processed
     * @param password the pin code
     * @return the brainkey file, or null if the file or the password are
     * incorrect
     */
    public static String getBrainkeyFromByte(byte[] input, String password, Context context) {
        try {
            byte[] publicKey = new byte[33];
            byte[] rawData = new byte[input.length - 33];
            System.arraycopy(input, 0, publicKey, 0, publicKey.length);
            System.arraycopy(input, 33, rawData, 0, rawData.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ECKey randomECKey = ECKey.fromPublicOnly(publicKey);
            byte[] finalKey = randomECKey.getPubKeyPoint().multiply(ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKey()).normalize().getXCoord().getEncoded();
            MessageDigest md1 = MessageDigest.getInstance("SHA-512");
            finalKey = md1.digest(finalKey);
            rawData = decryptAES(rawData, byteToString(finalKey).getBytes());

            byte[] checksum = new byte[4];
            System.arraycopy(rawData, 0, checksum, 0, 4);
            byte[] compressedData = new byte[rawData.length - 4];
            System.arraycopy(rawData, 4, compressedData, 0, compressedData.length);
            byte[] wallet_object_bytes = null;
            try {
                wallet_object_bytes = Util.decompress(compressedData, Util.XZ);
            } catch (Exception e) {
                Log.e(TAG, "Exception while trying to decompress data using XZ");
            }
            if(wallet_object_bytes == null) {
                wallet_object_bytes = Util.decompress(compressedData, Util.LZMA);
            }
            if(wallet_object_bytes == null) {
                Log.e(TAG, "Bin file could not be decompressed");
                return null;
            }
            String wallet_string = new String(wallet_object_bytes, "UTF-8");
            JsonObject wallet;
            Log.d(TAG, "Trying to parse wallet_string: "+wallet_string);
            try{
                wallet=new JsonParser().parse(wallet_string).getAsJsonObject();
            }catch(Exception e){
                Log.e(TAG, "Exception while trying to parse wallet_string. Msg: "+e.getMessage());
                Log.e(TAG, "wallet_string: "+wallet_string);
                wallet_string = wallet_string + "}";
                wallet = new JsonParser().parse(wallet_string).getAsJsonObject();
            }
try {
    JsonObject scwallWallet = wallet.get("scwall_wallet").getAsJsonObject();
    JsontoDatabase(scwallWallet, password, context);
}catch(Exception e){
    e.printStackTrace();
}

            if (wallet.get("wallet").isJsonArray()) {
                wallet = wallet.get("wallet").getAsJsonArray().get(0).getAsJsonObject();
            } else {
                wallet = wallet.get("wallet").getAsJsonObject();
            }
            byte[] encKey_enc = new BigInteger(wallet.get("encryption_key").getAsString(), 16).toByteArray();
            byte[] temp = new byte[encKey_enc.length - (encKey_enc[0] == 0 ? 1 : 0)];
            System.arraycopy(encKey_enc, (encKey_enc[0] == 0 ? 1 : 0), temp, 0, temp.length);
            byte[] encKey = decryptAES(temp, password.getBytes("UTF-8"));
            temp = new byte[encKey.length];
            System.arraycopy(encKey, 0, temp, 0, temp.length);

            byte[] encBrain = new BigInteger(wallet.get("encrypted_brainkey").getAsString(), 16).toByteArray();
            while(encBrain[0] == 0){
                byte[]temp2 = new byte[encBrain.length-1];
                System.arraycopy(encBrain, 1, temp2, 0, temp2.length);
                encBrain = temp2;
            }
            String BrainKey = new String((decryptAES(encBrain, temp)), "UTF-8");
            return BrainKey;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
        }
        return null;
    }

    /**
     * Method to generate the file form a brainkey
     *
     * @param BrainKey The input brainkey
     * @param password The pin code
     * @param accountName The account name
     * @return The array byte of the file, or null if an error ocurred
     */
    public static byte[] getBytesFromBrainKey(String BrainKey, String password, String accountName, Context context) {
        byte[] result = null;
        try {
            byte[] encKey = new byte[32];
            SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
            randomStrengthener.addEntropySource(new AndroidRandomSource());
            SecureRandom secureRandom = randomStrengthener.generateAndSeedRandomNumberGenerator();
            secureRandom.nextBytes(encKey);
            byte[] encKey_enc = encryptAES(encKey, password.getBytes("UTF-8"));
            byte[] encBrain = encryptAES(BrainKey.getBytes("ASCII"), encKey);

            /**
             * Data to Store
             */
            JsonObject wallet = new JsonObject();
            wallet.add("encryption_key", new JsonParser().parse(byteToString(encKey_enc)));
            wallet.add("encrypted_brainkey", new JsonParser().parse(byteToString(encBrain)));
            JsonArray wallets = new JsonArray();
            wallets.add(wallet);
            JsonObject wallet_object = new JsonObject();
            wallet_object.add("wallet", wallets);
            JsonArray accountNames = new JsonArray();
            JsonObject jsonAccountName = new JsonObject();
            jsonAccountName.add("name", new JsonParser().parse(accountName));
            accountNames.add(jsonAccountName);
            wallet_object.add("linked_accounts", accountNames);

            wallet_object.add("scwall_wallet", DatabaseToJson(password,context));

            byte[] compressedData = Util.compress(wallet_object.toString().getBytes("UTF-8"), COMPRESS_TYPE);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] checksum = md.digest(compressedData);
            byte[] rawData = new byte[compressedData.length + 4];
            System.arraycopy(checksum, 0, rawData, 0, 4);
            System.arraycopy(compressedData, 0, rawData, 4, compressedData.length);
            byte[] randomKey = new byte[32];
            secureRandom.nextBytes(randomKey);
            ECKey randomECKey = ECKey.fromPrivate(md.digest(randomKey));
            byte[] randPubKey = randomECKey.getPubKey();
            byte[] finalKey = randomECKey.getPubKeyPoint().multiply(ECKey.fromPrivate(md.digest(password.getBytes("UTF-8"))).getPrivKey()).normalize().getXCoord().getEncoded();
            MessageDigest md1 = MessageDigest.getInstance("SHA-512");
            finalKey = md1.digest(finalKey);
            rawData = encryptAES(rawData, byteToString(finalKey).getBytes());
            result = new byte[rawData.length + randPubKey.length];
            System.arraycopy(randPubKey, 0, result, 0, randPubKey.length);
            System.arraycopy(rawData, 0, result, randPubKey.length, rawData.length);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException. Msg: "+e.getMessage());
        } catch (NoSuchAlgorithmException ex){
            Log.e(TAG, "NoSuchAlgorithmException. Msg: "+ ex.getMessage());
        }
        return result;
    }

    private static byte[] encryptAES(byte[] input, byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] result = md.digest(key);
            byte[] ivBytes = new byte[16];
            System.arraycopy(result, 32, ivBytes, 0, 16);
            byte[] sksBytes = new byte[32];
            System.arraycopy(result, 0, sksBytes, 0, 32);

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(true, new ParametersWithIV(new KeyParameter(sksBytes), ivBytes));
            byte[] temp = new byte[input.length + (16 - (input.length % 16))];
            System.arraycopy(input, 0, temp, 0, input.length);
            Arrays.fill(temp, input.length, temp.length, (byte) (16 - (input.length % 16)));
            byte[] out = new byte[cipher.getOutputSize(temp.length)];
            int proc = cipher.processBytes(temp, 0, temp.length, out, 0);
            cipher.doFinal(out, proc);
            temp = new byte[out.length - 16];
            System.arraycopy(out, 0, temp, 0, temp.length);
            return temp;

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (DataLengthException ex) {
            ex.printStackTrace();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } catch (InvalidCipherTextException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String byteToString(byte[] input) {
        StringBuilder result = new StringBuilder();
        for (byte in : input) {
            if ((in & 0xff) < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(in & 0xff));
        }
        return result.toString();
    }

    private static byte[] decryptAES(byte[] input, byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] result = md.digest(key);
            byte[] ivBytes = new byte[16];
            System.arraycopy(result, 32, ivBytes, 0, 16);
            byte[] sksBytes = new byte[32];
            System.arraycopy(result, 0, sksBytes, 0, 32);
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, new ParametersWithIV(new KeyParameter(sksBytes), ivBytes));

            byte[] pre_out = new byte[cipher.getOutputSize(input.length)];
            int proc = cipher.processBytes(input, 0, input.length, pre_out, 0);
            int proc2 = cipher.doFinal(pre_out, proc);
            byte[] out = new byte[proc + proc2];
            System.arraycopy(pre_out, 0, out, 0, proc + proc2);

            //Unpadding
            byte countByte = (byte) ((byte) out[out.length - 1] % 16);
            int count = countByte & 0xFF;

            if ((count > 15) || (count <= 0)) {
                return out;
            }

            byte[] temp = new byte[count];
            System.arraycopy(out, out.length - count, temp, 0, temp.length);
            byte[] temp2 = new byte[count];
            Arrays.fill(temp2, (byte) count);
            if (Arrays.equals(temp, temp2)) {
                temp = new byte[out.length - count];
                System.arraycopy(out, 0, temp, 0, out.length - count);
                return temp;
            } else {
                return out;
            }
        } catch (NoSuchAlgorithmException | DataLengthException | IllegalStateException | InvalidCipherTextException ex) {
        }
        return null;
    }

    public static JsonObject DatabaseToJson(String password, Context context) {
        JsonObject answer = new JsonObject();
        try {
            SCWallDatabase db = new SCWallDatabase(context);

            List<AccountSeed> seeds = db.getSeeds();
            JsonArray seedsObject = new JsonArray();
            for (AccountSeed seed : seeds) {
                JsonObject seedObject = seed.toJson(password);
                List<GeneralCoinAccount> accounts = db.getGeneralCoinAccounts(seed);
                JsonArray accountsObject = new JsonArray();
                for (GeneralCoinAccount account : accounts) {
                    JsonObject accountObject = account.toJson();
                    accountsObject.add(accountObject);
                }
                seedObject.add("accounts", accountsObject);
                seedsObject.add(seedObject);
            }
            answer.add("seeds", seedsObject);
        }catch (Exception e){
            e.printStackTrace();
        }
        return answer;
    }

    public static void JsontoDatabase(JsonObject in, String password,Context context) {
        try {
            SCWallDatabase db = new SCWallDatabase(context);

            JsonArray seedsObject = in.getAsJsonArray("seeds");
            for (int i = 0; i < seedsObject.size(); i++) {
                JsonObject seedObject = seedsObject.get(i).getAsJsonObject();
                AccountSeed seed = AccountSeed.fromJson(seedObject, password);
                long idSeed = db.putSeed(seed);
                seed.setId(idSeed);
                JsonArray accountsObject = seedObject.get("accounts").getAsJsonArray();
                for (int j = 0; j < accountsObject.size(); j++) {
                    JsonObject accountObject = accountsObject.get(j).getAsJsonObject();
                    GeneralCoinAccount account = (GeneralCoinAccount) CryptoCoinFactory.getAccountFromJson(accountObject, seed);
                    db.putGeneralCoinAccount(account);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
