package net.rptools.extra.addonui;

import net.rpTools.aoTool.addon.addon.LibraryInfo;
import net.rpTools.aoTool.addon.wrappers.WAddOnLibrary;
import net.rpTools.aoTool.files.Prompt;
import net.rpTools.aoTool.threading.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EditorContainer extends JComponent {
    private static final Logger log = LogManager.getLogger(EditorContainer.class);
    private JTabbedPane addOnTabs;
    private JPanel contentPane;
    private List<WAddOnLibrary> wAddOnLibraryList = new ArrayList<>();
    private final Map<Integer, WAddOnLibrary> tabMap = new HashMap<>();
    public EditorContainer() {
        this(new ArrayList<>());
    }
    public EditorContainer(List<WAddOnLibrary> wAddOnLibraries) {
        super();
        this.wAddOnLibraryList = wAddOnLibraries;
        initialise();
    }
    private void initialise(){
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        if (wAddOnLibraryList.isEmpty() || wAddOnLibraryList.getFirst() == null) {
            return;
        }

        addOnTabs.removeAll();
        addTab(wAddOnLibraryList.getFirst());

        if(wAddOnLibraryList.size() > 1) {
            ConcurrentLinkedQueue<Pair<String, AddOnEditor>> list = new ConcurrentLinkedQueue<>();
            ThreadPool.submitCompletable(() -> {
                for (WAddOnLibrary wAddOnLibrary : wAddOnLibraryList.stream().skip(1).toList()) {
                    AddOnEditor aoe = new AddOnEditor(wAddOnLibrary);
                    LibraryInfo libraryInfo = wAddOnLibrary.getLibraryInfo().resultNow();
                    String t = MessageFormat.format("{0}, {1}", libraryInfo.name(), libraryInfo.version());
                    list.offer(new Pair<>(t, aoe));
                }
                return list;
            }).thenRun(() ->
                    SwingUtilities.invokeLater(() -> {
                        while (!list.isEmpty()) {
                            Pair<String, AddOnEditor> pair = list.poll();
                            if (addOnTabs.indexOfTab(pair.getValue0()) == -1) {
                                addTab(pair.getValue0(), pair.getValue1());
                            }
                        }
                    })
            );
        }
    }

    public WAddOnLibrary getCurrentAddOn(){
        int index = addOnTabs.getSelectedIndex();
        return  ((AddOnEditor) addOnTabs.getComponent(index)).getAddOn();
    }
    public void removeAddOn(WAddOnLibrary WAddOnLibrary){
        if(Prompt.save()){
            // do save stuff
        }
        wAddOnLibraryList.remove(WAddOnLibrary);
        LibraryInfo info = WAddOnLibrary.getLibraryInfo().resultNow();
        String title = MessageFormat.format("{0}, {1}", info.name(), info.version());
        addOnTabs.removeTabAt(addOnTabs.indexOfTab(title));
    }
    public void addAddOn(WAddOnLibrary WAddOnLibrary){
        if(!wAddOnLibraryList.contains(WAddOnLibrary)){
            wAddOnLibraryList.add(WAddOnLibrary);
            addTab(WAddOnLibrary);
        } else {
            log.info("Exists \n{},\n{}", WAddOnLibrary.toString(), wAddOnLibraryList.get(wAddOnLibraryList.indexOf(WAddOnLibrary)).toString());
        }
    }

    private void addTab(WAddOnLibrary WAddOnLibrary){
        AddOnEditor addOnEditor = new AddOnEditor(WAddOnLibrary);
        LibraryInfo info = WAddOnLibrary.getLibraryInfo().resultNow();
        String title = MessageFormat.format("{0}, {1}", info.name(), info.version());
        addTab(title, addOnEditor);
    }
    private void addTab(String title, AddOnEditor editor){
        addOnTabs.addTab(title, editor);
//        editor.getLayout().layoutContainer(getMain());
    }
    private void createUIComponents() {
    }

    public JPanel getMain(){
        return contentPane;
    }
}
