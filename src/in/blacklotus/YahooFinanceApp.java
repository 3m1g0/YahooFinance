package in.blacklotus;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class YahooFinanceApp {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					YahooFinanceApp window = new YahooFinanceApp();
					window.show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public YahooFinanceApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(0, 0, 400, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel countLabel = new JLabel("Count");
		frame.getContentPane().add(countLabel, BorderLayout.CENTER);
	}
	
	/**
	 * Show the frame
	 */
	public void show() {
		frame.setVisible(true);
	}
	
	/**
	 * Hide the frame
	 */
	public void hide() {
		frame.setVisible(false);
	}
}
