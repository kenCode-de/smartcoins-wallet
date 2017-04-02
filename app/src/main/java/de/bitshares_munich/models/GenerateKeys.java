package de.bitshares_munich.models;

import com.google.gson.Gson;

/**
 * Created by Syed Muhammad Muzzammil on 6/15/16.
 */
public class GenerateKeys {
    public Keys keys;
    public String status;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
