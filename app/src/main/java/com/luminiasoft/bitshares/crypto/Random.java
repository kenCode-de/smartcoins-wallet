package com.luminiasoft.bitshares.crypto;


import com.luminiasoft.bitshares.crypto.AndroidRandomSource;
import com.luminiasoft.bitshares.crypto.SecureRandomStrengthener;
import java.security.SecureRandom;

/**
 * Created by nelson on 12/20/16.
 */
public class Random {

    public static SecureRandom getSecureRandom(){
        SecureRandomStrengthener randomStrengthener = SecureRandomStrengthener.getInstance();
        randomStrengthener.addEntropySource(new AndroidRandomSource());
        return randomStrengthener.generateAndSeedRandomNumberGenerator();
    }
}
