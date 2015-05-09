import java.awt.image.WritableRaster;
import java.io.IOException;


//import JTransforms library to compute 2D DFT and inverse DFT
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class FFTMethods {

	public static void fastFourierTransform(double[] fftArray, int imageWidth, int imageHeight, WritableRaster writableRaster) {
		//create a new instance of DoubleFFT_2D to compute 2D DFT of complex and real, double precision data
		DoubleFFT_2D twoDFFT = new DoubleFFT_2D(imageWidth, imageHeight);
		//DoubleFFT_2D twoDFFT = new DoubleFFT_2D(imageHeight, imageWidth);
	
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				//get samples from red, green and blue colour bands (for better precision, rather than just one band)
				double r = writableRaster.getSampleDouble(x, y, 0);
				double g = writableRaster.getSampleDouble(x, y, 1);
				double b = writableRaster.getSampleDouble(x, y, 2);
				
				//add samples together
				double rgb = r+g+b;
				
				//place in fft array
				//real part is set to value of rgb
				fftArray[2 * (x + y * imageWidth)] = rgb;
				//imaginary part is set to zero
				fftArray[2 * (x + y * imageWidth) + 1] = 0;
			}
		}				
		//computes 2D forward DFT of complex data leaving the result in fftArray
		twoDFFT.complexForward(fftArray);
	}

	public static void inverseFFT(double[] convolutedData, double[] finalImage, int width, int height) throws IOException {
		//create a new instance of DoubleFFT_2D to compute 2D DFT of complex and real, double precision data
		DoubleFFT_2D twoDFFT = new DoubleFFT_2D(width, height);
		
		//computes 2D inverse DFT of complex data leaving the result in finalImage
		twoDFFT.complexInverse(convolutedData, true);
	
		//assign elements of convolutedData to elements of finalImage
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				finalImage[y * width + x] = convolutedData[y * 2 * width + 2 * x];
			}
		}
	}
}
