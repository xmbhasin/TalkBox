package utilities;

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;

public class TalkBoxLogger {

	static Logger logger;

	public static void setupLogger(File talkBoxDataPath, String logsDirRel) {

		logger = Logger.getGlobal();

		File logsDir = new File(talkBoxDataPath, logsDirRel);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String dateString = format.format(new Date());
		String now = dateString + "-log.txt";
		File logFile = new File(logsDir, now);
		try {
			logsDir.mkdirs();
			logFile.createNewFile();
			FileHandler fh;
			fh = new FileHandler(logFile.toString(), true);
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("Start of Log");
	}

	public static void logButtonPressEvent(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton btn = (JButton) e.getSource();
			logger.log(Level.INFO, "Pressed {0} button", new Object[] { btn.getText() });
		}
	}
}
