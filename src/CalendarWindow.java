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
import java.util.Calendar;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CalendarWindow extends JLayeredPane implements KeyListener {
	// JComponenets
	private JTextArea[][] textAreas;
	private JLabel monthTitle;
	private JLabel[] resetBoxText;
	private MyButton resetBox;
	// Basic
	private int GWIDTH, GHEIGHT;
	private String month, day, boxFormat, startingDay, firstDay; 
	private String[] months, daysOfTheWeek;
	private int date;
	private boolean startDate;
	private String currentDirectory;
	// My Classes
	private Calendar cal;
	private Calendar Calendar;
	private CalendarMain calendarMain;
	private AutoDayChange autoDayChange;
	// Other
	private AffineTransform affineTransform; 
	private FontRenderContext frc;
	private JFileChooser jfc;

	public CalendarWindow(int GWIDTH, int GHEIGHT, CalendarMain calendarMain) {
		this.GWIDTH = GWIDTH;
		this.GHEIGHT = GHEIGHT;
		this.calendarMain = calendarMain;
		
		setLayout(null);
		setUpFields();
		setUpComponents();
		load("");
		
		// makes the current day's text blue
		updateCurrentDateBox();
		
		autoDayChange.start();
	}
	
	// Methods for SetUp
	private void setUpFields() { // Instantiate the fields
		// string related fields
		boxFormat = ""; // the format for each box and the day of the week the first day starts on
		months = new String[] {"January", "February", "March", "April", "May","June", "July", "August", "September", "October", "November", "December"};
		daysOfTheWeek = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // starts with Sunday because Calendar.DAY_OF_WEEK does
		currentDirectory = "";
		
		// calendar related fields (including strings)
		Calendar = Calendar.getInstance();
		month = months[Calendar.get(Calendar.MONTH)];
		day = Calendar.get(Calendar.DAY_OF_MONTH) + " " + daysOfTheWeek[Calendar.get(Calendar.DAY_OF_WEEK) - 1]; // format goes "(number) (day of the week)"
		jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		
		// threads
		autoDayChange = new AutoDayChange();
		
		// components
		affineTransform = new AffineTransform();
		frc = new FontRenderContext(affineTransform, true, true);
		textAreas = new JTextArea[7][6];
		resetBoxText = new JLabel[3];
		
		// format of days
		date = 1;
		startDate = false;
		cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		firstDay = cal.getTime().toString().substring(0, 3);
	}
	// Component SetUp
	private void setUpComponents() { // Creates the JComponents and places them accordingly
		calendarMain.setIconImage(new ImageIcon("icon.png").getImage());
		
		titles();
		days();
	}
	private void titles() { // Month and days of the week
		monthTitle = new JLabel(month);
		monthTitle.setForeground(Color.WHITE);
		monthTitle.setFont(new Font("Arial", Font.BOLD, 50));
		Rectangle2D box = monthTitle.getFont().getStringBounds(monthTitle.getText(), frc);
		monthTitle.setBounds(GWIDTH / 100, GHEIGHT / 100, (int)box.getWidth() + (int)box.getWidth()/10, (int)box.getHeight());
		setLayer(monthTitle, 2);
		add(monthTitle);
		
		resetBox = new MyButton();
		resetBox.setBackground(Color.RED);
		resetBox.setHoverBackgroundColor(new Color(200, 0, 0));
		resetBox.setPressedBackgroundColor(new Color(175, 0, 0));
		resetBox.setFocusable(false);
		resetBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
		resetBox.setBounds(GWIDTH - 60, 10, 50, 50);
		resetBox.addActionListener(new ResetBoxAction());
		resetBox.setFont(new Font("Arial", Font.BOLD, 70));
		add(resetBox);
		
		for(int i = 0; i < 3; i++) {
			if(i == 0) {
				resetBoxText[i] = new JLabel("Reset Calendar");
				resetBoxText[i].setBounds(GWIDTH - 175, 11, 200, 15);
			} else if(i == 1) {
				resetBoxText[i] = new JLabel("At the End");
				resetBoxText[i].setBounds(GWIDTH - 138, 28, 200, 15);
			}else if(i == 2) {
				resetBoxText[i] = new JLabel("Of the Month");
				resetBoxText[i].setBounds(GWIDTH - 159, 46, 200, 15);
			}
			resetBoxText[i].setForeground(Color.WHITE);
			resetBoxText[i].setFont(new Font("Arial", Font.BOLD, 15));
			box = resetBoxText[i].getFont().getStringBounds(resetBoxText[i].getText(), frc);
			add(resetBoxText[i]);
		}
		
		// sorts daysOfTheWeek to the correct order
		String temp = daysOfTheWeek[0];
		for(int i = 0; i < daysOfTheWeek.length - 1; i++) {
			daysOfTheWeek[i] = daysOfTheWeek[i + 1];
		}
		daysOfTheWeek[daysOfTheWeek.length - 1] = temp;

		// days of the week
		for(int i = 0; i < daysOfTheWeek.length; i++) {
			JLabel text = new JLabel(daysOfTheWeek[i]);
			text.setFont(new Font("Arial", Font.BOLD, 25));
			text.setForeground(Color.WHITE);
			box = text.getFont().getStringBounds(text.getText(), frc);
			int pos = 38;
			if(daysOfTheWeek[i].equals("Wednesday")) pos = 15;
			else if(daysOfTheWeek[i].equals("Thursday")) pos = 27;
			else if(daysOfTheWeek[i].equals("Saturday")) pos = 27;
			else if(daysOfTheWeek[i].equals("Tuesday")) pos = 33;
			text.setBounds(GWIDTH/7 * i + pos, 65, (int)box.getWidth() + (int)box.getWidth()/10, (int)box.getHeight());
			add(text);
		}
	}
	private void days() { // Sets up each JTextArea and their components
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < daysOfTheWeek.length; i++) {
				format(i, j);
				JTextArea text = new JTextArea();
				text.setFont(new Font("Arial", Font.BOLD, 13));
				text.insert(boxFormat, 0);
				text.setForeground(Color.BLACK);
				text.setBackground(Color.WHITE);
				Rectangle2D box = text.getFont().getStringBounds(text.getText(), frc);
				text.setBounds(GWIDTH/7 * i + 10, j * 140 + 100, 165, 130);
				add(text);
				textAreas[i][j] = text;
				
				JScrollPane jp = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jp.setBounds(text.getX(), text.getY(), text.getWidth(), text.getHeight() + 2);
				jp.setBorder(null);
				jp.getVerticalScrollBar().setUI(new VertScrollBarUI());
				jp.getHorizontalScrollBar().setUI(new HorScrollBarUI());
				add(jp);
				
				boxFormat = "";
			}
		}
	}
	private void format(int i, int j) { // Format for each calendar box
		if(date > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) startDate = false;
		if(j == 0 && firstDay.equals(daysOfTheWeek[i].substring(0, 3))) startDate = true;
		if(startDate) {
			if(date < 10) boxFormat += "___________" + date + "___________";
			else boxFormat += "___________" + date + "__________";
			date++;
			if(daysOfTheWeek[i].equals("Wednesday")) { // block day
				boxFormat += "\n2. \n\n4. \n\n6. ";
			} else if(daysOfTheWeek[i].equals("Thursday")) { // block day
				boxFormat += "\n1. \n\n3. \n\n5. \n\n7. ";
			} else if(!daysOfTheWeek[i].equals("Saturday") && !daysOfTheWeek[i].equals("Sunday")){
				for(int k = 1; k < 8 && startDate; k++) { // other days
					boxFormat += "\n" + k + ". ";
				}
			} else {
				boxFormat += "\n";
			}
		}
	}
	
	// Calendar information management
	private void load(String path) { // Loads all of the calendar data
		// loads each day's information
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 6; j++) {
				try {
					Scanner input = null;
					if(path.length() > 0) input = new Scanner(new File(path + "" + i + "." + j + ".txt"));
					else input = new Scanner(new File(i + "." + j + ".txt"));
					
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
		// loads the month information and resets it if needed
		try {
			Scanner input = null;
			if(path.length() > 0) input = new Scanner(new File(path + "month.txt"));
			else input = new Scanner(new File("month.txt"));
			String monthData = input.next();
			String monthChange = input.next();
			if(monthChange.equals("true")) {
				resetBox.setBackground(Color.GREEN);
				resetBox.setHoverBackgroundColor(new Color(0, 200, 0));
				resetBox.setPressedBackgroundColor(new Color(0, 175, 0));
				if(!monthData.equals(month)) reset();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void save(String path) { // Saves all of the calendar data 
		// saves all of the daily data
		for(int i = 0; i < 7; i++) {
			for(int j = 0; j < 6; j++) {
				try {
					PrintStream out = null;
					if(path.length() > 0) out = new PrintStream(new File(path + "" + i + "." + j + ".txt"));
					else out = new PrintStream(new File(i + "." + j + ".txt"));
					out.println(textAreas[i][j].getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		// saves the month data
		PrintStream out = null;
		try {
			if(path.length() > 0) out = new PrintStream(new File(path + "month.txt"));
			else out = new PrintStream(new File("month.txt"));
			if(resetBox.getBackground() == Color.GREEN) out.print(month + "  true");
			else out.print(month + " false");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private void reset() { // Resets the calendar and makes it up to date
		date = 1;
		startDate = false;
		cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		firstDay = cal.getTime().toString().substring(0, 3);
		for(int j = 0; j < 6; j++) {
			for(int i = 0; i < daysOfTheWeek.length; i++) {
				format(i, j);
				textAreas[i][j].setText(boxFormat);
				boxFormat = "";
				if(Calendar.getInstance().getTime().getDate() + 1 == date) textAreas[i][j].setForeground(Color.BLUE);
			}
		}
	}
	private int resetConfirm() { // Asks the user if they are sure they want to reset the calendar
		return JOptionPane.showConfirmDialog(this, "Are you sure you want to reset?");
	}
	private class AutoDayChange extends Thread { // Used to Automatically change the color of the current date box
		private boolean run = true;
		private boolean dateUpdated = false;
		
		public void run() {
			while(run) {
				try {
					/*if(!dateUpdated) {
						Thread.sleep(1000);
						if(updateCurrentDateBox()) dateUpdated = true;
					} else {
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");  
						LocalDateTime now = LocalDateTime.now();  
						String currentTime = dtf.format(now);
						Thread.sleep(timeUntilEndOfDay(currentTime) * 1000); // multiplied by 1000 to convert the seconds to milliseconds
						updateCurrentDateBox();
					}*/
					Thread.sleep(1000);
					updateCurrentDateBox();
					// check to see if the month check box is checked and if the current month is not up to date
					if(resetBox.getBackground() == Color.GREEN && !month.toLowerCase().equals(monthTitle.getText().toLowerCase())) reset();
					//System.out.println("ButtonColor " + resetBox.getBackground() + " : GreenColor " + Color.GREEN + " : Month " + month.toLowerCase() + " : MonthTitle " + monthTitle.getText().toLowerCase());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private int timeUntilEndOfDay(String currentTime) { // this just calculates how much time, in seconds, there is until the end of the day
		return (24 - Integer.parseInt(currentTime.substring(0, 2)) - 1) * 60 * 60 + (60 - Integer.parseInt(currentTime.substring(3, 5))) * 60 + (60 - Integer.parseInt(currentTime.substring(6, 8)));
	}
	private boolean updateCurrentDateBox() {
		date = 1;
		startDate = false;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < daysOfTheWeek.length; j++) {
				if(date > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) startDate = false;
				if(i == 0 && firstDay.equals(daysOfTheWeek[j].substring(0, 3))) startDate = true;
				if(startDate) date++;
				if(Calendar.getInstance().getTime().getDate() + 1 == date) {
					textAreas[j][i].setForeground(Color.BLUE);
					return true;
				} else {
					textAreas[j][i].setForeground(Color.BLACK);
				}
			}
		}
		return false;
	}
	
	// UI and unimplemented methods
	private class ResetBoxAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(resetBox.getBackground() == Color.GREEN) {
				resetBox.setBackground(Color.RED);
				resetBox.setHoverBackgroundColor(new Color(200, 0, 0));
				resetBox.setPressedBackgroundColor(new Color(175, 0, 0));
			} else {
				resetBox.setBackground(Color.GREEN);
				resetBox.setHoverBackgroundColor(new Color(0, 200, 0));
				resetBox.setPressedBackgroundColor(new Color(0, 175, 0));
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

	// KeyListener methods
	public void keyPressed(KeyEvent event) {
		int key = event.getKeyCode();
		if(key == KeyEvent.VK_ESCAPE) {
			save("");
			System.exit(0);
		} else if(key == KeyEvent.VK_R) {
			calendarMain.mouseInput.mouseReleased(null);
			if(resetConfirm() == 0) reset();
		} else if(key == KeyEvent.VK_MINUS) {
			calendarMain.setState(JFrame.ICONIFIED);
		} else if(key == KeyEvent.VK_S) {
			save(currentDirectory);
		} else if(key == KeyEvent.VK_B) {
			jfc.setDialogTitle("Backup a calendar");
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				currentDirectory = jfc.getSelectedFile().getAbsolutePath() + "\\";
				save(currentDirectory);
			} else save("");
		} else if(key == KeyEvent.VK_L) {
			jfc.setDialogTitle("Load a calendar");
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				currentDirectory = jfc.getSelectedFile().getAbsolutePath() + "\\";
				load(currentDirectory);
			} else load("");
		}
	}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
}