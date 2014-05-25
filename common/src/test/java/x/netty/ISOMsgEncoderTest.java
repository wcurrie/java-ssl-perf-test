package x.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jpos.iso.ISOMsg;
import org.junit.Before;
import org.junit.Test;
import x.Messages;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ISOMsgEncoderTest {

    private EmbeddedChannel channel;

    @Before
    public void setUp() throws Exception {
        channel = new EmbeddedChannel(new ISOMsgEncoder());
    }

    @Test
    public void writesPackedMessage() throws Exception {
        ISOMsg ping = Messages.ping();
        channel.writeOutbound(ping);

        ByteBuf actual = (ByteBuf) channel.readOutbound();
        assertThat(actual.toString(Charset.defaultCharset()), is(new String(Messages.pack(ping))));
    }
}