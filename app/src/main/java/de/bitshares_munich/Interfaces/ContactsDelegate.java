package de.bitshares_munich.Interfaces;

import java.io.Serializable;

/**
 * Created by afnan on 5/26/16.
 */
public interface ContactsDelegate extends Serializable {
    void OnUpdate(String s,int id);
}
