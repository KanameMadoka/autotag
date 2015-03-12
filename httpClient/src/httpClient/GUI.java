package httpClient;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Toolkit;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

public class GUI {

	private JFrame frame;
	private JTextField txtTest;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/Resources/faceblur.jpg")));
		frame.setResizable(false);
		frame.setBounds(100, 100, 579, 564);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnMenus = new JMenu("Menus");
		menuBar.add(mnMenus);
		
		HttpClient client = new HttpClient();
		
		JMenuItem mntmPreferences = new JMenuItem("Login");
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Login newlogin = new Login(client, frame);
				newlogin.frame.setVisible(true);
				newlogin.frame.setEnabled(true);
				frame.setEnabled(false);
				
				
			}
		});
		mnMenus.add(mntmPreferences);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Preferences");
		mnMenus.add(mntmNewMenuItem);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnMenus.add(mntmAbout);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnMenus.add(mntmExit);
		frame.getContentPane().setLayout(null);
		
		txtTest = new JTextField();
		txtTest.setText("test");
		txtTest.setBounds(138, 107, 200, 50);
		frame.getContentPane().add(txtTest);
		txtTest.setColumns(10);
	}

}
