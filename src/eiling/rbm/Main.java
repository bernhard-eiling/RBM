package eiling.rbm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JPanel {

	
	//////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 1L;
	private static final int borderWidth = 5;
	private static final int maxWidth = 520;
	private static final int maxHeight = maxWidth;
	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView recView;			// reconstruction image view
    private ImageView negView;
    private ImageView smallSrcView;
    private ImageView smallRecView;

	private ImgPanel srcPanel;
	private ImgPanel recPanel;
    private ImgPanel negPanel;
    /*
    private ImgPanel smallSrcPanel;
    private ImgPanel smallRecPanel;
    */
	
	JPanel displayPanel;
    JLabel displayLabel;
    JPanel samplesPanel = new JPanel(new GridBagLayout());
    JPanel controls = new JPanel(new GridBagLayout());
    JPanel dataPanel = new JPanel(new GridLayout(3, 1));
    JLabel mseLabel;
    JLabel bppLabel;
    JLabel compressionLabel;

    //private String imgName = "cameraman.png";
    //private String imgName = "fingerprint.png";
    private String imgName = "LenaBW.gif";


    /////////////////
    // RBM

    RBM rbm;
    private int rbmBlockSide = 16;
    private int visibleUnits = rbmBlockSide * rbmBlockSide;
    private int hiddenUnits = 64;
    private double learningRate= 0.3d;
    private int epochs = 1000;
    private int rbmNumBlockX;
    private int rbmNumBlockY;
    private ImgAsVectors inputVectorImgForRBM;
    Matrix reducedVectors;

    ////////////////


	public Main() {
		
		super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,borderWidth,0,0);
 
        // load the default image
        File input = new File("data/batch/" + imgName);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));

		recView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		recView.setMaxSize(new Dimension(maxWidth, maxHeight));

        negView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
        negView.setMaxSize(new Dimension(maxWidth, maxHeight));
