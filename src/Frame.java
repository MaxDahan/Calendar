import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

public class Frame extends JFrame {
	public final int WIDTH = 1280, HEIGHT = 970;

	public Frame() {
		int BORDERSIZE = WIDTH/150 + HEIGHT/150;
		
		requestFocus();
		setSize(WIDTH, HEIGHT);
		setAlwaysOnTop(false);
		setUndecorated(true);
		getContentPane().setBackground(Color.BLACK);
		getRootPane().setBorder(BorderFactory.createLineBorder(Color.WHITE, BORDERSIZE));
		addMouseListener(new MouseInput());

		Main main = new Main(WIDTH - BORDERSIZE * 2, HEIGHT - BORDERSIZE * 2, this);
		addKeyListener(main);
		add(main);
		
		requestFocus();
		setVisible(true);
	}
	
	// listens for mouse click
	public class MouseInput extends MouseAdapter {
		private int lastX = 0; private int lastY = 0;
    	private Thread plus_follow = new Thread();
        public void mousePressed(MouseEvent e) {
        	requestFocus();
        	lastX = getX(); lastY = getY();
        	plus_follow = new MouseFollow();
        	plus_follow.start();
        }
        public void mouseReleased(MouseEvent e) {
        	plus_follow.stop();
        	lastX = Math.abs(lastX - getX());
        	lastY = Math.abs(lastY - getY());
        }
	}
	// makes the frame follow the mouse
	private class MouseFollow extends Thread {
		private boolean first = true;
		private int mouseLastx = 0;
		private int mouseLasty = 0;
		
		public void run() {
			while(true) {
				try {Thread.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
				Point mousePos = MouseInfo.getPointerInfo().getLocation();
				if(!first) {
					mouseLastx = (int)mousePos.getX() - mouseLastx;
					mouseLasty = (int)mousePos.getY() - mouseLasty;
					setLocation(getX() + mouseLastx, getY() + mouseLasty);
				}
				first = false;
				mouseLastx = (int)mousePos.getX();
				mouseLasty = (int)mousePos.getY();
			}
		}
	}
	public static void main(String[] args) {
		new Frame();
	}
}
