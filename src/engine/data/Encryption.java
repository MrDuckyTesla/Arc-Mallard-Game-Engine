package engine.data;

import engine.data.serializations.FastSerializable;

/**
 * Encryption interface made to work well with Serializable
 * @author Nico L. (MrDuckyTesla)
 */
public interface Encryption extends FastSerializable<Encryption> {
	
	/**
	 * Encrypts given bytes into a smaller format
	 * @param bytes bytes to be encrypted
	 * @return encrypted bytes
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] encrypt(byte[] bytes);
	
	/**
	 * Decrypts given bytes back to original format
	 * @param bytes encrypted bytes to be decrypted
	 * @return decrypted bytes
	 * @author Nico L. (MrDuckyTesla)
	 */
	public abstract byte[] decrypt(byte[] bytes);
	
}
