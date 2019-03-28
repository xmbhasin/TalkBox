package config.talkbox;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileSystemView;

public class TBCLog extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextArea textArea;
	File[] logFiles;
	int currentLogFile = 0;
	private static final Dimension MINIMUM_SIZE = new Dimension(480, 300);
	String talkBoxDataPath;
	static JFileChooser fileChooser;
	JButton btnPreviousLog;
	JButton btnNextLog;
	JButton btnLoadLog;

	public static void main(String[] args) {

		fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		fileChooser.setDialogTitle("Please choose the parent directory of the TalkBoxData directory.");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setVisible(true);
		// only show directories
		fileChooser.setAcceptAllFileFilterUsed(false);
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File talkBoxDataParentDir = fileChooser.getSelectedFile();
			TBCLog logApp = new TBCLog((new File(talkBoxDataParentDir, "TalkBoxData").toString()));
			logApp.setVisible(true);
		} else if (returnValue == JFileChooser.CANCEL_OPTION) {
			System.exit(1);
		}

	}

	public TBCLog(String talkBoxDataPath) {

		this.talkBoxDataPath = talkBoxDataPath;

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		textArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		textArea.setEditable(false);

		JLabel lblTalkboxConfigurationLogs = new JLabel("TalkBox Configuration Logs");
		lblTalkboxConfigurationLogs.setFont(new Font("Chalkboard", Font.PLAIN, 16));

		readLogs();
		setMinimumSize(MINIMUM_SIZE);
		// setResizable(false);
		setLocationRelativeTo(null);

		btnPreviousLog = new JButton("Previous Log");
		btnPreviousLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (logFiles != null && logFiles.length > 0)
					readCurrentLog("prev");
			}
		});

		btnNextLog = new JButton("Next Log");
		btnNextLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (logFiles != null && logFiles.length > 0)
					readCurrentLog("next");
			}
		});

		btnLoadLog = new JButton("Load Log");

		btnLoadLog.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fileChooser.setVisible(true);
				fileChooser.setCurrentDirectory(new File(talkBoxDataPath, "config-logs"));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File loadedLog = fileChooser.getSelectedFile();
					loadLogFile(loadedLog);
				} else if (returnValue == JFileChooser.CANCEL_OPTION) {

				}
			}
		});
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGap(16)
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
								.addComponent(btnPreviousLog, GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE).addGap(47)
								.addComponent(btnNextLog, GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE).addGap(53)
								.addComponent(btnLoadLog, GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
						.addComponent(scroll, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE))
				.addGap(18))
				.addGroup(groupLayout
						.createSequentialGroup().addGap(131).addComponent(lblTalkboxConfigurationLogs,
								GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(141, Short.MAX_VALUE)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGap(10)
				.addComponent(lblTalkboxConfigurationLogs, GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scroll, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnLoadLog, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnPreviousLog, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addComponent(btnNextLog, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(18)));
		getContentPane().setLayout(groupLayout);

	}

	protected void loadLogFile(File loadedLog) {

		BufferedReader input;
		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(loadedLog)));
			textArea.read(input, "Reading Selected Log File");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void readCurrentLog(String target) {
		if (logFiles.length > 0) {
			if (target.equals("prev") && currentLogFile < logFiles.length - 1) {
				currentLogFile++;
			} else if (target.equals("next") && currentLogFile > 0) {
				currentLogFile--;
			}
		}
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(new FileInputStream(logFiles[currentLogFile])));
			textArea.read(input, "Reading Log File");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void readLogs() {
		File configLogs = new File(this.talkBoxDataPath, "config-logs");
		if (configLogs.isDirectory())
			logFiles = configLogs.listFiles();
		if (logFiles != null && logFiles.length > 0) {
			Arrays.sort(logFiles, Collections.reverseOrder());
			readCurrentLog("current");
		}

	}
}