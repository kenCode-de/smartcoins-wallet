package de.bitsharesmunich.cryptocoincore.dash;

import org.bitcoinj.core.CustomNetworkParameters;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Stopwatch;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DashSerializer;
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
 *
 */
public class DashNetworkParameters extends CustomNetworkParameters{

    /**
     * The schema used for this network
     */
    public static final String DASH_SCHEME = "dash";
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    /**
     * The constants definitions
     */
    public static final CoinDefinitions DASH_DEFINITIONS = new DashCoinDefinitions();
    /**
     * Logger
     */
    private static final Logger sLog = LoggerFactory.getLogger(DashNetworkParameters.class);

    private static DashNetworkParameters sInstance;

    /**
     * Basic constructor, generates the dash genesis block
     */
    public DashNetworkParameters() {
        super(DASH_DEFINITIONS);
        interval = (int)(576);
        targetTimespan = (int)(86400);
        maxTarget = DASH_DEFINITIONS.proofOfWorkLimit;
        dumpedPrivateKeyHeader = 128 + DASH_DEFINITIONS.AddressHeader;
        addressHeader = DASH_DEFINITIONS.AddressHeader;
        p2shHeader = DASH_DEFINITIONS.p2shHeader;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader};
        port = DASH_DEFINITIONS.Port;
        packetMagic = DASH_DEFINITIONS.PacketMagic;
        bip32HeaderPub = 0x0488B21E; //The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderPriv = 0x0488ADE4; //The 4 byte header that serializes in base58 to "xprv"
        genesisBlock.setDifficultyTarget(DASH_DEFINITIONS.genesisBlockDifficultyTarget);
        genesisBlock.setTime(DASH_DEFINITIONS.genesisBlockTime);
        genesisBlock.setNonce(DASH_DEFINITIONS.genesisBlockNonce);

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        id = ID_MAINNET;
        subsidyDecreaseBlockCount = DASH_DEFINITIONS.subsidyDecreaseBlockCount;
        spendableCoinbaseDepth = DASH_DEFINITIONS.spendableCoinbaseDepth;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals(DASH_DEFINITIONS.genesisHash),
                genesisHash);

        DASH_DEFINITIONS.initCheckpoints(checkpoints);

        dnsSeeds = DASH_DEFINITIONS.dnsSeeds;

        httpSeeds = DASH_DEFINITIONS.httpSeeds;
        addrSeeds = DASH_DEFINITIONS.addrSeeds;
    }

    public static synchronized DashNetworkParameters get() {
        if (sInstance == null) {
            sInstance = new DashNetworkParameters();
        }
        return sInstance;
    }

    /**
     * Gets the protocol id for the main dash net
     */
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
            if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget())
                throw new VerificationException("Unexpected change in difficulty at height " + storedPrev.getHeight() +
                        ": " + Long.toHexString(nextBlock.getDifficultyTarget()) + " vs " +
                        Long.toHexString(prev.getDifficultyTarget()));
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
        if (watch.elapsed(TimeUnit.MILLISECONDS) > 50)
            sLog.info("Difficulty transition traversal took {}", watch);

        Block blockIntervalAgo = cursor.getHeader();
        int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());
        // Limit the adjustment step.
        final int targetTimespan = this.getTargetTimespan();
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newTarget = Utils.decodeCompactBits(prev.getDifficultyTarget());
        newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
        newTarget = newTarget.divide(BigInteger.valueOf(targetTimespan));

        if (newTarget.compareTo(this.getMaxTarget()) > 0) {
            sLog.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
            newTarget = this.getMaxTarget();
        }

        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        long receivedTargetCompact = nextBlock.getDifficultyTarget();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newTarget = newTarget.and(mask);
        long newTargetCompact = Utils.encodeCompactBits(newTarget);

        if (newTargetCompact != receivedTargetCompact)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    Long.toHexString(newTargetCompact) + " vs " + Long.toHexString(receivedTargetCompact));
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
    public int getProtocolVersionNum(final ProtocolVersion version) {
        return version.getBitcoinProtocolVersion();
    }

    @Override
    public BitcoinSerializer getSerializer(boolean parseRetain) {
        return new DashSerializer(this, parseRetain);
    }

    @Override
    public String getUriScheme() {
        return DASH_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }

}
