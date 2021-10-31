/*
    Sineware Cloud Services Client Daemon (prolinuxd)
    Copyright (C) 2021 Seshan Ravikumar

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
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class Main {
    public static void main(String[] args) throws URISyntaxException {
        log.info("Starting prolinuxd on " + System.getProperty("os.name") + "...");

        if(System.getenv("SINEWARE_CLOUD_TOKEN") == null || System.getenv("SINEWARE_CLOUD_TOKEN").isBlank()) {
            log.error("Env SINEWARE_CLOUD_TOKEN is not set.");
            System.exit(-1);
        }

        log.info("Attempting to connect to Sineware Cloud Services...");

        WebSocketClient client = new CloudClient(new URI("wss://update.sineware.ca/api/v1/gateway"));
        client.connect();
    }
}