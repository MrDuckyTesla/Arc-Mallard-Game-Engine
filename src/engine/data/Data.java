package engine.data;

import java.io.IOException;

import engine.data.serializations.FastSerializable;

/**
 * Class for easy management of serialization, compression, encryption and storage
 * @param <T> Data type to be stored
 * @author Nico L. (MrDuckyTesla)
 */
public interface Data<T extends Serialization<T> > {
	
	/**
	 * Saves data using given serialization, compression, encryption and storage
	 * @throws IOException Will throw if java file writing fails
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract void save() throws IOException;
	
	/**
	 * Loads data using given serialization, compression, encryption and storage
	 * @param prototypes dummy variables needed by serialization
	 * @return Data that was serialized
	 * @throws IOException  Will throw if java file loading fails
	 * @author Nico L. (MrDuckyTesla)
	 */
	public T load(FastSerializable<?>... prototypes) throws IOException;
}
