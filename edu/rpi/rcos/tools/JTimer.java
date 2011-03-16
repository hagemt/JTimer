package edu.rpi.rcos.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Creates a display that communicates some time remaining to a speaker.
 * @author Tor E Hagemann <hagemt@rpi.edu>
 */
public class JTimer extends JFrame implements javax.swing.SwingConstants {
	private static final long serialVersionUID = 143771059963842011L;
	private static final int DEFAULT_VALUE = 600;

	// GUI Fields
	private JPanel topPanel, bottomPanel;
	private JLabel clockDisplay;
	private JToggleButton toggleButton;
	private JButton stopButton, resetButton;

	// Data Fields
	private int remaining, total;
	private Timer timer;

	/**
	 * Constructs a timer with ten minutes on the clock.
	 */
	public JTimer() {
		this(DEFAULT_VALUE);
	}

	/**
	 * Constructs a timer for the given number of seconds.
	 * @param seconds a positive integer less or equal to Integer.MAX_VALUE
	 */
	public JTimer(int seconds) {
		// Sanitize inputs
		if (seconds < 1) {
			throw new IllegalArgumentException(
					"timer must begin with positive value");
		}
		// Initialize counters and timer function
		total = remaining = seconds;
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Once we're out of time, halt and prepare for restart
				if (--remaining == 0) {
					timer.stop();
					toggleButton.setSelected(false);
					toggleButton.setEnabled(false);
					toggleButton.setText("Start");
					stopButton.setEnabled(false);
				}
				update();
			}
		});

		// Initialize Panels
		topPanel = new JPanel(new GridBagLayout());
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topPanel.setBorder(BorderFactory.createTitledBorder("Time Remaining"));

		// Initialize Label @see update() TODO proper font size?
		clockDisplay = new JLabel(); update();
		clockDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 512));

		// Initialize Buttons
		toggleButton = new JToggleButton("Start Timer");
		toggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// State-dependent functionality to control timer
				if (toggleButton.isSelected()) {
					toggleButton.setText("Pause");
					stopButton.setEnabled(true);
					timer.restart();
				} else {
					toggleButton.setText("Restart");
					stopButton.setEnabled(false);
					timer.stop();
				}
			}
		});
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Stop ordinarily functions rather like a pause
				if (timer.isRunning()) {
					toggleButton.doClick();
				}
			}
		});
		stopButton.setEnabled(false);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Reset the start button and remaining seconds
				toggleButton.setEnabled(true);
				remaining = total; update();
				// Manipulate focus so the user only needs to tap the space bar to cycle
				toggleButton.requestFocusInWindow();
			}
		});

		// Initialize Layout
		topPanel.add(clockDisplay);
		bottomPanel.add(toggleButton);
		bottomPanel.add(stopButton);
		bottomPanel.add(resetButton);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(topPanel, BorderLayout.CENTER);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		// Initialize Window Properties (LaF, operation, and title)
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (info.getName().equals("Nimbus")) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (Exception e) { }
			}
		}
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Timer");
		this.pack();
	}

	/**
	 * Process the current state to properly set the display.
	 */
	private void update() {
		// Get data atomically and calculate
		int seconds = remaining;
		int minutes = seconds / 60;
		int hours = minutes / 60;

		// Change the color based on progress
		double completion = (double) (total - seconds) / total;
		int red = (int)(completion * 0xCC);
		int green = (int)(Math.cbrt(1d - completion) * 0xCC);
		clockDisplay.setForeground(new Color(red, green, 0));
		
		// Beep once (at 1 minute by default)
		if (10 * seconds == total) {
			java.awt.Toolkit.getDefaultToolkit().beep();
		}

		// Restrict counts
		minutes %= 60; seconds %= 60;

		// Construct and set the display
		StringBuilder sb = new StringBuilder();
		if (hours > 0) {
			sb.append(hours);
			sb.append(':');
		}
		if (minutes < 10) {
			sb.append('0');
		}
		sb.append(minutes);
		sb.append(':');
		if (seconds < 10) {
			sb.append('0');
		}
		sb.append(seconds);
		clockDisplay.setText(sb.toString());
	}
	
	@Override
	public String toString() {
		return clockDisplay.getText();
	}

	/**
	 * USAGE: java edu.rpi.rcos.tools.JTimer [N>0]
	 * @param args a single integer between 1 and Integer.MAX_VALUE, or none
	 */
	public static void main(String... args) {
		final int seconds;
		int value = DEFAULT_VALUE;
		try {
			value = Integer.parseInt(args[0]);
			if (value < 1) {
				throw new Exception("invalid interval");
			}
		} catch (Exception e) {
			System.err.println("USAGE: java edu.rpi.rcos.tools.JTimer [N>0]");
			return;
		} finally {
			seconds = value;
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Window invocation
				new JTimer(seconds).setVisible(true);
			}
		});
	}
}
