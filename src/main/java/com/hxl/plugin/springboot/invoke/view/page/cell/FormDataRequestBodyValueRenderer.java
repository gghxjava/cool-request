package com.hxl.plugin.springboot.invoke.view.page.cell;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FormDataRequestBodyValueRenderer extends JPanel implements TableCellRenderer {
    private final JTextField fileJTextField = new JTextField();
    private final JPanel fileSelectJPanel = new JPanel(new BorderLayout());


    public FormDataRequestBodyValueRenderer() {
        fileSelectJPanel.add(fileJTextField, BorderLayout.CENTER);
        JLabel fileSelectJLabel = new JLabel(AllIcons.General.OpenDisk);
        fileSelectJPanel.add(fileSelectJLabel, BorderLayout.EAST);


//        this.add("file", fileSelectJPanel);
//        this.add("text", textSelectJPanel);

    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {
        if (table.getValueAt(row, 3).equals("text")) {
            JTextField jTextField = new JTextField(value.toString());
            jTextField.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return jTextField;
        } else {
            fileJTextField.setText(value.toString());
            fileJTextField.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return fileSelectJPanel;
        }
//        textJTextField.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//        fileJTextField.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

//        textJTextField.setText(table.getValueAt(row, column).toString());
//        fileJTextField.setText(table.getValueAt(row, column).toString());

//        fileSelectJPanel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//        this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//        this.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

//        return this;
    }
}