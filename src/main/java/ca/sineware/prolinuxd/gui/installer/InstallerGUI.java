package ca.sineware.prolinuxd.gui.installer;

import com.formdev.flatlaf.FlatLightLaf;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class InstallerGUI extends JFrame {

    InstallType installType = InstallType.ENTIRE_DISK;

    public InstallerGUI() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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

        c.gridy += 2;
        final JButton installButton = new JButton("Install");
        contents.add(installButton, c);

        // Only visible when custom scheme is selected.
        bootPartition.getPanel().setVisible(false);
        rootPartition.getPanel().setVisible(false);

        add(contents);

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
        });

        setSize(800,600);
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
        panel.setLayout(new FlowLayout());
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