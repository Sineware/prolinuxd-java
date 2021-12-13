package ca.sineware.prolinuxd.dbus;

import ca.sineware.prolinuxd.dbus.systemd.ListUnitsStruct;
import ca.sineware.prolinuxd.dbus.systemd.SystemdManager;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DbusClient {
    public static DBusConnection conn = null;

    public DbusClient() throws Exception {
        if(conn == null) {
            throw new Exception("D-Bus connection not initialized");
        }
    }
    public static void connectDbus() throws DBusException {
        conn = DBusConnection.getConnection( DBusConnection.DBusBusType.SYSTEM );
        //conn.requestBusName("ca.sineware.prolinuxd");

        SystemdManager i = conn.getRemoteObject("org.freedesktop.systemd1", "/org/freedesktop/systemd1", SystemdManager.class);
        log.info("Got systemd remote object");
        List<ListUnitsStruct> units = i.ListUnits();
        log.info("Got systemd units");
        log.info(String.valueOf(units.getClass()));
        for(ListUnitsStruct unit : units) {
            //log.info(unit.unitName);
        }
    }


}
