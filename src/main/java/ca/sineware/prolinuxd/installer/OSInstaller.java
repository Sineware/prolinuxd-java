package ca.sineware.prolinuxd.installer;

import ca.sineware.prolinuxd.Main;
import ca.sineware.prolinuxd.gui.SwingAppender;
import ca.sineware.prolinuxd.gui.installer.InstallerGUI;
import ca.sineware.prolinuxd.gui.weston.Weston;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
public class OSInstaller {
    public InstallerGUI ig;

    public static boolean frameOpen;

    public OSInstaller() throws Exception {
        log.info(System.getenv("XDG_RUNTIME_DIR"));
        //String xdgDir = ;
        String xdgDir = "/run/user/0";
        if("true".equals(Main.baseSysConf.get("prolinux", "weston"))) {
            log.info("Starting Weston Wayland Compositor...");
            try {
                Weston.startWeston();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Could not start Weston!");
                throw e;
            }
            Thread.sleep(2000);
            File waylandSocket = new File(xdgDir + "/wayland-0");
            while(!waylandSocket.exists()) {
                log.info("Waiting for Weston to start...");
                Thread.sleep(2000);
            }
        }
        try {
            ig = new InstallerGUI();
            frameOpen = true;
        } catch (Throwable e) {
            e.printStackTrace();
            log.info("Could not start installer GUI!");
            System.exit(-3);
        }
    }

    public static void installOS(InstallerConfig conf) throws Exception {
        log.info("Executing install commands...");
        log.info("Formatting drive " + conf.targetDisk + " for " + conf.hostname);
        URL layoutUrl = Resources.getResource("layout.sgdisk");
        String layoutString = "";
        layoutString = Resources.toString(layoutUrl, StandardCharsets.UTF_8);
        ByteArrayInputStream layoutInput = new ByteArrayInputStream(layoutString.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream layoutOutput = new ByteArrayOutputStream();
        CommandLine partitionCmd = CommandLine.parse("sfdisk " + conf.targetDisk);
        DefaultExecutor partitionExec = new DefaultExecutor();
        partitionExec.setStreamHandler(new PumpStreamHandler(layoutOutput, layoutOutput, layoutInput));
        partitionExec.execute(partitionCmd);
        log.info(layoutOutput.toString());

        singleStepCmd("lvmscandisk");

    }
    public static String singleStepCmd(String cmd) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor cmdExec = new DefaultExecutor();
        cmdExec.setStreamHandler(new PumpStreamHandler(outputStream, outputStream, null));
        cmdExec.execute(cmdLine);
        log.info(outputStream.toString());
        return outputStream.toString();
    }
}

