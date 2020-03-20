package brown.parker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/*
 * Represents a Magic card.  This class imports graphics packages to display the card image
 */
public class Card {

	private String name;
	private float price;
	private ArrayList<String> tags;
	private String imagePath;
	
	//need for closing window later
	JFrame frame;	
	
	public Card(String name, float price, String imagePath) {
		this.name = name;
		this.price = price;
		tags = new ArrayList<String>();
		this.imagePath = imagePath;
	}
	
	public void addTag(String tag) {
		tags.add(tag);
	}
	
	public void removeTag(String tag) {
		for(int i = 0 ; i < tags.size();i++) {
			if(tag.equals(tags.get(i))){
				tags.remove(i);
			}
		}
	}
	
	public ArrayList<String> getTags(){
		return tags;
	}
	
	//SOURCE: https://stackoverflow.com/questions/13448368/trying-to-display-url-image-in-jframe
	//SOURCE: https://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
	public void displayImage() {
		try {
			URL url = new URL(imagePath);
			BufferedImage image = ImageIO.read(url);
			
			BufferedImage scaledImage = new BufferedImage(image.getWidth() / 2, image.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = scaledImage.createGraphics();
			
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(image,0,0,image.getWidth() / 2, image.getHeight() / 2,null);
			g2.dispose();
			
			
			JLabel lable = new JLabel(new ImageIcon(scaledImage));
			frame = new JFrame();
			
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(lable);
			frame.pack();//sets size to image size
			frame.setLocation(200,200);
			frame.setVisible(true);
		}
		catch(Exception image) {
			System.out.println("ERROR: cannot display image");
			System.exit(1);
		}
	}
	
	public void closeImage() {
		if(frame != null) {
			frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
		}
	}
	
	@Override 
	public String toString() {
		return "Name: " + name + "\nPrice: " + price + "\nImage Link: " + imagePath + "\nTags: " + tags + "\n";
	}
	
	//used for arraylist's .contains method, each card has a unique name
	@Override 
	public boolean equals(Object other) {
	Card o = (Card) other;
		if(name.equals(o.getName())) {
			return true;
		}
	else return false;
	}
	
	public String getName() {
		return name;
	}
	
	public float getPrice() {
		return price;
	}
}
