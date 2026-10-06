package engine.data;

/**
 * Head interface for other serialization implementations
 * @param <T> Class to be serialized
 * @author Nico L. (MrDuckyTesla)
 */
public interface Serialization<T> {
	
	/**
	 * Serializes the class implementing this interface
	 * @return serialized version of class with data intact
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] serialize();

}
