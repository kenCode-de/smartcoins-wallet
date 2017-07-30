package de.bitsharesmunich.cryptocoincore.dogecoin;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.CustomBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;

/**
 * Constant to define the DogeCoin network
 */
public class DogeCoinDefinitions extends CoinDefinitions {

    /**
     * Consturctor
     */
    public DogeCoinDefinitions() {
        genesisBlockDifficultyTarget = (0x1e0ffff0L);
        genesisTxInBytes = "04ffff001d0104084e696e746f6e646f";
        genesisTxOutBytes = "040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9";
        genesisBlockValue = 88;
        genesisBlockTime = 1386325540L;
        genesisBlockNonce = (99943L);
        genesisHash = "1a91e3dace36e2be3bf030a65679fe821aa1d6ef92e7c9902eb318182c355691";
        Port = 22556;
        AddressHeader = 30;
        p2shHeader = 22;
        PacketMagic = 0xc0c0c0c0;
        proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 100000;
        httpSeeds = null;
        satoshiKey = "048240a8748a80a286b270ba126705ced4f2ce5a7847b3610ea3c06513150dade2a8512ed5ea86320824683fc0818f0ac019214973e677acd1244f6d0571fc5103";

        dnsSeeds = new String[]{
            "dnsseed.masternode.io",
            "dnsseed.dashpay.io",
            "dnsseed.dash.org",
            "dnsseed.dashdot.io"
        };
    }

    /**
     * Return the block for this Network coin
     * @param n The network parameter
     */
    @Override
    public Block getCoinBlock(NetworkParameters n) {
        return new CustomBlock(n);
    }
    
}
