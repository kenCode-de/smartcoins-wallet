package org.bitcoinj.core;

/**
 * Overrides the default block
 */
public class CustomBlock extends Block {

    /**
     * Consturctor
     */
    public CustomBlock(NetworkParameters params) {
        super(params,1);
    }
}
