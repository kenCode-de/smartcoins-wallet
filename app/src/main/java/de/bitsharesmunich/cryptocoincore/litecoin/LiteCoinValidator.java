package de.bitsharesmunich.cryptocoincore.litecoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.CustomNetworkParameters;
import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;

/**
 * Validates the addresses and URIs associated to the litecoin coin
 */
public class LiteCoinValidator extends GeneralCoinValidator {

    /**
     * The connection network param
     */
    private NetworkParameters sParam = CustomNetworkParameters.fromCoin(Coin.LITECOIN);
    /**
     * Constant start of the litecoin URI
     */
    private final static String sUriStart = "litecoin:";
    /**
     * Constant amount parameter start of the litecoin URI
     */
    private final static String sUriAmountStart = "amount=";
    /**
     * Constant separator for the litecoin URI
     */
    private final static String sUriSeparator = "?";
    /**
     * constant parameter union for a litecoin URI
     */
    private final static String sUriAnd = "&";
    /**
     * Singleton instance of this class
     */
    static private LiteCoinValidator sInstance = null;

    private LiteCoinValidator() {}

    /**
     * if there's no instance of this class, creates one, otherwise
     * returns the already created
     *
     */
    public static LiteCoinValidator getInstance() {
        if (LiteCoinValidator.sInstance == null) {
            LiteCoinValidator.sInstance = new LiteCoinValidator();
        }

        return LiteCoinValidator.sInstance;
    }

    /**
     * Validates if an address string is valid for a litecoin address
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
     * Returns a well formed litecoin URI with an address and an amount
     * @param address a coin address
     * @param amount an amount for the coin address
     * @return a well formed litecoin URI with the given address and amount
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
     * Extract the address part of a litecoin URI
     * @param uri the URI from which to extract the address
     * @return the address part of the given litecoin URI
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
     * Extract the amount part of a litecoin URI
     * @param uri the URI from which to extract the amount
     * @return the amount part of the given litecoin URI
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
