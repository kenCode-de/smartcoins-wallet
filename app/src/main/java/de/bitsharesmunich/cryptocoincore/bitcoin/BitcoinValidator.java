package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;

/**
 * Created by Henry Varona on 26/2/2017.
 */

public class BitcoinValidator extends GeneralCoinValidator {

    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    static private BitcoinValidator instance = null;

    private BitcoinValidator() {

    }

    public static BitcoinValidator getInstance() {
        if (BitcoinValidator.instance == null) {
            BitcoinValidator.instance = new BitcoinValidator();
        }

        return BitcoinValidator.instance;
    }

    @Override
    public boolean validateAddress(String address) {
        try {
            Address.fromBase58(this.param,address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }
}
