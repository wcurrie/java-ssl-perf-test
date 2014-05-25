package x;

import java.io.IOException;

public class GnuPlot {
    public static void plot(String runName) throws InterruptedException, IOException {
        int i = new ProcessBuilder("./plot.gp", runName + ".csv")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start().waitFor();
        if (i == 0) {
            new ProcessBuilder("open", runName + ".png")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();
        }
    }
}
