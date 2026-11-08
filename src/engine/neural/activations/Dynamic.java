package engine.neural.activations;

import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;
import engine.neural.Activation;

public class Dynamic implements Activation {
	
	private final Activation[] activations;
	private final int layers[];
	private int derivativeLayer, derivativeNum, functionLayer = 0, functionNum = 0;

	public Dynamic(int[] layers, Activation... activations) {
		if (activations.length == 0) {throw new IllegalArgumentException();} 
		for (Activation a : activations) {
			if (a == null) {throw new IllegalArgumentException();}
		} 
		this.activations = activations; 
		this.layers = layers;
		
		this.derivativeLayer = activations.length - 1;
		this.derivativeNum = layers[this.derivativeLayer + 1];
	}

	@Override
	public float function(float x) {
		float fun = this.activations[this.functionLayer].function(x);
		
		this.functionNum++;
		if (this.functionNum >= this.layers[this.functionLayer + 1]) {
			this.functionNum = 0; this.functionLayer++;
			this.functionLayer = this.functionLayer >= this.activations.length? 0 : this.functionLayer;
		} return fun; 
	}

	@Override
	public float derivative(float x) {
		float der = this.activations[this.derivativeLayer].derivative(x);
		
		this.derivativeNum--;
		if (this.derivativeNum <= 0) {
			this.derivativeLayer--;
			this.derivativeLayer = this.derivativeLayer < 0? this.activations.length - 1 : this.derivativeLayer;
			this.derivativeNum = this.layers[this.derivativeLayer+1];
		} return der;
	}

	@Override
	public byte[] serialize() {
		return ByteHelper.mergeBytes(
			ByteHelper.toBytes(this.layers),
			ByteHelper.toBytes(this.activations),
			ByteHelper.toBytes(this.derivativeLayer),
			ByteHelper.toBytes(this.derivativeNum),
			ByteHelper.toBytes(this.functionLayer),
			ByteHelper.toBytes(this.functionNum)
		);
	}

	@Override
	public Activation deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
		Dynamic d = new Dynamic(b.readIntArr(), (Activation[]) b.readObjArr(prototypes[0]));
		d.derivativeLayer = b.readInt(); d.derivativeNum = b.readInt();
		d.functionLayer = b.readInt(); d.functionNum = b.readInt(); return d;
	}

	@Override
	public Activation[] getProtoArray(int length) {
		return new Dynamic[length];
	}

}
