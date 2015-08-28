package ru.recog;

import java.awt.EventQueue;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ru.recog.video.Skip24VideoCap;
import ru.recog.video.VideoCap;

public class MyFrame extends JFrame {
    private JPanel contentPane;
    
    VideoCap videoCap; 


  /**
  * Launch the application.
  */
    public static void main(String[] args) {
    	if (args.length == 0) {
    		System.out.println("Video location required");
    		System.exit(1);
    	}
    	final String videoURL = args[0];
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MyFrame frame = new MyFrame(videoURL);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

  /**
  * Create the frame.
  */
    public MyFrame() {
    	this(null);

    }
    
    public MyFrame(String videoURL) {
    	
    	if (videoURL == null)
    		videoCap = new VideoCap();
    	else
    		videoCap = new Skip24VideoCap(videoURL, DetectUtil.CASCADE_LPRHAAR);
//			videoCap = new Skip24VideoCap(videoURL, "/Users/pps/dev/javablur/cascade.xml");	

        
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 490);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
  
        new MyThread().start();
    }
 
//    VideoCap videoCap = new VideoCap();
//    videoCap.open(0);
 
    public void paint(Graphics g){
        g = contentPane.getGraphics();
        g.drawImage(videoCap.getOneFrame(), 0, 0, this);
    }
 
    class MyThread extends Thread{
        @Override
        public void run() {
            for (;;){
                repaint();
                try { Thread.sleep(30);
                } catch (InterruptedException e) {    }
            }  
        } 
    }
}