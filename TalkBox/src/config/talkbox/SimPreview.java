package config.talkbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import utilities.TalkBoxLogger;

public class SimPreview extends JPanel {

	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getGlobal();
	ArrayList<AudioButton> buttons = new ArrayList<AudioButton>();
	protected JPanel buttonsPanel;
	protected JPanel swapButtonsPanel;
	protected JPanel allButtonsPanel;
	AudioButton currentBtn;
	private int nButtons = 0;
	private int nButtonsPrev = 0;
	JButton swap1;
	JButton swap2;
	JButton swap3;
	JButton swapAll;
	JButton stopAudio;
	private int currentProfile = 0;
	static JLabel profileNumber;

	public enum SimPreviewMode {
		PLAY_MODE, EDIT_MODE;
	}

	public SimPreviewMode mode = SimPreviewMode.PLAY_MODE;

	public SimPreview() {

		setBackground(Color.DARK_GRAY);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(10, 10));

		JLabel simTitle = new JLabel("TalkBox");
		simTitle.setHorizontalAlignment(SwingConstants.CENTER);
		simTitle.setVerticalAlignment(SwingConstants.TOP);
		simTitle.setFont(new Font("Chalkboard", Font.PLAIN, 50));
		simTitle.setForeground(Color.WHITE);
		add(simTitle, BorderLayout.BEFORE_FIRST_LINE);

		swapButtonsPanel = new JPanel();
		swapButtonsPanel.setForeground(new Color(0, 0, 0));
		swapButtonsPanel.setBackground(Color.DARK_GRAY);
		swapButtonsPanel.setLayout(new BoxLayout(swapButtonsPanel, BoxLayout.Y_AXIS));
		swapButtonsPanel.add(Box.createVerticalStrut(15));
		swap1 = new JButton("Profile 1");
		swapButtonsPanel.add(swap1);
		swap2 = new JButton("Profile 2");
		swapButtonsPanel.add(swap2);

		// allButtonsPanel.add(swapButtonsPanel, BorderLayout.NORTH);
		add(swapButtonsPanel, BorderLayout.WEST);
		swap3 = new JButton("Profile 3");
		swapButtonsPanel.add(swap3);
		swapAll = new JButton("  Swap   ");
		swapAll.setToolTipText("Swap through all profiles sequentially.");
		swapButtonsPanel.add(swapAll);
		// allButtonsPanel.add(buttonsPanel);
		// add(swapButtonsPanel, BorderLayout.EAST);

		buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
		buttonsPanel.setBackground(Color.DARK_GRAY);
		add(buttonsPanel);
		// Get number of audio buttons from TalkBoxDeserializer
		nButtons = TalkBoxConfig.numAudButtons;
		setupButtons();
		currentBtn = buttons.get(0);
		profileNumber = new JLabel();
		profileNumber.setForeground(Color.CYAN);
		profileNumber.setText("  Profile 1");
		swapButtonsPanel.add(profileNumber);

		swapButtonsPanel.add(Box.createVerticalStrut(50));
		stopAudio = new JButton("Stop Audio");
		stopAudio.setToolTipText("Stop currently playing audio.");
		swapButtonsPanel.add(stopAudio);

