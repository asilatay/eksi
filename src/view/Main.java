package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import service.EngineManager;
import service.EngineManagerImpl;
import service.TitleManager;
import service.TitleManagerImpl;
import service.UserManager;
import service.UserManagerImpl;

public class Main extends JFrame{
	private static final long serialVersionUID = 5375171468886679796L;
	private static final String mainJFrameTitle = "ÝYTE - A.Asil Atay";
	private static final String crudeLinkURL = "C:\\webharvest\\HAM\\";
	private static final String eksiurl ="https://eksisozluk.com/";
	private static final String directory = "C:\\webharvest\\KAYDET\\";
	//private static final String sitemapurl ="https://eksisozluk.com/sitemap.xml";
	
	public static void main(String[] args) {
		createMainMenu();

	}
	
	public static void createMainMenu() {
		EngineManager engineManager = new EngineManagerImpl();
		TitleManager titleManager = new TitleManagerImpl();
		UserManager userManager = new UserManagerImpl();
		
		JFrame frame = new JFrame(mainJFrameTitle);
		JPanel panel = new JPanel();
		panel.setLayout(null);
//		JLabel label = new JLabel("This is a label!");
		JButton button0 = new JButton();
		button0.setText("Çýkýþ");
		button0.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
				
			}
		});
		button0.setBounds(400, 750, 80, 80);
		panel.add(button0);
		
		JButton button1 = new JButton();
		button1.setText("Linkleri Çýkar");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.createCrudeLinks(crudeLinkURL);
			}
		});
		button1.setBounds(10, 10, 150, 200);
		panel.add(button1);
		
		JButton button2 = new JButton();
		button2.setText("Bugün Populer Link Kaydet");
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.getLinksFromMainPage(eksiurl);
			}
		});
		button2.setBounds(180, 10, 200, 200);
		panel.add(button2);
		
		JButton button3 = new JButton();
		button3.setText("Local Link Ayrýþtýr");
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.getDocumentWithjSoup(eksiurl, directory);
			}
		});
		button3.setBounds(400, 10, 200, 200);
		panel.add(button3);
		
		JButton button4 = new JButton();
		button4.setText("Baþlýklarý Birleþtir");
		button4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.findDuplicateTitlesAndMerge();
			}
		});
		button4.setBounds(620, 10, 200, 200);
		panel.add(button4);
		
		JButton button5 = new JButton();
		button5.setText("Entry Dýþa Aktar(TXT)");
		button5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.writeAllEntriesToDocument();
			}
		});
		button5.setBounds(10, 230, 200, 200);
		panel.add(button5);
		
		JTextArea parameterTextArea = new JTextArea();
		parameterTextArea.setText("Entry Sayýsý");
		parameterTextArea.setBounds(430, 300, 150, 50);
		parameterTextArea.setEditable(true);
		panel.add(parameterTextArea);
		
		JButton button6 = new JButton();
		button6.setText("CoOccurence Matrix Hesapla (Entry Sayýsý Parametreli)");
		button6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.createCoOccurenceMatrix(Integer.parseInt(parameterTextArea.getText()));
			}
		});
		button6.setBounds(450, 450, 400, 200);
		panel.add(button6);
		
		JButton button7 = new JButton();
		button7.setText("Entry Dýþa Aktar(Parametreli)");
		button7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				engineManager.writeSpecificEntryCountToDocument(Integer.parseInt(parameterTextArea.getText()));
			}
		});
		button7.setBounds(10, 450, 400, 200);
		panel.add(button7);
		
		frame.add(panel);
		frame.setSize(900, 900);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
