package ca.sineware.prolinuxd.gui;

import ca.sineware.prolinuxd.gui.installer.InstallerGUI;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import javax.swing.*;
import java.util.Date;


// Log4j Appender for Swing GUI
@Plugin(
  name = "SwingAppender",
  category = Core.CATEGORY_NAME,
  elementType = Appender.ELEMENT_TYPE)
public class SwingAppender extends AbstractAppender {

    public static JTextArea logArea = null;
    public static JScrollPane logScrollPane = null;

    protected SwingAppender(String name, Filter filter) {
        super(name, filter, null);
    }

    @PluginFactory
    public static SwingAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Filter") Filter filter) {
        return new SwingAppender(name, filter);
    }

    @Override
    public void append(LogEvent event) {
        if(logScrollPane != null) {
            logArea.append("[" + new Date(event.getInstant().getEpochMillisecond()).toString() + "] " + event.getMessage().getFormattedMessage() + "\n");
            JScrollBar vertical = logScrollPane.getVerticalScrollBar();
            vertical.setValue( vertical.getMaximum() );
        }
    }
}