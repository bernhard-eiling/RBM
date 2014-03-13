package eiling.rbm;

import java.io.Serializable;

/**
 * Created by Bernhard on 02.02.14.
 */
public class Matrix implements Serializable {

    double[][] data;
    int rows;
    int cols;

    public Matrix(int r, int c) {
        this.data = new double[r][c];
        this.rows = r;
        this.cols = c;
    }

    public Matrix(double[][] d) {
        this.data = d;
        this.rows = d.length;
        this.cols = d[0].length;
    }

    Matrix transpose() {
        Matrix output = new Matrix(this.getNumCols(), this.getNumRows());

        for (int r = 0; r < getNumRows(); r++) {
            for (int c = 0; c < getNumCols(); c++) {
                output.setEntry(c, r, this.getEntry(r, c));
            }
        }
        return output;
    }

    Matrix multiply(Matrix matrix) {
/*
        if ( this.getNumCols() != matrix.getNumRows()) {
            System.err.println("Multi: Dimensions of rows and cols dont fit.");
            System.exit(1);
        }
*/
        Matrix product = new Matrix(this.getNumRows(), matrix.getNumCols());
        double sum = 0;

        for (int r1 = 0; r1 < this.getNumRows(); r1++) {
            for (int c2 = 0; c2 < matrix.getNumCols(); c2++) {
                for (int r2 = 0; r2 < matrix.getNumRows(); r2++) {
                    sum += data[r1][r2] * matrix.getEntry(r2, c2);
                }
                product.setEntry(r1, c2, sum);
                sum = 0;
            }
        }
        return product;
    }

    Matrix add(Matrix m) {
        Matrix returnMatrix = new Matrix(this.getNumRows(), this.getNumCols());
        if (this.getNumRows() != m.getNumRows() || this.getNumCols() != m.getNumCols()) {
            System.err.println("Add: Dimensions of rows and cols dont fit.");
            System.exit(1);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    double val = this.getEntry(r, c) + m.getEntry(r, c);
                    returnMatrix.setEntry(r, c, val);
                }
            }
        }
        return returnMatrix;
    }

    Matrix subtract(Matrix m) {
        Matrix returnMatrix = new Matrix(this.getNumRows(), this.getNumCols());
        if (this.getNumRows() != m.getNumRows() || this.getNumCols() != m.getNumCols()) {
            System.err.println("Subtract: Dimensions of rows and cols dont fit.");
            System.exit(1);
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    double val = this.getEntry(r, c) - m.getEntry(r, c);
                    returnMatrix.setEntry(r, c, val);
                }
            }
        }
        return returnMatrix;
    }

    Matrix subtract(double v) {
        Matrix returnMatrix = new Matrix(this.getNumRows(), this.getNumCols());
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double val = this.getEntry(r, c) - v;
                returnMatrix.setEntry(r, c, val);
            }
        }
        return returnMatrix;
    }

    Matrix scalarMultiply(double v) {
        Matrix returnMatrix = new Matrix(this.getNumRows(), this.getNumCols());
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double val = this.getEntry(r, c) * v;
                returnMatrix.setEntry(r, c, val);
            }
        }
        return returnMatrix;
    }

    Matrix divide(double v) {
        Matrix returnMatrix = new Matrix(this.getNumRows(), this.getNumCols());
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double val = this.getEntry(r, c) / v;
                returnMatrix.setEntry(r, c, val);
            }
        }
        return returnMatrix;
    }

    void setSubMatrix(int r, int c, Matrix m) {
        for (int row = 0; row < m.getNumRows(); row++) {
            for (int col = 0; col < m.getNumCols(); col++) {
                double val = m.getEntry(row, col);
                this.setEntry(row + r, col + c, val);
            }
        }
    }

    // end row and col are NOT included in submatrix
    Matrix getSubMatrix(int startRow, int endRow, int startCol, int endCol) {
        Matrix returnMatrix = new Matrix(endRow - startRow, endCol - startCol);
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                double val = this.getEntry(r, c);
                returnMatrix.setEntry(r - startRow, c - startCol, val);
            }
        }
        return returnMatrix;
    }

    void randomizeMinusOneOne() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = (Math.random() * 2.0d) - 1.0d;
            }
        }
    }

    void randomizeZeroOne() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = Math.random();
            }
        }
    }

    void setRow(int r, double val) {
        for (int c = 0; c < cols; c++) {
            data[r][c] = val;
        }
    }

    void setCol(int c, double val) {
        for (int r = 0; r < rows; r++) {
            data[r][c] = val;
        }
    }

    void setEntry(int r, int c, double val) {
        data[r][c] = val;
    }

    void setAll(double val) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                data[r][c] = val;
            }
        }
    }

    double sum() {
        double sum = 0;
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                sum += this.getEntry(r, c);
            }
        }
        return sum;
    }

    void multiAll(double val) {
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                data[r][c] *= val;
            }
        }
    }

    int getNumRows() {
        return this.rows;
    }

    int getNumCols() {
        return this.cols;
    }

    double getEntry(int r, int c) {
        return this.data[r][c];
    }

    double[][] getData() {
        return this.data;
    }

    void setData(double[][] d) {
        try {
            this.data = d;
        } catch (Exception e) {

            System.err.println("data is not fitting matrix.");
        }
        /*
        if (this.getNumRows() != d.length || this.getNumCols() != d.length) {
            System.err.println("Num of Rows and Cols not fitting.");
            System.exit(1);
        } else {
            this.data = d;
        }
        */
    }

}
