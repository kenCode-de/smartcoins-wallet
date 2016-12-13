package com.luminiasoft.bitshares;

/**
 * Created by nelson on 11/9/16.
 */
public class Asset extends GrapheneObject {
    public String id;
    public int precision;
    public String symbol;

    public Asset(String id) {
        super(id);
    }
}
