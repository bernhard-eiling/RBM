package eiling.rbm;


public class ImgAsVectors {
	
	private ImageView imageView;
	private int blockSize;
	private int vectorSize;
	private int numBlocksY;
	private int numBlocksX;
    private int numBlocks;
//	private ArrayList<ArrayList<int[]>> imageVectors = new ArrayList<ArrayList<int[]>>(); 
//	private ArrayList<ArrayList<Integer>> vectorMeans = new ArrayList<ArrayList<Integer>>();
	private int[][][] imageVectors;
    private double[][] imageVectorsRBM;
	private int[][] vectorMeans;

	public ImgAsVectors(ImageView imageView, int blockSide) {
		
		this.imageView = imageView;
		this.blockSize = blockSide;
		this.vectorSize = blockSide * blockSide;
		numBlocksX = imageView.getImgWidth() / blockSide;
		numBlocksY = imageView.getImgHeight() / blockSide;
        numBlocks = numBlocksX * numBlocksY;
		//imageVectors = new int[numBlocksY][numBlocksX][vectorSize];
        imageVectorsRBM = new double[numBlocks + 1][vectorSize];
		vectorMeans = new int[numBlocksY][numBlocksX];
		
		// init imageVectors / vectorMeans
//		for (int y = 0; y <= numBlocksY; y++) {			
//			imageVectors.add(y, new ArrayList<int[]>());
//			vectorMeans.add(y, new ArrayList<Integer>());
//			for (int x = 0; x <= numBlocksX; x++) {
//				imageVectors.get(y).add(x, new int[vectorSize]);
//				vectorMeans.get(y).add(x, -1);
//			}
//		}
		//this.vectorizeImage();
        this.vectorizeImageRBM();
	}

	private void vectorizeImage() {
		// loop blocks
		int pixels[] = imageView.getPixels();
		for (int yBlock = 0; yBlock < numBlocksY; yBlock++) {
			for (int xBlock = 0; xBlock < numBlocksX; xBlock++) {
				int vectorIndex = 0;
				// iterate block
				for (int y = 0; y < blockSize; y++) {
					for (int x = 0; x < blockSize; x++) {
						// index in imageView
						int pos = this.getPos(imageView.getImgWidth(), xBlock, yBlock, x, y);
						// write pixel in vector
						// WORKS ONLY WITH GREY IMG !!!
//						System.out.println("(pixels[pos] & 0xff): " + (pixels[pos] & 0xff));
						imageVectors[yBlock][xBlock][vectorIndex] = (pixels[pos] & 0xff);
						vectorIndex++;
					}
				}
			}
		}
	}

    private void vectorizeImageRBM() {
        // loop blocks
        int pixels[] = imageView.getPixels();
        int blockIndex = 0;
        for (int yBlock = 0; yBlock < numBlocksY; yBlock++) {
            for (int xBlock = 0; xBlock < numBlocksX; xBlock++) {
                int vectorIndex = 0;
                // iterate block
                for (int y = 0; y < blockSize; y++) {
                    for (int x = 0; x < blockSize; x++) {
                        // index in imageView
                        int pos = this.getPos(imageView.getImgWidth(), xBlock, yBlock, x, y);
                        // write pixel in vector
                        // WORKS ONLY WITH GREY IMG !!!
//						System.out.println("(pixels[pos] & 0xff): " + (pixels[pos] & 0xff));
                        imageVectorsRBM[blockIndex][vectorIndex] = (pixels[pos] & 0xff);
                        /*
                        System.out.println("pos: " + pos);
                        System.out.println("blockIndex: " + blockIndex);
                        System.out.println("vectorIndex: " + vectorIndex + "\n");
                        */
                        vectorIndex++;
                    }
                }
                blockIndex++;
            }
        }
    }

	////////////////////////7
	
	private int getPos(int w, int xBlock, int yBlock, int x, int y) {
		// y * w + x
		int pos = (yBlock * blockSize + y) * w + xBlock * blockSize + x;
		return pos;
	}

	public int[][][] getVectors() {
		return this.imageVectors;
	}

    public double[][] getVectorsForRBM() {
        return this.imageVectorsRBM;
    }
}
