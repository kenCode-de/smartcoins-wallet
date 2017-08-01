package de.bitsharesmunich.cryptocoincore.base;

/**
 * Created by Henry Varona on 26/2/2017.
 */

/**
 * An abstract class for all the coins validators
 */
public abstract class GeneralCoinValidator {
    /**
     * validates a string address
     * @param address the adress to validate
     * @return true is the address is valid, false otherwise
     */
    public abstract boolean validateAddress(String address);

    /**
     * Creates a URI for a specific coin with an address and an amount
     *
     * @param address a coin address
     * @param amount an amount for the coin address
     * @return an URI string having the address and the amount in the address coin type format
     */
    public abstract String toURI(String address, double amount);

    /**
     * extracts the address from a URI with a coin type format
     *
     * @param uri the URI from which to extract the address
     * @return the address part in the given URI
     */
    public abstract String getAddressFromURI(String uri);

    /**
     * extracts the amount from a URI with a coin type format
     *
     * @param uri the URI from which to extract the amount
     * @return the amount part in the given URI
     */
    public abstract double getAmount(String uri);
}
