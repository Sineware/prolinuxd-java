/*
    Sineware Cloud Services Client Daemon (prolinuxd)
    Copyright (C) 2022 Seshan Ravikumar

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ca.sineware.prolinuxd;
import ca.sineware.prolinuxd.dbus.DbusClient;
import ca.sineware.prolinuxd.gui.installer.InstallerGUI;
import ca.sineware.prolinuxd.installer.OSInstaller;
import lombok.extern.slf4j.Slf4j;
import org.ini4j.Ini;
import org.java_websocket.client.WebSocketClient;

import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.management.OperatingSystemMXBean;

@Slf4j
public class Main {
    public static Ini baseSysConf; // sineware.ini (read-only)
    public static Ini sysConf; // prolinux.ini (system specific )
    public static String cloudToken;

    public static void main(String[] args) throws Exception {
        log.info("Starting prolinuxd on " + System.getProperty("os.name") + "...");

        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        log.info(bean.getArch());
        log.info(bean.getName());
        log.info(bean.getVersion());
        log.info(String.valueOf(bean.getTotalMemorySize()));
        log.info(String.valueOf(bean.getSystemLoadAverage()));

        log.info("Attempting connection to D-Bus...");
        try {
            DbusClient.connectDbus();
            //log.info(String.valueOf(DbusClient.conn.getNames()));
        } catch (Exception e) {
            log.error("Could not connect to D-Bus!");
            e.printStackTrace();
            System.exit(-1);
        }

        log.info("Reading base system configuration file...");
        try {
            baseSysConf = new Ini(new File("/sineware.ini"));
        } catch (Exception e) {
            log.error("Could not read system configuration!");
            e.printStackTrace();
            System.exit(-2);
        }
        String systemStyle = baseSysConf.get("prolinux", "style");
        log.info("System style: " + systemStyle); // live or system

        if("live".equals(systemStyle)) {
            OSInstaller osi = new OSInstaller();
        } else if("system".equals(systemStyle)) {
            //String cloudToken = null;
            final Path path = Paths.get("/config/prolinux.ini");
            if(System.getenv("SINEWARE_CLOUD_TOKEN") != null && !System.getenv("SINEWARE_CLOUD_TOKEN").isBlank()) {
                cloudToken = System.getenv("SINEWARE_CLOUD_TOKEN");
            } else if(Files.exists(path)) { // check for the prolinux.ini configuration file.
                log.info("Reading system ProLinux configuration...");
                sysConf = new Ini(new File("/config/prolinux.ini"));
                cloudToken = sysConf.get("prolinux",  "cloud_token");
            }

            if(cloudToken != null) {
                log.info("Attempting to connect to Sineware Cloud Services...");

                WebSocketClient client = new CloudClient(new URI("wss://update.sineware.ca/api/v1/gateway"));
                //WebSocketClient client = new CloudClient(new URI("ws://localhost:3000/api/v1/gateway"));
                client.connect();
            } else {
                log.info("Booting in standalone mode...");
            }
        } else {
            log.error("Unknown or not set System style.");
            System.exit(-4);
        }
    }
}
