package de.bitsharesmunich.graphenej;

import java.math.BigInteger;

import de.bitsharesmunich.graphenej.models.BucketObject;

/**
 * Created by nelson on 12/23/16.
 */
public class Converter {
    public static final int OPEN_VALUE = 0;
    public static final int CLOSE_VALUE = 1;
    public static final int HIGH_VALUE = 2;
    public static final int LOW_VALUE = 3;

    private Asset base;
    private Asset quote;
    private BucketObject bucket;

    public Converter(Asset base, Asset quote, BucketObject bucket){
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
    }

    public long getQuoteValue(int bucketAttribute){
        BigInteger base;
        BigInteger quote;
        switch (bucketAttribute){
            case OPEN_VALUE:
                base = bucket.open_base;
                quote = bucket.open_quote;
                break;
            case CLOSE_VALUE:
                base = bucket.close_base;
                quote = bucket.close_quote;
                break;
            case HIGH_VALUE:
                base = bucket.high_base;
                quote = bucket.high_quote;
                break;
            case LOW_VALUE:
                base = bucket.low_base;
                quote = bucket.low_quote;
                break;
            default:
                base = bucket.close_base;
                quote = bucket.close_quote;
        }
        BigInteger basePrecisionAdjusted = base.divide(BigInteger.valueOf(this.base.getPrecision()));
        BigInteger quotePrecisionAdjusted = quote.divide(BigInteger.valueOf(this.quote.getPrecision()));
        BigInteger converted = basePrecisionAdjusted.divide(quotePrecisionAdjusted);
        return converted.longValue();
    }
}
