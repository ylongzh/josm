// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.gui.preferences.advanced;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.openstreetmap.josm.data.Preferences.MapListSetting;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

public class MapListEditor extends ExtendedDialog {

    EntryListModel entryModel;
    PrefEntry entry;

    JList entryList;
    JTable table;
    MapTableModel tableModel;

    List<List<String>> dataKeys;
    List<List<String>> dataValues;
    Integer entryIdx;

    public MapListEditor(JComponent gui, PrefEntry entry, MapListSetting setting) {
        super(gui, tr("Change list of maps setting"), new String[] {tr("OK"), tr("Cancel")});
        this.entry = entry;
        List<Map<String, String>> orig = setting.getValue();

        dataKeys = new ArrayList<List<String>>();
        dataValues = new ArrayList<List<String>>();
        if (orig != null) {
            for (Map<String, String> m : orig) {
                List<String> keys = new ArrayList<String>();
                List<String> values = new ArrayList<String>();
                for (Entry<String, String> e : m.entrySet()) {
                    keys.add(e.getKey());
                    values.add(e.getValue());
                }
                dataKeys.add(keys);
                dataValues.add(values);
            }
        }
        setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setRememberWindowGeometry(getClass().getName() + ".geometry", WindowGeometry.centerInWindow(gui, new Dimension(500, 350)));
        setContent(build(), false);
    }

    public List<Map<String,String>> getData() {
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        for (int i=0; i < dataKeys.size(); ++i) {
            Map<String,String> m = new LinkedHashMap<String, String>();
            for (int j=0; j < dataKeys.get(i).size(); ++j) {
                m.put(dataKeys.get(i).get(j), dataValues.get(i).get(j));
            }
            data.add(m);
        }
        return data;
    }

    protected JPanel build() {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel(tr("Key: {0}", entry.getKey())), GBC.std(0,0).span(2).weight(1, 0).insets(0,0,5,10));

        JPanel left = new JPanel(new GridBagLayout());

        entryModel = new EntryListModel();
        entryList = new JList(entryModel);
        entryList.getSelectionModel().addListSelectionListener(new EntryListener());
        JScrollPane scroll = new JScrollPane(entryList);
        left.add(scroll, GBC.eol().fill());

        JToolBar sideButtonTB = new JToolBar(JToolBar.HORIZONTAL);
        sideButtonTB.setBorderPainted(false);
        sideButtonTB.setOpaque(false);
        sideButtonTB.add(new NewEntryAction());
        RemoveEntryAction removeEntryAction = new RemoveEntryAction();
        entryList.getSelectionModel().addListSelectionListener(removeEntryAction);
        sideButtonTB.add(removeEntryAction);
        left.add(sideButtonTB, GBC.eol());

        left.setPreferredSize(new Dimension(80, 0));

        p.add(left, GBC.std(0,1).fill().weight(0.3, 1.0));

        tableModel = new MapTableModel();
        table = new JTable(tableModel);
        table.putClientProperty("terminateEditOnFocusLost", true);
        table.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(tr("Key"));
        table.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(tr("Value"));
        DefaultCellEditor editor = new DefaultCellEditor(new JosmTextField());
        editor.setClickCountToStart(1);
        table.setDefaultEditor(table.getColumnClass(0), editor);

        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(140, 0));
        p.add(pane, GBC.std(1,1).insets(5,0,0,0).fill().weight(0.7, 1.0));
        return p;
    }

    class EntryListModel extends AbstractListModel {
        @Override
        public Object getElementAt(int index) {
            return tr("Entry {0}", index+1);
        }

        @Override
        public int getSize() {
            return dataKeys.size();
        }

        public void add() {
            dataKeys.add(new ArrayList<String>());
            dataValues.add(new ArrayList<String>());
            fireIntervalAdded(this, getSize() - 1, getSize() - 1);
        }

        public void remove(int idx) {
            dataKeys.remove(idx);
            dataValues.remove(idx);
            fireIntervalRemoved(this, idx, idx);
        }
    }

    class NewEntryAction extends AbstractAction {
        public NewEntryAction() {
            putValue(NAME, tr("New"));
            putValue(SHORT_DESCRIPTION, tr("add entry"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "add"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            entryModel.add();
        }
    }

    class RemoveEntryAction extends AbstractAction implements ListSelectionListener {
        public RemoveEntryAction() {
            putValue(NAME, tr("Remove"));
            putValue(SHORT_DESCRIPTION, tr("Remove the selected entry"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
            updateEnabledState();
        }

        protected void updateEnabledState() {
            setEnabled(entryList.getSelectedIndices().length == 1);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int idx = entryList.getSelectedIndices()[0];
            entryModel.remove(idx);
        }
    }

    class EntryListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            TableCellEditor editor = table.getCellEditor();
            if (editor != null) {
                ((DefaultCellEditor) editor).stopCellEditing();
            }
            if (entryList.getSelectedIndices().length != 1) {
                entryIdx = null;
                table.setEnabled(false);
            } else {
                entryIdx = entryList.getSelectedIndices()[0];
                table.setEnabled(true);
            }
            tableModel.fireTableDataChanged();
        }
    }

    class MapTableModel extends AbstractTableModel {
        private List<List<String>> data() {
            if (entryIdx == null) return Collections.emptyList();
            @SuppressWarnings("unchecked")
            List<List<String>> result = Arrays.asList(dataKeys.get(entryIdx), dataValues.get(entryIdx));
            return result;
        }

        private int size() {
            if (entryIdx == null) return 0;
            return dataKeys.get(entryIdx).size();
        }

        @Override
        public int getRowCount() {
            return entryIdx == null ? 0 : size() + 1;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? tr("Key") : tr("Value");
        }

        @Override
        public Object getValueAt(int row, int column) {
            return size() == row ? "" : data().get(column).get(row);
        }

        @Override
        public void setValueAt(Object o, int row, int column) {
            String s = (String) o;
            if (row == size()) {
                data().get(0).add("");
                data().get(1).add("");
                data().get(column).set(row, s);
                fireTableRowsInserted(row+1, row+1);
            } else {
                data().get(column).set(row, s);
                fireTableCellUpdated(row, column);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    }

}
