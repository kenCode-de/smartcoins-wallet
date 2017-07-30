package org.bitcoinj.core;

import static org.bitcoinj.core.Utils.HEX;
import static org.bitcoinj.core.Utils.readUint32;
import static org.bitcoinj.core.Utils.uint32ToByteArrayBE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;



/**
 * This overrides the bitcoinSerializer used in bitcoinj, to be able to signing transaction using
 * the bitcoinj library
 */

public class DashSerializer extends BitcoinSerializer {
    private static final Logger sLog = LoggerFactory.getLogger(DashSerializer.class);
    private static final int sCommandLen = 12;

    private final NetworkParameters mParams;
    private final boolean mParseRetain;

    /**
     * The list of each type of message on the Dash Network
     */
    private static final Map<Class<? extends Message>, String> sNames = new HashMap<>();

    /**
     * Fill the list of the type of message the Dash Network has
     */
    static {
        sNames.put(VersionMessage.class, "version");
        sNames.put(InventoryMessage.class, "inv");
        sNames.put(Block.class, "block");
        sNames.put(GetDataMessage.class, "getdata");
        sNames.put(Transaction.class, "tx");
        sNames.put(AddressMessage.class, "addr");
        sNames.put(Ping.class, "ping");
        sNames.put(Pong.class, "pong");
        sNames.put(VersionAck.class, "verack");
        sNames.put(GetBlocksMessage.class, "getblocks");
        sNames.put(GetHeadersMessage.class, "getheaders");
        sNames.put(GetAddrMessage.class, "getaddr");
        sNames.put(HeadersMessage.class, "headers");
        sNames.put(BloomFilter.class, "filterload");
        sNames.put(FilteredBlock.class, "merkleblock");
        sNames.put(NotFoundMessage.class, "notfound");
        sNames.put(MemoryPoolMessage.class, "mempool");
        sNames.put(RejectMessage.class, "reject");
        sNames.put(GetUTXOsMessage.class, "getutxos");
        sNames.put(UTXOsMessage.class, "utxos");
        }

    /**
     * Constructs a BitcoinSerializer with the given behavior.
     *
     * @param params           networkParams used to create Messages instances and termining packetMagic
     * @param parseRetain      retain the backing byte array of a message for fast reserialization.
     */
    public DashSerializer(NetworkParameters params, boolean parseRetain) {
        super(params, parseRetain);
        this.mParams = params;
        this.mParseRetain = parseRetain;
    }

    /**
     * Writes message to to the output stream.
     */
    @Override
    public void serialize(String name, byte[] message, OutputStream out) throws IOException {
        byte[] header = new byte[4 + sCommandLen + 4 + 4 /* checksum */];
        uint32ToByteArrayBE(mParams.getPacketMagic(), header, 0);

        // The header array is initialized to zero by Java so we don't have to worry about
        // NULL terminating the string here.
        for (int i = 0; i < name.length() && i < sCommandLen; i++) {
            header[4 + i] = (byte) (name.codePointAt(i) & 0xFF);
        }

        Utils.uint32ToByteArrayLE(message.length, header, 4 + sCommandLen);

        byte[] hash = Sha256Hash.hashTwice(message);
        System.arraycopy(hash, 0, header, 4 + sCommandLen + 4, 4);
        out.write(header);
        out.write(message);

        if (sLog.isDebugEnabled())
            sLog.debug("Sending {} message: {}", name, HEX.encode(header) + HEX.encode(message));
    }

    /**
     * Writes message to to the output stream.
     */
    @Override
    public void serialize(Message message, OutputStream out) throws IOException {
        String name = sNames.get(message.getClass());
        if (name == null) {
            throw new Error("BitcoinSerializer doesn't currently know how to serialize " + message.getClass());
        }
        serialize(name, message.bitcoinSerialize(), out);
    }

