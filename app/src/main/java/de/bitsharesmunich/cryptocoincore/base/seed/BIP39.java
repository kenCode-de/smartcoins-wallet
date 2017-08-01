package de.bitsharesmunich.cryptocoincore.base.seed;

import org.bitcoinj.crypto.MnemonicCode;

import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.graphenej.crypto.SecureRandomGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * A BIP39 seed type
 */

public class BIP39 extends AccountSeed {

    /**
     * Teh amount of words for this seed
     */
    private final int wmWordNumber = 12;

    /**
     * Constructor from the dataabse
     * @param id The id on the database of this seed
     * @param mnemonicCode A list of each word of the mnemonic Code
     * @param additional The passPhrase if it has.
     */
    public BIP39(long id, List<String> mnemonicCode, String additional) {
        this.mId = id;
        this.mType = SeedType.BIP39;
        this.mMnemonicCode = mnemonicCode;
        this.mAdditional = additional;
    }

    /**
     *  Contrcutor without database
     * @param words The words separate by space
     * @param passPhrase the pass phrase to be used, can be empty
     */
    public BIP39(String words, String passPhrase) {
        this.mId = -1;
        this.mType = SeedType.BIP39;
        words = words.toLowerCase();
        this.mMnemonicCode = Arrays.asList(words.split(" "));
        this.mAdditional = passPhrase;
    }

    /**
     * Constructor that generates the list of words
     * @param wordList Dictionary to be used
     */
    public BIP39(String[] wordList) {
        try {
            this.mId = -1;
            this.mType = SeedType.BIP39;
            this.mAdditional = "";
            int entropySize = ((this.wmWordNumber * 11) / 8) * 8;
            // We get a true random number
            SecureRandom secureRandom = SecureRandomGenerator.getSecureRandom();
            byte[] entropy = new byte[entropySize / 8];
            secureRandom.nextBytes(entropy);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] shaResult = md.digest(entropy);
            int mask = 0x80;
            int cheksum = 0;
            for (int i = 0; i < entropySize / 32; i++) {
                cheksum = cheksum ^ (shaResult[0] & mask);
                mask = mask / 2;
            }
            int[] wordsIndex = new int[(entropySize / 11) + 1];
            for (int i = 0; i < wordsIndex.length; i++) {
                wordsIndex[i] = 0;
            }

            int lastIndex = 0;
            int lastBit = 0;
            for (int i = 0; i < entropy.length; i++) {
                for (int j = 7; j >= 0; j--) {
                    if (lastBit == 11) {
                        lastBit = 0;
                        ++lastIndex;
                    }
                    wordsIndex[lastIndex] = wordsIndex[lastIndex] ^ ((int) (Math.pow(2, 11 - (lastBit + 1))) * (entropy[i] & ((int) Math.pow(2, j))) >> j);
                    ++lastBit;
                }
            }
            for (int j = 7; j >= 0; j--) {
                if (lastBit == 11) {
                    break;
                }
                wordsIndex[lastIndex] = wordsIndex[lastIndex] ^ ((int) (Math.pow(2, 11 - (lastBit + 1))) * (cheksum & ((int) Math.pow(2, j))) >> j);
                ++lastBit;
            }
            StringBuilder words = new StringBuilder();
            for (int windex : wordsIndex) {
                words.append(wordList[windex]).append(" ");
            }
            words.deleteCharAt(words.length() - 1);
            this.mMnemonicCode = Arrays.asList(words.toString().split(" "));
        } catch (NoSuchAlgorithmException ex) {
        }
    }

    /**
     *  Gets the seed generated to be used to create keys
     * @return An array with the seed calculated
     */
    @Override
    public byte[] getSeed() {
        return MnemonicCode.toSeed(this.getMnemonicCode(), this.getAdditional());
    }

}
