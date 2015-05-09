import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.math3.complex.Complex;

@SuppressWarnings("serial")
public class ImageWindow extends JFrame implements ActionListener, ChangeListener {

	//initial alpha, gamma, sigma values
	private static double sigma;
	private static double gamma;
	private static double alpha;

	//GUI buttons
	private JButton browseButton;
	private JButton blurButton;
	private JButton wienerButton;
	private JButton sharpenButton;
	private JButton cropButton;
	private JButton saveButton;
	
	//GUI sliders - oriented vertically
	JSlider sigmaSlider = new JSlider(JSlider.VERTICAL,0,1000,100);
	JSlider gammaSlider = new JSlider(JSlider.VERTICAL,0,1000,100);
	JSlider alphaSlider = new JSlider(JSlider.VERTICAL,0,1000,1000);

	//GUI text fields
	private JTextField path2FileInput;
	public static JTextField sigmaValue;
	public static JTextField gammaValue;
	public static JTextField alphaValue;
	
	//booleans to check if buttons have been clicked
	Boolean wienerButtonClicked = false;
	Boolean blurButtonClicked = false;
	
	//mouse variables
	int dragStatus = 0,c1,c2,c3,c4;

	//image panels and BufferedImages
	private ImagePanel imagePanel, imagePanel2;
	private BufferedImage image4Processing, blurredImage, deblurredOriginalImage, deblurredBlurryImage;

