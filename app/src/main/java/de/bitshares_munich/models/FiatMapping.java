package de.bitshares_munich.models;

import de.bitshares_munich.utils.DefaultHashMap;
import de.bitsharesmunich.graphenej.Asset;

/**
 * Used to maintain a mapping between countries and their equivalent
 * existing smartcoins. If no smartcoin exists for a specific country currency, we
 * fall back to the USD.
 *
 * Created by nelson on 12/24/16.
 */
public class FiatMapping {

    public static DefaultHashMap<String, Asset> getMap(){
        // Here we instantiate a DefaultHashMap specifying the bitUSD as the default value.
        DefaultHashMap<String, Asset> map = new DefaultHashMap<>(new Asset("1.3.121"));

        // Chinese Yuan
        map.put("CN", new Asset("1.3.113"));

        // Euro
        map.put("AT", new Asset("1.3.120"));
        map.put("BE", new Asset("1.3.120"));
        map.put("CY", new Asset("1.3.120"));
        map.put("DE", new Asset("1.3.120"));
        map.put("EE", new Asset("1.3.120"));
        map.put("ES", new Asset("1.3.120"));
        map.put("FI", new Asset("1.3.120"));
        map.put("FR", new Asset("1.3.120"));
        map.put("GR", new Asset("1.3.120"));
        map.put("IE", new Asset("1.3.120"));
        map.put("IT", new Asset("1.3.120"));
        map.put("LT", new Asset("1.3.120"));
        map.put("LU", new Asset("1.3.120"));
        map.put("LV", new Asset("1.3.120"));
        map.put("MT", new Asset("1.3.120"));
        map.put("NL", new Asset("1.3.120"));
        map.put("PT", new Asset("1.3.120"));
        map.put("SI", new Asset("1.3.120"));
        map.put("SK", new Asset("1.3.120"));
        map.put("ME", new Asset("1.3.120"));

        // Argentinian pesos
        map.put("AR", new Asset("1.3.1017"));

        // Canadadian dollar
        map.put("CA", new Asset("1.3.115"));

        // UK Pound sterling
        map.put("GB", new Asset("1.3.118"));
        map.put("IE", new Asset("1.3.118"));

        // Swiss Franc
        map.put("CH", new Asset("1.3.116"));

        // South Korea Won
        map.put("KR", new Asset("1.3.102"));

        // Japanese yen
        map.put("JP", new Asset("1.3.119"));

        // Hong Kong dollar
        map.put("HK", new Asset("1.3.109"));

        // Singapore dollar
        map.put("SG", new Asset("1.3.108"));

        // Russian ruble
        map.put("RU", new Asset("1.3.110"));

        // Australian dollar
        map.put("AU", new Asset("1.3.117"));

        // Swedish krona
        map.put("SE", new Asset("1.3.111"));

        return map;
    }
}
