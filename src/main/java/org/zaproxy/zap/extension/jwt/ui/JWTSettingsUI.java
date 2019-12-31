/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.jwt.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.zaproxy.zap.extension.fuzz.impl.AddPayloadDialog;
import org.zaproxy.zap.extension.fuzz.impl.PayloadGeneratorsContainer;
import org.zaproxy.zap.extension.fuzz.payloads.ui.impl.FileStringPayloadGeneratorUIHandler;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.jwt.JWTConfiguration;
import org.zaproxy.zap.extension.jwt.JWTI18n;
import org.zaproxy.zap.model.MessageLocation;

/**
 * Used to get the input from user regarding truststore path or Api for adding ZAP certificate to
 * default truststore.
 *
 * <p>TODO need to think more on how to handle cases of HMAC implementations for JWT.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public class JWTSettingsUI extends JFrame {

    public static final int DEFAULT_THREAD_COUNT = 2;

    public static final int DEFAULT_HMAC_MAX_KEY_LENGTH = 52;

    private String trustStorePath;

    /**
     * Thread count is used by BruteForce Attack. Please go through {@link
     * org.zaproxy.zap.extension.jwt.BruteforceAttack} for more information
     */
    private int threadCount = DEFAULT_THREAD_COUNT;

    private int hmacMaxKeyLength;

    private static final long serialVersionUID = 1L;

    private JWTConfiguration jwtConfiguration;
    private JScrollPane settingsScrollPane;
    private JPanel footerPanel;
    private JPanel settingsPanel;
    private JTextField threadCountTextField;
    private JTextField maxHmacKeyLengthTextField;
    private JFileChooser trustStoreFileChooser;
    private JPasswordField trustStorePasswordField;
    private char[] trustStorePassword;

    public JWTSettingsUI() {
        jwtConfiguration = JWTConfiguration.getInstance();
        setTitle(JWTI18n.getMessage("jwt.toolmenu.settings"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JLabel lblHeaderlabel = new JLabel(JWTI18n.getMessage("jwt.settings.header"));
        contentPane.add(lblHeaderlabel, BorderLayout.NORTH);

        settingsScrollPane = new JScrollPane();
        contentPane.add(settingsScrollPane, BorderLayout.CENTER);
        settingsPanel = new JPanel();
        settingsScrollPane.setViewportView(settingsPanel);
        GridBagLayout gridBagLayout = new GridBagLayout();
        settingsPanel.setLayout(gridBagLayout);

        footerPanel = new JPanel();
        contentPane.add(footerPanel, BorderLayout.SOUTH);
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));

        init();
    }

    private void init() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;

        this.hmacSettingsSection(gridBagConstraints);
        this.rsaSettingsSection(gridBagConstraints);

        gridBagConstraints.gridy++;
        JButton saveButton = new JButton();
        saveButton.setText(JWTI18n.getMessage("jwt.settings.button.save"));
        saveButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jwtConfiguration.setThreadCount(threadCount);
                        jwtConfiguration.setHmacMaxKeyLength(hmacMaxKeyLength);
                        jwtConfiguration.setTrustStorePath(trustStorePath);
                        jwtConfiguration.setTrustStorePassword(trustStorePassword);
                    }
                });
        JButton resetButton = new JButton();
        resetButton.setText(JWTI18n.getMessage("jwt.settings.button.reset"));
        resetButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jwtConfiguration.setThreadCount(DEFAULT_THREAD_COUNT);
                        jwtConfiguration.setHmacMaxKeyLength(DEFAULT_HMAC_MAX_KEY_LENGTH);
                        jwtConfiguration.setTrustStorePath("");
                        threadCountTextField.setText("");
                        maxHmacKeyLengthTextField.setText("");
                        trustStoreFileChooser.resetChoosableFileFilters();
                        trustStorePassword = null;
                    }
                });
        footerPanel.add(saveButton, gridBagConstraints);
        footerPanel.add(resetButton, gridBagConstraints);
    }

    private void rsaSettingsSection(GridBagConstraints gridBagConstraints) {
        JLabel lblRSABasedSettings = new JLabel(JWTI18n.getMessage("jwt.settings.rsa.header"));
        settingsPanel.add(lblRSABasedSettings, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel lblTrustStorePathAttribute =
                new JLabel(JWTI18n.getMessage("jwt.settings.rsa.trustStorePath"));
        settingsPanel.add(lblTrustStorePathAttribute, gridBagConstraints);

        gridBagConstraints.gridx++;
        trustStoreFileChooser = new JFileChooser();
        trustStoreFileChooser.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File trustStoreFile = trustStoreFileChooser.getSelectedFile();
                        trustStorePath = trustStoreFile.getAbsolutePath();
                    }
                });
        lblTrustStorePathAttribute.setLabelFor(trustStoreFileChooser);
        settingsPanel.add(trustStoreFileChooser, gridBagConstraints);

        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
        JLabel lblTrustStorePassword =
                new JLabel(JWTI18n.getMessage("jwt.settings.rsa.trustStorePassword"));
        settingsPanel.add(lblTrustStorePassword, gridBagConstraints);

        gridBagConstraints.gridx++;
        trustStorePasswordField = new JPasswordField();
        trustStorePasswordField.setColumns(30);
        trustStorePasswordField.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (trustStorePasswordField.getPassword() != null) {
                            trustStorePassword = trustStorePasswordField.getPassword();
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {}
                });
        lblTrustStorePassword.setLabelFor(trustStorePasswordField);
        settingsPanel.add(trustStorePasswordField, gridBagConstraints);
        JButton showFuzzerPanel = new JButton();
        showFuzzerPanel.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addFuzzerPanel();
                    }
                });
        settingsPanel.add(showFuzzerPanel);

        // settingsPanel.add(addPayloadDialog);
    }

    private void addFuzzerPanel() {
        FileStringPayloadGeneratorUIHandler payloadGeneratorUIHandler =
                new FileStringPayloadGeneratorUIHandler();
        PayloadGeneratorsContainer payloadGeneratorsContainer =
                new PayloadGeneratorsContainer(
                        Arrays.asList(payloadGeneratorUIHandler), "JWT Fuzzer");
        AddPayloadDialog addPayloadDialog =
                new AddPayloadDialog(
                        this,
                        payloadGeneratorsContainer,
                        new MessageLocation() {

                            @Override
                            public int compareTo(MessageLocation o) {
                                // TODO Auto-generated method stub
                                return 0;
                            }

                            @Override
                            public boolean overlaps(MessageLocation otherLocation) {
                                // TODO Auto-generated method stub
                                return false;
                            }

                            @Override
                            public String getValue() {
                                // TODO Auto-generated method stub
                                return "Sasan";
                            }

                            @Override
                            public Class<? extends Message> getTargetMessageClass() {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public String getDescription() {
                                // TODO Auto-generated method stub
                                return null;
                            }
                        });
        addPayloadDialog.pack();
        addPayloadDialog.setVisible(true);
        /** Look at the file {@code MessageLocationPayloadsPanel} */
        // addPayloadDialog.getPayloadGeneratorUI().getPayloadGenerator().iterator()
    }

    private void hmacSettingsSection(GridBagConstraints gridBagConstraints) {
        JLabel lblHMACBasedSettings = new JLabel(JWTI18n.getMessage("jwt.settings.hmac.header"));
        settingsPanel.add(lblHMACBasedSettings, gridBagConstraints);
        gridBagConstraints.gridy++;

        JLabel lblThreadCountAttribute =
                new JLabel(JWTI18n.getMessage("jwt.settings.hmac.bruteforce.theadCount"));
        settingsPanel.add(lblThreadCountAttribute, gridBagConstraints);

        gridBagConstraints.gridx++;
        threadCountTextField = new JTextField();
        threadCountTextField.setColumns(5);
        threadCountTextField.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        try {
                            if (threadCountTextField.getText() != "") {
                                threadCount =
                                        Integer.parseInt(threadCountTextField.getText().trim());
                            } else {
                                threadCount = DEFAULT_THREAD_COUNT;
                            }
                        } catch (NumberFormatException ex) {
                            // TODO need to handle exception
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {}
                });
        lblThreadCountAttribute.setLabelFor(threadCountTextField);
        settingsPanel.add(threadCountTextField, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy++;
        final JLabel lblMaxHmacKeyLengthAttribute =
                new JLabel(JWTI18n.getMessage("jwt.settings.hmac.bruteforce.keylength"));
        settingsPanel.add(lblMaxHmacKeyLengthAttribute, gridBagConstraints);

        gridBagConstraints.gridx++;
        maxHmacKeyLengthTextField = new JTextField();
        maxHmacKeyLengthTextField.setColumns(5);
        maxHmacKeyLengthTextField.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        try {
                            if (maxHmacKeyLengthTextField.getText() != "") {
                                hmacMaxKeyLength =
                                        Integer.parseInt(
                                                maxHmacKeyLengthTextField.getText().trim());
                            } else {
                                hmacMaxKeyLength = DEFAULT_HMAC_MAX_KEY_LENGTH;
                            }
                        } catch (NumberFormatException ex) {
                            // TODO need to handle exception
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {}
                });
        lblMaxHmacKeyLengthAttribute.setLabelFor(maxHmacKeyLengthTextField);
        settingsPanel.add(maxHmacKeyLengthTextField, gridBagConstraints);

        gridBagConstraints.gridy++;
        gridBagConstraints.gridx = 0;
    }
}
