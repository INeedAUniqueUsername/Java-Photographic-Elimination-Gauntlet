import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
public class TranscendenceSpriteMaker {
	private static final int RGB_BLACK = Color.BLACK.getRGB();
	private static final int RGB_WHITE = Color.WHITE.getRGB();
	//static final File dir = new File("src\\Picture Lab\\images");
	private static final File dir = new File("C:\\Users\\Alex\\Desktop\\Transcendence Multiverse\\Sources");
	private static final String[] EXTENSIONS = new String[]{
		/* "gif", "png", "bmp", */ "jpg"
	};
	private static final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
	private static final ImageWriteParam jpgParam = createJPGParam();
	private static int directories = 0;
	private static int images = 0;
	/*
	static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

		@Override
		public boolean accept(final File dir, final String name) {
			if(name.indexOf("Mask") != -1) {
				return false;
			}
			//Accept folders
			if(name.indexOf(".") == -1) {
				return true;
			}
			//Accept compatible formats
			for (final String ext : EXTENSIONS) {
				if (name.endsWith("." + ext)) {
					return true;
				}
			}
			return false;
		}
	};
	*/
	public static void main(String[] args) {
		
		String outputPath = "./Output";
		new File(outputPath).mkdir();
		processDirectory(dir, outputPath);
	}
	public static void print(String message) {
		System.out.println(message);
	}
	public static void processDirectory(File directory, String outputOrigin) {
		if (directory.isDirectory()) { // make sure it's a directory
			//Create a folder of the same name in the Output folder so we can output there
			String outputPath = outputOrigin + "\\" +directory.getName();
			new File(outputPath).mkdir();
			print("Processing Directory: " + directory.getPath());
			for (final File f : directory.listFiles()) {
				String fileName = f.getName();
				String filePath = outputPath + "\\" + fileName;
				if(f.isDirectory()) {
					//Process the images in this directory and make a folder its the output counterpart.
					processDirectory(f, outputPath);
				} else if(fileName.indexOf(".jpg") == -1) {
					print("Not a JPG image: "  + f.getPath());
				} else if(new File(filePath).exists()) {
					print("Image already made: " + f.getPath());
				} else {
					print("Processing Image: " + f.getPath());
					try {
						BufferedImage image = ImageIO.read(f);
						if(image == null) {
							print("Image does not exist: " + filePath);
							continue;
						}
						writeImageToPath(pencilSketch3(image), filePath);
						images++;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						print("Failed to load image: " + filePath);
						e.printStackTrace();
					}
				}
			}
			directories++;
		}
	}
	public static BufferedImage pencilSketch(BufferedImage i) {
		return createSprite(i, 20);
	}
	public static BufferedImage pencilSketch2(BufferedImage i) {
		return createSprite(i, 15);
	}
	public static BufferedImage pencilSketch3(BufferedImage i) {
		return edgeDetectChannelRatio(i, 0.6);
	}
	public static BufferedImage createSprite(BufferedImage i, int tolerance) {
		
		return edgeDetectChannelDifference(i, tolerance);
	}
	public static BufferedImage createSprite(BufferedImage i, double scale, int tolerance) {
		return edgeDetectChannelDifference(scaleImage(i, scale), tolerance);
	}
	public static BufferedImage createSprite2(BufferedImage i, int tolerance) {
		return edgeDetectRGBDifference(i, tolerance);
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
			
			writer.setOutput(new FileImageOutputStream(
			  new File(path)));
			writer.write(null, new IIOImage(image, null, null), jpgParam);
			
			print("Created Image: " + path);
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
	
	public static ImageWriteParam createJPGParam() {
		ImageWriteParam jpgWriteParam = writer.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(1.0f);
		return jpgWriteParam;
	}
	
	public static BufferedImage reduceColors(BufferedImage image, int range) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
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
				
				pixels[y][x] = new Color(r1, g1, b1).getRGB();
			}
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
		int[][] pixels = new int[height][width];
		SwapMode[] swapValues = SwapMode.values();
		String mode = swapValues[random(swapValues.length)].toString();
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
				pixels[y][x] = new Color(channels[0], channels[1], channels[2]).getRGB();
				
				
				//pixels[i] = new Color(random(channels), random(channels), random(channels), a1).getRGB();
				//pixels[i] = new Color(g1, r1, b1, a1).getRGB(); //GRB
			}
		}
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		setPixels(b, pixels);
		return b;
	}
	public static BufferedImage edgeDetectChannelDifference(BufferedImage image, int tolerance) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
		for(int y = 1; y < height-1; y++) {
			for(int x = 1; x < width-1; x++) {
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
				if(differentPixels > 1) {
					pixels[y][x] = RGB_BLACK;
				} else {
					pixels[y][x] = RGB_WHITE;
				}
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
	
	public static BufferedImage edgeDetectChannelRatio(BufferedImage image, double toleranceRatio) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
		for(int y = 1; y < height-1; y++) {
			for(int x = 1; x < width-1; x++) {
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
				if(differentPixels > 1) {
					pixels[y][x] = RGB_BLACK;
				} else {
					pixels[y][x] = RGB_WHITE;
				}
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
	
	public static BufferedImage edgeDetectRGBDifference(BufferedImage image, int tolerance) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
	
		for(int y = 1; y < height-1; y++) {
			for(int x = 1; x < width-1; x++) {
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
				if(differentPixels > 3) {
					pixels[y][x] = RGB_WHITE;
				} else {
					pixels[y][x] = RGB_BLACK;
				}
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
	
	public static BufferedImage edgeDetectRGBRatio(BufferedImage image, double toleranceRatio) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] pixels = new int[height][width];
	
		for(int y = 1; y < height-1; y++) {
			for(int x = 1; x < width-1; x++) {
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
				if(differentPixels > 3) {
					pixels[y][x] = RGB_WHITE;
				} else {
					pixels[y][x] = RGB_BLACK;
				}
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
	public static int trueCount(boolean... conditions) {
		int result = 0;
		for(boolean b : conditions) {
			if(b) {
				result++;
			}
		}
		return result;
	}
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
	public static double max(double... numbers) {
		return max(numbers.length-1, numbers);
	}
	public static double max(int start, double[] numbers) {
		if(start > 1) {
			return Math.max(numbers[1], numbers[0]);
		} else {
			return Math.max(numbers[start], max(start-1, numbers));
		}
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
