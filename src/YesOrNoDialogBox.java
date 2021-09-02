import java.awt.Color;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;

public class YesOrNoDialogBox implements KeyListener {
	public JFrame frame;
	public JLayeredPane panel;
	public MouseInput mouseInput;
	public MyButton yesBox, noBox;
	public int WIDTH, HEIGHT;
	public Main main;
	
	public YesOrNoDialogBox(Main main) {
		this.main = main;
		
		WIDTH = 250;
		HEIGHT = 125;
		
		int BORDERSIZE = WIDTH/50 + HEIGHT/50;
		frame = new JFrame();
		
		frame.requestFocus();
		frame.setSize(WIDTH, HEIGHT);
		frame.setAlwaysOnTop(false);
		frame.setUndecorated(true);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setLocation(main.frame.getX() + main.WIDTH/2 - WIDTH/2, main.frame.getY() + main.HEIGHT/2 - HEIGHT/2);
		frame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.WHITE, BORDERSIZE));
		mouseInput = new MouseInput();
		frame.addMouseListener(mouseInput);

		panel = new JLayeredPane();
		panel.setLayout(null);
		
		addComponents();
		
		frame.addKeyListener(this);
		frame.add(panel);
		frame.setVisible(true);
		
	}
	public void addComponents() {
		yesBox = new MyButton();
		yesBox.setBackground(Color.GREEN);
		yesBox.setHoverBackgroundColor(new Color(0, 200, 0));
		yesBox.setPressedBackgroundColor(new Color(0, 175, 0));
		yesBox.setFocusable(false);
		yesBox.setBounds(126, 10, 100, 90);
		yesBox.setBorder(null);
		yesBox.addActionListener(new YesBoxAction());
		yesBox.setFont(new Font("Arial", Font.BOLD, 70));
		panel.add(yesBox);
		
		noBox = new MyButton();
		noBox.setBackground(Color.RED);
		noBox.setHoverBackgroundColor(new Color(200, 0, 0));
		noBox.setPressedBackgroundColor(new Color(175, 0, 0));
		noBox.setFocusable(false);
		noBox.setBounds(10, 10, 100, 90);
		noBox.setBorder(null);
		noBox.addActionListener(new NoBoxAction());
		noBox.setFont(new Font("Arial", Font.BOLD, 70));
		panel.add(noBox);
	}
	
	public void stop() {
		mouseInput.stop();
		frame.dispose();
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_ESCAPE) {
			mouseInput.stop();
			frame.dispose();
		}
	}
	
	private class YesBoxAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			main.reset = "yes";
			stop();
		}
	}
	private class NoBoxAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			main.reset = "no";
			stop();
		}
	}
	// listens for mouse click
	public class MouseInput extends MouseAdapter {
		private int lastX = 0; private int lastY = 0;
    	private Thread plus_follow = new Thread();
        public void mousePressed(MouseEvent e) {
        	frame.requestFocus();
        	lastX = frame.getX(); lastY = frame.getY();
        	plus_follow = new MouseFollow();
        	plus_follow.start();
        }
        public void mouseReleased(MouseEvent e) {
        	plus_follow.stop();
        	lastX = Math.abs(lastX - frame.getX());
        	lastY = Math.abs(lastY - frame.getY());
        }
        
        public void stop() {
        	plus_follow.stop();
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
					frame.setLocation(frame.getX() + mouseLastx, frame.getY() + mouseLasty);
				}
				first = false;
				mouseLastx = (int)mousePos.getX();
				mouseLasty = (int)mousePos.getY();
			}
		}
	}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
}
