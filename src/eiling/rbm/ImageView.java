package eiling.rbm;
// Copyright (C) 2009 by Klaus Jung
// Date: 2009-01-12

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;



public class ImageView extends JScrollPane{

	private static final long serialVersionUID = 1L;
	
	private ImageScreen	screen = null;
	private Dimension maxSize = null;
	private int borderX = -1;
	private int borderY = -1;
	private double maxViewMagnification = 0.0;		// use 0.0 to disable limits 
	private boolean keepAspectRatio = true;
	
	int pixels[] = null;		// pixel array in ARGB format
	
	public ImageView(int width, int height) {
		// construct empty image of given size
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		init(bi, true);
	}
	
	public ImageView(File file) {
		// construct image from file
		loadImage(file);
	}
	
	public void setMaxSize(Dimension dim) {
		// limit the size of the image view
		maxSize = new Dimension(dim);
		
		Dimension size = new Dimension(maxSize);
		if(size.width - borderX > screen.image.getWidth()) size.width = screen.image.getWidth() + borderX;
		if(size.height - borderY > screen.image.getHeight()) size.height = screen.image.getHeight() + borderY;
		setPreferredSize(size);
	}
	
	public int getImgWidth() {
		return screen.image.getWidth();
	}

	public int getImgHeight() {
		return screen.image.getHeight();
	}
	
	public void resetToSize(int width, int height) {
		// resize image and erase all content
		if(width == getImgWidth() && height == getImgHeight()) return;
		
		screen.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		pixels = new int[getImgWidth() * getImgHeight()];
		screen.image.getRGB(0, 0, getImgWidth(), getImgHeight(), pixels, 0, getImgWidth());
		
		Dimension size = new Dimension(maxSize);
		if(size.width - borderX > width) size.width = width + borderX;
		if(size.height - borderY > height) size.height = height + borderY;
		setPreferredSize(size);

		screen.invalidate();
		screen.repaint();
	}
	
	public int[] getPixels() {
		// get reference to internal pixels array
		if(pixels == null) {
			pixels = new int[getImgWidth() * getImgHeight()];
			screen.image.getRGB(0, 0, getImgWidth(), getImgHeight(), pixels, 0, getImgWidth());
		}
		return pixels;
	}

	public void applyChanges() {
		// if the pixels array obtained by getPixels() has been modified,
		// call this method to make your changes visible
		if(pixels != null) setPixels(pixels);
	}
	
	public void setPixels(int[] pix) {
		// set pixels with same dimension
		setPixels(pix, getImgWidth(), getImgHeight());
	}
	
	public void setPixels(int[] pix, int width, int height) {
		// set pixels with arbitrary dimension
		if(pix == null || pix.length != width * height) throw new IndexOutOfBoundsException();
	
		if(width != getImgWidth() || height != getImgHeight()) {
			// image dimension changed
			screen.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			pixels = null;
		}
		
		screen.image.setRGB(0, 0, width, height, pix, 0, width);
		
		if(pixels != null && pix != pixels) {
			// update internal pixels array
			System.arraycopy(pix, 0, pixels, 0, Math.min(pix.length, pixels.length));
		}
		
		Dimension size = new Dimension(maxSize);
		if(size.width - borderX > width) size.width = width + borderX;
		if(size.height - borderY > height) size.height = height + borderY;
		setPreferredSize(size);

		screen.invalidate();
		screen.repaint();
	}
	
	public double getMaxViewMagnification() {
		return maxViewMagnification;
	}
	
	// set 0.0 to disable limits
	//
	public void setMaxViewMagnification(double mag) {
		maxViewMagnification = mag;
	}
	
	public boolean getKeepAspectRatio() {
		return keepAspectRatio;
	}
	
	public void setKeepAspectRatio(boolean keep) {
		keepAspectRatio = keep;
	}
	

	public void printText(int x, int y, String text) {
		Graphics2D g = screen.image.createGraphics();
		 
		Font font = new Font("TimesRoman", Font.BOLD, 12);
		g.setFont(font);
		g.setPaint(Color.black);
		g.drawString(text, x, y);		
		g.dispose();
		
		updatePixels();	// update the internal pixels array
	}
	
