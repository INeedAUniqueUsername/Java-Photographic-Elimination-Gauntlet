import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
public class TranscendenceSpriteMaker {

	// File representing the folder that you select using a FileChooser
	//static final File dir = new File("src\\Picture Lab\\images");
	static final File dir = new File("C:\\Users\\Alex\\Desktop\\Transcendence Multiverse\\Sources\\Transcendence_Source\\Resources");
	// array of supported extensions (use a List if you prefer)
	static final String[] EXTENSIONS = new String[]{
		"gif", "png", "bmp", "jpg" // and other formats you need
	};
	// filter to identify images based on their extensions
	static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

		@Override
		public boolean accept(final File dir, final String name) {
			if(name.indexOf("Mask") != -1) {
				return false;
			}
			for (final String ext : EXTENSIONS) {
				if (name.endsWith("." + ext)) {
					return true;
				}
			}
			return false;
		}
	};

	public static void main(String[] args) throws IOException {
		if (dir.isDirectory()) { // make sure it's a directory
			for (final File f : dir.listFiles(IMAGE_FILTER)) {
				createSprite2(f);
			}
		}
	}
	public static void pencilSketch(File f) throws IOException {
		createSprite(f, 20);
	}
	public static void createSprite(File f, int tolerance) throws IOException {
		writeImage(edgeDetect(ImageIO.read(f), tolerance), f.getName());
	}
	public static void createSprite(File f, double scale, int tolerance) throws IOException {
		writeImage(edgeDetect(scaleImage(ImageIO.read(f), scale), tolerance), f.getName());
	}
	public static void createSprite2(File f) throws IOException {
		writeImage(swapColors(ImageIO.read(f)), f.getName());
	}
	public static BufferedImage scaleImage(BufferedImage image, double scale) {
		int width = (int) (image.getWidth()*scale);
		int height = (int) (image.getHeight()*scale);
	    BufferedImage imageScaled = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    imageScaled.getGraphics().drawImage(image,0,0,width,height,null);
	    return imageScaled;
	}
	public static void writeImage(BufferedImage image, String name) {
		try {
			ImageIO.write(image, "png", new File("./" + name + ".png"));
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	
	public static BufferedImage reduceColors(BufferedImage image, int range) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		int i = 0;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int c1 = image.getRGB(x, y);
				int r1 = (c1 & 0x00ff0000) >> 16;
				int g1 = (c1 & 0x0000ff00) >> 8;
				int b1 = c1 & 0x000000ff;
				int a1 = (c1 >> 24) & 0xff;
				
				r1 = (r1 / range) * range;
				g1 = (g1 / range) * range;
				b1 = (b1 / range) * range;
				a1 = (a1 / range) * range;
				
				pixels[i] = new Color(r1, g1, b1, a1).getRGB();
				
				i++;
			}
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		setPixels(b, pixels);
		return b;
	}
	enum SwapMode {
		//RGB,
		GRB, RBG, BGR, BRG, GBR,
		RRB, RRG, RBR, RGR, BRR, GRR,
		BBR, BBG, BRB, BGB, RBB, GBB,
		GGR, GGB, GRG, GBG, RGG, BGG
		//, RRR, GGG, BBB
	}
	public static BufferedImage swapColors(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		SwapMode[] swapValues = SwapMode.values();
		String mode = swapValues[random(swapValues.length)].toString();
		int i = 0;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int c1 = image.getRGB(x, y);
				int r1 = (c1 & 0x00ff0000) >> 16;
				int g1 = (c1 & 0x0000ff00) >> 8;
				int b1 = c1 & 0x000000ff;
				int a1 = (c1 >> 24) & 0xff;
				//int[] channels = {r1, g1, b1};
				//shuffleArray(channels);
				
				int[] channels = new int[3];
				for(int c = 0; c < 3; c++) {
					switch(mode.charAt(c)) {
					case 'R':
						channels[c] = r1;
						break;
					case 'G':
						channels[c] = g1;
						break;
					case 'B':
						channels[c] = b1;
						break;
					case 'A':
						channels[c] = a1;
						break;
					}
				}
				pixels[i] = new Color(channels[0], channels[1], channels[2], a1).getRGB();
				
				
				//pixels[i] = new Color(random(channels), random(channels), random(channels), a1).getRGB();
				//pixels[i] = new Color(g1, r1, b1, a1).getRGB(); //GRB
				
				i++;
			}
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		setPixels(b, pixels);
		return b;
	}
	public static BufferedImage edgeDetect(BufferedImage image, int tolerance) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		int i = 0;
		for(int y = 0; y < height-1; y++) {
			for(int x = 0; x < width-1; x++) {
				int c1 = image.getRGB(x, y);
				int r1 = (c1 & 0x00ff0000) >> 16;
				int g1 = (c1 & 0x0000ff00) >> 8;
				int b1 = c1 & 0x000000ff;
				int a1 = (c1 >> 24) & 0xff;
				
				int c2 = image.getRGB(x+1, y+1);
				int r2 = (c2 & 0x00ff0000) >> 16;
				int g2 = (c2 & 0x0000ff00) >> 8;
				int b2 = c2 & 0x000000ff;
				int a2 = (c2 >> 24) & 0xff;
				if(
						Math.abs(r1 - r2) > tolerance ||
						Math.abs(g1 - g2) > tolerance ||
						Math.abs(b1 - b2) > tolerance ||
						Math.abs(a1 - a2) > tolerance
						) {
					pixels[i] = new Color(0).getRGB();
				} else {
					pixels[i] = new Color(255, 255, 255, 255).getRGB();
				}
				i++;
			}
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		setPixels(b, pixels);
		return b;
	}
	public static void setPixels(BufferedImage b, int[] pixels) {
		int[] d = ( (DataBufferInt) b.getRaster().getDataBuffer() ).getData();
		for(int i = 0; i < pixels.length; i++) {
			d[i] = pixels[i];
		}
	}
	private static int random(int[] array) {
		return array[random(array.length)];
	}
	private static <T> T random(T[] array) {
		return array[random(array.length)];
	}
	public static int random(int max) {
		return (int) (Math.random() * max);
	}
}
