package de.bitsharesmunich.cryptocoincore.base;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.net.discovery.HttpDiscovery;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by hvarona on 14/03/2017.
 */

public abstract class CoinDefinitions {

    public long genesisBlockDifficultyTarget;
    public String genesisTxInBytes;
    public String genesisTxOutBytes;
    public int genesisBlockValue;
    public long genesisBlockTime;
    public long genesisBlockNonce;
    public String genesisHash;
    public BigInteger proofOfWorkLimit;
    public int AddressHeader;
    public int p2shHeader;
    public int Port;
    public long PacketMagic;
    public int subsidyDecreaseBlockCount;
    public int spendableCoinbaseDepth;

    public String[] dnsSeeds;
    public int[] addrSeeds;
    public HttpDiscovery.Details[] httpSeeds;

    public String satoshiKey;


    public void initCheckpoints(Map<Integer, Sha256Hash> checkpoints) {

        checkpoints.put(1500, Sha256Hash.wrap("000000aaf0300f59f49bc3e970bad15c11f961fe2347accffff19d96ec9778e3"));
        checkpoints.put(4991, Sha256Hash.wrap("000000003b01809551952460744d5dbb8fcbd6cbae3c220267bf7fa43f837367"));
        checkpoints.put(9918, Sha256Hash.wrap("00000000213e229f332c0ffbe34defdaa9e74de87f2d8d1f01af8d121c3c170b"));
        checkpoints.put(16912, Sha256Hash.wrap("00000000075c0d10371d55a60634da70f197548dbbfa4123e12abfcbc5738af9"));
        checkpoints.put(23912, Sha256Hash.wrap("0000000000335eac6703f3b1732ec8b2f89c3ba3a7889e5767b090556bb9a276"));
        checkpoints.put(35457, Sha256Hash.wrap("0000000000b0ae211be59b048df14820475ad0dd53b9ff83b010f71a77342d9f"));
        checkpoints.put(45479, Sha256Hash.wrap("000000000063d411655d590590e16960f15ceea4257122ac430c6fbe39fbf02d"));
        checkpoints.put(55895, Sha256Hash.wrap("0000000000ae4c53a43639a4ca027282f69da9c67ba951768a20415b6439a2d7"));
        checkpoints.put(68899, Sha256Hash.wrap("0000000000194ab4d3d9eeb1f2f792f21bb39ff767cb547fe977640f969d77b7"));
        checkpoints.put(74619, Sha256Hash.wrap("000000000011d28f38f05d01650a502cc3f4d0e793fbc26e2a2ca71f07dc3842"));
        checkpoints.put(75095, Sha256Hash.wrap("0000000000193d12f6ad352a9996ee58ef8bdc4946818a5fec5ce99c11b87f0d"));
        checkpoints.put(88805, Sha256Hash.wrap("00000000001392f1652e9bf45cd8bc79dc60fe935277cd11538565b4a94fa85f"));
        checkpoints.put(90544, Sha256Hash.wrap("000000000001b284b79a44a95215d7e6cf9e22cd4f9b562f2cc796e941e0e411"));
    }

    public abstract Block getCoinBlock(NetworkParameters n);

}
