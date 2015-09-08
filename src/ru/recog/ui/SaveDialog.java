package ru.recog.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

public class SaveDialog extends JDialog implements PropertyChangeListener, ActionListener{
	
	
	private JComboBox<String> box;
	
	private String number = null;
	
	private JOptionPane pane;
	
	SaveDialog(JFrame parent)  {
		super(parent, true);
		
		box = new JComboBox<String>();
		box.setEditable(true);
		
		pane = new JOptionPane(box);
		pane.setMessageType(JOptionPane.OK_CANCEL_OPTION);
		pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                box.requestFocusInWindow();
            }
        });
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                    System.out.println("WC");

                    pane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
            }
        });
        
        pane.addPropertyChangeListener(this);
        box.addActionListener(this);
        
		setContentPane(new JScrollPane(pane, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
	}
	
	private Image concatImages(List<PlatePanel> panels) {
		int maxwidth = 0;
		int height = 0;
		for (PlatePanel pp : panels) {
			BufferedImage i = (BufferedImage)pp.getImage();
			if (i.getWidth()>maxwidth) maxwidth = i.getWidth();
			height = height+i.getHeight();
		}
		BufferedImage sumImage = new BufferedImage(maxwidth, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = sumImage.createGraphics();
		height = 0;
		for (PlatePanel pp : panels) {
			BufferedImage i = (BufferedImage)pp.getImage();
			g2.drawImage(i, 0, height, Color.DARK_GRAY, null);
			height = height+i.getHeight();
		}
		g2.dispose();
		
		return sumImage;
	}
	
	public String getNumber() {
		return number;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("comboBoxEdited".equals(e.getActionCommand()))
			pane.setValue(JOptionPane.OK_OPTION);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(pane) && "value".equals(evt.getPropertyName())) {
			if (JOptionPane.UNINITIALIZED_VALUE == evt.getNewValue())
				return;
			if (JOptionPane.OK_OPTION == (int)evt.getNewValue()) {
				doOK();
			} else if (JOptionPane.CANCEL_OPTION == (int)evt.getNewValue() 
					|| JOptionPane.CLOSED_OPTION == (int)evt.getNewValue()) {
				doCancel();
			}
			pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
		}
	}
	
	public void showDialog(List<PlatePanel> panels) {
		pane.setIcon(new ImageIcon(concatImages(panels)));
		pack();
		setVisible(true);
	}
	
	private void doOK() {
		String s = (String)box.getSelectedItem();
		if ( ru.recog.Utils.checkNumber(s) == null) {
			boolean found = false;
			for (int i = 0; i < box.getItemCount(); i++)
				if (s.toUpperCase().equalsIgnoreCase(box.getItemAt(i)))
					found = true;
			if (!found)
				box.insertItemAt(s.toUpperCase(), 0);
			number = s.toUpperCase();
			setVisible(false);
		} else System.out.println("Error processing "+s+" - "+ru.recog.Utils.checkNumber(s));
	}
	
	private void doCancel() {
		number = null;
		setVisible(false);
	}

}
