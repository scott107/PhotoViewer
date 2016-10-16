import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.event.*;
import java.util.Date;

public class Viewer extends JFrame{

	JLabel ImageShown = null;
	JButton PreviousButton = new JButton("< Prev");
	JButton NextButton = new JButton("Next >");
	JTextField CurrentPictureNumber = null;
	JLabel TotalPictures = null;	
	JLabel DescriptionLabel = new JLabel("Description: ");
	JLabel DateLabel = new JLabel("Date: ");
	JTextArea DescriptionText = new JTextArea(5,20);
	Model SavedPictures;
	Photo TempPhoto = null;
	JScrollPane sourceScrollPane;
	ImageIcon CurrentImageIcon;
	Container BottomHalfDisplay;
	DateFormat format = new SimpleDateFormat("yyyy-mm-dd");
	JFormattedTextField DateText = new JFormattedTextField(format);
	
	// creates picture viewer JFrame
	public Viewer(){
		try {
			SavedPictures = new Model();
		} catch (SQLException e1) {
			System.out.println(e1);
		}
		DateText.setColumns(10);
		// main display frame
		Container contentPane = getContentPane();
		// panel below the picture
		BottomHalfDisplay = new JPanel();
		BottomHalfDisplay.setLayout(new BorderLayout());
		
		// set picture for display
		
		ImageShown  = new JLabel ("");
		TempPhoto = SavedPictures.Getcurrentphoto();
		if (TempPhoto == null)
		// Display a temporary photo if the database is empty
		 {
			String dat = new String("");
			JLabel ImageShown = new JLabel("");
			ImageIcon firstImage = new ImageIcon("first.png");
			ImageShown.setIcon(firstImage);
			TempPhoto = new Photo(firstImage, "", dat);
		}
		ImageShown.setIcon(TempPhoto.picture);
		sourceScrollPane = new JScrollPane(ImageShown);
		
		// add picture to main frame
        contentPane.add(sourceScrollPane, BorderLayout.CENTER);
        setTitle("Photo Album");

        // panel for cycling through pictures
		JPanel lowerDisplay = new JPanel();
		lowerDisplay.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		TotalPictures = new JLabel(" of " + SavedPictures.Numberofphotos());
		CurrentPictureNumber = new JTextField("");
		CurrentPictureNumber.setText(Integer.toString(SavedPictures.Getcurrentphotonumber()));
		
		// add to the south side of the bottom panel
		lowerDisplay.add(CurrentPictureNumber);
		lowerDisplay.add(TotalPictures);
		lowerDisplay.add(PreviousButton);
		if (SavedPictures.Numberofphotos() == 1){
			PreviousButton.setEnabled(false);
			NextButton.setEnabled(false);
		}
		lowerDisplay.add(NextButton);
		
		BottomHalfDisplay.add(lowerDisplay, BorderLayout.SOUTH);
		
		// display date information
		JPanel middleDisplay = new JPanel();
		middleDisplay.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		
		// add to the center of the bottom panel
		middleDisplay.add(DateLabel);
		middleDisplay.add(DateText);
		
		BottomHalfDisplay.add(middleDisplay, BorderLayout.CENTER);
		
		// display description information
		JPanel topDisplay = new JPanel();
		topDisplay.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		
		// add to North sire of the bottom panel
		topDisplay.add(DescriptionLabel);
		topDisplay.add(DescriptionText);
		
		BottomHalfDisplay.add(topDisplay, BorderLayout.NORTH);
		
		// add bottom panel to south side of the main frame
		contentPane.add(BottomHalfDisplay, BorderLayout.SOUTH);
		
		// disable boxes in preparation for starting in browse mode
		DescriptionText.setEditable(false);
		DateText.setEditable(false);
		
		PossiblyDisableButtons();
		DescriptionText.setText(TempPhoto.description);
		DateText.setText(TempPhoto.date);
		
		// listen for user input on the Previous picture button
		PreviousButton.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent pressedPrev) {
				// enable next button
				NextButton.setEnabled(true);
				// check for first picture
				final Runnable UIthread = new Runnable(){
					public void run(){
				RefreshDisplay();
				//disable button?
				PossiblyDisableButtons();
				SerializeThisThing();
				}
				};
				Runnable DBthread = new Runnable(){
					public void run(){
				TempPhoto =	SavedPictures.Previous();
				SwingUtilities.invokeLater(UIthread);}
				};
				Thread SecondThread = new Thread(DBthread);
				SecondThread.start();
			}
		});
		
		// listen for user input on the Next picture button
		NextButton.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent pressedNext) {
				// enable previous button
				PreviousButton.setEnabled(true);
				// check for last picture
				final Runnable UIthread = new Runnable(){
					public void run(){
						RefreshDisplay();
						//disable button?
						PossiblyDisableButtons();
						SerializeThisThing();
					}
				};
				Runnable DBthread = new Runnable(){
					public void run(){
				TempPhoto = SavedPictures.Next();
				SwingUtilities.invokeLater(UIthread);
					}
				};
				Thread SecondThread = new Thread(DBthread);
				SecondThread.start();
			}
		});
		JPanel rightdisplay = new JPanel();
		JButton SaveButton = new JButton("Save Changes");
		SaveButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				final Runnable UIthread = new Runnable(){
					public void run(){
						SerializeThisThing();
						}
				};
				Runnable DBthread = new Runnable(){
					public void run(){
				SavedPictures.EditPhoto(DescriptionText.getText(), DateText.getText());
				SwingUtilities.invokeLater(UIthread);
					}
				};
				Thread SecondThread = new Thread(DBthread);
				SecondThread.start();
			}
			
		});
		JButton DeleteButton = new JButton("Delete");
		DeleteButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				final Runnable UIthread = new Runnable(){
					public void run(){
						RefreshDisplay();
						//disable button?
						PossiblyDisableButtons();
						SerializeThisThing();
					}
				};
				Runnable DBthread = new Runnable(){
					public void run(){
				SavedPictures.Deletecurrent();
				TempPhoto = SavedPictures.Getcurrentphoto();
				SwingUtilities.invokeLater(UIthread);}
				};
				Thread SecondThread = new Thread(DBthread);
				SecondThread.start();
			}
			
		});
		JButton AddButton = new JButton("Add Photo");
		AddButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser();
				int complete = fc.showOpenDialog(null);
				if (complete == JFileChooser.APPROVE_OPTION) {

					File file = fc.getSelectedFile();
					Path path = Paths.get(file.getAbsolutePath());
		            byte[] data;
		    		try {
		    		    data = Files.readAllBytes(path);
		    		    // write image to database
		                        SavedPictures.Addphoto(data, "", "");
		    		} catch (IOException e2) {
		    		    // TODO Auto-generated catch block
		    		    System.out.println(e2);
		    		}
					RefreshDisplay();
					NextButton.setEnabled(true);
				} else {
					System.out.println("File Open dialog canceled");
				}
				SerializeThisThing();
			}

		});
		CurrentPictureNumber.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				final Runnable UIthread = new Runnable(){
					public void run(){
						RefreshDisplay();				
						// disable buttons?
						PossiblyDisableButtons();
						SerializeThisThing();
					}
					};
					Runnable DBthread = new Runnable(){
						public void run(){
							TempPhoto = SavedPictures.Getphoto(Integer.parseInt(CurrentPictureNumber.getText()));
							SwingUtilities.invokeLater(UIthread);
						}
					};
					Thread SecondThread = new Thread(DBthread);
					SecondThread.start();
			}
			
		});
		rightdisplay.add(DeleteButton);
		rightdisplay.add(SaveButton);
		rightdisplay.add(AddButton);
		rightdisplay.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		BottomHalfDisplay.add(rightdisplay, BorderLayout.EAST);
		
		rightdisplay.setVisible(false);
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu filemenu = new JMenu ("File");
		filemenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(filemenu);
		
		JMenuItem exitMenuChoice = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuChoice.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SerializeThisThing();
				System.exit(0);
				
			}
			
		});
		filemenu.add(exitMenuChoice);
		
		JMenu viewmenu = new JMenu ("View");
		viewmenu .setMnemonic(KeyEvent.VK_V);
		menuBar.add(viewmenu);
		
		JMenuItem browse = new JMenuItem("Browse", KeyEvent.VK_B);
		browse.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rightdisplay.setVisible(false);
				DescriptionText.setEditable(false);
				DateText.setEditable(false);
			}
		});
		JMenuItem maintain = new JMenuItem("Maintain", KeyEvent.VK_M);
		maintain.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rightdisplay.setVisible(true);
				DescriptionText.setEditable(true);
				DateText.setEditable(true);
			}
		});
		viewmenu.add(browse);
		viewmenu.add(maintain);		
	}
	
	
	private void RefreshDisplay(){
		ImageShown.setIcon(TempPhoto.picture);
		CurrentPictureNumber.setText(Integer.toString(SavedPictures.Getcurrentphotonumber()));
		TotalPictures.setText(Integer.toString(SavedPictures.Numberofphotos()));
		DateText.setText(TempPhoto.date);
		DescriptionText.setText(TempPhoto.description);
		sourceScrollPane.repaint();
		CurrentPictureNumber.repaint();
		TotalPictures.repaint();
		DateText.repaint();
		DescriptionText.repaint();
	}	
	
	private void SerializeThisThing(){
		SavedPictures.close();
	}
	
	private void PossiblyDisableButtons(){
		if (SavedPictures.Getcurrentphotonumber() == SavedPictures.Numberofphotos() || SavedPictures.Numberofphotos() == 0){

			NextButton.setEnabled(false);
		}
		else
			NextButton.setEnabled(true);
		//disable button?
		if (SavedPictures.Getcurrentphotonumber() == 1 || SavedPictures.Numberofphotos() == 0){
			PreviousButton.setEnabled(false);
		}
		else
			PreviousButton.setEnabled(true);
	}
	
	// main program stream
	public static void main(String[] args) {
		
		JFrame outerframe = new Viewer();
		outerframe.setSize(1000, 1000);
		outerframe.setVisible(true);
		
	}


	

}


