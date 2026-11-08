package engine.neural.networks;

import java.util.Random;

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
	
	public T[] computeBestNoise(int num, Vector prediction, Simulation<T> s, Object...args) {
		// Set the best noise to the current noise
		Vector bestNoise = new Vector(this.noise.getMatrix().length);
		// Set the last prediction to the current prediction
		this.lastPrediction = prediction.copy();
		// Set the best results to the current simulation
		SimulationResult<T> bestResult = s.simulate(bestNoise, args);
		float bestReward = this.rewardFun.calculate(bestResult.sim, bestResult.arg);
		T[] stateOfBestSim = bestResult.sim; Vector candidate;
		// For the amount of simulations requested 
		for (int i = 0; i < num; i++) {
			// get a candidate noise vector
			candidate = new Vector(this.noise.getMatrix().length);
			for (int j = 0; j < candidate.getLength(); j++) {
				// Add noise to candidate whilst keeping it within -1, 1 range
				candidate.set(j, (float) rand.nextGaussian());
			} // Simulate the candidate with noise
			SimulationResult<T> result = s.simulate(candidate, args);
			// Get the reward associated with candidates simulation
			float currReward = this.rewardFun.calculate(result.sim, result.arg);
			// Replace best reward if current reward is better
			if (currReward > bestReward) {
				stateOfBestSim = result.sim; bestReward = currReward;
				bestNoise = new Vector(candidate.getMatrix());
			} // Set noise to best noise and return the simulation
		} this.noise = bestNoise; return stateOfBestSim;
	}
	
	public Vector getNoise() {return this.noise;}

}
