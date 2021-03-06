package ru.recog.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.segment.*;

public class PlateSelectionFrame extends JFrame implements ActionListener {
	
	
	private List<PlatePanel> platePanels = new ArrayList<PlatePanel>();
	
	private JPanel mainPanel;
	private JPanel buttonPanel;
	
	private JScrollPane scrollPane;
	private FrameProcessor loader;
	private Segmentation segmenter;
	private CalibrationSegmenter calib;
	
	public Segmentation getSegmentation() {
		return segmenter;
	}
	
	SegmentationResult doSegmentation(Mat m) {
		return segmenter.segment(m, MarkovSegmentation.USE_NN);
	}
	
	SegmentationResult doSegmentation(String filename) {
		
		System.out.println("Adding "+filename);
		SegmentationData data = calib.calculateSegmentationData(new File(filename), 
				Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE));
		
		return segmenter.segment(data, MarkovSegmentation.USE_NN);
	}



	private SaveDialog saveDialog = null;
	
	public PlateSelectionFrame(String dir, String dest, String calib1, String calib2) {
		
		super();
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		scrollPane = new JScrollPane(mainPanel);
		
		setPreferredSize(new Dimension(1200, 800));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		buttonPanel = new JPanel();
		JButton save = new JButton("save");
		buttonPanel.add(save);
		JButton addMore = new JButton("50 more");
		buttonPanel.add(addMore);
		
		save.addActionListener(this);
		addMore.addActionListener(this);
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		segmenter = SegmentationFactory.getMarkovSegmentation();
		calib = new CalibrationSegmenter(calib1, calib2);
		
		loader = new FrameProcessor(dir, dest);
		addMorePlates(25);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("save".equals(e.getActionCommand()))
				savePanels();
		if ("50 more".equals(e.getActionCommand()))
			addMorePlates(50);
	}
	
	public void addPlatePanel(String filename) {
		try {
			PlatePanel pp = new PlatePanel(this, filename);
			addPlatePanel(pp);
		} catch (ArrayIndexOutOfBoundsException e) {
			segmentationFault(filename);
			showExceptionDialog(e);
			System.out.println("AIOBE for file: "+filename);
			e.printStackTrace();
		} catch (NullPointerException e) {
			segmentationFault(filename);
			showExceptionDialog(e);
			System.out.println("NPE for file: "+filename);
			e.printStackTrace();
		}
	}
	
	public void addPlatePanel(PlatePanel pp) {
		platePanels.add(pp);
		pp.setParent(this);
//		pp.addActionListener(this);
		mainPanel.add(pp);
	}
	
	public void removePlatePanel(PlatePanel pp) {
		platePanels.remove(pp);
//		pp.removeActionListener(this);
//		mainPanel.remove(pp);
//		mainPanel.getLayout().removeLayoutComponent(pp);
		mainPanel.remove(pp);
		mainPanel.repaint();
//		mainPanel.paintImmediately(mainPanel.getVisibleRect());
//		scrollPane.repaint();
	}
	
	public void segmentationFault(PlatePanel panel) {
		System.out.println("segfault");
		try {
			loader.saveSegFault(panel);
			removePlatePanel(panel);

		} catch (IOException e) {
			e.printStackTrace();
			showExceptionDialog(e);
		}
	}
	
	public void segmentationFault(String filename) {
		System.out.println("segfault");
		try {
			loader.saveSegFault(filename);
		} catch (IOException e) {
			e.printStackTrace();
			showExceptionDialog(e);
		}
	}
	
	public void recognitionFault(PlatePanel panel) {
		try {
			System.out.println("recognitionFault");
			loader.saveRecFault(panel);
			removePlatePanel(panel);
		} catch (Exception e) {
			e.printStackTrace();
			showExceptionDialog(e);
		}
		
	}
	
	private void showExceptionDialog(Exception e) {
		JOptionPane.showMessageDialog(this, e, "ERROR", JOptionPane.ERROR_MESSAGE);
	}
	
	private void addMorePlates(int number) {
		for (String filename : loader.moreFrames(number))
			addPlatePanel(filename);
	}
	
	public void savePanels() {
		System.out.println("save panels");
		List<PlatePanel> panels = new ArrayList<PlatePanel>();
		for (PlatePanel pp : platePanels)
			if (pp.saveCheckbox.isSelected())
				panels.add(pp);
		
		if (panels.isEmpty()) return;
		
		if (saveDialog == null) saveDialog = new SaveDialog(this);
		saveDialog.showDialog(panels);
		String number = saveDialog.getNumber();
		
		if (number == null) return;
		loader.saveSegmentedPanels(panels, number);
		
		for (PlatePanel pp : panels)
			removePlatePanel(pp);
		
	}
	
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		final PlateSelectionFrame frame = //new PlateSelectionFrame(args[0], args[1]);
//				 new PlateSelectionFrame(Repository.getPlateFolderFile("051").getAbsolutePath(), "/Users/pps/dev/dest");
		 new PlateSelectionFrame("C:\\dev\\frames\\VNew\\detected1411", "C:\\dev\\frames\\VNew\\segmented1411", 
				 "C:\\dev\\frames\\VNew\\cal1411\\CalLine1.xml", "C:\\dev\\frames\\VNew\\cal1411\\CalLine2.xml");

//		File dir = new File("/Users/pps/dev/PlatesTestPictures");
////
////		
//		for (String filestr : dir.list()) {
//		
//			String filename = new File(dir, filestr).getAbsolutePath();
//			frame.addPlatePanel(filename);
//
//		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	

}
