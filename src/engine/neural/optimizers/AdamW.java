package engine.neural.optimizers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Queue;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;
import engine.neural.Optimizer;
import engine.neural.util.Matrix;
import engine.neural.util.Vector;

public class AdamW implements Optimizer {
	public static final float EPSILON = 0.00001f;
	
	private IdentityHashMap<Matrix, Matrix> frstMomentWght = new IdentityHashMap<>(), scndMomentWght = new IdentityHashMap<>();
	private IdentityHashMap<Vector, Vector> frstMomentBias = new IdentityHashMap<>(), scndMomentBias = new IdentityHashMap<>();
	
	private Queue<Matrix> tempfrstWght = new ArrayDeque<>(), tempscndWght = new ArrayDeque<>();
	private Queue<Vector> tempfrstBias = new ArrayDeque<>(), tempscndBias = new ArrayDeque<>();
	
	private float learningRate, wghtDecay, frstDecay = 0.9f, scndDecay = 0.999f; 
	private int totalLayers, currLayer = 0, step = 0;

	public AdamW(float learningRate, float wghtDecay, int[] layers) {
		this.learningRate = learningRate; this.wghtDecay = wghtDecay; totalLayers = layers.length - 1;
	}
	
	private AdamW(float learningRate, float wghtDecay, int layers) {
		this.learningRate = learningRate; this.wghtDecay = wghtDecay; totalLayers = layers;
	}

	@Override
	public void updateWeights(Matrix weights, Matrix gradient) {
		Matrix frstMoment = this.frstMomentWght.computeIfAbsent(weights, m -> this.tempfrstWght.isEmpty()? new Matrix(weights.getWid(), weights.getHgt()) : this.tempfrstWght.poll());
		Matrix scndMoment = this.scndMomentWght.computeIfAbsent(weights, m -> this.tempscndWght.isEmpty()? new Matrix(weights.getWid(), weights.getHgt()) : this.tempscndWght.poll());
		
		if (currLayer == 0) {step++;} currLayer = (currLayer + 1) % totalLayers; 
		
		float grad, old, frstPow =  (float) (1 - Math.pow(frstDecay, step)), scndPow =  (float) (1 - Math.pow(scndDecay, step));
		
		for (int i = 0; i < weights.getLength(); i++) {
			grad =  gradient.get(i);
			// m1 = b1 * m0 + (1 - b1) * g1
			frstMoment.set(i, frstDecay * frstMoment.get(i) + (1 - frstDecay) * grad);
			// v1 = b2 * v0 + (1 - b2) * g1^2
			scndMoment.set(i, scndDecay * scndMoment.get(i) + (1 - scndDecay) * grad * grad);
			// theta1 = (theta0 - alpha X mhat1 / (sqrt(vhat1) + epsilon), mhat1 = m1 / (1 - b1^t), vhat1 = v1 / (1 - b2^t)) - alpha * lambda * theta0
			old = weights.get(i);
			weights.set(i, (float) (old - learningRate * (frstMoment.get(i) / frstPow) / (Math.sqrt(scndMoment.get(i) / scndPow) + EPSILON) - learningRate * wghtDecay * old));
		}

	}

	@Override
	public void updateBiases(Vector biases, Vector delta) {
		Vector frstMoment = this.frstMomentBias.computeIfAbsent(biases, v -> this.tempfrstBias.isEmpty()? new Vector(biases.getHgt()) : this.tempfrstBias.poll());
		Vector scndMoment = this.scndMomentBias.computeIfAbsent(biases, v -> this.tempscndBias.isEmpty()? new Vector(biases.getHgt()) : this.tempscndBias.poll());
		
		float grad, old, frstPow =  (float) (1 - Math.pow(frstDecay, step)), scndPow =  (float) (1 - Math.pow(scndDecay, step));
		
		for (int i = 0; i < biases.getLength(); i++) {
			grad =  delta.get(i);
			// m1 = b1 * m0 + (1 - b1) * g1
			frstMoment.set(i, frstDecay * frstMoment.get(i) + (1 - frstDecay) * grad);
			// v1 = b2 * v0 + (1 - b2) * g1^2
			scndMoment.set(i, scndDecay * scndMoment.get(i) + (1 - scndDecay) * grad * grad);
			// theta1 = theta0 - alpha X mhat1 / (sqrt(vhat1) + epsilon), mhat1 = m1 / (1 - b1^t), vhat1 = v1 / (1 - b2^t)
			old = biases.get(i);
			biases.set(i, (float) (old - learningRate * (frstMoment.get(i) / frstPow) / (Math.sqrt(scndMoment.get(i) / scndPow) + EPSILON)));
		}
	}

	@Override
	public byte[] serialize() {
		return ByteHelper.mergeBytes(
			ByteHelper.toBytes(this.learningRate),
			ByteHelper.toBytes(this.wghtDecay), 
			ByteHelper.toBytes(this.totalLayers), 
			ByteHelper.toBytes(this.currLayer),
			ByteHelper.toBytes(this.step),
			AdamW.toBytes(this.frstMomentWght, this.scndMomentWght),
			AdamW.toBytes(this.frstMomentBias, this.scndMomentBias)
		);
	}
	
	public static <T extends Matrix> byte[] toBytes(IdentityHashMap<T, T> i, IdentityHashMap<T, T> j) {
		ArrayList<T> keys = new ArrayList<>(i.keySet());
		byte[] ibytes = ByteHelper.toBytes(keys.size()), jbytes = ByteHelper.toBytes(keys.size());
		for (T k : keys) {
			ibytes = ByteHelper.mergeBytes(ibytes, ByteHelper.toBytes(i.get(k)));
			jbytes = ByteHelper.mergeBytes(jbytes, ByteHelper.toBytes(j.get(k)));
		} return ByteHelper.mergeBytes(ibytes, jbytes);
	}

	@Override
	public Optimizer deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
		AdamW a = new AdamW(b.readFloat(), b.readFloat(), b.readInt()); 
		a.currLayer = b.readInt(); a.step = b.readInt();
		a.tempfrstWght = new ArrayDeque<>(Arrays.asList(b.readObjArr(new Matrix(0, 0))));
		a.tempscndWght = new ArrayDeque<>(Arrays.asList(b.readObjArr(new Matrix(0, 0))));
		a.tempfrstBias = new ArrayDeque<>(Arrays.asList((Vector[]) b.readObjArr(new Vector(0))));
		a.tempscndBias = new ArrayDeque<>(Arrays.asList((Vector[]) b.readObjArr(new Vector(0))));
		return a;
	}

	@Override
	public Optimizer[] getProtoArray(int length) {
		return new AdamW[length];
	}

}
