package com.hxl.plugin.springboot.invoke.view;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class BasicTableParamJPanel extends JPanel {
    private static final String[] TABLE_HEADER_NAME = {"Key", "Value", "操作"};

    private final DefaultTableModel defaultTableModel = new DefaultTableModel(null, TABLE_HEADER_NAME);
    private JTable jTable;

    public BasicTableParamJPanel() {
        init();
    }
    public Map<String,Object> getTableMap(){
        Map<String,Object>  header =new HashMap<>();
        foreach(header::put);
        return header;
    }
    public void foreach(BiConsumer<String,String> biConsumer){
        for (int i = 0; i < jTable.getModel().getRowCount(); i++) {
            String key =  jTable.getModel().getValueAt(i,0).toString();
            String value = jTable.getModel().getValueAt(i,1).toString();
            if (!("".equals(value) && "".equals(key))){
                biConsumer.accept(key , value );
            }
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        defaultTableModel.addRow(new String[]{"","","Delete"});
        jTable = new JTable(defaultTableModel) {
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        Action delete = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.parseInt( e.getActionCommand() );
                ((DefaultTableModel)table.getModel()).removeRow(modelRow);

                if (table.getModel().getRowCount()==0) defaultTableModel.addRow(new String[]{"", "", "Delete"});
            }
        };
        defaultTableModel.addTableModelListener(e -> {
            if (e.getType()==TableModelEvent.UPDATE &&  e.getColumn()==0 && defaultTableModel.getValueAt(defaultTableModel.getRowCount()-1, 0).toString().length()!=0){
                String[] strings = {"", "", "Delete"};
                defaultTableModel.addRow(strings);
            }
        });
        ButtonColumn buttonColumn = new ButtonColumn(jTable, delete, 2);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
        jTable.setSelectionBackground(Color.getColor("#00000000"));
        jTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        jTable.setDefaultEditor(Object.class, new CustomTableCellEditor());
        jTable.setRowHeight(35);
        add(new JScrollPane(jTable),BorderLayout.CENTER);
    }
}
