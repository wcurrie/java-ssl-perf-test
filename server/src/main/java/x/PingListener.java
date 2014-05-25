package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

import java.io.IOException;

public class PingListener implements ISORequestListener {
    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        if (!"0800".equals(m.getString(0))) {
            return false;
        }

        try {
            source.send(Messages.pong(m));
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (IOException ignored) {
        }
        return true;
    }

}
