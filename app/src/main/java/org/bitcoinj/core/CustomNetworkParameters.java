package org.bitcoinj.core;

import de.bitsharesmunich.cryptocoincore.base.CoinDefinitions;
import de.bitsharesmunich.cryptocoincore.dash.DashNetworkParameters;
import de.bitsharesmunich.cryptocoincore.dogecoin.DogeCoinNetworkParameters;
import de.bitsharesmunich.cryptocoincore.litecoin.LiteCoinNetworkParameters;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * This is used to override the Bitcoin Network parameters to other networks, to be able to use the
 * bitocoinj Library
 */

public abstract class CustomNetworkParameters extends NetworkParameters {
    /**
     * Constructor. Overrides the genesisBlock of the network parameter
     * @param coinDefinitions The constant of the network
     */
    protected CustomNetworkParameters(CoinDefinitions coinDefinitions) {
        super();
        alertSigningKey = Utils.HEX.decode(coinDefinitions.satoshiKey);
        genesisBlock = createGenesis(this, coinDefinitions);
    }

    /**
     * Generates the genesis block of a crypto coin network
     *
     * This is used to override the genesis block generated by the Bitcoin Network Parameters
     * @param n The network parameters
     * @param coinDefinitions The constant of the network
     * @return The genesis block
     */
    private static Block createGenesis(NetworkParameters n,CoinDefinitions coinDefinitions) {
        Block genesisBlock = coinDefinitions.getCoinBlock(n);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   coin dependent
            byte[] bytes = Utils.HEX.decode(coinDefinitions.genesisTxInBytes);

            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode(coinDefinitions.genesisTxOutBytes));

            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, org.bitcoinj.core.Coin.valueOf(coinDefinitions.genesisBlockValue, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && getId().equals(((NetworkParameters) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Gets the network parameters for a Crypto Coin
     * @param coin The crypto coin to retrieve the networks parameters
     */
    public static NetworkParameters fromCoin(de.bitsharesmunich.cryptocoincore.base.Coin coin) {
        switch (coin){
            case BITCOIN:
                return NetworkParameters.fromID(ID_MAINNET);
            case DASH:
                return DashNetworkParameters.get();
            case LITECOIN:
                return LiteCoinNetworkParameters.get();
            case DOGECOIN:
                return DogeCoinNetworkParameters.get();
        }
        return null;
    }
}
