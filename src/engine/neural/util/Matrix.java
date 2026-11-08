package engine.neural.util;

import java.util.Iterator;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;

public class Matrix implements Iterable<Float>, FastSerializable<Matrix> {
	
	private float[] matrix;
	private int wid, hgt;
	
	// Constructors
	
	public Matrix() {
		this.matrix = new float[] {};
		this.wid = 0; this.hgt = 0;
	}

	public Matrix(int wid, int hgt) {
		this.matrix = new float[wid*hgt];
		this.wid = wid; this.hgt = hgt;
	}
	
	private Matrix(float[] matrix, int wid, int hgt) {
		this.matrix = matrix.length == wid * hgt? matrix : new float[wid*hgt];
		this.wid = wid; this.hgt = hgt;
	}
	
	public void propagate() {
		for (int i = 0; i < this.matrix.length; i++) {
			this.matrix[i] = (float) (Math.random() * 2 - 1);
		}
	}
	
	public void propagate(float scale) {
		for (int i = 0; i < this.matrix.length; i++) {
			this.matrix[i] = (float) ((Math.random() * 2 - 1) * scale);
		}
	}
	
	public Matrix multiply(Matrix b) {
		float[] prod = new float[this.hgt * b.getWid()];
		if (b.getHgt() == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				for (int j = 0; j < this.hgt; j++) {
					for (int k = 0; k < b.wid; k++) {
						prod[j*b.getWid() + k] += this.matrix[i + j*this.wid] * b.get(i, k);
					}
				}
			} return new Matrix(prod, b.getWid(), this.hgt);
		} return null;
	}
	
	public Vector multiply(Vector b) {
		float[] prod = new float[this.hgt];
		if (b.getHgt() == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				for (int j = 0; j < this.hgt; j++) {
					prod[j] += this.matrix[i + j*this.wid] * b.get(i);
				}
			} return new Vector(prod);
		} return null;
	}
	
	public Matrix elementWise(Matrix b) {
		if (this.wid == b.getWid() && this.hgt == b.getHgt()) {
			float[] prod = new float[this.matrix.length];
			for (int i = 0; i < this.matrix.length; i++) {
				prod[i] = this.matrix[i]*b.getMatrix()[i];
			} return new Matrix(prod, this.wid, this.hgt);
		} return null;
	}
	
	public Matrix elementWise(float[] b) {
		if (this.matrix.length == b.length) {
			float[] prod = new float[this.matrix.length];
			for (int i = 0; i < this.matrix.length; i++) {
				prod[i] = this.matrix[i]*b[i];
			} return new Matrix(prod, this.wid, this.hgt);
		} return null;
	}
	
	public Matrix getTranspose() {
		Matrix trans = new Matrix(this.hgt, this.wid);
		for (int i = 0; i < this.hgt; i++) {
			trans.setCol(i, this.getRow(i).getMatrix());
		} return trans;
	}
	
	// Basic operations
	
	public Matrix scaleMatrixReturn(float scale) {
		Matrix copy = new Matrix(this.wid, this.hgt);
		for (int i = 0; i < this.matrix.length; i++) {
			copy.set(i, this.matrix[i] * scale);
		} return copy;
	}
	
	public void scaleMatrix(float scale) {
		for (int i = 0; i < this.matrix.length; i++) {
			this.matrix[i] *= scale;
		}
	}
	
	public void scaleRow(int row, float scale) {
		for (int i = 0; i < this.wid; i++) {
			this.matrix[row*this.wid + i] *= scale;
		}
	}
	
	public void scaleCol(int col, float scale) {
		for (int i = 0; i < this.hgt; i++) {
			this.matrix[i*this.wid + col] *= scale;
		}
	}
	
	public boolean scaleRow(int row, float[] scale) {
		if (scale.length == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				this.matrix[row*this.wid + i] *= scale[i];
			} return true;
		} return false;
	}
	
	public boolean scaleCol(int col, float[] scale) {
		if (scale.length == this.hgt) {
			for (int i = 0; i < this.hgt; i++) {
				this.matrix[i*this.wid + col] *= scale[i];
			} return true;
		} return false;
	}
	
	public void scale(int row, int col, float scale) {
		this.matrix[col + row*this.wid] *= scale;
	}
	
	public void scale(int num, float scale) {
		this.matrix[num] *= scale;
	}
	
	public boolean swapRow(int rowA, int rowB) {
		if (rowA < this.hgt && rowB < this.hgt) {
			float[] row1 = this.getRow(rowA).getMatrix();
			this.setRow(rowA, this.getRow(rowB).getMatrix());
			this.setRow(rowB, row1); return true;
		} return false;
	}
	
	public boolean swapCol(int colA, int colB) {
		if (colA < this.wid && colB < this.wid) {
			float[] col1 = this.getCol(colA).getMatrix();
			this.setCol(colA, this.getCol(colB).getMatrix());
			this.setCol(colB, col1); return true;
		} return false;
	}
	
	// Get
	
	public int getLength() {return this.matrix.length;}
	
	public float get(int num) {return this.matrix[num];}
	
	public float get(int row, int col) {return this.matrix[col + row*this.wid];}
	
	public Vector getRow(int row) {
		float[] rowL = new float[this.wid];
		for (int i = 0; i < this.wid; i++) {
			rowL[i] = this.matrix[row*this.wid + i];
		} return new Vector(rowL);
	}
	
	public Vector getCol(int col) {
		float[] colL = new float[this.hgt];
		for (int i = 0; i < this.hgt; i++) {
			colL[i] = this.matrix[i*this.wid + col];
		} return new Vector(colL);
	}
	
	public float[] getMatrix() {return this.matrix;}
	public int getWid() {return this.wid;}
	public int getHgt() {return this.hgt;}
	
	// Set
	
	public void set(int row, int col, float payload) {this.matrix[col + row*this.wid] = payload;}
	
	public void set(int num, float payload) {this.matrix[num] = payload;}
	
	public boolean setRow(int row, float[] set) {
		if (set.length == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				this.matrix[row*this.wid + i] = set[i];
			} return true;
		} return false;
	}
	
	public boolean setCol(int col, float[] set) {
		if (set.length == this.hgt) {
			for (int i = 0; i < this.hgt; i++) {
				this.matrix[i*this.wid + col] = set[i];
			} return true;
		} return false;
	}
	
	public void setRow(int row, float set) {
		for (int i = 0; i < this.wid; i++) {
			this.matrix[row*this.wid + i] = set;
		}
	}
	
	public void setCol(int col, float set) {
		for (int i = 0; i < this.hgt; i++) {
			this.matrix[i*this.wid + col] = set;
		}
	}
	
	public boolean setMatrix(float[] set) {
		if (set.length == this.matrix.length) {
			this.matrix = set; return true;
		} return false;
	}
	
	public void setMatrix(float set) {
		for (int i = 0; i < this.matrix.length; i++) {
			this.matrix[i] = set;
		}
	}
	
	// Add
	
	public boolean addRow(int row, float[] add) {
		if (add.length == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				this.matrix[row*this.wid + i] += add[i];
			} return true;
		} return false;
	}
	
	public boolean addCol(int col, float[] add) {
		if (add.length == this.hgt) {
			for (int i = 0; i < this.hgt; i++) {
				this.matrix[i*this.wid + col] += add[i];
			} return true;
		} return false;
	}
	
	public void addRow(int row, float add) {
		for (int i = 0; i < this.wid; i++) {
			this.matrix[row*this.wid + i] += add;
		} 
	}
	
	public void addCol(int col, float add) {
		for (int i = 0; i < this.hgt; i++) {
			this.matrix[i*this.wid + col] += add;
		}
	}
	
	public boolean addMatrix(float[] add) {
		if (add.length == this.matrix.length) {
			for (int i = 0; i < this.matrix.length; i++) {
				this.matrix[i] += add[i];
			} return true;
		} return false;
	}
	
	public void addMatrix(float add) {
		for (int i = 0; i < this.matrix.length; i++) {
			this.matrix[i] += add;
		}
	}
	
	public boolean broadcast(float[] b) {
		if (this.hgt == b.length) {
			for (int i = 0; i < this.hgt; i++) {
				for (int j = 0; j < this.wid; j++) {
					this.matrix[j + i*this.wid] += b[i];
				}
			} return true;
		} return false;
	}
	
	public void add(int row, int col, float payload) {this.matrix[col + row*this.wid] += payload;}
	
	// Sub
	
	public boolean subRow(int row, float[] sub) {
		if (sub.length == this.wid) {
			for (int i = 0; i < this.wid; i++) {
				this.matrix[row*this.wid + i] -= sub[i];
			} return true;
		} return false;
	}
	
	public boolean subCol(int col, float[] sub) {
		if (sub.length == this.hgt) {
			for (int i = 0; i < this.hgt; i++) {
				this.matrix[i*this.wid + col] -= sub[i];
			} return true;
		} return false;
	}
	
	public boolean subMatrix(float[] sub) {
		if (sub.length == this.matrix.length) {
			for (int i = 0; i < this.matrix.length; i++) {
				this.matrix[i] -= sub[i];
			} return true;
		} return false;
	}
	
	// To string and helper function
	
	public Matrix copy() {
		return new Matrix(this.matrix.clone(), this.wid, this.hgt);
	}
	
	public Vector toVector() {
		return new Vector(this.matrix);
	}
	
	@Override
	public String toString() {
		String self = "";
		for (int i = 0; i < this.matrix.length; i++) {
			self += this.format(matrix[i]);
			if ((i+1) % this.wid == 0) {
				self+="\n\n";
			} 
		} return self;
	}
	
	private String format(float num) {
		String numS = String.format("%.5f", num);
		int len = Math.max(13-numS.length(), 0);
		numS = String.format("%."+len+"f", num);
		return len == 0? numS + "\t" : numS + "\t";
	}

	@Override
	public Iterator<Float> iterator() {
		return new Iterator<Float>() {
			
			private int index = 0;

			@Override
			public boolean hasNext() {return index < matrix.length;}

			@Override
			public Float next() {
				if (!this.hasNext()) {throw new IndexOutOfBoundsException();}
				return matrix[this.index++];
			}
		};
	}

	@Override
	public byte[] serialize() {
		return ByteHelper.mergeBytes(
			ByteHelper.toBytes(this.matrix),
			ByteHelper.toBytes(this.wid),
			ByteHelper.toBytes(this.hgt)
		);
	}

	@Override
	public Matrix deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
		return new Matrix(
			b.readFloatArr(),
			b.readInt(),
			b.readInt()
		);
	}

	@Override
	public Matrix[] getProtoArray(int length) {
		return new Matrix[length];
	}

}
