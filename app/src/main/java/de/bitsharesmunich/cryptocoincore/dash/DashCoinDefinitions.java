package de.bitsharesmunich.cryptocoincore.dash;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.DashBlock;
import org.bitcoinj.core.Utils;

import static org.bitcoinj.core.Block.BLOCK_VERSION_GENESIS;

/**
 * Class used for definition of dash, for signing the transactions usign the bitocinj library
 *
  */

public class DashCoinDefinitions extends CoinDefinitions {

    /**
     * Constructor
     */
    public DashCoinDefinitions() {
        genesisBlockDifficultyTarget = (0x1e0ffff0L);
        genesisTxInBytes = "04ffff001d01044c5957697265642030392f4a616e2f3230313420546865204772616e64204578706572696d656e7420476f6573204c6976653a204f76657273746f636b2e636f6d204973204e6f7720416363657074696e6720426974636f696e73";
        genesisTxOutBytes = "040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9";
        genesisBlockValue = 50;
        genesisBlockTime = 1390095618L;
        genesisBlockNonce = (28917698);
        genesisHash = "00000ffd590b1485b3caadc19b22e6379c733355108f107a430458cdf3407ab6";
        Port = 9999;
        AddressHeader = 76;
        p2shHeader = 16;
        PacketMagic = 0xbf0c6bbd;
        proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 4730400;
        httpSeeds = null;
        satoshiKey = "048240a8748a80a286b270ba126705ced4f2ce5a7847b3610ea3c06513150dade2a8512ed5ea86320824683fc0818f0ac019214973e677acd1244f6d0571fc5103";
        dnsSeeds = new String[] {
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
        return new DashBlock(n, BLOCK_VERSION_GENESIS);
    }
}
