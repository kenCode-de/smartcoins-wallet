package de.bitsharesmunich.cryptocoincore.litecoin;

import com.google.common.base.Stopwatch;
import static com.google.common.base.Preconditions.checkState;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.CustomNetworkParameters;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.MonetaryFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class for the litecoin networks parameters
 */
public class LiteCoinNetworkParameters extends CustomNetworkParameters {

    /**
     * The schema for the LiteCoin network
     */
    public static final String LITECOIN_SCHEME = "litecoin";
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;
    /**
     * Constant for the LiteCoin network
     */
    public static final CoinDefinitions liteCoinDefinitions = new LiteCoinDefinitions();
    private static final Logger mLog = LoggerFactory.getLogger(LiteCoinNetworkParameters.class);
    private static LiteCoinNetworkParameters mInstance;

    /**
     * Basic Consturctor
     */
    private LiteCoinNetworkParameters() {
        super(liteCoinDefinitions);
        interval = (int) (2016);
        targetTimespan = (int) (1209600);
        maxTarget = liteCoinDefinitions.proofOfWorkLimit;
        dumpedPrivateKeyHeader = 128 + liteCoinDefinitions.AddressHeader;
        addressHeader = liteCoinDefinitions.AddressHeader;
        p2shHeader = liteCoinDefinitions.p2shHeader;
        acceptableAddressCodes = new int[]{addressHeader, p2shHeader};
        port = liteCoinDefinitions.Port;
        packetMagic = liteCoinDefinitions.PacketMagic;
        bip32HeaderPub = 0x0488B21E; //The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderPriv = 0x0488ADE4; //The 4 byte header that serializes in base58 to "xprv"
        genesisBlock.setDifficultyTarget(liteCoinDefinitions.genesisBlockDifficultyTarget);
        genesisBlock.setTime(liteCoinDefinitions.genesisBlockTime);
        genesisBlock.setNonce(liteCoinDefinitions.genesisBlockNonce);

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        id = ID_MAINNET;
        subsidyDecreaseBlockCount = liteCoinDefinitions.subsidyDecreaseBlockCount;
        spendableCoinbaseDepth = liteCoinDefinitions.spendableCoinbaseDepth;
        String genesisHash = genesisBlock.getHashAsString();
        System.out.println(genesisHash);
        checkState(genesisHash.equals(liteCoinDefinitions.genesisHash),
                genesisHash);

        liteCoinDefinitions.initCheckpoints(checkpoints);

        dnsSeeds = liteCoinDefinitions.dnsSeeds;
        httpSeeds = liteCoinDefinitions.httpSeeds;
        addrSeeds = liteCoinDefinitions.addrSeeds;
    }

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     */
    public static synchronized LiteCoinNetworkParameters get() {
        if (mInstance == null) {
            mInstance = new LiteCoinNetworkParameters();
        }
        return mInstance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }

    protected boolean isDifficultyTransitionPoint(StoredBlock storedPrev) {
        return ((storedPrev.getHeight() + 1) % this.getInterval()) == 0;
    }

    @Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
            final BlockStore blockStore) throws VerificationException, BlockStoreException {
        Block prev = storedPrev.getHeader();

        // Is this supposed to be a difficulty transition point?
        if (!isDifficultyTransitionPoint(storedPrev)) {

            // No ... so check the difficulty didn't actually change.
            if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget()) {
                throw new VerificationException("Unexpected change in difficulty at height " + storedPrev.getHeight()
                        + ": " + Long.toHexString(nextBlock.getDifficultyTarget()) + " vs "
                        + Long.toHexString(prev.getDifficultyTarget()));
            }
            return;
        }

        // We need to find a block far back in the chain. It's OK that this is expensive because it only occurs every
        // two weeks after the initial block chain download.
        final Stopwatch watch = Stopwatch.createStarted();
        StoredBlock cursor = blockStore.get(prev.getHash());
        for (int i = 0; i < this.getInterval() - 1; i++) {
            if (cursor == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the genesis block.");
            }
            cursor = blockStore.get(cursor.getHeader().getPrevBlockHash());
        }
        watch.stop();
        if (watch.elapsed(TimeUnit.MILLISECONDS) > 50) {
            mLog.info("Difficulty transition traversal took {}", watch);
        }

        Block blockIntervalAgo = cursor.getHeader();
        int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());
        // Limit the adjustment step.
        final int targetTimespan = this.getTargetTimespan();
        if (timespan < targetTimespan / 4) {
            timespan = targetTimespan / 4;
        }
        if (timespan > targetTimespan * 4) {
            timespan = targetTimespan * 4;
        }

        BigInteger newTarget = Utils.decodeCompactBits(prev.getDifficultyTarget());
        newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
        newTarget = newTarget.divide(BigInteger.valueOf(targetTimespan));

        if (newTarget.compareTo(this.getMaxTarget()) > 0) {
            mLog.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
            newTarget = this.getMaxTarget();
        }

        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        long receivedTargetCompact = nextBlock.getDifficultyTarget();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newTarget = newTarget.and(mask);
        long newTargetCompact = Utils.encodeCompactBits(newTarget);

        if (newTargetCompact != receivedTargetCompact) {
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: "
                    + Long.toHexString(newTargetCompact) + " vs " + Long.toHexString(receivedTargetCompact));
        }
    }

    @Override
    public Coin getMaxMoney() {
        return MAX_MONEY;
    }

    @Override
    public Coin getMinNonDustOutput() {
        return Transaction.MIN_NONDUST_OUTPUT;
    }

    @Override
    public MonetaryFormat getMonetaryFormat() {
        return new MonetaryFormat();
    }

    @Override
    public int getProtocolVersionNum(final NetworkParameters.ProtocolVersion version) {
        return version.getBitcoinProtocolVersion();
    }

    @Override
    public BitcoinSerializer getSerializer(boolean parseRetain) {
        //return new LiteCoinSerializer(this, false, parseRetain);
        return new BitcoinSerializer(this, parseRetain);
    }

    @Override
    public String getUriScheme() {
        return LITECOIN_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }

}
