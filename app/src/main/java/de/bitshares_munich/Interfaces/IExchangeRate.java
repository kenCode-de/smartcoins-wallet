package de.bitshares_munich.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mbilal on 5/20/16.
 */
public interface IExchangeRate {
    void callback_exchange_rate(JSONObject obj,int id) throws JSONException;
}
