package engine.data.serializations;

import engine.data.Serialization;

public interface FastSerializable<T> extends Serialization<T> {
	
	/**
	 * Serializes all fields of an object into bytes
	 * @return Byte representation of implementing class
	 */
	public abstract byte[] serialize();
	
	/**
	 * Deserializes bytes back into an object
	 * @param b ByteHelper wrapper holding bytes
	 * @return Deserialized object
	 */
	public abstract T deserialize(engine.data.util.ByteHelper b, FastSerializable<?>...prototypes);
	
	/**
	 * Returns an object array of length size
	 * @param length length of object array
	 * @return Object array, shouldnt be anything more than just
	 * {@snippet:
	 * return new Example[length];
	 * } 
	 */
	public abstract T[] getProtoArray(int length);

}
