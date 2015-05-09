import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;


public class ImagePanel extends Canvas {
	
	private BufferedImage imageToDisplay;
	
	public ImagePanel() {
		
	}
	
	public void setImage(BufferedImage newImage) {
		imageToDisplay = newImage;
		
		repaint(); //trigger redraw of this panel
	}
	
	public void paint(Graphics g){
		if(imageToDisplay != null)
			g.drawImage(imageToDisplay, 0, 0, this);
	}
}
