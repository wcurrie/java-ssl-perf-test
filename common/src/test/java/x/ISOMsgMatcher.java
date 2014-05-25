package x;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jpos.iso.ISOMsg;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ISOMsgMatcher extends TypeSafeMatcher<ISOMsg> {

    private final ISOMsg expected;

    public static TypeSafeMatcher<ISOMsg> isIsoMsg(ISOMsg expected) {
        return new ISOMsgMatcher(expected);
    }

    public ISOMsgMatcher(ISOMsg expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(ISOMsg item) {
        return toXML(expected).equals(toXML(item));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(toXML(expected));
    }

    @Override
    protected void describeMismatchSafely(ISOMsg item, Description mismatchDescription) {
        mismatchDescription.appendText(toXML(item));
    }

    public String toXML(ISOMsg m) {
        if (m == null) {
            return "null";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        m.dump(stream, "");
        stream.close();
        return out.toString();
    }
}