/*
        smallSrcView = new ImageView(rbmBlockSide, rbmBlockSide);
        smallRecView = new ImageView(rbmBlockSide * 10, rbmBlockSide * 2);
*/
        ////////////////////////
		// BUTTONS
        JButton loadPic = new JButton("Bild öffnen");
        loadPic.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadFile(openFile());
                preProcessRBM();
        	}        	
        });
        controls.add(loadPic, c);

        JButton loadFolder = new JButton("Ordner öffnen");
        loadFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                preProcessRBM();
                openFolder();
            }
        });
        controls.add(loadFolder, c);

        JButton learnButton = new JButton("Learn and Safe RBM");
        learnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                learnSafeRBM();
            }
        });
        controls.add(learnButton, c);

        JButton loadRBMButton = new JButton("Load RBM");
        loadRBMButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRBM();
            }
        });
        controls.add(loadRBMButton, c);

        JButton runRBMButton = new JButton("Run RBM");
        runRBMButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               processRBM();
            }
        });
        controls.add(runRBMButton, c);
        /////////////////////////////
        

        // images panel
        JPanel images = new JPanel(new GridLayout(1,2));
        srcPanel = new ImgPanel(srcView, "Originalbild", " ");
        recPanel = new ImgPanel(recView, "Rekonstruiertes Bild", " ");
        negPanel = new ImgPanel(negView, "Negativbild", " ");
        /*
        smallSrcPanel = new ImgPanel(smallSrcView, "small src", " ");
        smallRecPanel = new ImgPanel(smallRecView, "small rec", " ");
        */

        images.add(srcPanel);
        images.add(recPanel);
        images.add(negPanel);
        /*
        images.add(smallSrcPanel);
        images.add(smallRecPanel);
        */

        displayPanel = new JPanel(new FlowLayout());
        displayLabel = new JLabel("some text");   
        
        displayPanel.add(displayLabel);

        /////////////////////////////////////////
        // Data Panel
        mseLabel = new JLabel("MSE: ");
        bppLabel = new JLabel("BPP: ");
        compressionLabel = new JLabel("Comp.: ");
        dataPanel.add(mseLabel);
        dataPanel.add(bppLabel);
        dataPanel.add(compressionLabel);

        ////////////////////////////////////
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(dataPanel, BorderLayout.WEST);
        add(samplesPanel, BorderLayout.SOUTH);

        /////////////////////////////

        //////////////////////////
        // RBM
        rbm = new RBM(visibleUnits, hiddenUnits, learningRate, epochs);
        rbmNumBlockX = srcView.getImgWidth() / rbmBlockSide;
        rbmNumBlockY = srcView.getImgHeight() / rbmBlockSide;

        ////////////////////////////

        makeGray(srcView);

        preProcessRBM();
        //processRBM();
        /*
        generateListOfCodebookVectors();
        process();
        */
	}

    ///////////////////////////////////
    // RBM
    ///////////////////////////////////

    private void preProcessRBM() {
        inputVectorImgForRBM = new ImgAsVectors(srcView, rbmBlockSide);
        double[][] vectors = inputVectorImgForRBM.getVectorsForRBM();
        srcView.setPixels(generatePixelArray(vectors, srcView.getImgWidth(), srcView.getImgHeight(), rbmNumBlockX, rbmNumBlockY));
        srcView.applyChanges();

        reducedVectors = new Matrix(vectors.length, vectors[0].length);
        reducedVectors.setData(vectors);
        reducedVectors = reducedVectors.divide(255.0d);
    }

    private void processRBM() {


        // double[][] decodedImg = new double[vectors.length][vectors[0].length];

        // Blöcke auf einmal codieren / decodieren
        Matrix codedImg = rbm.code(reducedVectors);
        Matrix decodedImgArray = rbm.decode(codedImg);

        Matrix inflatedImg = decodedImgArray.scalarMultiply(255.0d);
        int[] recPixel = generatePixelArray(inflatedImg.getData(), recView.getImgWidth(), recView.getImgHeight(), rbmNumBlockX, rbmNumBlockY);

        recView.setPixels(recPixel);
        recView.applyChanges();

        negView.setPixels(generateNegativeImage(srcView.getPixels(), recView.getPixels()));
        negView.applyChanges();

        double psnr = Utilities.calcPSNR(srcView, recView.getPixels());
        double bpp = (double)hiddenUnits / (double)visibleUnits;
        System.out.println("PSNR: " + psnr);
        System.out.println("BPP: " + bpp);

        // calc the number of never activated or always activated nodes
        double[][] hiddenStates = codedImg.getData();
        int unusedStates = 0;
        for (int c = 0; c < hiddenStates[0].length; c++) {
            int numOnStates = 0;
            int numOffStates = 0;
            for (int r = 0; r < hiddenStates.length; r++) {
                double val = hiddenStates[r][c];
                if (val == 0.0d) numOffStates++;
                if (val == 1.0d) numOnStates++;
            }
            if (numOffStates >= hiddenStates.length || numOnStates >= hiddenStates.length) unusedStates++;
        }

        // counts the number of duplicate rows in the hidden states
        int numDuplicateHiddenStates = 0;
        for (int r1 = 0; r1 < hiddenStates.length; r1++) {
            double[] currentRow = hiddenStates[r1];
            for (int r2 = r1 + 1; r2 < hiddenStates.length; r2++) {
                if (Arrays.equals(currentRow, hiddenStates[r2])) {
                    numDuplicateHiddenStates++;
                    break;
                }
            }
        }

        double percent = 100.0d / (double)visibleUnits * numDuplicateHiddenStates;
        double contrast = Utilities.getContrastRatio(recView);
        System.out.println("Number of duplicate output vectors: " + numDuplicateHiddenStates + " of " + visibleUnits + " (" + percent + " %)");
        System.out.println("Used States: " + (100.0d - percent) + " %");
        System.out.println("Number of unused states: " + unusedStates);
        System.out.println("Contrast Ratio: " + contrast);
        System.out.println("mean of srcView: " + getMean(srcView));
        System.out.println("mean of recView: " + getMean(recView));
        /*
        System.out.println("Hidden States:");
        for (double[] row : codedImg.getData()) {
            System.out.println(Arrays.toString(row));
        }
        */
    }

    private void loadRBM() {

        File rbmFile;
        JFileChooser chooser = new JFileChooser("/");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("RBM File", "rbm");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) {
            //imgName = chooser.getSelectedFile().getName();
            rbmFile = chooser.getSelectedFile();
        } else {
            System.err.println("No File opened.");
            rbmFile = null;
        }

        try {
            FileInputStream fin = new FileInputStream(rbmFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            rbm = (RBM) ois.readObject();
            ois.close();
        } catch(Exception ex) {
            System.err.println("Couldnt load weights for RBM !");
            System.exit(0);
        }
    }

    private void learnSafeRBM() {
        rbm.train(reducedVectors);

        try {
            File file = new File("/", " " + imgName + " v:" + Math.sqrt(rbm.getNumVisible()) + "-h:" + rbm.getNumHidden() + "-LR:" + rbm.getLearningRate() + "-E:" + rbm.getMaxEpochs() + "-b:" + rbm.getBias() + ".rbm");
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rbm);
            oos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int[] generatePixelArray(double[][] codedImage, int w, int h, int rbmNumBlockX, int rbmNumBlockY) {
        int[] returnPixels = new int[w * h];

        for (int yBlock = 0; yBlock < rbmNumBlockY; yBlock++) {
            for (int y = 0; y < rbmBlockSide; y++) {
                for (int xBlock = 0; xBlock < rbmNumBlockX; xBlock++) {
                    for (int x = 0; x < rbmBlockSide; x++) {

                        int pos = this.getPos(w, xBlock, yBlock, x, y);
                        int posBlock = y * rbmBlockSide + x;
                        int indexBlock = yBlock * rbmNumBlockX + xBlock;

                        int val = 0xFF000000 + (((int)codedImage[indexBlock][posBlock] & 0xff) << 16) + (((int)codedImage[indexBlock][posBlock] & 0xff) << 8) + ((int)codedImage[indexBlock][posBlock] & 0xff);
                        returnPixels[pos] = val;
                    }
                }
            }
        }
        return returnPixels;
    }

    private int getPos(int w, int xBlock, int yBlock, int x, int y) {
        // y * w + x
        int pos = (yBlock * rbmBlockSide + y) * w + xBlock * rbmBlockSide + x;
        return pos;
    }

    private double[][] extendVector(double[][] inputVec) {
        for(int i = 0; i < inputVec.length; i++) {
            for(int j = 0; j < inputVec[i].length; j++) {
                inputVec[i][j] = inputVec[i][j] * 255.0d;
            }
        }
        return inputVec;
    }

    ///////////////////////////////////
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser("/Users/Bernhard/Dropbox/HTW Berlin/Bachelor/code/Bachelorarbeit RBM/data/batch");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif", "codebook");
        chooser.setFileFilter(filter);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) {
            imgName = chooser.getSelectedFile().getName();
            return chooser.getSelectedFile();
        } else {
            System.out.println("No File opened.");
            return null;
        }
	}
	
	private void loadFile(File file) {
		if(file != null) {
            srcView.loadImage(file);
    		makeGray(srcView);
    		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
    		// create empty destination images
    		recView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
    		frame.pack();
		}
	}

    private void openFolder() {

        JFileChooser chooser = new JFileChooser("/Users/Bernhard/Dropbox/HTW Berlin/Bachelor/code/Bachelorarbeit RBM/data");

        // Nur komplette Ordner koennen ausgewaehlt werden
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        String fileNames = "";

        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) {
            File[] folders = chooser.getSelectedFiles();

            for (File folder : folders) {
                File[] files = folder.listFiles();
                for (File file : files) {
                    fileNames += file.getName();
                }
            }

            double[][] inputVec = loadFolder(folders);
            reducedVectors = new Matrix(inputVec.length, inputVec[0].length);
            reducedVectors.setData(inputVec);
            reducedVectors = reducedVectors.divide(255.0d);
            imgName = "batch10";
            learnSafeRBM();
        } else {
            System.out.println("No File opened.");
        }
