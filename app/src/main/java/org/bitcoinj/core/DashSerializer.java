package org.bitcoinj.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.bitcoinj.core.Utils.HEX;
import static org.bitcoinj.core.Utils.readUint32;
import static org.bitcoinj.core.Utils.uint32ToByteArrayBE;

/**
 * Created by hvarona on 14/03/2017.
 */

public class DashSerializer extends BitcoinSerializer {
    private static final Logger log = LoggerFactory.getLogger(DashSerializer.class);
    private static final int COMMAND_LEN = 12;

    private final NetworkParameters params;
    private final boolean parseRetain;

    private static final Map<Class<? extends Message>, String> names = new HashMap<>();

    static {
        names.put(VersionMessage.class, "version");
        names.put(InventoryMessage.class, "inv");
        names.put(Block.class, "block");
        names.put(GetDataMessage.class, "getdata");
        names.put(Transaction.class, "tx");
        names.put(AddressMessage.class, "addr");
        names.put(Ping.class, "ping");
        names.put(Pong.class, "pong");
        names.put(VersionAck.class, "verack");
        names.put(GetBlocksMessage.class, "getblocks");
        names.put(GetHeadersMessage.class, "getheaders");
        names.put(GetAddrMessage.class, "getaddr");
        names.put(HeadersMessage.class, "headers");
        names.put(BloomFilter.class, "filterload");
        names.put(FilteredBlock.class, "merkleblock");
        names.put(NotFoundMessage.class, "notfound");
        names.put(MemoryPoolMessage.class, "mempool");
        names.put(RejectMessage.class, "reject");
        names.put(GetUTXOsMessage.class, "getutxos");
        names.put(UTXOsMessage.class, "utxos");
        //Dash specific messages
        /*names.put(DarkSendElectionEntryPingMessage.class, "dseep");

        names.put(TransactionLockRequest.class, "ix");
        names.put(TransactionLockVote.class, "txlvote");

        names.put(MasternodeBroadcast.class, "mnb");
        names.put(MasternodePing.class, "mnp");
        names.put(SporkMessage.class, "spork");
        names.put(GetSporksMessage.class, "getsporks");
        names.put(DarkSendEntryGetMessage.class, "dseg");
        names.put(SyncStatusCount.class, "ssc");*/


    }

    /**
     * Constructs a BitcoinSerializer with the given behavior.
     *
     * @param params           networkParams used to create Messages instances and termining packetMagic
     * @param parseRetain      retain the backing byte array of a message for fast reserialization.
     */
    public DashSerializer(NetworkParameters params, boolean parseRetain) {
        super(params, parseRetain);
        this.params = params;
        this.parseRetain = parseRetain;
    }

    /**
     * Writes message to to the output stream.
     */
    @Override
    public void serialize(String name, byte[] message, OutputStream out) throws IOException {
        byte[] header = new byte[4 + COMMAND_LEN + 4 + 4 /* checksum */];
        uint32ToByteArrayBE(params.getPacketMagic(), header, 0);

        // The header array is initialized to zero by Java so we don't have to worry about
        // NULL terminating the string here.
        for (int i = 0; i < name.length() && i < COMMAND_LEN; i++) {
            header[4 + i] = (byte) (name.codePointAt(i) & 0xFF);
        }

        Utils.uint32ToByteArrayLE(message.length, header, 4 + COMMAND_LEN);

        byte[] hash = Sha256Hash.hashTwice(message);
        System.arraycopy(hash, 0, header, 4 + COMMAND_LEN + 4, 4);
        out.write(header);
        out.write(message);

        if (log.isDebugEnabled())
            log.debug("Sending {} message: {}", name, HEX.encode(header) + HEX.encode(message));
    }

    /**
     * Writes message to to the output stream.
     */
    @Override
    public void serialize(Message message, OutputStream out) throws IOException {
        String name = names.get(message.getClass());
        if (name == null) {
            throw new Error("BitcoinSerializer doesn't currently know how to serialize " + message.getClass());
        }
        serialize(name, message.bitcoinSerialize(), out);
    }

