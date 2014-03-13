package eiling.rbm;

import java.io.Serializable;
import java.util.Random;

public class RBM implements Serializable {
	
	int numVisible;
	int numHidden;
	double learningRate;
    double bias = 1.0d;
    int maxEpochs;
    Matrix weights;
    Random random;
	
	public RBM(int numVisible, int numHidden, double learningRate, int epochs) {
		this.numVisible = numVisible;
		this.numHidden = numHidden;
		this.learningRate = learningRate;
        this.maxEpochs = epochs;

        long seed = (long) (1000*Math.random());
        random = new Random(seed);

        weights = new Matrix(numVisible + 1, numHidden + 1);
        // weights initalize with small positiv and negativ numbers

        // BARTHEL TEST
        for (int r = 0; r < weights.getNumRows(); r++) {
            for (int c = 0; c < weights.getNumCols(); c++) {
                double val = random.nextGaussian();
                weights.setEntry(r, c, 0.01 * val);
            }
        }
        /*
        weights.randomizeMinusOneOne();
        weights.multiAll(0.1d);
        */

        // filling first row and col with bias of 0.0
       // weights.setRow(0, 0.0d);
       // weights.setCol(0, 0.0d);
        weights.setRow(0, bias);
        weights.setCol(0, bias);
	}

	void train(Matrix dataTemp) {

		int numExamples = dataTemp.getNumRows(); // abzahl der datensets zum anlernen

		// set bias unit of 1.0 in extra first col of data
        Matrix data = new Matrix(dataTemp.getNumRows(), dataTemp.getNumCols() + 1);
	    data.setSubMatrix(0, 1, dataTemp);

        // remove mean of all vectors
       // double mean = getMeanOfData(dataTemp.getData());
       // data = data.subtract(mean);
        data.setCol(0, bias);

		for (int i = 0; i < maxEpochs; i++) {
			
			////////////////
			// POSITIV PHASE
			////////////////
			Matrix posHiddenActivations = data.multiply(weights); // activation energy

			Matrix posHiddenProbs = logisticsForMatrix(posHiddenActivations);

            Matrix posHiddenStates = new Matrix(posHiddenProbs.getNumRows(), posHiddenProbs.getNumCols());
			Matrix randomMatrix = new Matrix(numExamples, numHidden + 1);
            randomMatrix.randomizeZeroOne();

			for (int r = 0; r < randomMatrix.getNumRows(); r++) {
				for (int c = 0; c < randomMatrix.getNumCols(); c++) {
                    // double state = posHiddenProbs.getEntry(r,  c) > random.nextDouble() ? 1.0 : 0.0;
                    double state = posHiddenProbs.getEntry(r,  c) > randomMatrix.getEntry(r, c) ? 1.0 : 0.0;
                    posHiddenStates.setEntry(r, c, state);
				}
			}

            // Barthel test
           	Matrix posAssociations = data.transpose().multiply(posHiddenProbs);
            //Matrix posAssociations = data.transpose().multiply(posHiddenStates);

			/////////////////
			// NEGATIVE PHASE
			/////////////////
			// aufsummierte Kantengewichte/Aktivierungsenergie der aktivierten hidden units
			Matrix negVisibleActivations = posHiddenStates.multiply(weights.transpose());

            // hier neue reconstruction, irgendwas mit logistics ?

			
			// geklemmte Aktivierungsenergie (0..1)
			Matrix negVisibleProbs = logisticsForMatrix(negVisibleActivations);

            negVisibleProbs.setCol(0, bias); // fix bias unit

            // oder hier :)

			Matrix negHiddenActivations = negVisibleProbs.multiply(weights);

            // oder hier :)

			Matrix negHiddenProbs = logisticsForMatrix(negHiddenActivations);

			Matrix negAssociations = negVisibleProbs.transpose().multiply(negHiddenProbs);

			double numExamplesInverse = 1.0d / numExamples;
            Matrix dif = posAssociations.subtract(negAssociations);
            Matrix difNumEx = dif.scalarMultiply(numExamplesInverse);
            Matrix difLearningRate = difNumEx.scalarMultiply(learningRate);
			weights = weights.add(difLearningRate);

            Matrix errorMatrix = data.subtract(negVisibleProbs);
			double error = errorMatrix.sum() * errorMatrix.sum();
            error = Math.sqrt(error);
			System.out.println("Step: " + i + " | Error: " + error + "\n");
		}
	}