	public void clearImage() {
		Graphics2D g = screen.image.createGraphics();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getImgWidth(), getImgHeight());
		g.dispose();

		updatePixels();	// update the internal pixels array
	}
	
	public void loadImage(File file) {
		// load image from file
		BufferedImage bi = null;
		boolean success = false;
		
		try {
			bi = ImageIO.read(file);
			success = true;
		} catch (Exception e) {
   		 	JOptionPane.showMessageDialog(this, "Bild konnte nicht geladen werden.", "Fehler", JOptionPane.ERROR_MESSAGE);
   		 	bi = new BufferedImage(200, 150, BufferedImage.TYPE_INT_ARGB);
		}
				
		init(bi, !success);
		
		if(!success) printText(5, getImgHeight()/2, "Bild konnte nicht geladen werden.");
	}
	
	public void saveImage(String fileName) {
		try {
			File file = new File(fileName);
			String ext = (fileName.lastIndexOf(".")==-1)?"":fileName.substring(fileName.lastIndexOf(".")+1,fileName.length());
			if(!ImageIO.write(screen.image, ext, file)) throw new Exception("Image save failed");
		} catch(Exception e)  {
   		 	JOptionPane.showMessageDialog(this, "Bild konnte nicht geschrieben werden.", "Fehler", JOptionPane.ERROR_MESSAGE);			
		}
	}

	public void saveJPEGImage(String fileName, int quality) {
		try {
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = (ImageWriter)iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(quality/100.0f); 
			
			File file = new File(fileName);
			FileImageOutputStream output = new FileImageOutputStream(file);
			writer.setOutput(output);
			IIOImage image = new IIOImage(screen.image, null, null);
			writer.write(null, image, iwp);
			writer.dispose();
		} catch(Exception e)  {
   		 	JOptionPane.showMessageDialog(this, "Bild konnte nicht geschrieben werden.", "Fehler", JOptionPane.ERROR_MESSAGE);			
		}
	}


	private void init(BufferedImage bi, boolean clear)
	{
		screen = new ImageScreen(bi);
		setViewportView(screen);
				
		maxSize = new Dimension(getPreferredSize());
		
		if(borderX < 0) borderX = maxSize.width - bi.getWidth();
		if(borderY < 0) borderY = maxSize.height - bi.getHeight();
		
		if(clear) clearImage();
		
		pixels = null;
	}
	
	private void updatePixels() {
		if(pixels != null) screen.image.getRGB(0, 0, getImgWidth(), getImgHeight(), pixels, 0, getImgWidth());
	}
	
	class ImageScreen extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		private BufferedImage image = null;

		public ImageScreen(BufferedImage bi) {
			super();
			image = bi;
		}
		
		public void paintComponent(Graphics g) {
			
			if (image != null) {
				Rectangle r = this.getBounds();
								
				// limit image view magnification
				if(maxViewMagnification > 0.0) {
					int maxWidth = (int)(image.getWidth() * maxViewMagnification + 0.5);
					int maxHeight = (int)(image.getHeight() * maxViewMagnification + 0.5);
					
					if(r.width  > maxWidth) r.width = maxWidth;
					if(r.height  > maxHeight) r.height = maxHeight;
				}
				
				// keep aspect ratio
				if(keepAspectRatio) {
					double ratioX = (double)r.width / image.getWidth();
					double ratioY = (double)r.height / image.getHeight();
					if(ratioX < ratioY)
						r.height = (int)(ratioX * image.getHeight() + 0.5);
					else
						r.width = (int)(ratioY * image.getWidth() + 0.5);
				}
				
				g.drawImage(image, 0, 0, r.width, r.height, this);
			}
		}
		
		public Dimension getPreferredSize() {
			if(image != null) 
				return new Dimension(image.getWidth(), image.getHeight());
			else
				return new Dimension(100, 60);
		}
	}

}
