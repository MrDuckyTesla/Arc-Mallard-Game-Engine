package engine.data;

import engine.data.serializations.FastSerializable;

/**
 * Compression interface made to work well with Serializable
 * @author Nico L. (MrDuckyTesla)
 */
public interface Compression extends FastSerializable<Compression> {
	
	/**
	 * Compresses given bytes into a smaller format
	 * @param bytes bytes to be compressed
	 * @return compressed bytes
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] compress(byte[] bytes);
	
	/**
	 * Decompresses given bytes back to original format
	 * @param bytes compressed bytes to be decompressed
	 * @return decompressed bytes
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] decompress(byte[] bytes);
	
	/**
	 * Gets the amount of saved space from the last compression
	 * @return Space saved from last compression
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract int getSavedSpace();

}
