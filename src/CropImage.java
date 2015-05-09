import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class CropImage extends JFrame implements MouseListener, MouseMotionListener{
	
	int dragStatus = 0,c1,c2,c3,c4;
	
	public CropImage(){	
		
	}
	
	public void start(BufferedImage image) {
		
		BufferedImage inputImage = image; //image to be displayed
		
		CropImagePanel cropImagePanel = new CropImagePanel(inputImage); //create a new instance of the crop image panel
		
		add(cropImagePanel); //add the image
		setSize(400,400); //set the size of the window
		setVisible(true); //set visibility to true
		setTitle("Crop & Save Image"); //set title of the window
		
		//set the image window to the centre of the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		//add mouse listener
		addMouseListener(this);
		addMouseMotionListener(this);
		
		//set what happens when window is closed
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	}
	
	//add mouse event functions
		@Override
		public void mouseClicked(MouseEvent arg0) {	
			
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0) {
			
		}
		
		@Override
		public void mouseExited(MouseEvent arg0) {
			
		}
		
		@Override
		public void mousePressed(MouseEvent arg0) {
			
			repaint();
			c1=arg0.getX();
			c2=arg0.getY();
			
		}
		
		@Override
		public void mouseReleased(MouseEvent arg0) {
			
			repaint();
			if(dragStatus==1){
				c3 = arg0.getX();
				c4 = arg0.getY();
				try{
					draggedScreen();
				}
				catch(Exception e){
					
				}
			}
			
		}
		
		@Override public void mouseDragged(MouseEvent arg0) {
			
			repaint();
			dragStatus=1;
			c3=arg0.getX();
			c4=arg0.getY();
		
		}
		
		@Override
		public void mouseMoved(MouseEvent arg0) {
			
		}
		
		public void paint(Graphics g) {
			
			super.paint(g);
			int w = c1 - c3;
			int h = c2 - c4;
			w = w * -1; h = h * -1;
			if(w<0){
				w = w * -1;
			}
			g.drawRect(c1, c2, w, h);
			
		}
		
		public void draggedScreen()throws Exception {
			
			int w = c1 - c3;
			int h = c2 - c4;
			
			w = w * -1;
			h = h * -1;
			
			Robot robot = new Robot();
			BufferedImage croppedImage = robot.createScreenCapture(new Rectangle(c1,c2,w,h));
			
			//show file selection dialog
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select an directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //only directories can be selected
			chooser.setMultiSelectionEnabled(false); //single selection

			chooser.showOpenDialog(CropImage.this);

			String selectedDirectory = null;
			
			//if user clicks save button but doesn't choose a directory no NullPointerException is thrown
			try{
			selectedDirectory = chooser.getSelectedFile().getAbsolutePath();
			}catch(NullPointerException e){
				
			}
			
			//check if selection was successful
			if(selectedDirectory != null) {

				try {
					saveImage(selectedDirectory, croppedImage);
				} catch (IOException e1) { //in case reading image file fails
					JOptionPane.showMessageDialog( null, "Failed to save image to file.");
				}
			}
			//if there is no selection, do nothing
			else if(selectedDirectory == null){
				
			}
		}
		
		//method to save the image file
		private void saveImage(String imageDirectory, BufferedImage imageToSave) throws IOException {
			
			try{
			File saveDirectory = new File(imageDirectory + "/deblurredImage.jpg");
			ImageIO.write(imageToSave, "jpg", saveDirectory);
			} catch(IOException e){
				
			}
			
		}

}
