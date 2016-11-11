package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nelson on 11/5/16.
 */
public class Transaction implements ByteSerializable {

    private ECKey privateKey;
    private BlockData blockData;
    private List<BaseOperation> operations;
    private List<Extension> extensions;

    /**
     * Transaction constructor
     * @param wif: The user's private key in the base58 format.
     * @param block_data: Block data containing important information used to sign a transaction.
     * @param operation_list: List of operations to include in the transaction.
     */
    public Transaction(String wif, BlockData block_data, List<BaseOperation> operation_list){
        this.privateKey = DumpedPrivateKey.fromBase58(null, wif).getKey();
        this.blockData = block_data;
        this.operations = operation_list;
        this.extensions = new ArrayList<Extension>();
    }

    public ECKey getPrivateKey(){
        return this.privateKey;
    }

    public byte[] toBytes(){
        // Creating a List of Bytes and adding the first bytes from the chain id
        List<Byte> byteArray = new ArrayList<Byte>();
        byteArray.addAll(Bytes.asList(Util.hexToBytes(Chains.BITSHARES.CHAIN_ID)));

        // Adding the block data
        byteArray.addAll(Bytes.asList(this.blockData.toBytes()));

        // Adding the number of operations
        byteArray.add((byte) this.operations.size());

        // Adding all the operations
        for(BaseOperation operation : operations){
            byteArray.add(operation.getId());
            byteArray.addAll(Bytes.asList(operation.toBytes()));
        }

        //Adding the number of extensions
        byteArray.add((byte) this.extensions.size());

        for(Extension extension : extensions){
            //TODO: Implement the extension serialization
        }
        // Adding a last zero byte to match the result obtained by the python-graphenelib code
        // I'm not exactly sure what's the meaning of this last zero byte, but for now I'll just
        // leave it here and work on signing the transaction.
        //TODO: Investigate the origin and meaning of this last byte.
        byteArray.add((byte) 0 );

        return Bytes.toArray(byteArray);
    }
}
