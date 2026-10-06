package engine.data;

import java.io.IOException;

import engine.data.serializations.FastSerializable;

/**
 * Storage interface made to work well with Serializable
 * @author Nico L. (MrDuckyTesla)
 */
public interface Storage extends FastSerializable<Storage> {
	
	/**
	 * Saves given bytes to some medium
	 * @param bytes bytes to be saved
	 * @throws IOException Throws if bytes aren't saved
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract void save(byte[] bytes) throws IOException;
	
	/**
	 * Loads bytes from some medium
	 * @return bytes that were saved
	 * @throws IOException Throws if bytes cannot be retrieved
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] load() throws IOException;
	
}