	public Matrix code(Matrix dataTemp) {

		int numExamples = dataTemp.getNumRows();

        Matrix hiddenStates = new Matrix(numExamples, numHidden + 1);
        hiddenStates.setAll(1.0d);

        Matrix data = new Matrix(dataTemp.getNumRows(), dataTemp.getNumCols() + 1);

		data.setSubMatrix(0, 1, dataTemp);
        data.setCol(0, bias);
		
		Matrix hiddenActivations = data.multiply(weights);

		Matrix hiddenProbs = logisticsForMatrix(hiddenActivations);

        // checks if prob is over or under mean brightness
        for (int r = 0; r < hiddenProbs.getNumRows(); r++) {
            for (int c = 0; c < hiddenProbs.getNumCols(); c++) {
                double state = hiddenProbs.getEntry(r,  c) > 0.5 ? 1.0 : 0.0;
                //double state = hiddenProbs.getEntry(r,  c) > random.nextDouble() ? 1.0 : 0.0;
                hiddenStates.setEntry(r, c, state);
            }
        }

		// cut of bias units
		hiddenStates = hiddenStates.getSubMatrix(0, hiddenStates.getNumRows(), 1, hiddenStates.getNumCols());

		return hiddenStates;
	}

	public Matrix decode(Matrix dataTemp) {
		
		int numExamples = dataTemp.getNumRows();

        Matrix visibleStates = new Matrix(numExamples, numVisible + 1);
        visibleStates.setAll(1.0d);

        Matrix data = new Matrix(numExamples, dataTemp.getNumCols() + 1);

        data.setSubMatrix(0, 1, dataTemp);
        data.setCol(0, bias);

        Matrix weightsT = weights.transpose();

        Matrix visibleActivations = data.multiply(weightsT);

        Matrix visibleProbs = logisticsForMatrix(visibleActivations);

		visibleStates = visibleProbs;

		// cut of bias units
        visibleStates = visibleStates.getSubMatrix(0, visibleStates.getNumRows(), 1, visibleStates.getNumCols());
		
		//System.out.println(visibleStates);

		return visibleStates;
	}

    private Matrix logisticsForMatrix(Matrix m) {
        Matrix returnMatrix = new Matrix(m.getNumRows(), m.getNumCols());
        for (int r = 0; r < m.getNumRows(); r ++) {
            for (int c = 0; c < m.getNumCols(); c++) {
                double val = this.logistics(m.getEntry(r, c));
                returnMatrix.setEntry(r, c, val);
            }
        }
        return returnMatrix;
    }
	
	private double logistics(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}

    double getMeanOfData(double[][] data) {
        double mean = 0.0;
        for (double[] vector : data) {
            for (double val : vector) {
                mean += val;
            }
        }
        mean /= data.length * data[0].length;
        return mean;
    }

    double[] removeMeanFromVector(double[] vector) {
        double mean = 0.0;
        for (double val : vector) {
            mean += val;
        }
        mean /= vector.length;
        for (int i = 0; i < vector.length; i++) {
            vector[i] -= mean;
        }
        return vector;
    }

    public int getNumVisible() {
        return this.numVisible;
    }

    public int getNumHidden() {
        return this.numHidden;
    }

    public double getLearningRate() {
        return this.learningRate;
    }

    public int getMaxEpochs() {
        return maxEpochs;
    }

    public double getBias() {
        return this.bias;
    }

    public Matrix getWeights() {
        return this.weights;
    }
    public void setWeights(Matrix weights) {
        this.weights = weights;
    }
}
