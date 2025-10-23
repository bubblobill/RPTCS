package net.rptools.extra.addonui;

import net.rpTools.aoTool.addon.wrappers.WInfo;
import net.rpTools.aoTool.app.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.net.URI;
import java.util.*;
import java.util.List;

public class ConfigForm extends JComponent{
    private static final Logger log = LogManager.getLogger(ConfigForm.class);
    private JPanel contentPane;
    private JTextField nameField;
    private JTextField namespaceField;
    private JTextField versionField;
    private JTextField shortDescriptionField;
    private JTextArea descriptionField;
    private JCheckBox allowsExternalUriAccessField;
    private JFormattedTextField websiteField;
    private JFormattedTextField gitField;
    private JTextField authorsField;
    private final DefaultComboBoxModel<String> licenseModel = new DefaultComboBoxModel<>();
    private JComboBox<String> licenseField;
    private JTextField readmeField;
    private JTextField licenseFileField;
    private JLabel nameLabel;
    private JLabel namespaceLabel;
    private JLabel versionLabel;
    private JLabel descriptionShortLabel;
    private JLabel descriptionLabel;
    private JLabel externalUriLabel;
    private JLabel websiteLabel;
    private JLabel gitLabel;
    private JLabel authorsLabel;
    private JLabel licenseLabel;
    private JLabel licenseFileLabel;
    private JLabel readmeLabel;
    private JScrollPane scrollPane;
    private Font font = Font.decode("sansSerif");
    WInfo WInfo;

    public ConfigForm(){ this(new WInfo()); }
    public ConfigForm(WInfo WInfo){
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        initUIComponents();
        setComponentValues(WInfo);
        log.info("new {}", getClass().getSimpleName());
    }
    private void createUIComponents(){}
    private void initUIComponents() {
        InputVerifier URLChecker = new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                try {
                    new URI((String) ((JFormattedTextField)input).getValue());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        Map<TextAttribute, Object> fontAttributes = new HashMap<>();
        fontAttributes.put(TextAttribute.SIZE, gitField.getFont().getSize2D() + 1.65f);
        fontAttributes.put(TextAttribute.WIDTH, TextAttribute.WIDTH_SEMI_EXTENDED);
        font = font.deriveFont(fontAttributes);

        scrollPane.setBorder(null);

        gitField.setInputVerifier(URLChecker);
        websiteLabel.setInputVerifier(URLChecker);

        externalUriLabel.setLabelFor(allowsExternalUriAccessField);

        descriptionField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        authorsField.setFont(font);
        authorsField.addActionListener(a ->{
            String[] split = a.getActionCommand()
                    .replaceAll("\\[", "")
                    .replaceAll("\\]", "")
                    .split(",");
            WInfo.setAuthors(Arrays.stream(split).toList());
        });

        licenseModel.addAll(Constants.licenses);
        licenseField.setModel(licenseModel);

        nameField.setFont(font);
        versionField.setFont(font);
        websiteField.setFont(font);
        gitField.setFont(font);
        namespaceField.setFont(font);
        readmeField.setFont(font);
        licenseFileField.setFont(font);

        licenseField.addActionListener(a -> {
            if(a.getActionCommand().equals("comboBoxEdited")) {
                String value = String.valueOf(licenseField.getSelectedItem());
                if (licenseModel.getIndexOf(value) == -1) {
                    licenseModel.addElement(value);
                }
                WInfo.setLicense(value);
            }
        });
        licenseField.addItemListener(i -> WInfo.setLicense(String.valueOf(licenseField.getSelectedItem())));
        allowsExternalUriAccessField.addChangeListener(l -> WInfo.setAllowsUriAccess(allowsExternalUriAccessField.isSelected()));

        nameField.addActionListener((a)-> WInfo.setName(a.getActionCommand()));
        versionField.addActionListener((a)-> WInfo.setVersion(a.getActionCommand()));
        gitField.addActionListener((a)-> WInfo.setGitUrl(a.getActionCommand()));
        namespaceField.addActionListener((a)-> WInfo.setNamespace(a.getActionCommand()));
        descriptionField.addPropertyChangeListener(a-> WInfo.setDescription(String.valueOf(a.getNewValue())));
        shortDescriptionField.addActionListener((a)-> WInfo.setShortDescription(a.getActionCommand()));
        readmeField.addActionListener((a)-> WInfo.setReadMeFile(a.getActionCommand()));
        licenseFileField.addActionListener((a)-> WInfo.setLicenseFile(a.getActionCommand()));

    }
    public void setComponentValues(WInfo WInfo){
        this.WInfo = WInfo;

        List<String> authors = new ArrayList<>();
        Collections.addAll(authors, WInfo.getAuthors());
        authorsField.setText(Arrays.toString(authors.toArray(String[]::new)));

        if(!WInfo.getLicense().isEmpty() && licenseModel.getIndexOf(WInfo.getLicense()) == -1){
            licenseModel.addElement(WInfo.getLicense());
        }
        licenseField.setSelectedItem(WInfo.getLicense());

        allowsExternalUriAccessField.setSelected(WInfo.isAllowsUriAccess());
        nameField.setText(WInfo.getName());
        versionField.setText(WInfo.getVersion());
        websiteField.setText(WInfo.getWebsite());
        gitField.setText(WInfo.getGitUrl());
        namespaceField.setText(WInfo.getNamespace());
        descriptionField.setText(WInfo.getDescription());
        shortDescriptionField.setText(WInfo.getShortDescription());
        readmeField.setText(WInfo.getReadMeFile());
        licenseFileField.setText(WInfo.getLicenseFile());
    }
    public JPanel getMain(){ return contentPane; }
}
