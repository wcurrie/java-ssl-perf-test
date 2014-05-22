package x;

import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ErrorLogListener extends SimpleLogListener {
    private static final Set<String> EXCLUDED_TAGS;
    static {
        if (isDebug()) {
            EXCLUDED_TAGS = Collections.emptySet();
        } else {
            EXCLUDED_TAGS = new HashSet<String>(Arrays.asList("session-start", "session-end"));
        }
    }

    @Override
    public synchronized LogEvent log(LogEvent ev) {
        if (shouldLog(ev)) {
            return super.log(ev);
        }
        return ev;
    }

    private boolean shouldLog(LogEvent ev) {
        return !EXCLUDED_TAGS.contains(ev.getTag());
    }

    public static boolean isDebug() {
        return Boolean.getBoolean("jpos.all");
    }
}
