import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

public class CalendarMain extends JFrame {
	private final static int GWIDTH = 1280, GHEIGHT = 970, BORDERSIZE = GWIDTH/150 + GHEIGHT/150;
	public MouseInput mouseInput = new MouseInput();
	
	public CalendarMain(int GWIDTH, int GHEIGHT) {
		requestFocus();
		setSize(GWIDTH, GHEIGHT);
		setAlwaysOnTop(false);
		setUndecorated(true);
		getContentPane().setBackground(Color.BLACK);
		getRootPane().setBorder(BorderFactory.createLineBorder(Color.WHITE, BORDERSIZE));
		addMouseListener(mouseInput);
		
		CalendarWindow calendarWindow = new CalendarWindow(GWIDTH - BORDERSIZE * 2, GHEIGHT - BORDERSIZE * 2, this);
		addKeyListener(calendarWindow);
		add(calendarWindow);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new CalendarMain(GWIDTH, GHEIGHT);
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
}