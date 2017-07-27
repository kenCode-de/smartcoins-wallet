package de.bitsharesmunich.cryptocoincore.base;

import java.util.List;

/**
 *  This representas a generic cryptocurrency account
 */

public abstract class CryptoCoinAccount {
    /**
     * The id on the database
     */
    protected long mId = -1;
    /**
     * The name of the coin, in the case of the graphene type the name of the account
     */
    protected String mName;
    /**
     * The type of coin network
     */
    protected Coin mCoin;
    /**
     * The account seed to determine the addresses
     */
    protected AccountSeed mSeed;

    /**
     * Basic constructor
     * @param id the id on the dataabse
     * @param name the name fo the account
     * @param coin the type of coin network
     * @param seed the seed to generate the addresses
     */
    public CryptoCoinAccount(long id, String name, Coin coin, AccountSeed seed) {
        this.mId = id;
        this.mName = name;
        this.mCoin = coin;
        this.mSeed = seed;
    }


    /**
     * Getter for the Seed account
     */
    public AccountSeed getSeed() {
        return this.mSeed;
    }

    /**
     * Getter for the Id on the database
     */
    public long getId() {
        return mId;
    }

    /**
     * Setter of the id on the database
     */
    public void setId(long id) {
        this.mId = id;
    }

    /**
     * Getter of the name of the account
     */
    public String getName() {
        return mName;
    }

    /**
     * Setter of the name of the account
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Getter of the coin network type
     */
    public Coin getCoin() {
        return mCoin;
    }

    /**
     * Setter of the coin network type
     */
    public void setCoin(Coin coin) {
        this.mCoin = coin;
    }

    /**
     * Gets a list of the balance, for each coin in this account, it can be only one balance for
     * cryptocurrencies like bitcoin, or many like Bitshares
     * @return A list with the balance of each cryptocoin of this account
     */
    public abstract List<Balance> getBalance();
}