/*
        try {
            File file = new File("/", " " + "batch10" + " v:" + Math.sqrt(rbm.getNumVisible()) + "-h:" + rbm.getNumHidden() + "-LR:" + rbm.getLearningRate() + "-E:" + rbm.getMaxEpochs() + ".rbm");
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rbm);
            oos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    */
    }

    private double[][] loadFolder(File[] folders) {

        ImageView loadView;
        ImgAsVectors loadVec;
        double[][] vectors = new double[0][visibleUnits];

        for (File folder : folders) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (!file.getName().equals(".DS_Store")) {
                    loadView = new ImageView(file);
                    loadVec = new ImgAsVectors(loadView, rbmBlockSide);
                    double[][] currentVec = loadVec.getVectorsForRBM();
                    int vectorsLength = vectors.length;
                    vectors = Arrays.copyOf(vectors, vectorsLength + currentVec.length);
                    System.arraycopy(currentVec, 0, vectors, vectorsLength, currentVec.length);
                }
            }
        }
        return vectors;
    }
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("RBM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new Main();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

	public void makeGray(ImageView imgView) {
		int pixels[] = imgView.getPixels();
		for(int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			pixels[i] = 0xff000000 | (gray << 16) | (gray << 8) | gray;
		}
		imgView.applyChanges();
	}


    private int[] generateNegativeImage(int[] pixels1, int[] pixels2) {
        int[] negPixels = new int[pixels1.length];
        double mean = 0.0d;
        for (int i = 0; i < pixels1.length; i++) {
            int negVal = (pixels1[i] & 0xff) - (pixels2[i] & 0xff) + 128;
            System.out.println();
            if (negVal > 255)
                negVal = 255;
            if (negVal < 0)
                negVal = 0;
            mean += negVal;
            negPixels[i] = 0xFF000000 | (negVal << 16) | (negVal << 8) | negVal;
        }
        mean /= pixels1.length;
        System.out.println("---------------------------");
        System.out.println("Mean of negView: " + mean);
        return negPixels;
    }

    private double getMean(ImageView view) {
        int[] pixels = view.getPixels();
        double mean = 0.0d;
        for (int i = 0; i < pixels.length; i++) {
            int val = (pixels[i] & 0xff);
            mean += val;
        }
        mean /= pixels.length;
        return mean;
    }
}
