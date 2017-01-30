package de.bitshares_munich.interfaces;

import org.json.JSONObject;

/**
 * Created by mbilal on 5/31/16.
 */
public interface IRelativeHistory {
    void relativeHistoryCallback(JSONObject msg);
}
