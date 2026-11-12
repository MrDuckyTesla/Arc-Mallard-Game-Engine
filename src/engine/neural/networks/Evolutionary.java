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
import engine.physics.util.Vector2D;

public class Evolutionary<T extends Copyable<T>> extends Feedforward {
	
	private class Candidate {
		private Feedforward net; private float fitness;
		private T[] state; private Object[] args;
		
		public Candidate(Feedforward net, float fitness, T[] state, Object...args) {
			this.net = net; this.fitness = fitness; this.state = state; this.args = args;
		}
	}
	
	private RewardFunction<T> rewardFun; 
	private Random rand = new Random();
	PriorityQueue<Candidate> candidates;

	public Evolutionary(int[] networkSizes, Initializer initializer, Activation activation, Cost cost, Optimizer optimizer, RewardFunction<T> reward) {
		super(networkSizes, initializer, activation, cost, optimizer); this.rewardFun = reward;
		this.candidates = new PriorityQueue<>(Comparator.comparingDouble(c -> -c.fitness));
	}
	
	@FunctionalInterface
	public interface Simulation<T> {
		SimulationResult<T> simulate(Feedforward net, T[] state, Object...args);
	}
	
	public void initBatch(T[] state, int numBatch, Object...args) {
		T[] s; Feedforward n;
		for (int i = 0; i < numBatch; i++) {
			s = state.clone();
			for (int j = 0; j < state.length; j++) {s[j] = state[j].copy();}
			n = new Feedforward(networkSizes, initializer, activation, cost, optimizer);
			this.candidates.add(new Candidate(n, 0, s, args));
		}
	}
	
	public void stepBatch(Simulation<T> s) {
		PriorityQueue<Candidate> updated = new PriorityQueue<>(Comparator.comparingDouble(c -> -c.fitness)); 
		for (Candidate c : candidates) {
			SimulationResult<T> r = s.simulate(c.net, c.state, c.args);
			c.fitness += this.rewardFun.calculate(r.sim, r.arg); c.state = r.sim;
			 c.args = r.arg; updated.add(c);
		} candidates = updated;
	}
	
	@SuppressWarnings("unchecked")
	public void updateBatch(T[] state, Object...args) {
		PriorityQueue<Candidate> updated = new PriorityQueue<>(Comparator.comparingDouble(c -> -c.fitness)); 
		int len = candidates.size(), fract = len / 10; Candidate c; 
		Candidate[] top = new Evolutionary.Candidate[fract];
		for (int i = 0; i < len; i++) {
			c = candidates.poll(); c.state = state.clone(); c.fitness = 0; c.args = args;
			for (int j = 0; j < state.length; j++) {c.state[j] = state[j].copy();}
			if (i < fract) {updated.add(c); top[i] = c;} 
			else if (i < fract * 8) {
				Candidate choice = top[rand.nextInt(fract)];
				c.net = addNoiseReturn(choice.net, 0f, 0.1f); updated.add(c);
			} 
			else {
				c.net = new Feedforward(networkSizes, initializer, activation, cost, optimizer);
				updated.add(c);
			}
		} this.candidates = updated;
	}
	
	private void addNoise(Feedforward net, float mean, float stddev) {
		for (int i = 1; i < this.networkSizes.length; i++) {
			net.weights[i-1].addMatrix(getNoiseArr(net.weights[i-1].getLength(), mean, stddev));
			net.biases[i-1].addMatrix(getNoiseArr(net.biases[i-1].getLength(), mean, stddev));
		}
	}
	
	private Feedforward addNoiseReturn(Feedforward net, float mean, float stddev) {
		Feedforward n = new Feedforward(networkSizes, initializer, activation, cost, optimizer);
		for (int i = 1; i < this.networkSizes.length; i++) {
			n.weights[i-1] = net.weights[i-1].copy();
			n.biases[i-1] = net.biases[i-1].copy();
			n.weights[i-1].addMatrix(getNoiseArr(net.weights[i-1].getLength(), mean, stddev));
			n.biases[i-1].addMatrix(getNoiseArr(net.biases[i-1].getLength(), mean, stddev));
		} return n;
	}
	
	private float[] getNoiseArr(int len, float mean, float stddev) {
		float[] arr = new float[len];
		for (int i = 0; i < len; i++) {
			arr[i] = (float) rand.nextGaussian(mean, stddev);
		} return arr;
	}
	
	public float getBestFitness() {return this.candidates.peek().fitness;}
	
	@Override
	// Literally just a forward pass but without all the storing
	public Vector predict(Vector input) {
		Feedforward net = candidates.peek().net;
		if (input.getHgt() == net.weights[0].getWid()) {  // Gatekeep
			for (int i = 0; i < net.weights.length; i++) {
				// multiply by weights and add bias
				input = (Vector) net.weights[i].multiply(input);
				input.addMatrix(net.biases[i].getMatrix());
				// Apply activation function throughout input vector
				for (int j = 0; j < input.getHgt(); j++) {
					input.set(j, net.activation.function(input.get(j)));
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
