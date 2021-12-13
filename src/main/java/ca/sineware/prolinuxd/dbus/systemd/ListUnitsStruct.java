package ca.sineware.prolinuxd.dbus.systemd;

import lombok.AllArgsConstructor;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.types.UInt32;


@AllArgsConstructor
public class ListUnitsStruct extends Struct {
    @Position(0)
    public String unitName;

    @Position(1)
    public String desc;

    @Position(2)
    public String loadState;

    @Position(3)
    public String activeState;

    @Position(4)
    public String subState;

    @Position(5)
    public String unitFollowedState;

    @Position(6)
    public DBusPath objectPath;

    @Position(7)
    public UInt32 jobID;

    @Position(8)
    public String jobType;

    @Position(9)
    public DBusPath jobObjectPath;
}
