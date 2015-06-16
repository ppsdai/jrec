package ru.recog;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class SimpleViewer extends JFrame implements ActionListener{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String ACTION_1 = "Action 1";
	private static String ACTION_BLUR = "Blur";
	private static String ACTION_2 = "Action 2";
	
	
	private JLabel imageLabel;
	private ImageIcon imageIcon;
	
	public SimpleViewer(String framename) {
		super(framename);
		setupGUI();
		
	}
	
	private void loadImage(Image image) {
		System.out.println("before");
		imageIcon.setImage(image);
		System.out.println("before");
		imageLabel.repaint();
	}
	
	private Image getMainImage() {
		return imageIcon.getImage();
	}
	
	private void setupGUI() {
		setLayout(new BorderLayout());
		setSize(800,600);
		
		imageIcon = new ImageIcon();
		imageLabel = new JLabel(imageIcon);
		try {
			loadImage(ImageIO.read(new File("/Users/pps/dev/fruits.jpg")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JScrollPane scrollPane = new JScrollPane(imageLabel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.getContentPane().add(scrollPane,BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton b1 = new JButton(ACTION_1);
		JButton b2 = new JButton(ACTION_BLUR);
		JButton b3 = new JButton(ACTION_2);
		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);
		buttonPanel.add(b1);
		buttonPanel.add(b2);
		buttonPanel.add(b3);
		add(buttonPanel,BorderLayout.SOUTH);
		
	}
	
	private void blur() {
		System.out.println("Blur");
		/*
		
		Mat src = Imgproc.imread( argv[1], 1 );


		waitKey(2000);

		//  namedWindow( argv[1], WINDOW_AUTOSIZE );
		imshow("Unprocessed Image1",src);

		dst = src.clone();
		GaussianBlur( src, dst, Size( 15, 15 ), 0, 0 );

		//namedWindow( window_name2, WINDOW_AUTOSIZE );
		imshow("Processed Image2",dst);
		*/
		
		/*
		try {
			imageLabel.setIcon(new ImageIcon(ImageIO.read(new File("/Users/pps/dev/huy.png"))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (ACTION_BLUR.equals(e.getActionCommand())) {
			blur();
		}
		else if (ACTION_1.equals(e.getActionCommand())) {
			System.out.println(ACTION_1);
		} else if (ACTION_2.equals(e.getActionCommand())) {
			System.out.println(ACTION_2);
		}
	}
	

}
