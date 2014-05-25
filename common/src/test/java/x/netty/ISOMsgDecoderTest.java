package x.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.jpos.iso.ISOMsg;
import org.junit.Before;
import org.junit.Test;
import x.Messages;

import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static x.ISOMsgMatcher.isIsoMsg;

public class ISOMsgDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() throws Exception {
        ISOMsgDecoder decoder = new ISOMsgDecoder();
        channel = new EmbeddedChannel(decoder);
    }

    @Test
    public void singleMessageInTwoChunks() throws Exception {
        ISOMsg ping = Messages.ping();
        byte[] bytes = Messages.pack(ping);
        int halfWay = bytes.length / 2;
        ByteBuf firstHalf = Unpooled.copiedBuffer(bytes, 0, halfWay);
        ByteBuf secondHalf = Unpooled.copiedBuffer(bytes, halfWay, bytes.length - halfWay);

        channel.writeInbound(firstHalf);
        channel.writeInbound(secondHalf);

        assertThat((ISOMsg) channel.readInbound(), isIsoMsg(ping));
        assertThat(channel.readInbound(), is(nullValue()));
    }

    @Test
    public void oneAndAHalfMessagesThenRemainingHalf() throws Exception {
        ISOMsg ping = Messages.ping();
        ISOMsg pong = Messages.pong(ping);
        byte[] pingBytes = Messages.pack(ping);
        byte[] pongBytes = Messages.pack(pong);
        byte[] bytes = join(pingBytes, pongBytes);

        int cutPoint = pingBytes.length + pongBytes.length/2;
        ByteBuf firstHalf = Unpooled.copiedBuffer(bytes, 0, cutPoint);
        ByteBuf secondHalf = Unpooled.copiedBuffer(bytes, cutPoint, bytes.length - cutPoint);

        channel.writeInbound(firstHalf);
        channel.writeInbound(secondHalf);

        assertThat((ISOMsg) channel.readInbound(), isIsoMsg(ping));
        assertThat((ISOMsg) channel.readInbound(), isIsoMsg(pong));
        assertThat(channel.readInbound(), is(nullValue()));
    }

    private byte[] join(byte[] pingBytes, byte[] pongBytes) {
        ByteBuffer buffer = ByteBuffer.allocate(pingBytes.length + pongBytes.length);
        buffer.put(pingBytes);
        buffer.put(pongBytes);
        buffer.flip();
        return buffer.array();
    }
}