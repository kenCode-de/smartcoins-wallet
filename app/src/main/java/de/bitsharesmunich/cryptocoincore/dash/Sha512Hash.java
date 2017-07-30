package de.bitsharesmunich.cryptocoincore.dash;

import com.google.common.io.ByteStreams;
import static com.google.common.base.Preconditions.checkArgument;

import org.spongycastle.util.encoders.Hex;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


/**
 * Calculates the hash of a byte array. This is used in the dash address calculations
 *
 */

class Sha512Hash implements Serializable, Comparable{
    /**
     * The byte array
     */
    private byte[] mBytes;
    public static final Sha512Hash ZERO_HASH = new Sha512Hash(new byte[64]);

    /**
     * Creates a Sha512Hash by wrapping the given byte array. It must be 64 bytes long.
     */
    public Sha512Hash(byte[] rawHashBytes) {
        checkArgument(rawHashBytes.length == 64);
        this.mBytes = rawHashBytes;

    }

    /**
     * Creates a Sha512Hash by decoding the given hex string. It must be 64 characters long.
     */
    public Sha512Hash(String hexString) {
        checkArgument(hexString.length() == 64);
        this.mBytes = Hex.decode(hexString);
    }

    /**
     * Calculates the (one-time) hash of contents and returns it as a new wrapped hash.
     */
    public static Sha512Hash create(byte[] contents) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new Sha512Hash(digest.digest(contents));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Returns a hash of the given files contents. Reads the file fully into memory before hashing so only use with
     * small files.
     * @throws java.io.IOException
     */
    public static Sha512Hash hashFileContents(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        try {
            return create(ByteStreams.toByteArray(in));
        } finally {
            in.close();
        }
    }

    /**
     * Returns true if the hashes are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Sha512Hash)) return false;
        return Arrays.equals(mBytes, ((Sha512Hash) other).mBytes);
    }

    /**
     * Hash code of the byte array as calculated by {@link java.util.Arrays#hashCode()}. Note the difference between a SHA256
     * secure bytes and the type of quick/dirty bytes used by the Java hashCode method which is designed for use in
     * bytes tables.
     */
    @Override
    public int hashCode() {
        // Use the last 4 bytes, not the first 4 which are often zeros in Bitcoin.
        return (mBytes[63] & 0xFF) | ((mBytes[62] & 0xFF) << 8) | ((mBytes[61] & 0xFF) << 16) | ((mBytes[60] & 0xFF) << 24);
    }

    @Override
    public String toString() {
        return Utils.HEX.encode(mBytes);
    }

    /**
     * Returns the bytes interpreted as a positive integer.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1, mBytes);
    }

    /**
     * Gets the raw bytes
     */
    public byte[] getBytes() {
        return mBytes;
    }

    /**
     * Gets a duplicate of this object
     */
    public Sha512Hash duplicate() {
        return new Sha512Hash(mBytes);
    }

    @Override
    public int compareTo(Object o) {
        checkArgument(o instanceof Sha512Hash);
        int thisCode = this.hashCode();
        int oCode = ((Sha512Hash)o).hashCode();
        return thisCode > oCode ? 1 : (thisCode == oCode ? 0 : -1);
    }

    /**
     * Gets a trim of the first 256 bits of the array
     */
    public Sha256Hash trim256()
    {
        byte [] result = new byte[32];
        for (int i = 0; i < 32; i++){
            result[i] = mBytes[i];
        }
        return new Sha256Hash(result);
    }
}

