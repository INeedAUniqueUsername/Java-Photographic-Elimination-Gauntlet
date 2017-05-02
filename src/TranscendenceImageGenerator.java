import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JOptionPane;
public class TranscendenceImageGenerator {
	public static final int RGB_BLACK = Color.BLACK.getRGB();
	public static final int RGB_WHITE = Color.WHITE.getRGB();
	private static final File dir = new File("C:\\Users\\Alex\\Desktop\\Transcendence Multiverse\\Sources");
	
	//private static final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
	//private static final ImageWriteParam jpgParam = createJPGParam();
	private static int directories = 0;
	private static int images = 0;
	public static final ImageManipulator PENCIL_SKETCH_OLD = new EdgeDetectChannelDifference(20);
	public static final ImageManipulator PENCIL_SKETCH = new EdgeDetectRGBRatio(0.6);
	public static final ImageManipulator PENCIL_SKETCH_2 = new EdgeDetectChannelRatio(0.8);
	
	public static final ImageManipulator mode = getManipulator();
	
	public static ImageManipulator getManipulator() {
		String[] options = {
			"Pencil Sketch", "Swap Colors"
		};
		switch(options[JOptionPane.showOptionDialog(
				null,
				"Select a mode",
				"Transcendence Image Manipulator",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				null)]) {
		case "Pencil Sketch":
			return PENCIL_SKETCH;
		case "Swap Colors":
			return new ColorSwap();
		default: return null;
		}
	}
	public static void main(String[] args) {
		
		String outputPath = "./Transcendence Image Output";
		new File(outputPath).mkdir();
		File directory = new File(JOptionPane.showInputDialog("Specify image directory"));
		processDirectory(directory, outputPath);
		JOptionPane.showMessageDialog(null, "Processed " + images + " images in " + directories + " directories");
	}
	public static void print(String message) {
		System.out.println(message);
	}
	public static void processDirectory(File path, String outputOrigin) {
		if (path.isDirectory()) { // make sure it's a directory
			//Create a folder of the same name in the Output folder so we can output there
			String outputPath = outputOrigin + "\\" + path.getName();
			new File(outputPath).mkdir();
			print("Processing Directory: " + path.getPath());
			for (final File f : path.listFiles()) {
				processDirectory(f, outputPath);
			}
			directories++;
		} else if(path.isFile()) {
			String fileName = path.getName();
			String filePath = outputOrigin + "\\" + fileName;
			if(fileName.indexOf(".jpg") == -1) {
				print("Not a JPG image: "  + path.getPath());
			} else if(new File(filePath).exists()) {
				print("Image already made: " + path.getPath());
			} else {
				print("Processing Image: " + path.getPath());
				try {
					BufferedImage image = ImageIO.read(path);
					if(image == null) {
						print("Image does not exist: " + filePath);
						return;
					}
					writeImageToPath(manipulate(image, mode), filePath);
					images++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					print("Failed to load image: " + filePath);
					e.printStackTrace();
				}
			}
		}
	}
	public static BufferedImage scaleImage(BufferedImage image, double scale) {
		int width = (int) (image.getWidth()*scale);
		int height = (int) (image.getHeight()*scale);
		BufferedImage imageScaled = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		imageScaled.getGraphics().drawImage(image,0,0,width,height,null);
		return imageScaled;
	}
	public static void writeImageToName(BufferedImage image, String name) {
		try {
			ImageIO.write(image, "png", new File("./Output\\" + name));
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	public static void writeImageToPath(BufferedImage image, String path) {
		try {
			ImageIO.write(image, "jpg", new File(path));
			
			/*
			writer.setOutput(new FileImageOutputStream(
			  new File(path)));
			writer.write(null, new IIOImage(image, null, null), jpgParam);
			*/
			print("Created Image: " + path);
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	/*
	public static ImageWriteParam createJPGParam() {
		ImageWriteParam jpgWriteParam = writer.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(1.0f);
		return jpgWriteParam;
	}
	*/
	public static BufferedImage manipulate(BufferedImage image, ImageManipulator manipulator) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
		for(int y = 1; y < height-1; y++) {
			for(int x = 1; x < width-1; x++) {
				pixels[y][x] = manipulator.getRGB(image, x, y);
			}
		}
		int y_last = height-1;
		for(int x = 0; x < width; x++) {
			pixels[0][x] = RGB_WHITE;
			pixels[y_last][x] = RGB_WHITE;
		}
		int x_last = width-1;
		for(int y = 0; y < height; y++) {
			pixels[y][0] = RGB_WHITE;
			pixels[y][x_last] = RGB_WHITE;
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		setPixels(b, pixels);
		return b;
	}
	public static void setPixels(BufferedImage b, int[][] pixels) {
		int[] line = new int[b.getWidth() * b.getHeight()];
		for(int i = 0, r = 0; r < pixels.length; r++) {
			for(int c = 0; c < pixels[r].length; c++, i++) {
				line[i] = pixels[r][c];
			}
		}
		setPixels(b, line);
	}
	public static void setPixels(BufferedImage b, int[] pixels) {
		int[] d = ( (DataBufferInt) b.getRaster().getDataBuffer() ).getData();
		for(int i = 0; i < pixels.length; i++) {
			d[i] = pixels[i];
		}
	}
}
abstract class ImageManipulator {
	public static final int RGB_BLACK = Color.BLACK.getRGB();
	public static final int RGB_WHITE = Color.WHITE.getRGB();
	
	public abstract int getRGB(BufferedImage image, int x, int y);
	
	public static int getRGBDifference(int rgb1, int rgb2) {
		int r1 = (rgb1 & 0x00ff0000) >> 16;
		int g1 = (rgb1 & 0x0000ff00) >> 8;
		int b1 = (rgb1 & 0x000000ff);
		int a1 = (rgb1 >> 24) & 0xff;
		
		int r2 = (rgb2 & 0x00ff0000) >> 16;
		int g2 = (rgb2 & 0x0000ff00) >> 8;
		int b2 = (rgb2 & 0x000000ff);
		int a2 = (rgb2 >> 24) & 0xff;
		
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2) + Math.abs(a1 - a2);
	}
	public static double getRGBRatio(int rgb1, int rgb2) {
		int r1 = (rgb1 & 0x00ff0000) >> 16;
		int g1 = (rgb1 & 0x0000ff00) >> 8;
		int b1 = (rgb1 & 0x000000ff);
		int a1 = (rgb1 >> 24) & 0xff;
		
		int r2 = (rgb2 & 0x00ff0000) >> 16;
		int g2 = (rgb2 & 0x0000ff00) >> 8;
		int b2 = (rgb2 & 0x000000ff);
		int a2 = (rgb2 >> 24) & 0xff;
		
		return
				(double) Math.abs(r1 - r2) / r1 +
				(double) Math.abs(g1 - g2) / g1 +
				(double) Math.abs(b1 - b2) / b1 +
				(double) Math.abs(a1 - a2) / a1;
	}
	public static int getMaxChannelDifference(int rgb1, int rgb2) {
		int r1 = (rgb1 & 0x00ff0000) >> 16;
		int g1 = (rgb1 & 0x0000ff00) >> 8;
		int b1 = (rgb1 & 0x000000ff);
		int a1 = (rgb1 >> 24) & 0xff;
		
		int r2 = (rgb2 & 0x00ff0000) >> 16;
		int g2 = (rgb2 & 0x0000ff00) >> 8;
		int b2 = (rgb2 & 0x000000ff);
		int a2 = (rgb2 >> 24) & 0xff;
		
		return (int) max(Math.abs(r1 - r2), Math.abs(g1 - g2), Math.abs(b1 - b2), Math.abs(a1 - a2));
	}
	public static double getMaxChannelRatio(int rgb1, int rgb2) {
		int r1 = (rgb1 & 0x00ff0000) >> 16;
		int g1 = (rgb1 & 0x0000ff00) >> 8;
		int b1 = (rgb1 & 0x000000ff);
		int a1 = (rgb1 >> 24) & 0xff;
		
		int r2 = (rgb2 & 0x00ff0000) >> 16;
		int g2 = (rgb2 & 0x0000ff00) >> 8;
		int b2 = (rgb2 & 0x000000ff);
		int a2 = (rgb2 >> 24) & 0xff;
		
		return max(
				(double) Math.abs(r1 - r2) / r1,
				(double) Math.abs(g1 - g2) / g1,
				(double) Math.abs(b1 - b2) / b1,
				(double) Math.abs(a1 - a2) / a1);
	}
	public static double max(double... array) {
		double result = Double.MIN_VALUE;
		for(Double n : array) {
			if(n > result) {
				result = n;
			}
		}
		return result;
	}
	public static int trueCount(boolean... conditions) {
		int result = 0;
		for(boolean b : conditions) {
			if(b) {
				result++;
			}
		}
		return result;
	}
	public static int random(int max) {
		return (int) (Math.random() * max);
	}
}
class EdgeDetectChannelDifference extends ImageManipulator {
	private int tolerance;
	public EdgeDetectChannelDifference(int tolerance) {
		this.tolerance = tolerance;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
		if(x == image.getWidth() - 1 || y == image.getHeight() - 1) {
			return RGB_WHITE;
		}
		int c1 = image.getRGB(x, y);
		int differentPixels = trueCount(
				getMaxChannelDifference(c1, image.getRGB(x+1, y)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x, y+1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x+1, y+1)) > tolerance
				/*
				getMaxChannelDifference(c1, image.getRGB(x-1, y)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x-1, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x+1, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x-1, y+1)) > tolerance
				*/
				);
		return differentPixels > 1 ? RGB_BLACK : RGB_WHITE;
	}
}
class EdgeDetectRGBDifference extends ImageManipulator {
	private int tolerance;
	public EdgeDetectRGBDifference(int tolerance) {
		this.tolerance = tolerance;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
		int c1 = image.getRGB(x, y);
		int differentPixels = trueCount(
				getRGBDifference(c1, image.getRGB(x+1, y)) > tolerance,
				getRGBDifference(c1, image.getRGB(x, y+1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x+1, y+1)) > tolerance
				/*
				getRGBDifference(c1, image.getRGB(x-1, y)) > tolerance,
				getRGBDifference(c1, image.getRGB(x, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x-1, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x+1, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x-1, y+1)) > tolerance
				*/
				);
		return differentPixels > 1 ? RGB_BLACK : RGB_WHITE;
	}
}
class EdgeDetectRGBRatio extends ImageManipulator {
	private double toleranceRatio;
	public EdgeDetectRGBRatio(double toleranceRatio) {
		this.toleranceRatio = toleranceRatio;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
		int c1 = image.getRGB(x, y);
		int differentPixels = trueCount(
				getRGBRatio(c1, image.getRGB(x+1, y)) > toleranceRatio,
				getRGBRatio(c1, image.getRGB(x, y+1)) > toleranceRatio,
				getRGBRatio(c1, image.getRGB(x+1, y+1)) > toleranceRatio
				/*
				getRGBDifference(c1, image.getRGB(x-1, y)) > tolerance,
				getRGBDifference(c1, image.getRGB(x, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x-1, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x+1, y-1)) > tolerance,
				getRGBDifference(c1, image.getRGB(x-1, y+1)) > tolerance
				*/
				);
		return differentPixels > 1 ? RGB_BLACK : RGB_WHITE;
	}
}
class EdgeDetectChannelRatio extends ImageManipulator {
	private double toleranceRatio;
	public EdgeDetectChannelRatio(double toleranceRatio) {
		this.toleranceRatio = toleranceRatio;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
		if(x == image.getWidth() - 1 || y == image.getHeight() - 1) {
			return RGB_WHITE;
		}
		int c1 = image.getRGB(x, y);
		int differentPixels = trueCount(
				getMaxChannelRatio(c1, image.getRGB(x+1, y)) > toleranceRatio,
				getMaxChannelRatio(c1, image.getRGB(x, y+1)) > toleranceRatio,
				getMaxChannelRatio(c1, image.getRGB(x+1, y+1)) > toleranceRatio
				/*
				getMaxChannelDifference(c1, image.getRGB(x-1, y)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x-1, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x+1, y-1)) > tolerance,
				getMaxChannelDifference(c1, image.getRGB(x-1, y+1)) > tolerance
				*/
				);
		return differentPixels > 1 ? RGB_BLACK : RGB_WHITE;
	}
}
class ColorSwap extends ImageManipulator {
	enum SwapMode {
		//RGB,
		GRB, RBG, BGR, BRG, GBR,
		RRB, RRG, RBR, RGR, BRR, GRR,
		BBR, BBG, BRB, BGB, RBB, GBB,
		GGR, GGB, GRG, GBG, RGG, BGG
		//, RRR, GGG, BBB
	}
	private final String mode;
	public ColorSwap() {
		SwapMode[] swapValues = SwapMode.values();
		mode = swapValues[random(swapValues.length)].toString();
	}
	public ColorSwap(String mode) {
		this.mode = mode;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
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
		return new Color(channels[0], channels[1], channels[2]).getRGB();
	}
}
class ColorReduce extends ImageManipulator {
	int range;
	public ColorReduce(int range) {
		this.range = range;
	}
	@Override
	public int getRGB(BufferedImage image, int x, int y) {
		int c1 = image.getRGB(x, y);
		int r1 = (c1 & 0x00ff0000) >> 16;
		int g1 = (c1 & 0x0000ff00) >> 8;
		int b1 = c1 & 0x000000ff;
		int a1 = (c1 >> 24) & 0xff;
		
		r1 = (r1 / range) * range;
		g1 = (g1 / range) * range;
		b1 = (b1 / range) * range;
		a1 = (a1 / range) * range;
		
		return new Color(r1, g1, b1).getRGB();
	}
	
}