package config.talkbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SimRecorderSplit extends JSplitPane {
	
	private JTextField txtNumberOfButtons;
	static SimPreview simPreview;
	
	public SimRecorderSplit(int height) {
		setOrientation(JSplitPane.VERTICAL_SPLIT); //verticle split has to be set because default is horizontal
		setDividerLocation((int) (0.5 * height));


		simPreview = new SimPreview(); //creates talkBox simulator object
		JPanel recorder = new Recorder(); //creates a recorder object

		setTopComponent(simPreview); /* adds talkBox Simulator object to this JSplitPane. This JSplitPane is then set to
		the left side of another JSplitPane in ControlsProfileSplit class */
		setBottomComponent(recorder); //adds recording sector to the bottom of this JSplitPane.
	}
	
	static void updateSimPreview(int numOfButtons) {
		simPreview.updateButtons(numOfButtons);
	}
}

