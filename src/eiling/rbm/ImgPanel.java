package eiling.rbm;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class ImgPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	
	private JLabel status1;
	private JPanel imgPanel;
	
	public ImgPanel(ImageView imgView, String title, String message) {
		setLayout(new BorderLayout(5,5));
		TitledBorder titBorder = BorderFactory.createTitledBorder(title);
	    setBorder(titBorder);
        imgPanel = new JPanel(new GridLayout(1, 1));
        imgPanel.add(imgView);
	    
        add(imgPanel, BorderLayout.CENTER);
        
        JPanel statusLine = new JPanel(new GridLayout(2,1));

        status1 = new JLabel(message);
        
        statusLine.add(status1);
       
        add(status1, BorderLayout.SOUTH);
    }
	
	
	public void setMessage(String message) {
		status1.setText(message);
	}

}
