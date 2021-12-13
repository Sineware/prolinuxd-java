package ca.sineware.prolinuxd.gui.weston;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Weston {
    static Runtime rt;
    static Process pt;
    public static void startWeston() throws Exception {
        rt = Runtime.getRuntime();
        pt = rt.exec("weston-launch --tty=/dev/tty7 --user=root -- --xwayland");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(pt.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(pt.getErrorStream()));

        String line;
        /*while ((line = stdError.readLine()) != null)
            System.out.println(line);*/
    }
}