    private Message makeMessage(String command, int length, byte[] payloadBytes, byte[] hash, byte[] checksum) throws ProtocolException {
        // We use an if ladder rather than reflection because reflection is very slow on Android.
        Message message;
        if (command.equals("version")) {
            return new VersionMessage(params, payloadBytes);
        } else if (command.equals("inv")) {
            message = makeInventoryMessage(payloadBytes, length);
        } else if (command.equals("block")) {
            message = makeBlock(payloadBytes, length);
        } else if (command.equals("merkleblock")) {
            message = makeFilteredBlock(payloadBytes);
        } else if (command.equals("getdata")) {
            message = new GetDataMessage(params, payloadBytes, this, length);
        } else if (command.equals("getblocks")) {
            message = new GetBlocksMessage(params, payloadBytes);
        } else if (command.equals("getheaders")) {
            message = new GetHeadersMessage(params, payloadBytes);
        } else if (command.equals("tx")) {
            message = makeTransaction(payloadBytes, 0, length, hash);
        } else if (command.equals("addr")) {
            message = makeAddressMessage(payloadBytes, length);
        } else if (command.equals("ping")) {
            message = new Ping(params, payloadBytes);
        } else if (command.equals("pong")) {
            message = new Pong(params, payloadBytes);
        } else if (command.equals("verack")) {
            return new VersionAck(params, payloadBytes);
        } else if (command.equals("headers")) {
            return new HeadersMessage(params, payloadBytes);
        } else if (command.equals("alert")) {
            return makeAlertMessage(payloadBytes);
        } else if (command.equals("filterload")) {
            return makeBloomFilter(payloadBytes);
        } else if (command.equals("notfound")) {
            return new NotFoundMessage(params, payloadBytes);
        } else if (command.equals("mempool")) {
            return new MemoryPoolMessage();
        } else if (command.equals("reject")) {
            return new RejectMessage(params, payloadBytes);
        } else if (command.equals("utxos")) {
            return new UTXOsMessage(params, payloadBytes);
        } else if (command.equals("getutxos")) {
            return new GetUTXOsMessage(params, payloadBytes);
        } /*else if (command.equals("dseep")) {
            return new DarkSendElectionEntryPingMessage(params, payloadBytes);
        } else if (command.equals("ix")) {
            return new TransactionLockRequest(params, payloadBytes);
        } else if (command.equals("txlvote")) {
            return new TransactionLockVote(params, payloadBytes);
        } else if (command.equals("dsq")) {
            return new DarkSendQueue(params, payloadBytes);
        } else if (command.equals("mnb")) {
            return new MasternodeBroadcast(params, payloadBytes);
        } else if( command.equals("mnp")) {
            return new MasternodePing(params, payloadBytes);
        } else if (command.equals("spork")) {
            return new SporkMessage(params, payloadBytes, 0);
        } else if(command.equals("ssc")) {
            return new SyncStatusCount(params, payloadBytes);
        } else if(command.equals("sendheaders")) {
            return new SendHeadersMessage(params);
        } else if(command.equals("getsporks")) {
            return new GetSporksMessage(params);
        }else if(command.equals("govsync")) {
            return new GovernanceSyncMessage(params);
        }*/
        else{
            log.warn("No support for deserializing message with name {}", command);
            return new UnknownMessage(params, command, payloadBytes);
        }
        return message;
    }

    /**
     * Get the network parameters for this serializer.
     */
    public NetworkParameters getParameters() {
        return params;
    }

    /**
     * Make an address message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public AddressMessage makeAddressMessage(byte[] payloadBytes, int length) throws ProtocolException {
        return new AddressMessage(params, payloadBytes, this, length);
    }

    /**
     * Make an alert message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Message makeAlertMessage(byte[] payloadBytes) throws ProtocolException {
        return new AlertMessage(params, payloadBytes);
    }

    /**
     * Make a block from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Block makeBlock(final byte[] payloadBytes, final int offset, final int length) throws ProtocolException {
        return new Block(params, payloadBytes, offset, this, length);
    }

    /**
     * Make an filter message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Message makeBloomFilter(byte[] payloadBytes) throws ProtocolException {
        return new BloomFilter(params, payloadBytes);
    }

    /**
     * Make a filtered block from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public FilteredBlock makeFilteredBlock(byte[] payloadBytes) throws ProtocolException {
        return new FilteredBlock(params, payloadBytes);
    }

    /**
     * Make an inventory message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public InventoryMessage makeInventoryMessage(byte[] payloadBytes, int length) throws ProtocolException {
        return new InventoryMessage(params, payloadBytes, this, length);
    }

    /**
     * Make a transaction from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Transaction makeTransaction(byte[] payloadBytes, int offset,
                                       int length, byte[] hash) throws ProtocolException {
        Transaction tx = new Transaction(params, payloadBytes, offset, null, this, length);
        if (hash != null)
            tx.setHash(Sha256Hash.wrapReversed(hash));
        return tx;
    }

    @Override
    public void seekPastMagicBytes(ByteBuffer in) throws BufferUnderflowException {
        int magicCursor = 3;  // Which byte of the magic we're looking for currently.
        while (true) {
            byte b = in.get();
            // We're looking for a run of bytes that is the same as the packet magic but we want to ignore partial
            // magics that aren't complete. So we keep track of where we're up to with magicCursor.
            byte expectedByte = (byte)(0xFF & params.getPacketMagic() >>> (magicCursor * 8));
            if (b == expectedByte) {
                magicCursor--;
                if (magicCursor < 0) {
                    // We found the magic sequence.
                    return;
                } else {
                    // We still have further to go to find the next message.
                }
            } else {
                magicCursor = 3;
            }
        }
    }

    /**
     * Whether the serializer will produce cached mode Messages
     */
    @Override
    public boolean isParseRetainMode() {
        return parseRetain;
    }


    public static class BitcoinPacketHeader {
        /** The largest number of bytes that a header can represent */
        public static final int HEADER_LENGTH = COMMAND_LEN + 4 + 4;

        public final byte[] header;
        public final String command;
        public final int size;
        public final byte[] checksum;

        public BitcoinPacketHeader(ByteBuffer in) throws ProtocolException, BufferUnderflowException {
            header = new byte[HEADER_LENGTH];
            in.get(header, 0, header.length);

            int cursor = 0;

            // The command is a NULL terminated string, unless the command fills all twelve bytes
            // in which case the termination is implicit.
            for (; header[cursor] != 0 && cursor < COMMAND_LEN; cursor++) ;
            byte[] commandBytes = new byte[cursor];
            System.arraycopy(header, 0, commandBytes, 0, cursor);
            command = Utils.toString(commandBytes, "US-ASCII");
            cursor = COMMAND_LEN;

            size = (int) readUint32(header, cursor);
            cursor += 4;

            if (size > Message.MAX_SIZE || size < 0)
                throw new ProtocolException("Message size too large: " + size);

            // Old clients don't send the checksum.
            checksum = new byte[4];
            // Note that the size read above includes the checksum bytes.
            System.arraycopy(header, cursor, checksum, 0, 4);
            cursor += 4;
        }
    }
}
