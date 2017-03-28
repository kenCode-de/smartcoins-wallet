package de.bitsharesmunich.cryptocoincore.litecoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;

import de.bitsharesmunich.cryptocoincore.base.Coin;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinValidator;

/**
 * Created by Henry Varona on 26/2/2017.
 */

public class LiteCoinValidator extends GeneralCoinValidator {

    private NetworkParameters param = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

    private final static String uriStart = "litecoin:";
    private final static String uriAmountStart = "amount=";
    private final static String uriSeparator = "?";
    private final static String uriAnd = "&";

    static private LiteCoinValidator instance = null;

    private LiteCoinValidator() {

    }

    public static LiteCoinValidator getInstance() {
        if (LiteCoinValidator.instance == null) {
            LiteCoinValidator.instance = new LiteCoinValidator();
        }

        return LiteCoinValidator.instance;
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
