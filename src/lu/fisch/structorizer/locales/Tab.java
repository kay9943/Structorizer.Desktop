/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.locales;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class Tab extends javax.swing.JPanel {

    /**
     * Creates new form Tab
     */
    public Tab() {
        initComponents();
        
        // configure the table
        table.setDefaultRenderer(Object.class, new BoardTableCellRenderer());
        table.setRowHeight(25);
        
        table.setModel(new TranslatorTableModel());
        DefaultTableModel model = ((DefaultTableModel)table.getModel());
        model.setColumnCount(3);
        model.setRowCount(0);
        table.getColumnModel().getColumn(0).setHeaderValue("String");
        
        table.getColumnModel().getColumn(2).setHeaderValue("Please load a language!");
        table.getTableHeader().repaint();
    }
    
    public JTable getTable()
    {
        return table;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        
        jScrollPane1.setViewportView(table);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}


class BoardTableCellRenderer extends DefaultTableCellRenderer {

    Color backgroundColor = getBackground();

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        TableModel model = table.getModel();
        String key = (String) model.getValueAt(row, 0);
        
        if(key!=null && key.startsWith(Locale.startOfSubSection))
        {
        	// START KGU 2016-08-04: Issue #220
        	if (model instanceof TranslatorTableModel)
        	{
        		((TranslatorTableModel) model).forbidRowEditable(row);
        	}
        	// END KGU 2016-08-04
            if (!isSelected)
                    c.setBackground(Color.cyan);
            else
                    c.setBackground(Color.blue);
        }
        else
        if((value instanceof String && ((String) value).equals("")) || (value==null))
        {
            // START KGU#244 2016-09-06: Show an explicit deletion as well
            boolean isDeleted = col == 2 && Translator.loadedLocale.valueDiffersFrom(key, (String)value);
            // END KGU#244 2016-09-06
        	if (!isSelected)
                //c.setBackground(Color.orange);
        	    c.setBackground(isDeleted ? Color.red : Color.orange);
            else
                c.setBackground(Color.yellow);
        }
        // START KGU#231 2016-08-08: Issue #220 - detect any substantial change
        //else if(col==2 && !Translator.loadedLocale.hasValuePresent(key))
        else if (col==2 && Translator.loadedLocale.valueDiffersFrom(key, (String)value))
        // END KGU#231 2016-08-08
        {
            if(!isSelected)
                c.setBackground(Color.green);
            else
                c.setBackground(Color.green.darker());
        }
        else if (!isSelected) 
        {
            c.setBackground(backgroundColor);
        }
        
        return c;
    }
    
}

@SuppressWarnings("serial")
class MyRenderer extends DefaultTableCellRenderer {

    Color backgroundColor = getBackground();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        return c;
    }
}