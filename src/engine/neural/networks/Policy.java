package engine.neural.networks;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;
import engine.neural.*;

import engine.neural.util.Matrix;
import engine.neural.util.Vector;

import engine.neural.networks.Policy.Simulation.SimulationResult;

public class Policy<T> extends Feedforward {
	
	private RewardFunction<T> rewardFun;
	private Vector noise, lastPrediction;
	private Random rand = new Random();

	public Policy(int[] networkSizes, Initializer initializer, Activation activation, Cost cost, Optimizer optimizer, RewardFunction<T> reward) {
		super(networkSizes, initializer, activation, cost, optimizer);
		noise = new Vector(networkSizes[networkSizes.length-1]);
		this.rewardFun = reward;
	}
	
	@FunctionalInterface
	public interface RewardFunction<T> {
		float calculate(T[] state, Object...args);
	}
	
	@FunctionalInterface
	public interface Simulation<T> {
		SimulationResult<T> simulate(Vector noise, Object...args);
		
		public static class SimulationResult<T> {
			public final T[] sim;
			public final Object[] arg;
			
			public SimulationResult(T[] sim, Object...args) {
				this.sim = sim; this.arg = args;
			}
		}
	}
	
	public void backward() {
		// Create delta vector as prediction + noise
		Vector delta = this.lastPrediction.copy();
		this.noise.scaleMatrix(-1);
		delta.addMatrix(this.noise.getMatrix());
		// Element wise multiplication of the activation derivative of last preactivation
		for (int i = 0; i < delta.getHgt(); i++) {
			delta.scale(i, this.activation.derivative(this.preActivations[this.weights.length-1].get(i)));
		} Matrix gradient, weight;  // Store gradient and weight before change
		// Loop though weights backwards
		for (int i = this.weights.length-1; i >= 0 ; i--) {
			// Get gradient of current layer (delta * activations Transpose)
			gradient = delta.multiply(this.activations[i].getTranspose());
			// Store weights before changing them in update
			weight = this.weights[i].copy();
			// Update weights and biases
			this.optimizer.updateWeights(this.weights[i], gradient);
			this.optimizer.updateBiases(this.biases[i], delta);
			// make sure not updating input layer
			if (i != 0) {  // Multiply delta by weights (weight Transpose * delta)
				delta = weight.getTranspose().multiply(delta);
				for (int j = 0; j < delta.getHgt(); j++) {
					// Multiply delta by activation derivative of last preactivation
					delta.scale(j, this.activation.derivative(this.preActivations[i-1].get(j)));
				}
			}
		}
	}
	
	public void computeBestNoise(int num, Vector prediction, Simulation<T> s, Object...args) {
		// Create an empty vector for best noise
		Vector bestNoise = new Vector(this.noise.getMatrix().length);
		// Set the last prediction to the current prediction
		this.lastPrediction = prediction.copy();
		// Set the best results to the current simulation
		SimulationResult<T> bestResult = s.simulate(bestNoise, args);
		float bestReward = this.rewardFun.calculate(bestResult.sim, bestResult.arg);
		Vector candidate;
		// For the amount of simulations requested 
		for (int i = 0; i < num; i++) {
			// get a candidate noise vector
			candidate = new Vector(this.noise.getMatrix().length);
			for (int j = 0; j < candidate.getLength(); j++) {
				// Add noise to candidate
				candidate.set(j, (float) rand.nextGaussian());
			} // Simulate the candidate with noise
			SimulationResult<T> result = s.simulate(candidate, args);
			// Get the reward associated with candidates simulation
			float currReward = this.rewardFun.calculate(result.sim, result.arg);
			// Replace best reward if current reward is better
			if (currReward > bestReward) {
				bestReward = currReward;
				bestNoise = new Vector(candidate.getMatrix());
			} // Set noise to best noise and return the simulation
		} this.noise = bestNoise;
	}
	
	public void computeBestAverageNoise(int num, int top, Vector prediction, Simulation<T> s, Object...args) {
		class Candidate {
			public Vector noise; public float reward;
			public Candidate(Vector noise, float reward) {
				this.noise = noise; this.reward = reward;
			}
		} // Set the last prediction to the current prediction
		this.lastPrediction = prediction.copy();
		// Create an array of empty vectors for best noise and initialize variables
		PriorityQueue<Candidate> elite = new PriorityQueue<>(top, Comparator.comparingDouble(c -> c.reward));
		// Set other values in array to initial values
		Vector candidate = new Vector(this.noise.getMatrix().length);
		SimulationResult<T> result = s.simulate(candidate, args);
		elite.add(new Candidate(candidate, this.rewardFun.calculate(result.sim, result.arg)));
		// For the amount of simulations requested 
		for (int i = 0; i < num; i++) {
			// get a candidate noise vector
			candidate = new Vector(this.noise.getMatrix().length);
			for (int j = 0; j < candidate.getLength(); j++) {
				// Add noise to candidate
				candidate.set(j, (float) rand.nextGaussian());
			} // Simulate the candidate with noise
			result = s.simulate(candidate, args);
			// Get the reward associated with candidates simulation
			float currReward = this.rewardFun.calculate(result.sim, result.arg);
			// Check if queue has less than top candidates
			if (elite.size() < top) {
				elite.add(new Candidate(new Vector(candidate.getMatrix()), currReward));
			} // Replace best reward if current reward is better
			else if (currReward > elite.peek().reward) {
				elite.poll();
				elite.add(new Candidate(new Vector(candidate.getMatrix()), currReward));
			} // Set noise to average noise and return the simulation
		} Vector average = new Vector(candidate.getLength());
		for (int i = 0; i < top; i++) {
			average.addMatrix(elite.poll().noise.getMatrix());
		} average.scaleMatrix(1f/top); this.noise = average;
	}
	
	public Vector getNoise() {return this.noise;}
	
	@Override
	public Network deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
//		Feedforward(int[] networkSizes, Initializer initializer, Activation activation, Cost cost, Optimizer optimizer)
		Policy<T> p = new Policy<>( 
			b.readIntArr(),
			(Initializer) b.readObject(prototypes[0]),
			(Activation) b.readObject(prototypes[1]),
			(Cost) b.readObject(prototypes[2]),
			(Optimizer) b.readObject(prototypes[3]),
			this.rewardFun
		);
		p.biases = b.readObjArr(new Vector());
		p.activations = b.readObjArr(new Vector());
		p.preActivations = b.readObjArr(new Vector());
		p.weights = b.readObjArr(new Matrix());
		return p;
	}

}
