package de.bitsharesmunich.cryptocoincore.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;

/**
 * Validates the addresses and URIs associated to the bitcoin coin
 */
public class BitcoinValidator extends GeneralCoinValidator {

    /**
     * The connection param (mainnet or testnet)
     */
    private NetworkParameters sParam = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);

    /**
     * Constant start for a bitcoin URI
     */
    private final static String sUriStart = "bitcoin:";
    /**
     * Constant amount parameter start for a bitcoin URI
     */
    private final static String sUriAmountStart = "amount=";
    /**
     * Constant separator for the bitcoin URI
     */
    private final static String sUriSeparator = "?";
    /**
     * constant parameter union for a bitcoin URI
     */
    private final static String sUriAnd = "&";

    /**
     * Singleton instance of this class
     */
    private static BitcoinValidator sInstance = null;

    private BitcoinValidator() {}

    /**
     * Singleton constructor
     *
     * @return the only instance this class should have
     */
    public static BitcoinValidator getInstance() {
        if (BitcoinValidator.sInstance == null) {
            BitcoinValidator.sInstance = new BitcoinValidator();
        }

        return BitcoinValidator.sInstance;
    }

    /**
     * Validates if an address string is valid for a bitcoin address
     * @param address the address to validate
     * @return true if the address is valid, false otherwise
     */
    @Override
    public boolean validateAddress(String address) {
        try {
            Address.fromBase58(this.sParam,address);
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
        URI.append(sUriStart);
        URI.append(address);
        if(amount >0){
            URI.append(sUriSeparator + sUriAmountStart);
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
        int startAddress = uri.indexOf(sUriStart);
        if(startAddress == -1){
            return null;
        }
        startAddress += sUriStart.length();
        if(uri.contains(sUriSeparator)){
            return uri.substring(startAddress,uri.indexOf(sUriSeparator));
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
        int startAddress = uri.indexOf(sUriStart);
        if(startAddress == -1){
            return -1;
        }
        uri = uri.substring(startAddress + sUriStart.length());
        if(uri.contains(sUriSeparator)){
            uri = uri.substring(uri.indexOf(sUriSeparator));
            int amountIndex = uri.indexOf(sUriAmountStart);
            if(amountIndex>=0){
                uri = uri.substring(amountIndex+ sUriAmountStart.length());
                if(uri.contains(sUriAnd)){
                    return Double.parseDouble(uri.substring(0,uri.indexOf(sUriAnd)))* Math.pow(10,Coin.BITCOIN.getPrecision());
                }
                return Double.parseDouble(uri)* Math.pow(10,Coin.BITCOIN.getPrecision());
            }
        }
        return -1;
    }
}
