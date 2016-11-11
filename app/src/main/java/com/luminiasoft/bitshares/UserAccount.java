package com.luminiasoft.bitshares;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class tha represents a graphene user account.
 * Created by nelson on 11/8/16.
 */
public class UserAccount extends GrapheneObject implements ByteSerializable {

    /**
     * Constructor that expects a user account in the string representation.
     * That is in the 1.2.x format.
     * @param id: The string representing the account id.
     */
    public UserAccount(String id) {
        super(id);
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(byteArrayOutputStream);
        try {
            Varint.writeUnsignedVarLong(this.instance, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }
}
