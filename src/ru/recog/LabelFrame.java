package ru.recog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

import org.opencv.core.Mat;

public class LabelFrame extends JFrame implements ActionListener {
	
	List<Mat> imageList = new ArrayList<Mat>();
	JPanel labelPanel;
	boolean useBorders = false;
	
	
	
	public LabelFrame() {
		this("LabelFrame", false);
	}
	
	public LabelFrame(String name) {
		this(name, false);
	}
	
	public LabelFrame(String name, boolean useBorders) {
		super(name);
		this.useBorders = useBorders;
		labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.Y_AXIS));
		JScrollPane jscp = new JScrollPane(labelPanel);
		jscp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jscp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jscp, BorderLayout.CENTER);
		
//		getContentPane().setLayout(new BorderLayout());
//		getContentPane().add(labelPanel, BorderLayout.CENTER);
//		JButton saveButton = new JButton("save");
//		saveButton.addActionListener(this);
//		getContentPane().add(saveButton, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		

	}
	
	
//	public void addImage(Image image, String label) {
//		JLabel jlabel = new JLabel(label);
//		jlabel.setIcon(new ImageIcon(DetectUtil.Mat2BufferedImage(m)));
//		
//		if (useBorders) 
//			jlabel.setBorder(new LineBorder(Color.green, 1));
//		labelPanel.add(jlabel);
//		
//		imageList.add(image);
//	}
	
	public void addImage(Mat m, String label) {
		JLabel jlabel = new JLabel(label);
		jlabel.setIcon(new ImageIcon(DetectUtil.Mat2BufferedImage(m)));
		
		if (useBorders) 
			jlabel.setBorder(new LineBorder(Color.green, 1));
		labelPanel.add(jlabel);
		
		imageList.add(m);
	}
	
	public void addImage(Mat m, String label, int scale)  {
		JLabel jlabel = new JLabel(label);

		if (m.rows()==0 || m.cols()==0) {
//			throw new IllegalArgumentException("Matrix with size "+m.size()+" cannot be processed.");
			System.err.println("Matrix with size "+m.size()+" cannot be processed.");
			try {
				jlabel.setIcon(new ImageIcon(ImageIO.read(new File("/Users/pps/warning.png"))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (scale!=1)
				jlabel.setIcon(new ImageIcon(DetectUtil.Mat2BufferedImage(ImageUtils.scaleUp(m,scale))));
			else
				jlabel.setIcon(new ImageIcon(DetectUtil.Mat2BufferedImage(m)));
		}
		
		if (useBorders) 
			jlabel.setBorder(new LineBorder(Color.green, 1));
		labelPanel.add(jlabel);
		
		imageList.add(m);
	}
	
	public void addImage(Mat m, String label, int scale, int hints) {
		JLabel jlabel = new JLabel(label);
		
		BufferedImage image = DetectUtil.Mat2BufferedImage(m);
		Image scaled = image.getScaledInstance(m.cols()*scale, m.rows()*scale, hints);
		jlabel.setIcon(new ImageIcon(scaled));
		
		if (useBorders) 
			jlabel.setBorder(new LineBorder(Color.green, 1));
		labelPanel.add(jlabel);
		
		imageList.add(m);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("save".equals(e.getActionCommand()))
			System.out.println("Saving shit");
	}

}
