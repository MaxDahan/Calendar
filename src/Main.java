import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Main extends JLayeredPane implements KeyListener {
	// my classes
	public Frame frame;
	public AutoDayChange autoDayChange;
	public YesOrNoDialogBox yesOrNoDialogBox;
	public TextFieldThread textFieldThread;
	// java
	public Calendar calendar, cal;
	public DialogBoxThread dialogBoxThread;
	// swing
	public AffineTransform affineTransform;
	public FontRenderContext frc;
	public JTextArea[][] textAreas;
	public JLabel monthTitle, yearTitle;
	public JTextField pathTextField;
	// primitives
	public boolean startDate, dialogBoxOpen, running;
	public int WIDTH, HEIGHT, date, currentYear, viewYear;
	public ArrayList<String> months, daysOfTheWeek;
	public String boxFormatText, path, day, firstDay, currentMonth, viewMonth, reset;

	public Main(int WIDTH, int HEIGHT, Frame frame) {
		setUpPane();
		setUpFields(WIDTH, HEIGHT, frame);
		setUpComponents();
		loadInfo(true);
		loadTextAreas();
		setFirstDay();
		
		autoDayChange.start();
		textFieldThread.start();
	}
	
	// set up methods
	public void setUpPane() {
		setLayout(null);
	}
	public void setUpFields(int WIDTH, int HEIGHT, Frame frame) {
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		this.frame = frame;

		autoDayChange = new AutoDayChange();
		textFieldThread = new TextFieldThread();
		running = true;
		
		reset = "";
		path = "C:\\Users\\" + System.getProperty("user.name") + "\\OneDrive\\Calendar\\";
		dialogBoxOpen = false;
		
		months = new ArrayList<String>() {{add("January"); add("February");add("March");add("April");add("May");add("June");add("July");add("August");add("September");add("October");add("November");add("December");}};
		daysOfTheWeek = new ArrayList<String>() {{add("Sunday"); add("Monday");add("Tuesday");add("Wednesday");add("Thursday");add("Friday");add("Saturday");}};
		// starts with Sunday because Calendar.DAY_OF_WEEK does
		boxFormatText = "";

		calendar = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		day = calendar.get(calendar.DAY_OF_MONTH) + " " + daysOfTheWeek.get(calendar.get(calendar.DAY_OF_WEEK) - 1); // format goes "(number) (day of the week)"
		
		currentMonth = months.get(calendar.get(calendar.MONTH));
		viewMonth = currentMonth;
		currentYear = calendar.get(calendar.YEAR);
		viewYear = currentYear;
		
		
		affineTransform = new AffineTransform();
		frc = new FontRenderContext(affineTransform, true, true);
		
		setFirstDay();
	}
	public void setUpComponents() { // setting up JComponents like titles and calling the method to set up day JTextAreas
		textAreas = new JTextArea[7][6];
		frame.setIconImage(new ImageIcon(this.getClass().getResource("icon.png")).getImage());
		
		monthTitle = new JLabel("September");
		monthTitle.setForeground(Color.WHITE);
		monthTitle.setFont(new Font("Arial", Font.BOLD, 50));
		monthTitle.setText(currentMonth);
		Rectangle2D monthBox = monthTitle.getFont().getStringBounds("September", frc); // sets up the box for the month title to have space for the largest month name
		monthTitle.setBounds(WIDTH / 100, HEIGHT / 100, (int)monthBox.getWidth() + (int)monthBox.getWidth()/10, (int)monthBox.getHeight());
		setLayer(monthTitle, 2);
		add(monthTitle);
		
		yearTitle = new JLabel("2000");
		yearTitle.setForeground(Color.WHITE);
		yearTitle.setFont(new Font("Arial", Font.BOLD, 50));
		yearTitle.setText(currentYear + "");
		Rectangle2D yearBox = yearTitle.getFont().getStringBounds("2000", frc); // sets up the box for the year title to have space for the largest year
		yearTitle.setBounds(WIDTH - WIDTH / 100 - (int)yearBox.getWidth(), HEIGHT / 100, (int)yearBox.getWidth() + (int)yearBox.getWidth()/10, (int)yearBox.getHeight());
		setLayer(yearTitle, 2);
		add(yearTitle);
		
		pathTextField = new JTextField(path, 50);
		pathTextField.setForeground(Color.WHITE);
		pathTextField.setBackground(Color.BLACK);
		pathTextField.setFocusable(false);
		pathTextField.setFocusable(true);
		pathTextField.setFont(new Font("Arial", Font.PLAIN, 20));
		pathTextField.setBounds(290, 18, 825, 40);
		pathTextField.addActionListener(new TextFieldAction());
		add(pathTextField);
		
		// sorts daysOfTheWeek to the correct order
		String temp = daysOfTheWeek.get(0);
		for(int i = 0; i < daysOfTheWeek.size() - 1; i++) {
			daysOfTheWeek.set(i, daysOfTheWeek.get(i + 1));
		}
		daysOfTheWeek.set(daysOfTheWeek.size() - 1, temp);

		// days of the week
		for(int i = 0; i < daysOfTheWeek.size(); i++) {
			JLabel text = new JLabel(daysOfTheWeek.get(i));
			text.setFont(new Font("Arial", Font.BOLD, 25));
			text.setForeground(Color.WHITE);
			Rectangle2D textBox = text.getFont().getStringBounds(text.getText(), frc);
			int pos = 38;
			if(daysOfTheWeek.get(i).equals("Wednesday")) pos = 15;
			else if(daysOfTheWeek.get(i).equals("Thursday")) pos = 27;
			else if(daysOfTheWeek.get(i).equals("Saturday")) pos = 27;
			else if(daysOfTheWeek.get(i).equals("Tuesday")) pos = 33;
			text.setBounds(WIDTH/7 * i + pos, 65, (int)textBox.getWidth() + (int)textBox.getWidth()/10, (int)textBox.getHeight());
			add(text);
		}
		
		setUpTextAreas();

	}
	public void setUpTextAreas() { // the text areas represent each day
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < daysOfTheWeek.size(); i++) {
				boxFormatter(i, j);
				JTextArea text = new JTextArea();
				text.setFont(new Font("Arial", Font.BOLD, 13));
				text.insert(boxFormatText, 0);
				text.setForeground(Color.BLACK);
				text.setBackground(Color.WHITE);
				Rectangle2D box = text.getFont().getStringBounds(text.getText(), frc);
				text.setBounds(WIDTH/7 * i + 10, j * 140 + 100, 165, 130);
				add(text);
				textAreas[i][j] = text;
				
				JScrollPane jp = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jp.setBounds(text.getX(), text.getY(), text.getWidth(), text.getHeight() + 2);
				jp.setBorder(null);
				jp.getVerticalScrollBar().setUI(new VertScrollBarUI());
				jp.getHorizontalScrollBar().setUI(new HorScrollBarUI());
				add(jp);
				
				boxFormatText = "";
			}
		}
		date = 1;
		startDate = false;
	}
	
	// reset methods

	public void resetMonth() {
		dialogBoxThread = new DialogBoxThread();
		yesOrNoDialogBox = new YesOrNoDialogBox(this);
		dialogBoxThread.start();
	}
	public void changeMonth(int direction) { // method called when you change months, it saves the current month and loads in the new one
		save();
		if(direction == 1) { // next month
			if (months.indexOf(viewMonth) + 1 >= months.size()) { // next year
				viewMonth = months.get(0);
				viewYear++;
			} else {
				viewMonth = months.get(months.indexOf(viewMonth) + 1);
			}
		} else { // previous month
			if(months.indexOf(viewMonth) - 1 < 0) { // previous year
				viewYear--;
				viewMonth = months.get(months.size() - 1);
			} else {
				viewMonth = months.get(months.indexOf(viewMonth) - 1);
			}
		}
		monthTitle.setText(viewMonth);
		yearTitle.setText(viewYear + "");
		setFirstDay();
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < daysOfTheWeek.size(); i++) {
				boxFormatter(i, j);

				textAreas[i][j].setText(boxFormatText);
				boxFormatText = "";
			}
		}
		loadTextAreas();
		updateCurrentDateBox();
		repaintComponents();
	}
	public void setFirstDay() {
		date = 1;
		startDate = false;
		cal = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		cal.set(Calendar.YEAR, viewYear);
		cal.set(Calendar.MONTH, months.indexOf(viewMonth));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		firstDay = cal.getTime().toString().substring(0, 3);
	}
	
	public void boxFormatter(int i, int j) { // formats the text within a daily JTextArea
		if(date > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) startDate = false;
		if(j == 0 && firstDay.equals(daysOfTheWeek.get(i).substring(0, 3))) startDate = true;
		if(startDate) {
			if(date < 10) boxFormatText += "___________" + date + "___________\n";
			else boxFormatText += "___________" + date + "__________\n";
			date++;
			boxFormatText += "During: \n\nDue: ";
		}
	}
	
	
	// load and save methods
	private void loadTextAreas() {
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 6; j++) {
				try {
					Scanner input = null;
					if(path.length() > 0) input = new Scanner(new File(path + "" + viewYear + " " + viewMonth + " " + i + "." + j + ".txt"));
					else input = new Scanner(new File(viewYear + " " + viewMonth + " " + i + "." + j + ".txt"));
					
					String currentText = "";
					textAreas[i][j].setText("");
					boolean beginning = true;
					while(input.hasNextLine()) {
						if(!beginning) textAreas[i][j].append("\n");
						else beginning = false;
						textAreas[i][j].append(input.nextLine());
					}
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void loadInfo(boolean initial) { // loads the viewMonth and position of the frame
		try {
			// finds the file
			Scanner input = null;
			if(path.length() > 0) input = new Scanner(new File(path + "0. info.txt"));
			else input = new Scanner(new File("0. info.txt"));
			
			// checks the month
			viewMonth = input.next();
			monthTitle.setText(viewMonth);
			
			// checks the frame location (X and Y)
			String windowX = input.next();
			String windowY = input.next();
			frame.setLocation(Integer.parseInt(windowX), Integer.parseInt(windowY));
			
			// checks the year
			viewYear = Integer.parseInt(input.next());
			yearTitle.setText(viewYear + "");
			
			// checks file path written in pathTextField
			if(initial) {
				String oldPath = path;
				path = input.next();
				pathTextField.setText(path);
				if(!oldPath.equals(path)) { // if the path changed
					loadInfo(true);
				}
			}
			
			setFirstDay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void save() { // saves JTextAreas and the Info
		saveJTextAreas();
		saveInfo(path);
	}
	public void saveJTextAreas() {
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 6; j++) {
				try {
					PrintStream out = null;
					if(path.length() > 0) out = new PrintStream(new File(path + "" + viewYear + " " + viewMonth + " " + i + "." + j + ".txt"));
					else out = new PrintStream(new File(viewYear + " " + viewMonth + " " + i + "." + j + ".txt"));
					out.println(textAreas[i][j].getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void saveInfo(String oldPath) { // saves the info data
		PrintStream out = null;
		try {
			if(path.length() > 0) out = new PrintStream(new File(oldPath + "0. info.txt"));
			else out = new PrintStream(new File("0. info.txt"));
			out.print(viewMonth + " "); // saves the month displayed on monthTitle
			out.print(frame.getX() + " " + frame.getY() + " "); // saves the X and Y coordinates of the pane
			out.print(viewYear + " "); // saves the year displayed on yearTitle
			out.print(path + " "); // saves the path written in pathTextField
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	// user interaction
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if(key == KeyEvent.VK_ESCAPE) {
			running = false;
			save();
			System.exit(0);
		} else if(key == KeyEvent.VK_MINUS) {
			frame.setState(JFrame.ICONIFIED);
		} else if(key == KeyEvent.VK_S) {
			save();
		} else if(key == KeyEvent.VK_R && !dialogBoxOpen) {
			resetMonth();
		} else if(key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP) {
			changeMonth(1); // next month
		} else if(key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN) {
			changeMonth(0); // previous month
		}
	}
	
	
	// updater methods
	public void repaintComponents() { // repaints changing components when called
		for(int i = 0; i < textAreas.length; i++) {
			for(int j = 0; j < textAreas[i].length; j++) {
				textAreas[i][j].repaint();
			}
		}
		monthTitle.repaint();
		yearTitle.repaint();
	}	
	public boolean updateCurrentDateBox() {
		date = 1;
		startDate = false;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < daysOfTheWeek.size(); j++) {
				if(date > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) startDate = false;
				if(i == 0 && firstDay.equals(daysOfTheWeek.get(j).substring(0, 3))) startDate = true;
				if(startDate) date++;
				if(Calendar.getInstance().getTime().getDate() + 1 == date && viewMonth.contentEquals(currentMonth)) {
					textAreas[j][i].setForeground(Color.BLUE);
					return true;
				} else {
					textAreas[j][i].setForeground(Color.BLACK);
				}
			}
		}
		return false;
	}
	
	// added classes for buttons, threads, and custom scroll bars
	private class TextFieldAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			save();
			
			String oldPath = path;
			path = pathTextField.getText();
			if(!path.substring(path.length() - 1).equals("\\") && !path.substring(path.length() - 1).equals("/")) {
				path += "\\";
			}
			pathTextField.setText(path);
			saveInfo(oldPath);
			
			viewMonth = currentMonth;
			viewYear = currentYear;
			monthTitle.setText(viewMonth);
			yearTitle.setText(viewYear + "");
			setFirstDay();
			for(int j = 0; j < 6; j++) {
				for(int i = 0; i < daysOfTheWeek.size(); i++) {
					boxFormatter(i, j);

					textAreas[i][j].setText(boxFormatText);
					boxFormatText = "";
				}
			}
			loadInfo(false);
			loadTextAreas();
			updateCurrentDateBox();
			repaintComponents();
			
		}
	}
	private class TextFieldThread extends Thread {
		public boolean timerRunning = false;
		public void run() {
			try {
				dialogBoxOpen = true;
				while(running) {
					if(!timerRunning) {
						String newPath = pathTextField.getText();
						Thread.sleep(100);
						if(!newPath.equals(path)) { // if a new path has been typed into the pathTextField box start the timer that will reset it after 10 seconds
							timerRunning = true;
						}
					} else {
						// wait 10 seconds then reset the text in the pathTextField to the actual path
						Thread.sleep(10000);
						pathTextField.setText(path);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private class DialogBoxThread extends Thread {
		public int timer = 0;
		public void run() {
			try {
				dialogBoxOpen = true;
				while(timer <= 500) {
					if(reset.equals("yes")) {
						setFirstDay();
						for(int j = 0; j < 6; j++) {
							for(int i = 0; i < daysOfTheWeek.size(); i++) {
								boxFormatter(i, j);
				
								textAreas[i][j].setText(boxFormatText);
								boxFormatText = "";
							}
						}
						repaintComponents();
						break;
					} else if(reset.equals("no")) {
						break;
					}
					timer++;
					Thread.sleep(10);
				}
				dialogBoxOpen = false;
				reset = "";
				timer = 0;
				yesOrNoDialogBox.stop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private class AutoDayChange extends Thread { // Used to Automatically change the color of the current date box
		private boolean run = true;
		
		public void run() {
			while(run) {
				try {
					updateCurrentDateBox();
					Thread.sleep(1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private class VertScrollBarUI extends BasicScrollBarUI { // JScrollPaneUI Vertical Scroll Bar
		//part behind scroll
	    protected void paintTrack(Graphics g, JComponent c, Rectangle tb) {
	    	g.setColor(new Color(226, 226, 226));
	    	g.fillRect((int)tb.getX(), (int)tb.getY(), (int)tb.getWidth(), (int)tb.getHeight());
	    	
	    }
	    //actual bar
	    protected void paintThumb(Graphics g, JComponent c, Rectangle tb) {
	    	
	    	g.setColor(new Color(70, 70, 70));
	    	g.fillRect((int)tb.getX() + 1, (int)tb.getY() + 5, (int)tb.getWidth() - 4, (int)tb.getHeight() - 10);
	    	
	    	//top rounded edge
	    	g.fillOval((int)tb.getX() + 1, (int)tb.getY(), 12, 12);
	    	
	    	//bottom rounded edge
	    	g.fillOval((int)tb.getX() + 1, (int)tb.getY() + (int)tb.getHeight() - 13, 12, 12);
	    }
	
	    protected JButton createDecreaseButton(int orientation) {
	    	JButton button = new JButton();
	        BufferedImage img = null;
	        button.setBorder(null);
	        button.setFocusable(false);
	        try {img = ImageIO.read(this.getClass().getResource("up.png"));} catch (IOException e1) {e1.printStackTrace();}
	        button.setIcon(new ImageIcon(img));
	        button.setPreferredSize(new Dimension(20, 20));
		    button.setForeground(Color.BLACK);
	        button.setBackground(new Color(226, 226, 226));
	        return button;
	    }
	 
	    protected JButton createIncreaseButton(int orientation) {
	    	JButton button = new JButton();
	        BufferedImage img = null;
	        button.setBorder(null);
	        button.setFocusable(false);
	        try {img = ImageIO.read(this.getClass().getResource("down.png"));} catch (IOException e1) {e1.printStackTrace();}
	        button.setIcon(new ImageIcon(img));
	        button.setPreferredSize(new Dimension(14, 14));
		    button.setForeground(Color.BLACK);
	        button.setBackground(new Color(226, 226, 226));
	        return button;
	    }
	}
	private class HorScrollBarUI extends BasicScrollBarUI { // JScrollPaneUI Horizontal Scroll Bar
		//part behind scroll
	    protected void paintTrack(Graphics g, JComponent c, Rectangle tb) {
	    	g.setColor(new Color(226, 226, 226));
	    	g.fillRect((int)tb.getX(), (int)tb.getY(), (int)tb.getWidth(), (int)tb.getHeight());
	    	
	    }
	    //actual bar
	    protected void paintThumb(Graphics g, JComponent c, Rectangle tb) {
	    	g.setColor(new Color(70, 70, 70));
	    	g.fillRect((int)tb.getX() + 5, (int)tb.getY() + 2, (int)tb.getWidth() - 10, (int)tb.getHeight() - 4);
	    	
	    	//top rounded edge
	    	g.fillOval((int)tb.getX(), (int)tb.getY() + 2, 12, 12);
	    	
	    	//bottom rounded edge
	    	g.fillOval((int)tb.getX() + (int)tb.getWidth() - 13, (int)tb.getY() + 2, 12, 12);
	    }
	    
	    protected JButton createDecreaseButton(int orientation) {
	    	JButton button = new JButton();
	        BufferedImage img = null;
	        button.setBorder(null);
	        button.setFocusable(false);
	        try {img = ImageIO.read(this.getClass().getResource("left.png"));} catch (IOException e1) {e1.printStackTrace();}
	        button.setIcon(new ImageIcon(img));
	        button.setPreferredSize(new Dimension(14, 14));
		    button.setForeground(Color.BLACK);
	        button.setBackground(new Color(226, 226, 226));
	        return button;
	    }
	 
	    protected JButton createIncreaseButton(int orientation) {
	    	JButton button = new JButton();
	        BufferedImage img = null;
	        button.setBorder(null);
	        button.setFocusable(false);
	        try {img = ImageIO.read(this.getClass().getResource("right.png"));} catch (IOException e1) {e1.printStackTrace();}
	        button.setIcon(new ImageIcon(img));
	        button.setPreferredSize(new Dimension(14, 14));
		    button.setForeground(Color.BLACK);
	        button.setBackground(new Color(226, 226, 226));
	        return button;
	    }
	}
	
	// unused methods
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}