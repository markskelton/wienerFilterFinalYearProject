import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.apache.commons.math3.analysis.function.Sinc;
import org.apache.commons.math3.complex.Complex;


public class FilterMethods {

	//deconvolve data using the Wiener filter
	public static Complex deconvolutionByWiener(Complex image, Complex PSF) {
		//power spectrum of the noise/power spectrum of undegraded image
		//double constant = Math.pow(1.07, 32) / 10000.0;
		double constant = 8 / 10000;
		//H = H(u,v)^2 - PSF squared
		double H = Math.pow(PSF.getReal(), 2) + Math.pow(PSF.getImaginary(), 2);
		//H(u,v)^2 / (H(u,v)^2 + K)
		double wiener = H / (H + constant);
	
		//returns a Complex whose value is (image/PSF) - G(u,v)/H(u,v)
		Complex divide = image.divide(PSF);
		//returns a Complex whose value is (divide*wiener)
		return divide.multiply(wiener);
	}

	//generate the motion blur
	public static Complex[] motionBlurFunction(double[] degradation, int width, int height, double alpha, double gamma, double sigma) {
		//array for storing final complex values
		Complex[] complex = new Complex[width * height];

		//array to temporarily hold the real/imaginary values of the complex conjugate
		double[] temp = new double[2 * width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double theta = Math.PI * ( (((x - width/2) % width) * gamma) + ((((y - height/2) % height) * sigma) ));
				
				//create new instance of Sinc as we are generating Gaussian blur
				Sinc sinc = new Sinc();
				
				//compute real and imaginary parts
				double real = (Math.cos(theta) * sinc.value(theta)) * alpha;
				double imaginary = (Math.sin(theta) * sinc.value(theta)) * alpha;

				//return the conjugate of the complex number
				Complex cConj = new Complex(real, imaginary).conjugate();

				//store real and imaginary parts in temp array
				temp[2 * (x + y * width)] = cConj.getReal();
				temp[2 * (x + y * width) + 1] = cConj.getImaginary();
			}
		}

		//translate x and y values
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int xTranslated = (x + width / 2) % width;
				int yTranslated = (y + height / 2) % height;

				double real = temp[2 * (xTranslated + yTranslated * width)];
				double imaginary = temp[2 * (xTranslated + yTranslated * width) + 1];

				degradation[2 * (x + y * width)] = real;
				degradation[2 * (x + y * width) + 1] = imaginary;

				Complex c = new Complex(real, imaginary);
				complex[y * width + x] = c;
			}
		}

		return complex;
	}

	//create the image
	public static BufferedImage createImage(int width, int height, double[] imageResult) {
		int[] rgb = new int[imageResult.length];
	
		double minValue = Double.MAX_VALUE;
		double maxValue = -Double.MAX_VALUE;
	
		int xyPointer;
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				xyPointer = x + y*width;
				if(imageResult[xyPointer] < minValue) minValue = imageResult[xyPointer];
				if(imageResult[xyPointer] > maxValue) maxValue = imageResult[xyPointer];
			}
		}
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				xyPointer = x + y*width;
				
				int pixelData = (int)(255.0*((imageResult[xyPointer])-minValue) / (maxValue-minValue) + 0.5);
				if(pixelData < 0) {
					pixelData = 0;
				} else if(pixelData > 255) {
					pixelData = 255;
				}
	
				rgb[xyPointer] = 0xFF000000 | pixelData << 16 | pixelData << 8 | pixelData;
			}
		}
	
		final BufferedImage unblurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final WritableRaster unblurredRaster = unblurredImage.getRaster();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				unblurredRaster.setSample(x, y, 0, (rgb[x + y * width] >> 16) & 0xFF); // red
				unblurredRaster.setSample(x, y, 1, (rgb[x + y * width] >> 8) & 0xFF); // green
				unblurredRaster.setSample(x, y, 2, rgb[x + y * width] & 0xFF); // blue
				unblurredRaster.setSample(x, y, 3, (rgb[x + y*width] >> 24) & 0xFF); // alpha
			}
		}
	
		return unblurredImage;
	}

}
