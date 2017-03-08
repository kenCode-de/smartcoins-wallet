package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by Henry Varona on 26/2/2017.
 */
public abstract class GeneralCoinValidator {
    public abstract boolean validateAddress(String address);

    public abstract String toURI(String address, double amount);
    public abstract String getAddressFromURI(String uri);
    public abstract double getAmount(String uri);
}