	public ImageWindow() {
		//set the size of the image window
		setSize((int) (1080 * 1.5), (int) (400 * 1.5));
		//set the image window to the centre of the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		//set the title of the image window
		setTitle("Blurred|Lines");

		//when we close window application should exit too
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//
		//Build User Interface
		//
		path2FileInput = new JTextField(25);
		path2FileInput.setFocusable(false);
		sigmaValue = new JTextField(3);
		sigmaValue.setText("0.1");
		gammaValue = new JTextField(3);
		gammaValue.setText("0.1");
		alphaValue = new JTextField(3);
		alphaValue.setText("1.0");
		browseButton = new JButton("Browse");
		blurButton = new JButton("Blur");
		wienerButton = new JButton("Deblur");
		sharpenButton = new JButton("Sharpen");
		cropButton = new JButton("Crop");
		saveButton = new JButton("Save");

		//can't click these buttons until an image is loaded
		wienerButton.setEnabled(false);
		blurButton.setEnabled(false);
		sharpenButton.setEnabled(false);
		cropButton.setEnabled(false);
		saveButton.setEnabled(false);

		//register for clicks
		browseButton.addActionListener(this);
		blurButton.addActionListener(this);
		wienerButton.addActionListener(this);
		sharpenButton.addActionListener(this);
		cropButton.addActionListener(this);
		saveButton.addActionListener(this);

		//panel with UI elements at the top of the window
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //new panel with FlowLayout
		topPanel.add(new JLabel("Input file: ")); //add label for file to be chosen
		topPanel.add(path2FileInput); //add the text field for file path to topPanel
		topPanel.add(browseButton); //add browse button to topPanel
		topPanel.add(blurButton); //add blur button to topPanel
		topPanel.add(wienerButton); //add wiener button to topPanel
		topPanel.add(sharpenButton); //add save button to topPanel
		topPanel.add(cropButton); //add crop button to topPanel
		topPanel.add(saveButton); //add save button to topPanel
		topPanel.add(Box.createHorizontalStrut(50)); //add some space
		topPanel.add(new JLabel("Sigma")); //add label for Sigma value input to topPanel
		topPanel.add(sigmaValue); //add text field for Sigma value to topPanel
		topPanel.add(new JLabel("Gamma")); //add label for Gamma value input to topPanel
		topPanel.add(gammaValue); //add text field for Gamma value to topPanel
		topPanel.add(new JLabel("Alpha")); //add label for Alpha value input to topPanel
		topPanel.add(alphaValue); //add text field for Alpha value to topPanel

		//panel with images
		JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //new panel with FlowLayout for images
		imagePanel = new ImagePanel(); //first image panel
		imagePanel2 = new ImagePanel(); //second image panel
		imagePanel.setSize(480, 480); //set first image panel dimensions
		imagePanel2.setSize(480, 480); //set second image panel dimensions
		outputPanel.add(imagePanel); //add first image panel to outputPanel
		outputPanel.add(imagePanel2); //add second image panel to outputPanel
		
		//slider settings
		sigmaSlider.setMajorTickSpacing(1000);
		sigmaSlider.setMinorTickSpacing(100);
		sigmaSlider.setPaintTicks(true);
		sigmaSlider.addChangeListener(this);
		gammaSlider.setMajorTickSpacing(1000);
		gammaSlider.setMinorTickSpacing(100);
		gammaSlider.setPaintTicks(true);
		gammaSlider.addChangeListener(this);
		alphaSlider.setMajorTickSpacing(1000);
		alphaSlider.setMinorTickSpacing(100);
		alphaSlider.setPaintTicks(true);
		alphaSlider.addChangeListener(this);
		
		//new panel
		JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Font myFont = new Font("Serif", Font.ITALIC | Font.BOLD, 12);
		JLabel companyName = new JLabel("Mark Skelton Creations™");
		companyName.setFont(myFont);
		aPanel.add(companyName);
		
		//panel with sliders
		JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 55, 0));
		sliderPanel.add(sigmaSlider);
		sliderPanel.add(gammaSlider);
		sliderPanel.add(alphaSlider);

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH); //set topPanel at top of screen
		add(outputPanel, BorderLayout.WEST); //set outputPanel in middle of screen
		add(sliderPanel, BorderLayout.EAST);
		add(aPanel, BorderLayout.SOUTH);
	}

	//will handle button clicks
	@Override
	public void actionPerformed(ActionEvent e){

		//handle browse button click
		if (e.getSource() == browseButton) {
			onBrowseButtonClick();
		} else if (e.getSource() == blurButton) {
			try {
				onBlurButtonClick();
				blurButtonClicked = true;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == wienerButton) {
			try {
				onWienerButtonClick();
				wienerButtonClicked = true;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == sharpenButton) {
			try {
				onSharpenButtonClick();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == cropButton) {
			try {
				onCropButtonClick();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == saveButton) {
			try {
				onSaveButtonClick();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e){
		
		double sigmaChanged = sigmaSlider.getValue();
		double gammaChanged = gammaSlider.getValue();
		double alphaChanged = alphaSlider.getValue();
		
		sigmaValue.setText("" + sigmaChanged/1000);
		gammaValue.setText("" + gammaChanged/1000);
		alphaValue.setText("" + alphaChanged/1000);
		
	}

	//handle's what to do when browse button is clicked
	private void onBrowseButtonClick(){
		
		//show file selection dialog
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select an image file");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); //only files can be selected
		chooser.setMultiSelectionEnabled(false); //single selection

		chooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Image files (.png .jpg .bmp)";
			}

			@Override
			public boolean accept(File file) {
				String name = file.getName();
				if(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp"))
					return true;

				return false;
			}
		});

		chooser.showOpenDialog(ImageWindow.this);

		File selectedFile = chooser.getSelectedFile();

		//check if selection was successful
		if(selectedFile != null) {

			path2FileInput.setText(selectedFile.getAbsolutePath());

			try {
				image4Processing = loadImage(selectedFile);

				//image was loaded successfully, enable blur/deblur/sharpen/crop buttons
				wienerButton.setEnabled(true);
				blurButton.setEnabled(true);
				sharpenButton.setEnabled(true);
				cropButton.setEnabled(true);
				saveButton.setEnabled(true);

				//show image in the main panel
				imagePanel.setImage(image4Processing);

			} catch (IOException e1) { //in case reading image file fails
				blurButton.setEnabled(false);
				JOptionPane.showMessageDialog( null, "Failed to read image from file " + selectedFile.getAbsolutePath());
			}
		}
	}

	//handle's what to do when blur button is clicked
	private void onBlurButtonClick() throws Exception {
		if(image4Processing == null) //skip if there is nothing to work with
			return;

		BufferedImage bufferedImage = (BufferedImage) image4Processing; //set bufferedImage to equal chosen image
		
		//if the image is larger than the image panel, rescale it
		//int w = bufferedImage.getWidth();
		//int h = bufferedImage.getHeight();

		//get the width and height of the image
		int imageWidth = bufferedImage.getWidth();
		int imageHeight = bufferedImage.getHeight();
		//create a new fft array, needs to be twice the size of the image to hold real and imaginary parts
		double[] fftArray = new double[2 * imageWidth * imageHeight];
		//get raster info about image and store as WritableRaster so values can later be rewritten
		WritableRaster writableRasterInfo = bufferedImage.getRaster();

		//computes 2D forward DFT of complex data leaving the result in fftArray
		FFTMethods.fastFourierTransform(fftArray, imageWidth, imageHeight, writableRasterInfo);
		
		//add the motion blur degradation
		double[] degradationArray = new double[2 * imageWidth * imageHeight];
		applyMotionBlurDegradation(degradationArray, imageWidth, imageHeight);

		//convolve data
		double[] convoluted = new double[2 * imageWidth * imageHeight];
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) {
				//extract the real number from fftArray
				double realImg = fftArray[2 * (x + y * imageWidth)];
				//extract the imaginary number from fftArray
				double imaginaryImg = fftArray[2 * (x + y * imageWidth) + 1];
				//create a new complex number from FFT values of image
				Complex complexImage = new Complex(realImg, imaginaryImg);

				//extract the real number from the degradationArray
				double realDegradated = degradationArray[2 * (x + y * imageWidth)];
				//extract the imaginary number from the degradationArray
				double imgDegradated = degradationArray[2 * (x + y * imageWidth) + 1];
				//the degradation values are then multiplied to form the complex conjugate
				Complex m = complexImage.multiply(new Complex(realDegradated, imgDegradated));
				//extract the real number from the complex conjugate of the degradation
				convoluted[2 * (x + y * imageWidth)] = m.getReal();
				//extract the imaginary number from the complex conjugate of the degradation
				convoluted[2 * (x + y * imageWidth) + 1] = m.getImaginary();
			}
		}

		//create a new array to hold the image result
		double[] imageResult = new double[imageWidth * imageHeight];
		//perform the inverse fft on the convoluted data to get back to time domain
		//and store this in the imageResult array
		FFTMethods.inverseFFT(convoluted, imageResult, imageWidth, imageHeight);

		//create a new buffered image to hold the result of blurring the image
		blurredImage = FilterMethods.createImage(imageWidth, imageHeight, imageResult);
		//display the blurred image
		imagePanel2.setImage(blurredImage);
	}

	//handle's what to do when wiener button is clicked
	private void onWienerButtonClick() throws Exception {
		if (image4Processing == null)
			return;

		imagePanel2.setImage(wienerFilter(blurredImage));
		
		//imagePanel2.setImage(image4Processing);
		/*
		//if there is no blurredImage use original image
		if (blurredImage == null){
			deblurredOriginalImage = wienerFilter(image4Processing);
			imagePanel2.setImage(deblurredOriginalImage);
		}
		//if there is a blurredImage use it (image blurred by blur function)
		else if (blurredImage != null){
			deblurredBlurryImage = wienerFilter(blurredImage);
			imagePanel2.setImage(deblurredBlurryImage);
		}*/
	}
	
	private void onSharpenButtonClick() throws Exception {
		Kernel kernel = new Kernel(3, 3,
				new float[]{
					-1, -1, -1,
					-1,  9, -1,
					-1, -1, -1});
		
		if(blurButtonClicked == false && wienerButtonClicked == false){ //allows user to sharpen original input image
			BufferedImageOp op = new ConvolveOp(kernel);
			image4Processing = op.filter(image4Processing, null);
			imagePanel2.setImage(image4Processing);
		}
		
		if(blurButtonClicked == true && wienerButtonClicked == false){ //allows user to sharpen the blurred image
			BufferedImageOp op = new ConvolveOp(kernel);
			blurredImage = op.filter(blurredImage, null);
			imagePanel2.setImage(blurredImage);
		}
		
		if(wienerButtonClicked == true){
			if(deblurredOriginalImage != null){ //if original image is deblurred
				BufferedImageOp op = new ConvolveOp(kernel);
				deblurredOriginalImage = op.filter(deblurredOriginalImage, null);
				imagePanel2.setImage(deblurredOriginalImage);
			}
			else if(deblurredBlurryImage != null){ //if blurred image is deblurred (blurred by blur function)
				BufferedImageOp op = new ConvolveOp(kernel);
				deblurredBlurryImage = op.filter(deblurredBlurryImage, null);
				imagePanel2.setImage(deblurredBlurryImage);
			}
		}
		
	}
	
	private void onCropButtonClick() throws Exception{
		
		CropImage cropImage = new CropImage();
		
		if(blurButtonClicked == false && wienerButtonClicked == false){
			cropImage.start(image4Processing);
		}
		
		if(blurButtonClicked == true && wienerButtonClicked == false){
			cropImage.start(blurredImage);
		}
		
		if(wienerButtonClicked == true){
			if (deblurredOriginalImage != null){
				cropImage.start(deblurredOriginalImage);
			}
			else if (deblurredBlurryImage != null){
				cropImage.start(deblurredBlurryImage);
			}
		}
		
	}
	
	private void onSaveButtonClick() throws Exception{
		
		//show file selection dialog
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //only directories can be selected
		chooser.setMultiSelectionEnabled(false); //single selection

		chooser.showOpenDialog(this);

		String selectedDirectory = null;
		
		//if user clicks save button but doesn't choose a directory no NullPointerException is thrown
		try{
		selectedDirectory = chooser.getSelectedFile().getAbsolutePath();
		}catch(NullPointerException e){
			
		}
		
		BufferedImage imageToSave = null;
		
		//set image to save
		if(blurButtonClicked == false && wienerButtonClicked == false){
			imageToSave = image4Processing;
		}
		
		if(blurButtonClicked == true && wienerButtonClicked == false){
			imageToSave = blurredImage;
		}
		
		if(wienerButtonClicked == true){
			if (deblurredOriginalImage != null){
				imageToSave = deblurredOriginalImage;
			}
			else if (deblurredBlurryImage != null){
				imageToSave = deblurredBlurryImage;
			}
		}
		
		//check if selection was successful
		if(selectedDirectory != null) {

			try {
				saveImage(selectedDirectory, imageToSave);
			} catch (IOException e1) { //in case reading image file fails
				//JOptionPane.showMessageDialog( null, "Failed to save image to file.");
			}
		}
		//if there is no selection, do nothing
		else if(selectedDirectory == null){
			
		}
	}
	
	//method to save the image file
	private void saveImage(String imageDirectory, BufferedImage imageToSave) throws IOException {
				
		try{
			File saveDirectory = new File(imageDirectory + "/newImage.jpg");
			ImageIO.write(imageToSave, "jpg", saveDirectory);
		} catch(IOException e){
					
		}
				
	}

	public static BufferedImage wienerFilter(BufferedImage bufferedImage) throws IOException {

		//get sigma, gamma and alpha values from user input
		String getSigma = sigmaValue.getText();
		String getGamma = gammaValue.getText();
		String getAlpha = alphaValue.getText();

		//convert the String values to double
		sigma = Double.parseDouble(getSigma);
		gamma = Double.parseDouble(getGamma);
		alpha = Double.parseDouble(getAlpha);

		//get the raster for the image
		WritableRaster writableRaster = bufferedImage.getRaster();

		//get height and width from the raster
		int imageWidth = bufferedImage.getWidth();
		int imageHeight = bufferedImage.getHeight();

		//perform FFT forward operation
		double[] fftArray = new double[2 * imageWidth * imageHeight];
		//computes 2D forward DFT of complex data leaving the result in fftArray
		FFTMethods.fastFourierTransform(fftArray, imageWidth, imageHeight, writableRaster);

		//array to hold complex data of image
		Complex[] complexImage = new Complex[imageWidth * imageHeight];

		//fill the complexImage array with data from fftArray
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				complexImage[x + y * imageWidth] = new Complex(fftArray[2 * (x + y * imageWidth)], fftArray[2 * (x + y * imageWidth) + 1]);
			}
		}

		//create a new array to hold the point spread function
		double[] degradation = new double[2 * imageWidth * imageHeight];
		Complex[] complexPsf = applyMotionBlurDegradation(degradation, imageWidth, imageHeight);

		//deconvolve the data
		double[] convolutedData = new double[2 * imageWidth * imageHeight];
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				Complex imageData = complexImage[x + y * imageWidth];
				Complex degradatedData = complexPsf[x + y * imageWidth];
				Complex decovolutedData = FilterMethods.deconvolutionByWiener(imageData, degradatedData);
				convolutedData[2 * (x + y * imageWidth)] = decovolutedData.getReal();
				convolutedData[2 * (x + y * imageWidth) + 1] = decovolutedData.getImaginary();
			}
		}

		//perform fft inverse operation
		double[] imageResult = new double[imageWidth * imageHeight];
		FFTMethods.inverseFFT(convolutedData, imageResult, imageWidth, imageHeight);

		return FilterMethods.createImage(imageWidth, imageHeight, imageResult);
	}

	//method to load the image file
	private BufferedImage loadImage(File imagefile) throws IOException {
		BufferedImage image = ImageIO.read(imagefile);
		return image;
	}

	public static void main(String[] args) {
		//start and show the Window
		new ImageWindow().setVisible(true);
	}

	private static Complex[] applyMotionBlurDegradation(double[] degradation, int width, int height) {
		//get sigma, gamma and alpha values from user input
		String getSigma = sigmaValue.getText();
		String getGamma = gammaValue.getText();
		String getAlpha = alphaValue.getText();

		sigma = Double.parseDouble(getSigma);
		gamma = Double.parseDouble(getGamma);
		alpha = Double.parseDouble(getAlpha);
		
		if(alpha == 0){
			JOptionPane.showMessageDialog(null, "Only a black image will be produced if alpha is set to 0.");
		}
		
		return FilterMethods.motionBlurFunction(degradation, width, height, alpha, gamma, sigma);
	}
}
