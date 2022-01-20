package ca.sineware.prolinuxd.installer;

import ca.sineware.prolinuxd.Main;
import ca.sineware.prolinuxd.gui.SwingAppender;
import ca.sineware.prolinuxd.gui.installer.InstallerGUI;
import ca.sineware.prolinuxd.gui.weston.Weston;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            EventQueue.invokeLater(() -> {
                try {
                    ig = new InstallerGUI();
                    frameOpen = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            log.info("Could not start installer GUI!");
            System.exit(-3);
        }
    }

    public static void installOS(InstallerConfig conf) throws Exception {
        log.info("Executing install commands...");
        log.info("Formatting drive " + conf.targetDisk + " for " + conf.hostname);

        // This is determined by the UUID in layout.sgdisk
        // We use UUIDs instead of "sda3" because it could also be "nvme0n1p3"
        // todo: We should probably dynamically create and use instead of hardcoding
        String lvmPart = "/dev/disk/by-partuuid/5d4e6148-3e2b-4ca3-bb6a-7f97d7a54070";

        // Check for an existing ProLinuxVG and remove it
        // todo: use a better system to detect any volume group that a device used to be a part of
        final Path path = Paths.get("/dev/ProLinuxVG");
        if(Files.exists(path)) {
            log.info("Detected previous ProLinux installation (ProLinuxVG), removing it...");
            singleStepCmd("lvchange -an /dev/ProLinuxVG/lvol_grub", false);
            singleStepCmd("lvchange -an /dev/ProLinuxVG/lvol_config", false);
            singleStepCmd("lvchange -an /dev/ProLinuxVG/lvol_root_a", false);
            singleStepCmd("lvchange -an /dev/ProLinuxVG/lvol_root_b", false);
            singleStepCmd("lvchange -an /dev/ProLinuxVG/lvol_data", false);

            singleStepCmd("lvremove -y /dev/ProLinuxVG/lvol_grub", false);
            singleStepCmd("lvremove -y /dev/ProLinuxVG/lvol_config", false);
            singleStepCmd("lvremove -y /dev/ProLinuxVG/lvol_root_a", false);
            singleStepCmd("lvremove -y /dev/ProLinuxVG/lvol_root_b", false);
            singleStepCmd("lvremove -y /dev/ProLinuxVG/lvol_data", false);

            singleStepCmd("vgchange -an ProLinuxVG");
            singleStepCmd("vgremove -y ProLinuxVG");

            singleStepCmd("pvremove " + lvmPart);
        }

        log.info("Formatting with default GPT layout...");
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

        // LVM consists of:
        // - Physical Volumes (individual partitions or entire block devices)
        // - Volume Groups (groups of physical volumes treated as one space)
        // - Logical Volumes (multiple "partitions" within a volume group, these are formatted with filesystems)

        singleStepCmd("lvmdiskscan");

        log.info("Creating LVM physical volume on " + lvmPart + "...");
        singleStepCmd("pvcreate -ffy " + lvmPart);

        log.info("Physical volume summary: ");
        singleStepCmd("pvdisplay");
        singleStepCmd("pvscan");

        log.info("Creating LVM volume group...");
        singleStepCmd("vgcreate ProLinuxVG " + lvmPart);

        log.info("Extending VG over additional volumes...");
        // todo (incl. raid)

        log.info("Volume group summary: ");
        singleStepCmd("vgdisplay");

        log.info("Creating LVM logical volumes");
        singleStepCmd("lvcreate -y -L 16M ProLinuxVG -n lvol_grub");
        singleStepCmd("lvcreate -y -L 128M ProLinuxVG -n lvol_config");
        singleStepCmd("lvcreate -y -L 4G ProLinuxVG -n lvol_root_a");
        singleStepCmd("lvcreate -y -L 4G ProLinuxVG -n lvol_root_b");
        singleStepCmd("lvcreate -y -l 100%FREE ProLinuxVG -n lvol_data");

        log.info("Logical volumes summary: ");
        singleStepCmd("lvdisplay");

        log.info("Formatting grub, config, and data logical volumes:");
        singleStepCmd("mkfs.ext4 -F /dev/ProLinuxVG/lvol_grub");
        singleStepCmd("mkfs.ext4 -F /dev/ProLinuxVG/lvol_config");
        singleStepCmd("mkfs.ext4 -F /dev/ProLinuxVG/lvol_data");

        log.info("Extracting RootFS to logical volume Root A");
        singleStepCmd("dd if=/system/prolinux-server.squashfs.img of=/dev/ProLinuxVG/lvol_root_a bs=4096 status=progress");

        log.info("Mounting directories...");
        singleStepCmd("mount /dev/ProLinuxVG/lvol_root_a /mnt");
        singleStepCmd("mount /dev/ProLinuxVG/lvol_grub /mnt/boot/");

        log.info("Installing GRUB...");
        singleStepCmd("grub-install --boot-directory /mnt/boot /dev/sda");
        singleStepCmd("cp /system/grub-hdd.cfg /mnt/boot/grub/grub.cfg");

        log.info("Installation successful!");
        log.info("Please reboot and remove the installation media.");
    }

    public static int singleStepCmd(String cmd) throws Exception { return singleStepCmd(cmd, true); }
    public static int singleStepCmd(String cmd, boolean throwOnExitCode) throws Exception {
        // Hacky but lets things like partitions settle
        Thread.sleep(1000);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputErrStream = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor cmdExec = new DefaultExecutor();
        cmdExec.setStreamHandler(new PumpStreamHandler(outputStream, outputErrStream, null));
        log.info ("-----------------------------------------------");
        log.info("About to execute: " + cmd);
        cmdExec.execute(cmdLine, resultHandler);

        while(!resultHandler.hasResult()) {
            Thread.sleep(50);
            if(outputStream.toString().length() != 0)
                log.info("[stdout] " + outputStream.toString());
            if(outputErrStream.toString().length() != 0)
                log.info("[stderr] " + outputErrStream.toString());
            outputStream.reset();
        }
        int exitCode = resultHandler.getExitValue();
        log.info("Exit Code: " + exitCode);
        if(throwOnExitCode && exitCode != 0)
            throw new Exception("Command " + cmd + " exited with error code " + exitCode);
        return exitCode;
    }
}

