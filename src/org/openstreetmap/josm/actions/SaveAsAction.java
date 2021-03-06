// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;

import java.awt.event.KeyEvent;
import java.io.File;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Export the data.
 *
 * @author imi
 */
public class SaveAsAction extends SaveActionBase {
    private static SaveAsAction instance = new SaveAsAction();

    /**
     * Construct the action with "Save" as label.
     */
    public SaveAsAction() {
        super(tr("Save As..."), "save_as", tr("Save the current data to a new file."),
            Shortcut.registerShortcut("system:saveas", tr("File: {0}", tr("Save As...")),
            KeyEvent.VK_S, Shortcut.CTRL_SHIFT));
        putValue("help", ht("/Action/SaveAs"));
    }

    public static SaveAsAction getInstance() {
        return instance;
    }

    @Override protected File getFile(Layer layer) {
        return layer.createAndOpenSaveFileChooser();
    }
}
