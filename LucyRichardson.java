import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.Math.*;

public class LucyRichardson {

	private static int H = 128;
	private static int SIGMA = 10;

	private static int distance (int i, int j, int k, int l) {
		return (i - k) * (i - k) + (j - l) * (j - l);
	}

	private static double[] spread() {
		double[] p = new double[2*(H+1)*(H+1)];
		
		int i, j;
  		float temp = 0;
  		
  		for(i = -H; i <= H; i++) {
    		for(j = -H; j <= H; j++) {
				p[i * i + j * j] =  Math.exp(-((double)(i * i + j * j))/SIGMA);
				temp += p[i * i + j * j];
    		}
  		}  
    
  		for(i = -H; i <= H; i++) {
    		for(j = -H; j <= H; j++) {
      			p[i * i + j * j] = p[i * i + j * j]/temp;
    		}
  		}
		
		return p;
	}

	private static double[][] getImageArray(BufferedImage image) {

		int width = image.getWidth();
		int height = image.getHeight();

		double imageArray[][] = new double[height][width];

		for(int y = 0; y < height; y++) {
			
			for(int x=0; x < width; x++) {


				int p = image.getRGB(x,y);
 
                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                int avg = (r+g+b)/3;

				imageArray[x][y] = avg;

			}
		}

		return imageArray;
	}

	private static BufferedImage getImage(double[][] imageArray) {

		int height = imageArray.length;
		int width = imageArray[0].length;

		BufferedImage image = new BufferedImage( height, width , BufferedImage.TYPE_INT_ARGB);

		for(int y = 0; y < height; y++) {
			
			for(int x=0; x < width; x++) {

				int avg = (int) imageArray[x][y];
				int alpha = 255;
				int p = (alpha << 24) | (avg << 16) | (avg << 8) | avg;

				image.setRGB(x, y, p);
			}
		}
		return image;

	}

	private static double[][] lucyRichardson(double[][] blurredArray, double[] p, int iterations) {

		int height = blurredArray.length;
		int width = blurredArray[0].length;

		double[][] imageOld = blurredArray;
		double[][] imageNew = new double[height][width];

		
		int i1, i2, j1, j2, k1, k2;
  		double temp, tmp;

    	for(i1 = 0; i1 < height; i1++) {
      		for(i2 = 0; i2 < width; i2++) {

				tmp = 0;
				for(j1 = Math.max(0, i1 - H + 1); j1 < Math.min(i1 + H - 1, height); j1++) {
					for(j2 = Math.max(0, i2 - H + 1); j2 < Math.min(i2 + H - 1, width); j2++) {
						temp = 0;
						for(k1 = Math.max(0, i1 - H + 1); k1 < Math.min(i1 + H - 1, height); k1++) {
							for(k2 = Math.max(0, i2 - H + 1); k2 < Math.min(i2 + H - 1, width); k2++) {
								//System.out.println(j1+" "+j2+" "+k1+" "+k2);
								temp = temp + imageOld[k1][k2] * p[distance(j1,j2,k1,k2)];
							}
						}
						tmp = tmp + blurredArray[j1][j2] * (p[distance(j1,j2,i1,i2)])/temp;
					}
	  			}
				imageNew[i1][i2] = imageOld[i1][i2] * tmp;
      		}
		}

		if(iterations == 0) {
			return imageNew;
		} else {
			return lucyRichardson(imageNew,p,iterations-1);
		}


	}

	private static BufferedImage convertToGrayscale(BufferedImage image) {

		int width = image.getWidth();
		int height = image.getHeight();

		for(int y = 0; y < height; y++) {
			
			for(int x=0; x < width; x++) {

				int p = image.getRGB(x,y);
 
                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                int avg = (r+g+b)/3;
 
                p = (a<<24) | (avg<<16) | (avg<<8) | avg;
 
                image.setRGB(x, y, p);
			}
		}
		File file = null;
		try
        {
            file = new File("blurredBoardGrey.png");
            ImageIO.write(image, "png", file);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }

		return image;
	}

	private static void writeImage(String filename, BufferedImage image) {
		File file = null;
		try {
            file = new File(filename);
            ImageIO.write(image, "png", file);
        } catch(IOException e) {
            System.out.println(e);
        }
	}

	private static BufferedImage readImage(String filename) {
		File file = null;
		BufferedImage image = null;

		try {
			file = new File(filename);
			image = ImageIO.read(file);
		} catch (IOException e) {
			System.out.println(e);
		}
		return image;
	}

	public static void main( String args[] ) throws IOException {

		double[] p = spread();
		
		BufferedImage image = readImage("blurredBoard.png");
		
		double[][] imageArray = getImageArray(image);

		BufferedImage grayImage = getImage(imageArray);

		writeImage("grey.png",grayImage);

		double[][] newImageArray = lucyRichardson(imageArray,p,0);

		BufferedImage restoredImage = getImage(newImageArray);

		writeImage("restored.png",restoredImage);

		

	}
}