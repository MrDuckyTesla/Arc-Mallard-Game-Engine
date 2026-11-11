package engine.neural.networks;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;
import engine.neural.Activation;
import engine.neural.Cost;
import engine.neural.Initializer;
import engine.neural.Network;
import engine.neural.Optimizer;
import engine.neural.networks.Policy.RewardFunction;
import engine.neural.networks.Policy.Simulation;
import engine.neural.networks.Policy.Simulation.SimulationResult;
import engine.neural.util.Copyable;
import engine.neural.util.Matrix;
import engine.neural.util.Vector;

public class Evolutionary<T extends Copyable<T>> extends Feedforward {
	
	private class Candidate {
		private Feedforward net; private float fitness;
		private T[] state;
		
		public Candidate(Feedforward net, float fitness, T[] state) {
			this.net = net; this.fitness = fitness; this.state = state;
		}
	}
	
	private RewardFunction<T> rewardFun; 
	private Random rand = new Random();
	PriorityQueue<Candidate> candidates;

	public Evolutionary(int[] networkSizes, Initializer initializer, Activation activation, Cost cost, Optimizer optimizer, RewardFunction<T> reward) {
		super(networkSizes, initializer, activation, cost, optimizer); this.rewardFun = reward;
		this.candidates = new PriorityQueue<>(Comparator.comparingDouble(c -> c.fitness));
	}
	
	public void initBatch(T[] state, int numBatch) {
		T[] s; Feedforward n;
		for (int i = 0; i < numBatch; i++) {
			s = state.clone();
			for (int j = 0; j < state.length; j++) {s[i] = state[i].copy();}
			n = new Feedforward(networkSizes, initializer, activation, cost, optimizer);
			this.candidates.add(new Candidate(n, 0, s));
		}
	}
	
	public void stepBatch(Simulation<T> s, Object...args) {
		for (Candidate c : candidates) {
			SimulationResult<T> r = s.simulate(new Vector(networkSizes[networkSizes.length-1]), args);
			c.fitness += this.rewardFun.calculate(r.sim, r.arg); c.state = r.sim;
		}
	}
	
	public void updateBatch() {
		for (Candidate c : candidates) {
			
		}
	}
	
	private void addNoise(Feedforward net, float mean, float stddev) {
		for (int i = 1; i < this.networkSizes.length; i++) {
			net.weights[i-1].addMatrix(getNoiseArr(net.weights[i-1].getLength(), mean, stddev));
			net.biases[i-1].addMatrix(getNoiseArr(net.biases[i-1].getLength(), mean, stddev));
		}
	}
	
	private float[] getNoiseArr(int len, float mean, float stddev) {
		float[] arr = new float[len];
		for (int i = 0; i < len; i++) {
			arr[i] = (float) rand.nextGaussian(mean, stddev);
		} return arr;
	}
	
	@Override
	// Literally just a forward pass but without all the storing
	public Vector predict(Vector input) {
		if (input.getHgt() == weights[0].getWid()) {  // Gatekeep
			for (int i = 0; i < this.weights.length; i++) {
				// multiply by weights and add bias
				input = (Vector) this.weights[i].multiply(input);
				input.addMatrix(this.biases[i].getMatrix());
				// Apply activation function throughout input vector
				for (int j = 0; j < input.getHgt(); j++) {
					input.set(j, this.activation.function(input.get(j)));
				}
			}
		} return input;
	}

	@Override
	public byte[] serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Network deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Network[] getProtoArray(int length) {
		return new Evolutionary[length];
	}

}
