package net.rptools.extra.addonui;

import net.rpTools.aoTool.addon.addon.StatSheet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SheetForm extends JComponent {
    private static final Logger log = LogManager.getLogger(SheetForm.class);
    private StatSheet statSheet;
    private JPanel contentPane;

    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField propTypeField;
    private JTextField entryField;


    public SheetForm(){
        this(new StatSheet());
    }
    public SheetForm(StatSheet statSheet){
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        this.statSheet = statSheet;
        initControls();
        log.info("new {}", getClass().getSimpleName());
    }
    private void initControls(){
        if(statSheet.name() != null) {
            nameField.setText(statSheet.name());
        }
        if(statSheet.description() != null) {
            descriptionField.setText(statSheet.description());
        }
        if(statSheet.propertyTypes() != null) {
            propTypeField.setText(Arrays.toString(statSheet.propertyTypes().toArray(String[]::new)));
        }
        if(statSheet.entry() != null) {
            entryField.setText(statSheet.entry().getPath());
        }

        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                statSheet = new StatSheet(nameField.getText(), statSheet.description(), statSheet.entry(), statSheet.propertyTypes(), statSheet.namespace());
            }
        });
        descriptionField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                statSheet = new StatSheet(statSheet.name(), descriptionField.getText(), statSheet.entry(), statSheet.propertyTypes(), statSheet.namespace());
            }
        });
        propTypeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                statSheet = new StatSheet(statSheet.name(), statSheet.description(), statSheet.entry(), Arrays.stream(nameField.getText().split(",")).collect(Collectors.toSet()), statSheet.namespace());
            }
        });
        entryField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Path p = Path.of(entryField.getText());
                if(!p.startsWith("sheets")){
                    p = Path.of("sheets", entryField.getText());
                }
                if(!p.getFileName().endsWith(".hbs")){
                    p = Path.of(p + ".hbs");
                }
                try {
                    statSheet = new StatSheet(statSheet.name(), statSheet.description(), new URI("lib://" + statSheet.namespace() + "/" + p).toURL(), statSheet.propertyTypes(), statSheet.namespace());
                } catch (MalformedURLException | URISyntaxException ignored){}

            }
        });
    }

    public JPanel getMain(){ return contentPane; }
}
