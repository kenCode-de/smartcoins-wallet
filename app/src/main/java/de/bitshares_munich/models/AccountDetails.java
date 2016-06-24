package de.bitshares_munich.models;

import java.util.ArrayList;

/**
 * Created by qasim on 5/18/16.
 */
public class AccountDetails {

    public String status;
    public String brain_key;
    public String address;
    public String account_id;
    public String pub_key;
    public String wif_key;
    public String msg;
    public ArrayList<AccountAssets> AccountAssets;
    public Boolean isSelected;
    public Boolean isLifeTime=false;
    public String account_name;

}