		setupAudioButtons();
		setUpSwapButtons();
		setUpStopAudioButton();
	}

	private void setUpSwapButtons() {

		swap1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TalkBoxLogger.logButtonPressEvent(e);
				setProfile(0);
			}
		});
		swap2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TalkBoxLogger.logButtonPressEvent(e);
				setProfile(1);
			}
		});
		swap3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TalkBoxLogger.logButtonPressEvent(e);
				setProfile(2);
			}
		});

		swapAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				TalkBoxLogger.logButtonPressEvent(e);
				if (TalkBoxConfig.profilesList.size() > 0) {
					int nextProfile = (currentProfile + 1) % TalkBoxConfig.numAudSets;
					setProfile(nextProfile);
				}
			}
		});

	}

	private void setUpStopAudioButton() {
		stopAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TalkBoxLogger.logButtonPressEvent(e);
				if (currentBtn.clip != null) {
					currentBtn.clip.stop();
				}
			}
		});
	}

	protected void setProfile(int newProfile) {
		if (currentProfile != newProfile && TalkBoxConfig.profilesList.size() > newProfile) {
			profileNumber.setText("  Profile " + (newProfile + 1));
			revalidate();
			repaint();
			loadProfile(newProfile);
			logger.log(Level.INFO, "Switching from profile {0} to profile {1}",
					new Object[] { currentProfile + 1, newProfile + 1 });
			currentProfile = newProfile;
		}
	}

	protected void loadProfile(int newProfile) {
		TalkBoxConfig.profilesList.setCurrentProfile(newProfile);
		ArrayList<String> profileFileNames = TalkBoxConfig.profilesList.get(newProfile).getAudioFileNames();
		for (int i = 0; i < TalkBoxConfig.numAudButtons; ++i) {
			buttons.get(i).setAudioFile(profileFileNames.get(i));
		}
	}

	private void setupAudioButtons() {
		for (AudioButton b : buttons) {
			if (b.getActionListeners().length < 1) {
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						logger.log(Level.INFO, "Button number {0} was pressed.", new Object[] { b.buttonNumber });
						if (mode == SimPreviewMode.PLAY_MODE) {
							if (currentBtn.clip != null && currentBtn.clip.isActive()) {
								currentBtn.clip.stop();
							}
							currentBtn = b;
							b.playSound();
						} else if (mode == SimPreviewMode.EDIT_MODE) {
							removeHighlight();
							currentBtn = b;
							highlightBtn();
						}
					}
				});
			}
			b.setDropTarget(new DropTarget() {

				public synchronized void drop(DropTargetDropEvent evt) {
					try {
						evt.acceptDrop(DnDConstants.ACTION_COPY);
						List<File> droppedFiles = (List<File>) evt.getTransferable()
								.getTransferData(DataFlavor.javaFileListFlavor);
						if (droppedFiles.size() > 0) {
							File file = droppedFiles.get(0);
							String fileName = file.getName();
							if (fileName.endsWith(".wav")) {
								setButtonAudio(b, file);
							} else if (fileName.matches(".*\\.(png|jpg|gif|bmp)$")) {
								setButtonImage(b, file);
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			});
		}
	}

	void setButtonImage(File image) {
		setButtonImage(currentBtn, image);
	}

	void setButtonImage(AudioButton b, File image) {
		int buttonNumber = b.buttonNumber;
		String filename = image.getAbsolutePath();
		try {
			ImageIcon icon = new ImageIcon(scaleImage(40, 40, ImageIO.read(new File(filename))));
			b.setIcon(icon);
			b.revalidate();
			b.repaint();
			TalkBoxConfig.iconButtonsMap.put(buttonNumber - 1, icon);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String uploadedImageIcon = String.format("button-%d.jpg", buttonNumber);

		File uploadedImageFile = new File(TalkBoxConfig.profilesList.getCurrentProfileFolder(), uploadedImageIcon);
		createFile(uploadedImageFile);
	}

	public static BufferedImage scaleImage(int w, int h, BufferedImage img) {
		BufferedImage bi;
		bi = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
		Graphics2D g2d = (Graphics2D) bi.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g2d.drawImage(img, 0, 0, w, h, null);
		g2d.dispose();
		return bi;
	}

	void setButtonAudio(File audio) throws IOException {
		setButtonAudio(currentBtn, audio);
	}

	void setButtonAudio(AudioButton b, File sourceAudioFile) throws IOException {
		int buttonNumber = b.buttonNumber;

		String destAudioFileName = String.format("button-%d.wav", buttonNumber);
		File destAudioFile = new File(TalkBoxConfig.profilesList.getCurrentProfileFolder(), destAudioFileName);
		createFile(destAudioFile);

		@SuppressWarnings("resource")
		FileChannel src = new FileInputStream(sourceAudioFile).getChannel();
		@SuppressWarnings("resource")
		FileChannel dest = new FileOutputStream(destAudioFile).getChannel();
		dest.transferFrom(src, 0, src.size());

		b.setAudioFile(destAudioFileName);
		TalkBoxConfig.profilesList.setAudioFileAtIndexOfCurrentProfile(buttonNumber - 1, destAudioFileName);

	}

	private void createFile(File file) {
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeHighlight() {
		if (currentBtn != null) {
			currentBtn.setForeground(Color.BLACK);
			currentBtn.setFont(new Font("Chalkboard", Font.PLAIN, 25));
		}
	}

	public void highlightBtn() {
		if (currentBtn != null) {
			currentBtn.setForeground(Color.BLUE);
			currentBtn.setFont(new Font("Chalkboard", Font.BOLD, 25));
		}
	}

	public class AudioButton extends JButton {

		protected static final long serialVersionUID = 1L;
		protected String fileName;
		protected File profileFolder;
		protected File audioFile;
		public int buttonNumber;
		private Clip clip;

		public AudioButton(int buttonNumber, String text) {
			super(text);
			this.buttonNumber = buttonNumber;
			setMargin(new Insets(0, 0, 0, 0));
			setFont(new Font("Chalkboard", Font.PLAIN, 25));
			setPreferredSize(new Dimension(80, 80));
			setVerticalAlignment(SwingConstants.BOTTOM);
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setIconTextGap(-5);
		}

		public void setAudioFile(String fileName) {
			this.fileName = fileName;
			this.profileFolder = TalkBoxConfig.profilesList.getCurrentProfileFolder();
			if (fileName != null) {
				audioFile = new File(profileFolder, fileName);
			} else {
				audioFile = null;
			}

			if (this.clip != null) {
				if (this.clip.isActive()) {
					this.clip.stop();
				}
				Clip clip = this.clip;
				closeClip(clip);
				this.clip = null;
			}
		}

		private void closeClip(Clip clip) {
			Thread clipStopper = new Thread(new Runnable() {
				public void run() {
					clip.close();
				}
			});
			clipStopper.start();
		}

		public void playSound() {
			if (audioFile != null) {
				try {
					if (clip == null) {
						clip = AudioSystem.getClip();
					}
					if (!clip.isOpen()) {
						AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
						clip.open(ais);
						ais.close();
					}

					clip.setMicrosecondPosition(0);
					clip.start(); // allows audio clip to be played
				} catch (Exception e) {
					System.err.println("Could not play back audio.");
					System.err.println(e.getMessage());
				}
			} else {
				System.err.println("No audio file associated with this button.");
			}
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
				AudioButton ab = new AudioButton(i + 1, Integer.toString(i + 1));
				ArrayList<String> audioFilePaths = TalkBoxConfig.profilesList.getAudioFilesOfCurrentProfile();
				if (i < audioFilePaths.size()) {
					if (audioFilePaths.get(i) != null) {
						String audioFilePath = audioFilePaths.get(i);
						System.out.println(audioFilePath);
						ab.setAudioFile(audioFilePath);
					}
				} else {
					for (Profile p : TalkBoxConfig.profilesList) {
						p.audioFileNames.add(null);
					}
				}

				if (TalkBoxConfig.buttonsMap.get(i) != null) {
					ab.setText(TalkBoxConfig.buttonsMap.get(i));
				}
				if (TalkBoxConfig.iconButtonsMap.get(i) != null) {
					ab.setIcon(TalkBoxConfig.iconButtonsMap.get(i));
				}
				buttons.add(ab);
				buttonsPanel.add(buttons.get(i));
			}
		}
		nButtonsPrev = nButtons;
	}

	public void updateButtons(int nButtons) {
		this.nButtons = nButtons;
		setupButtons();
		setupAudioButtons();
		revalidate();
		repaint();
	}
}