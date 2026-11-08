package engine.neural.optimizers;

import java.util.*;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;
import engine.neural.*;
import engine.neural.util.Matrix;
import engine.neural.util.Vector;


public class SGDMomentum implements Optimizer {
	
	private final float learningRate, momentum;
	
	/**
	 * Store velocity matrix based off of which matrix is given
	 */
	private IdentityHashMap<Matrix, Matrix> velocityWeights = new IdentityHashMap<>();
	/**
	 * Store velocity matrix based off of which vector is given
	 */
	private IdentityHashMap<Vector, Vector> velocityBiases = new IdentityHashMap<>();
	/**
	 * Stores deserialized weights
	 */
	private Queue<Matrix> tempWeights = new ArrayDeque<>();
	/**
	 * Stores deserialized biases
	 */
	private Queue<Vector> tempBiases = new ArrayDeque<>();

	public SGDMomentum(float learningRate, float momentum) {
		this.learningRate = learningRate;
		this.momentum = momentum;
	}

	@Override
	// momentum * velocity - learningRate * gradient
	public void updateWeights(Matrix weights, Matrix gradient) {
		// Adds old weights if they exist or makes new ones
		Matrix velocity = this.velocityWeights.computeIfAbsent(
			weights, m -> this.tempWeights.isEmpty()? 
				new Matrix(weights.getWid(), weights.getHgt()) :
				this.tempWeights.poll()
		); // Scale velocity matrix by momentum
		velocity.scaleMatrix(this.momentum);
		// Scale gradient by learning rate
		Matrix temp = gradient.copy();
		temp.scaleMatrix(this.learningRate);
		// Subtract gradient from velocity
		velocity.subMatrix(temp.getMatrix());
		// Add velocity to weights
		weights.addMatrix(velocity.getMatrix());
	}

	@Override
	// momentum * velocity - learningRate * delta
	public void updateBiases(Vector biases, Vector delta) {
		Vector velocity = this.velocityBiases.computeIfAbsent(
			biases, v -> this.tempBiases.isEmpty()?
				new Vector(biases.getHgt()) :
				this.tempBiases.poll()
		); // Scale velocity vector by momentum
		velocity.scaleMatrix(this.momentum);
		// Scale delta by learning rate
		Vector temp = delta.copy();
		temp.scaleMatrix(this.learningRate);
		// Subtract delta from velocity
		velocity.subMatrix(temp.getMatrix());
		// Add velocity to biases
		biases.addMatrix(velocity.getMatrix());
	}

	@Override
	public byte[] serialize() {
		return ByteHelper.mergeBytes(
			ByteHelper.toBytes(this.learningRate),
			ByteHelper.toBytes(this.momentum),
			toBytes(this.velocityWeights),
			toBytes(this.velocityBiases)
		);
	}
	
	public static <T extends Matrix> byte[] toBytes(IdentityHashMap<T, T> i) {
		byte[] bytes = ByteHelper.toBytes(i.size());
		for (Map.Entry<T, T> entry : i.entrySet()) {
			bytes = ByteHelper.mergeBytes(bytes, ByteHelper.toBytes(entry.getValue()));
		} return bytes;
	}

	@Override
	public Optimizer deserialize(ByteHelper bytes, FastSerializable<?>... prototypes) {
		SGDMomentum s = new SGDMomentum(bytes.readFloat(), bytes.readFloat());
		s.tempWeights = new ArrayDeque<>(Arrays.asList(bytes.readObjArr(new Matrix(0, 0))));
		s.tempBiases = new ArrayDeque<>(Arrays.asList((Vector[]) bytes.readObjArr(new Vector(0))));
		return s;
	}

	@Override
	public Optimizer[] getProtoArray(int length) {
		return new SGDMomentum[length];
	}

}
