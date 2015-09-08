package ru.recog.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.imgproc.SegmentationResult;
import ru.recog.imgproc.Segmenter;

public class PlatePanel extends JPanel implements ActionListener {
	
	private SegmentationResult segResult;
	private int scaleFactor = 3;
	private Mat m;
	private String filename;
	
	private int firstSegmentIndex = 0;
	
	private JButton segFault, recFault, showOrig;
	public JCheckBox saveCheckbox;
	
	private PlateSelectionFrame parent;
	
	private ImageIcon ii;
	
	public PlatePanel(String filename) {
		super();
		this.filename = filename;
		loadData();
		
		setBorder(new LineBorder(Color.darkGray, 1, true));
		setLayout(new BorderLayout());
		
		saveCheckbox = new JCheckBox();
		segFault = new JButton("seg");
		recFault = new JButton("rec");
		showOrig = new JButton("orig");
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		
		
		ii = new ImageIcon(ImageUtils.mat2Image(ImageUtils.scaleUp(m,scaleFactor)));
		JLabel l = new JLabel(ii);
		
		l.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { processMouseClick(e); }
		});
		
		left.add(l);
		left.add(saveCheckbox);
		
		
		right.add(segFault);
		right.add(recFault);
		right.add(showOrig);
		
		add(left, BorderLayout.LINE_START);
		add(right, BorderLayout.LINE_END);
		
		addActionListener(this);
		
	}
	
	
	private void loadData() {
		
		Mat orig = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		
//		try {
			segResult = Segmenter.segment(orig);
			
			for (int p : segResult.getCutPoints())
				Imgproc.line(m, new Point(p, 0), new Point(p, m.rows()-1), new Scalar(0,255,0));
			
			for (Rect r : segResult.getRevisedRectangles()) {
				Imgproc.line(m, new Point(r.x, r.y), new Point(r.x+r.width,r.y), new Scalar(255,0,0));
				Imgproc.line(m, new Point(r.x, r.y+r.height), new Point(r.x+r.width, r.y+r.height), new Scalar(255,0,0));
			}
			
//		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
//			System.out.println("for file: "+filename);
//			e.printStackTrace();
//		}
	}

	
	public void setParent(PlateSelectionFrame parent) {
		this.parent = parent;
	}
	
	public String getFilename() {
		return filename;
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("orig".equals(e.getActionCommand())) {
			JOptionPane.showMessageDialog(parent, "", "Original size",JOptionPane.OK_OPTION ,
					new ImageIcon(ImageUtils.mat2Image(segResult.getOriginalMat())));
		}
//			parent.removePlatePanel(this);
		if ("seg".equals(e.getActionCommand()))
			parent.segmentationFault(this);
		if ("rec".equals(e.getActionCommand()))
			parent.recognitionFault(this);
	}
	
	
	public void addActionListener(ActionListener al) {
		segFault.addActionListener(al);
		recFault.addActionListener(al);
		showOrig.addActionListener(al);
	}
	
	public void removeActionListener(ActionListener al) {
		segFault.removeActionListener(al);
		recFault.removeActionListener(al);
		showOrig.removeActionListener(al);
	}
	
	Image getImage() {
		return ii.getImage();
	}
	
	SegmentationResult getSegmentationResult() {
		return segResult;
	}
	
	int getFirstSegmentIndex() {
		return firstSegmentIndex;
	}
	
	private void processMouseClick(MouseEvent e) {
		int x = e.getX();
		int s = -1;
		for (int i = 0; i < segResult.getCutPoints().size(); i++) {
			if (x < scaleFactor*segResult.getCutPoints().get(i)) {
				s = i; break;
			}
		}
		
//		System.out.println("Segment # "+s);
		if (s!=-1) {
			saveCheckbox.setSelected(true);
			firstSegmentIndex = s;
			int drawX = s==0? 0 : segResult.getCutPoints().get(s-1)*scaleFactor;
//			Graphics g = ii.getImage().getGraphics();
			Image img = ImageUtils.mat2Image(ImageUtils.scaleUp(m,scaleFactor));
			Graphics g = img.getGraphics();
			g.setColor(Color.RED);
			g.drawLine(drawX, 1, drawX, m.rows()*scaleFactor-2);
			g.drawLine(drawX+1, 1, drawX+1, m.rows()*scaleFactor-2);
			g.drawLine(drawX+2, 1, drawX+2, m.rows()*scaleFactor-2);

			ii.setImage(img);
			final Component c = e.getComponent();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					c.repaint();
				}
			});
		}


	}
	
	
	
	public static void main(String args[]) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		final JFrame frame = new JFrame("big frame");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPane = new JScrollPane(panel);
		
		frame.setPreferredSize(new Dimension(800, 600));
		frame.add(scrollPane, BorderLayout.CENTER);
		
		File dir = new File("/Users/pps/dev/PlatesTestPictures");
//
//		
		for (String filestr : dir.list()) {
		
			String filename = new File(dir, filestr).getAbsolutePath();
//			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//			
//			try {
//				SegmentationResult result = Segmenter.segment(m);
//				
////				List<Mat> pieces = result.getRevisedSegments(); //FIXME
//				
//				for (int p : result.getCutPoints())
//					Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
//				
//				for (Rect r : result.getRevisedRectangles()) {
//					Imgproc.line(m1, new Point(r.x, r.y), new Point(r.x+r.width,r.y), new Scalar(255,0,0));
//					Imgproc.line(m1, new Point(r.x, r.y+r.height), new Point(r.x+r.width, r.y+r.height), new Scalar(255,0,0));
//				}
//				
//				PlatePanel pp = new PlatePanel(m1, result);
//				panel.add(pp);
//			} catch (ArrayIndexOutOfBoundsException e) {
//				// TODO Auto-generated catch block
//				System.out.println("for file: "+filename);
//				e.printStackTrace();
//			}


		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});
		

	}

}
