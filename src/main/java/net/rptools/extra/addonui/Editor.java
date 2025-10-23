package net.rptools.extra.addonui;

import com.google.common.net.MediaType;
import net.rpTools.aoTool.asset.Asset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Editor extends JComponent {
    private static final Logger log = LogManager.getLogger(Editor.class);
    private JEditorPane editorPane;
    private JPanel contentPane;
    private JComboBox<MediaType> docTypeCombo;
    private JButton openButton;
    private JPanel buttonPanel;


    public Editor(){
        super();
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        initUIComponents();
        log.info("new {}", getClass().getSimpleName());
    }
    public JPanel getMain(){
        return contentPane;
    }
    public JPanel getButtonPanel(){
        return buttonPanel;
    }
    public JEditorPane getEditorPane(){
        return editorPane;
    }
    private void createUIComponents() {}
    private void initUIComponents(){
        MediaType[] mediaTypes = Arrays.stream(Asset.Type.values()).map(Asset::getMediaType).toArray(MediaType[]::new);
        docTypeCombo.setModel(new DefaultComboBoxModel<>(mediaTypes));
        editorPane.addPropertyChangeListener(e -> {
            System.out.println(e);
            if(e.getPropertyName().equalsIgnoreCase("document")){
                docTypeCombo.setSelectedItem(editorPane.getEditorKitForContentType(editorPane.getContentType()));
            }
        });
        editorPane.setContentType("text/plain");
    }


}
