package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAddress;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;

/**
 * Created by Henry Varona on 26/2/2017.
 */

/**
 * Validates the addresses and URIs associated to the bitcoin coin
 */
public class BitcoinValidator extends GeneralCoinValidator {

    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET); /**< The connection param (mainnet or testnet)*/

    private final static String uriStart = "bitcoin:"; /**< constant start for a bitcoin URI*/
    private final static String uriAmountStart = "amount="; /**< constant amount parameter start for a bitcoin URI*/
    private final static String uriSeparator = "?"; /**< constant separator for a bitcoin URI*/
    private final static String uriAnd = "&"; /**< constant parameter union for a bitcoin URI*/

    static private BitcoinValidator instance = null; /**< singleton instance of this class*/

    private BitcoinValidator() {

    }

    /**
     * Singleton constructor
     *
     * @return the only instance this class should have
     */
    public static BitcoinValidator getInstance() {
        if (BitcoinValidator.instance == null) {
            BitcoinValidator.instance = new BitcoinValidator();
        }

        return BitcoinValidator.instance;
    }

    /**
     * Validates if an address string is valid for a bitcoin address
     * @param address the adress to validate
     * @return true if the address is valid, false otherwise
     */
    @Override
    public boolean validateAddress(String address) {
        try {
            Address.fromBase58(this.param,address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    /**
     * Returns a well formed bitcoin URI with an address and an amount
     * @param address a coin address
     * @param amount an amount for the coin address
     * @return a well formed bitcoin URI with the given address and amount
     */
    @Override
    public String toURI(String address, double amount) {
        StringBuilder URI = new StringBuilder();
        URI.append(uriStart);
        URI.append(address);
        if(amount >0){
            URI.append(uriSeparator+uriAmountStart);
            URI.append(Double.toString(amount));
        }
        return URI.toString();
    }

    /**
     * Extract the address part of a bitcoin URI
     * @param uri the URI from which to extract the address
     * @return the address part of the given bitcoin URI
     */
    @Override
    public String getAddressFromURI(String uri) {
        uri = uri.replace(" ","");
        int startAddress = uri.indexOf(uriStart);
        if(startAddress == -1){
            return null;
        }
        startAddress +=uriStart.length();
        if(uri.contains(uriSeparator)){
            return uri.substring(startAddress,uri.indexOf(uriSeparator));
        }
        return uri.substring(startAddress);
    }

    /**
     * Extract the amount part of a bitcoin URI
     * @param uri the URI from which to extract the amount
     * @return the amount part of the given bitcoin URI
     */
    @Override
    public double getAmount(String uri) {
        uri = uri.replace(" ","");
        int startAddress = uri.indexOf(uriStart);
        if(startAddress == -1){
            return -1;
        }
        uri = uri.substring(startAddress + uriStart.length());
        if(uri.contains(uriSeparator)){
            uri = uri.substring(uri.indexOf(uriSeparator));
            int amountIndex = uri.indexOf(uriAmountStart);
            if(amountIndex>=0){
                uri = uri.substring(amountIndex+uriAmountStart.length());
                if(uri.contains(uriAnd)){
                    return Double.parseDouble(uri.substring(0,uri.indexOf(uriAnd)))* Math.pow(10,Coin.BITCOIN.getPrecision());
                }
                return Double.parseDouble(uri)* Math.pow(10,Coin.BITCOIN.getPrecision());
            }
        }
        return -1;
    }
}
