package de.bitsharesmunich.cryptocoincore.base.seed;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.SeedType;

/**
 * Created by henry on 05/02/2017.
 */

public class Brainkey extends AccountSeed {

    public Brainkey(String words, int sequence) {
        this.id = -1;
        this.type = SeedType.BRAINKEY;
        this.mnemonicCode = Arrays.asList(words.split(" "));
        this.additional = Integer.toString(sequence);
    }

    public Brainkey(long id, List<String> mnemonicCode, String additional) {
        this.id = id;
        this.type = SeedType.BRAINKEY;
        this.mnemonicCode = mnemonicCode;
        this.additional = additional;
    }

    @Override
    public byte[] getSeed() {
        StringBuilder encoded = new StringBuilder();
        for (String word : this.getMnemonicCode()) {
            encoded.append(word);
            encoded.append(" ");
        }
        encoded.append(this.getAdditional());
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(encoded.toString().getBytes("UTF-8"));
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] result = sha256.digest(bytes);
            return result;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgotithmException. Msg: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println("UnsupportedEncodingException. Msg: " + e.getMessage());
        }
        return null;
    }
}