    private Message makeMessage(String command, int length, byte[] payloadBytes, byte[] hash, byte[] checksum) throws ProtocolException {
        // We use an if ladder rather than reflection because reflection is very slow on Android.
        Message message;
        if (command.equals("version")) {
            return new VersionMessage(mParams, payloadBytes);
        } else if (command.equals("inv")) {
            message = makeInventoryMessage(payloadBytes, length);
        } else if (command.equals("block")) {
            message = makeBlock(payloadBytes, length);
        } else if (command.equals("merkleblock")) {
            message = makeFilteredBlock(payloadBytes);
        } else if (command.equals("getdata")) {
            message = new GetDataMessage(mParams, payloadBytes, this, length);
        } else if (command.equals("getblocks")) {
            message = new GetBlocksMessage(mParams, payloadBytes);
        } else if (command.equals("getheaders")) {
            message = new GetHeadersMessage(mParams, payloadBytes);
        } else if (command.equals("tx")) {
            message = makeTransaction(payloadBytes, 0, length, hash);
        } else if (command.equals("addr")) {
            message = makeAddressMessage(payloadBytes, length);
        } else if (command.equals("ping")) {
            message = new Ping(mParams, payloadBytes);
        } else if (command.equals("pong")) {
            message = new Pong(mParams, payloadBytes);
        } else if (command.equals("verack")) {
            return new VersionAck(mParams, payloadBytes);
        } else if (command.equals("headers")) {
            return new HeadersMessage(mParams, payloadBytes);
        } else if (command.equals("alert")) {
            return makeAlertMessage(payloadBytes);
        } else if (command.equals("filterload")) {
            return makeBloomFilter(payloadBytes);
        } else if (command.equals("notfound")) {
            return new NotFoundMessage(mParams, payloadBytes);
        } else if (command.equals("mempool")) {
            return new MemoryPoolMessage();
        } else if (command.equals("reject")) {
            return new RejectMessage(mParams, payloadBytes);
        } else if (command.equals("utxos")) {
            return new UTXOsMessage(mParams, payloadBytes);
        } else if (command.equals("getutxos")) {
            return new GetUTXOsMessage(mParams, payloadBytes);
        }
        else{
            sLog.warn("No support for deserializing message with name {}", command);
            return new UnknownMessage(mParams, command, payloadBytes);
        }
        return message;
    }

    /**
     * Get the network parameters for this serializer.
     */
    public NetworkParameters getParameters() {
        return mParams;
    }

    /**
     * Make an address message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public AddressMessage makeAddressMessage(byte[] payloadBytes, int length) throws ProtocolException {
        return new AddressMessage(mParams, payloadBytes, this, length);
    }

    /**
     * Make an alert message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Message makeAlertMessage(byte[] payloadBytes) throws ProtocolException {
        return new AlertMessage(mParams, payloadBytes);
    }

    /**
     * Make a block from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Block makeBlock(final byte[] payloadBytes, final int offset, final int length) throws ProtocolException {
        return new Block(mParams, payloadBytes, offset, this, length);
    }

    /**
     * Make an filter message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Message makeBloomFilter(byte[] payloadBytes) throws ProtocolException {
        return new BloomFilter(mParams, payloadBytes);
    }

    /**
     * Make a filtered block from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public FilteredBlock makeFilteredBlock(byte[] payloadBytes) throws ProtocolException {
        return new FilteredBlock(mParams, payloadBytes);
    }

    /**
     * Make an inventory message from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public InventoryMessage makeInventoryMessage(byte[] payloadBytes, int length) throws ProtocolException {
        return new InventoryMessage(mParams, payloadBytes, this, length);
    }

    /**
     * Make a transaction from the payload. Extension point for alternative
     * serialization format support.
     */
    @Override
    public Transaction makeTransaction(byte[] payloadBytes, int offset,
                                       int length, byte[] hash) throws ProtocolException {
        Transaction tx = new Transaction(mParams, payloadBytes, offset, null, this, length);
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
            byte expectedByte = (byte)(0xFF & mParams.getPacketMagic() >>> (magicCursor * 8));
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
        return mParseRetain;
    }


    public static class BitcoinPacketHeader {
        /** The largest number of bytes that a header can represent */
        public static final int HEADER_LENGTH = sCommandLen + 4 + 4;

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
            for (; header[cursor] != 0 && cursor < sCommandLen; cursor++) ;
            byte[] commandBytes = new byte[cursor];
            System.arraycopy(header, 0, commandBytes, 0, cursor);
            command = Utils.toString(commandBytes, "US-ASCII");
            cursor = sCommandLen;

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
