package de.bitsharesmunich.cryptocoincore.litecoin;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;
import java.util.Map;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.CustomBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

/**
 * The LiteCoin Network constants definitions
 */
public class LiteCoinDefinitions extends CoinDefinitions {

    /**
     * Constructor
     */
    public LiteCoinDefinitions() {
        genesisBlockDifficultyTarget = (0x1e0ffff0L);
        genesisTxInBytes = "04ffff001d0104404e592054696d65732030352f4f63742f32303131205374657665204a6f62732c204170706c65e280997320566973696f6e6172792c2044696573206174203536";
        genesisTxOutBytes = "040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9";
        genesisBlockValue = 50;
        genesisBlockTime = 1317972665L;
        genesisBlockNonce = (2084524493L);
        genesisHash = "12a765e31ffd4059bada1e25190f6e98c99d9714d334efa41a195a7e7e04bfe2";
        Port = 9333;
        AddressHeader = 48;
        p2shHeader = 5;
        PacketMagic = 0xfbc0b6db;
        proofOfWorkLimit = Utils.decodeCompactBits(0x1e0ffff0L);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 840000;
        satoshiKey = "04fc9702847840aaf195de8442ebecedf5b095cdbb9bc716bda9110971b28a49e0ead8564ff0db22209e0374782c093bb899692d524e9d6a6956e7c5ecbcd68284";
        httpSeeds = null;
        addrSeeds = null;
        dnsSeeds = new String[]{
            "dnsseed.litecointools.com",
                "dnsseed.litecoinpool.org",
                "dnsseed.koin-project.com",
                "dnsseed.weminemnc.com"
        };
    }

    @Override
    public void initCheckpoints(Map<Integer, Sha256Hash> checkpoints) {
        checkpoints.put(91722, Sha256Hash.wrap("00000000000271a2dc26e7667f8419f2e15416dc6955e5a6c6cdf3f2574dd08e"));
        checkpoints.put(91812, Sha256Hash.wrap("00000000000af0aed4792b1acee3d966af36cf5def14935db8de83d6f9306f2f"));
        checkpoints.put(91842, Sha256Hash.wrap("00000000000a4d0a398161ffc163c503763b1f4360639393e0e4c8e300e0caec"));
        checkpoints.put(91880, Sha256Hash.wrap("00000000000743f190a18c5577a3c2d2a1f610ae9601ac046a38084ccb7cd721"));
        checkpoints.put(200000, Sha256Hash.wrap("000000000000034a7dedef4a161fa058a2d67a173a90155f3a2fe6fc132e0ebf"));
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
