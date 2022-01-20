package ca.sineware.prolinuxd.gui.installer;

import ca.sineware.prolinuxd.gui.SwingAppender;
import ca.sineware.prolinuxd.installer.InstallerConfig;
import ca.sineware.prolinuxd.installer.OSInstaller;
import com.formdev.flatlaf.FlatLightLaf;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

@Slf4j
public class InstallerGUI extends JFrame {

    InstallType installType = InstallType.ENTIRE_DISK;


    public InstallerGUI() throws Exception {
        FlatLightLaf.setup();

        JPanel contents = new JPanel();
        contents.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        final JLabel titleText = new JLabel();
        titleText.setText("Sineware ProLinux Installer");
        titleText.setFont(new Font("Sans", Font.PLAIN, 32));
        contents.add(titleText, c);

        c.gridy++;
        final JLabel instructionsText = new JLabel();
        instructionsText.setText("Welcome to the Sineware ProLinux (In Development) Installer! Not for production use.");
        contents.add(instructionsText, c);

        c.gridy++;
        final JPanel sysTypePanel = new JPanel();
        sysTypePanel.setLayout(new FlowLayout());
        final JRadioButton entireButton = new JRadioButton("Use Entire Disk", true);
        final JRadioButton customButton = new JRadioButton("Custom Scheme");
        final ButtonGroup sysTypeGroup = new ButtonGroup();
        sysTypeGroup.add(entireButton);
        sysTypeGroup.add(customButton);

        sysTypePanel.add(entireButton);
        sysTypePanel.add(customButton);
        contents.add(sysTypePanel, c);

        c.gridy++;
        final TextFieldWithLabel targetDisk= new TextFieldWithLabel("Target disk (ex. /dev/sda)");
        contents.add(targetDisk.getPanel(), c);

        c.gridy++;
        final TextFieldWithLabel bootPartition = new TextFieldWithLabel("Boot partition/disk (ex. /dev/sda1)");
        contents.add(bootPartition.getPanel(), c);

        c.gridy++;
        final TextFieldWithLabel rootPartition = new TextFieldWithLabel("Root partition/disk (ex. /dev/sda2)");
        contents.add(rootPartition.getPanel(), c);

        c.gridy++;
        contents.add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridy++;
        final TextFieldWithLabel cloudUsername = new TextFieldWithLabel("Sineware Cloud Username");
        contents.add(cloudUsername.getPanel(), c);
        c.gridy++;
        final TextFieldWithLabel cloudPassword = new TextFieldWithLabel("Sineware Cloud Password");
        contents.add(cloudPassword.getPanel(), c);
        c.gridy++;
        final TextFieldWithLabel hostname = new TextFieldWithLabel("Device Hostname (a-b, 1-9, _, -)");
        contents.add(hostname.getPanel(), c);

        c.gridy++;
        contents.add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridy++;
        final JButton installButton = new JButton("Install");
        contents.add(installButton, c);

        c.gridy++;
        contents.add(new JSeparator(SwingConstants.HORIZONTAL), c);

        c.gridy++;
        contents.add(new JLabel("Logs"), c);
        c.gridy++;
        SwingAppender.logArea = new JTextArea(15,45);
        SwingAppender.logScrollPane = new JScrollPane (SwingAppender.logArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        SwingAppender.logScrollPane.setPreferredSize(new Dimension(100, 200));
        contents.add(SwingAppender.logScrollPane, c);

        // Only visible when custom scheme is selected.
        bootPartition.getPanel().setVisible(false);
        rootPartition.getPanel().setVisible(false);

        add(contents);

        SwingUtilities.updateComponentTreeUI(this);
        pack();

        // Action Listeners
        entireButton.addActionListener(e -> {
            bootPartition.getPanel().setVisible(false);
            rootPartition.getPanel().setVisible(false);
            installType = InstallType.ENTIRE_DISK;
        });
        customButton.addActionListener(e -> {
            bootPartition.getPanel().setVisible(true);
            rootPartition.getPanel().setVisible(true);
            installType = InstallType.CUSTOM_SCHEME;
        });
        installButton.addActionListener(e -> {
            log.info("Starting install...");
            InstallerConfig conf = new InstallerConfig();
            conf.targetDisk = targetDisk.getText();
            conf.hostname = hostname.getText();
            try {
                new Thread(() -> {
                    try {
                        log.info("Starting OSInstaller.installOS in thread...");
                        OSInstaller.installOS(conf);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }).start();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        setSize(900,700);
        setVisible(true);//making the frame visible
    }
}

enum InstallType {
    ENTIRE_DISK,
    CUSTOM_SCHEME
}

class TextFieldWithLabel {
    private final JPanel panel;
    private final JTextField text;
    public TextFieldWithLabel(String label) {
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 40, 10));
        final JLabel textLabel = new JLabel(label);
        text = new JTextField(16);
        panel.add(textLabel);
        panel.add(text);
    }
    public String getText() {
        return text.getText();
    }
    public JPanel getPanel() {
        return panel;
    }
}