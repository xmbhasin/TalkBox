package config.talkbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class SimPreview extends JPanel {

	private static final long serialVersionUID = 1L;

	ArrayList<AudioButton> buttons = new ArrayList<AudioButton>();
	protected JPanel buttonsPanel;
	private JButton currentBtn;
	private int nButtons = 0;
	private int nButtonsPrev = 0;
	// HashMap holds integer which is button number and string which is filename
	// associated with the button
	HashMap<Integer, String> buttonsMap = new HashMap<Integer, String>();

	public SimPreview() {
		setBackground(Color.DARK_GRAY);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(10, 10));

		JLabel simTitle = new JLabel("TalkBox");
		simTitle.setHorizontalAlignment(SwingConstants.CENTER);
		simTitle.setVerticalAlignment(SwingConstants.TOP);
		simTitle.setFont(new Font("Chalkboard", Font.PLAIN, 50));
		simTitle.setForeground(Color.WHITE);
		add(simTitle, BorderLayout.PAGE_START);

		buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
		buttonsPanel.setBackground(Color.DARK_GRAY);
		add(buttonsPanel);
		// Get number of audio buttons from TalkBoxDeserializer
		nButtons = TalkBoxConfig.numAudButtons;
		setupButtons();
		addButtonAudio();
	}

	private void addButtonAudio() {

		for (AudioButton b : buttons) {

			b.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					removeHighlight();
					currentBtn = b;
					highlightBtn();
					playSound(b.fileName);
				}

			});
		}

	}

	public void removeHighlight() {
		if (currentBtn != null) {
			currentBtn.setForeground(Color.BLACK);
		}
	}

	public void highlightBtn() {
		if (currentBtn != null) {
			currentBtn.setForeground(Color.BLUE);
		}
	}

	/**
	 * ActionListeners of the buttons call playSound() method which plays the sound
	 * of the button. The Argument being passed in is the name of the Audio file
	 * which the button will play.
	 * 
	 * @param soundName name of audio file associated with the respective button
	 */

	protected void playSound(String soundName) {
		try {
			File file = new File("src/audioFiles/" + soundName); // gets the file from its
			// package using file name
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(file));
			clip.start(); // allows audio clip to be played
		} catch (Exception e) {
			System.err.println("Could not play back audio.");
			System.err.println(e.getMessage());
		}
	}

	public class AudioButton extends JButton {

		private static final long serialVersionUID = 1L;
		public String fileName;
		public int buttonNumber;

		public AudioButton(int buttonNumber, String text) {
			super(text);
			this.buttonNumber = buttonNumber;
			setVerticalAlignment(SwingConstants.BOTTOM);
			setFont(new Font("Chalkboard", Font.PLAIN, 25));
			setPreferredSize(new Dimension(70, 40));
		}
	}

	private void setupButtons() {
		if (nButtons < nButtonsPrev) {
			for (int i = nButtonsPrev - 1; i >= nButtons; i--) {
				buttonsPanel.remove(buttons.get(i));
				buttons.remove(i);
			}
		} else {
			for (int i = nButtonsPrev; i < nButtons; i++) {
				buttons.add(new AudioButton(i + 1, Integer.toString(i + 1)));
				buttonsPanel.add(buttons.get(i));
			}
		}
		nButtonsPrev = nButtons;

		for (int i = 0; i < nButtons; i++) {
			if (buttons.get(i).fileName != null) {
				buttonsMap.put(buttons.get(i).buttonNumber, buttons.get(i).fileName);
			}

			TalkBoxConfig.buttonsMap = buttonsMap;
		}

	}

	public void updateButtons(int nButtons) {
		this.nButtons = nButtons;
		setupButtons();
		revalidate();
		repaint();
	}
}