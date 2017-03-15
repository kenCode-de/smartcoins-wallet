package de.bitsharesmunich.graphenej.models.backup;

import de.bitshares_munich.utils.Crypt;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Util;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

/**
 * Class used to represent an entry in the "private_keys" array field in the JSON-formatted
 * backup file.
 *
 * Created by nelson on 2/14/17.
 */
public class PrivateKeyBackup {
    public String encrypted_key;
    public String pubkey;
    public int brainkey_sequence;
    public int id;

    public PrivateKeyBackup(byte[] privateKey, int brainkeySequence, int id, byte[] encryptionKey){
        this.encrypted_key = encryptPrivateKey(privateKey, encryptionKey);
        this.brainkey_sequence = brainkeySequence;
        this.id = id;
        deriveAddress(privateKey);
    }

    /*
     * From WIF Imported account.
     */
    public PrivateKeyBackup(String wif, String wif_key){
        this.encrypted_key = wif_key;
        this.brainkey_sequence = 0;
        this.id = 0;
        ECKey key = DumpedPrivateKey.fromBase58(NetworkParameters.fromID(NetworkParameters.ID_MAINNET), wif ).getKey();
        this.pubkey = new Address(ECKey.fromPublicOnly(key.getPubKey())).toString();
    }

    public byte[] decryptPrivateKey(byte[] encryptionKey){
        return Util.decryptAES(Util.hexToBytes(encrypted_key), encryptionKey);
    }

    public String encryptPrivateKey(byte[] data, byte[] encryptionKey){
        return Util.bytesToHex(Util.encryptAES(data, encryptionKey));
    }

    private void deriveAddress(byte[] privateKey){
        Address address = new Address(ECKey.fromPublicOnly(ECKey.fromPrivate(privateKey).getPubKey()));
        this.pubkey = address.toString();
    }
}
