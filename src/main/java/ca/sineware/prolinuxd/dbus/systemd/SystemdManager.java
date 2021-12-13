package ca.sineware.prolinuxd.dbus.systemd;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.interfaces.DBusInterface;

import java.util.List;

// ListUnits(out a(ssssssouso) units);

@DBusInterfaceName(value = "org.freedesktop.systemd1.Manager")
public interface SystemdManager extends DBusInterface {
    @DBusMemberName(value = "ListUnits")
    List<ListUnitsStruct> ListUnits();
}
