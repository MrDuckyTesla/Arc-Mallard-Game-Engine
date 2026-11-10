package engine.data.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import engine.data.Serialization;
import engine.data.serializations.EasySerializable;
import engine.data.serializations.FastSerializable;

public class ByteHelper {
	
	/**
	 * Merges multiple byte[] arrays into a singular flat byte[] array
	 * @param params byte arrays to flatten
	 * @return Singular flattened array of bytes
	 */
	public static byte[] mergeBytes(byte[]...params) {
		int total = 0;  // Total size across all arrays
		for (byte[] b : params) {total += b.length;}
		byte[] rtrn = new byte[total]; int offset = 0;
		for (byte[] b : params) {
			System.arraycopy(b, 0, rtrn, offset, b.length);
			offset += b.length;
		} return rtrn;
	}
	
	/**
	 * Function that serializes an array of objects
	 * @param <T> Object that implements Serializable
	 * @param o Object array being serialized
	 * @return The object in byte form, the bytes are ordered as so:
	 * [Array Length][[Byte Length][Object], ...[Byte Length][Object]]
	 */
	public static <T extends Serialization<T>> byte[] toBytes(T[] o) {
		if (o.length == 0) {throw new IllegalArgumentException("Given array was length 0");}
		byte[][] bytes = new byte[o.length+1][]; bytes[0] = ByteHelper.toBytes(o.length);
		for (int i = 0; i < o.length; i++) {bytes[i+1] = ByteHelper.toBytes(o[i]);}
		return ByteHelper.mergeBytes(bytes);
	}
	
	/**
	 * Function that serializes an array of strings
	 * @param s String array being serialized
	 * @return The String in byte form, the bytes are ordered as so:
	 * [Array Length][[String Length][String], ...[String Length][String]]
	 */
	public static byte[] toBytes(String[] s) {
		byte[][] bytes = new byte[s.length+1][];
		bytes[0] = ByteHelper.toBytes(s.length);
		for (int i = 0; i < s.length; i++) {
			bytes[i+1] = ByteHelper.toBytes(s[i]);
		} return ByteHelper.mergeBytes(bytes);
	}
	
	/**
	 * Function that serializes an array of floats
	 * @param f Float array being serialized
	 * @return The float array in byte form, the bytes are ordered as so:
	 * [Array Length][[Float], ...[Float]]
	 */
	public static byte[] toBytes(float[] f) {
		ByteBuffer b = ByteBuffer.allocate(f.length*4+4);
		b.putInt(f.length); for (float l : f) {b.putFloat(l);} return b.array();
	}
	
	/**
	 * Function that serializes an array of integers
	 * @param i Int array being serialized
	 * @return The int array in byte form, the bytes are ordered as so:
	 * [Array Length][[Int], ...[Int]]
	 */
	public static byte[] toBytes(int[] i) {
		ByteBuffer b = ByteBuffer.allocate(i.length*4+4);
		b.putInt(i.length); for (int n : i) {b.putInt(n);} return b.array();
	}
	
	/**
	 * Function that serializes an array of characters
	 * @param c Char array being serialized
	 * @return The char array in byte form, the bytes are ordered as so:
	 * [Array Length][[Char], ...[Char]]
	 */
	public static byte[] toBytes(char[] c) {
		ByteBuffer b = ByteBuffer.allocate(c.length*2+4);
		b.putInt(c.length); for (char h : c) {b.putChar(h);} return b.array();
	}
	
	/**
	 * Function that serializes an array of booleans
	 * @param b Bool array being serialized
	 * @return The bool array in byte form, the bytes are ordered as so:
	 * [Array Length][[Bool], ...[Bool]]
	 */
	public static byte[] toBytes(boolean[] b) {
		ByteBuffer y = ByteBuffer.allocate(b.length+4);  y.putInt(b.length); 
		for (boolean o : b) {y.put((byte) (o ? 1 : 0));} return y.array();
	}
	
	/**
	 * Function that prepends length to an array of bytes
	 * @param b byte array being serialized
	 * @return The byte array in.. byte form, the bytes are ordered as so:
	 * [Array Length][[Byte], ...[Byte]]
	 */
	public static byte[] toBytes(byte[] b) {
		ByteBuffer y = ByteBuffer.allocate(b.length+4);  
		y.putInt(b.length); y.put(b); return y.array();
	}
	
	/**
	 * Function that serializes an object
	 * @param <T> Object that implements Serializable
	 * @param object  Object being serialized
	 * @return The object in byte form, the bytes are ordered as so:
	 * [Byte Length][Object]
	 */
	public static <T extends Serialization<T>> byte[] toBytes(T object) {
		byte[] bytes = object.serialize();
		return ByteHelper.mergeBytes(
			ByteBuffer.allocate(4).putInt(bytes.length).array(), bytes
		);
	}
	
	/**
	 * Function that serializes a string
	 * @param s String to be serialized
	 * @return The string in byte form, the bytes are ordered as so:
	 * [String Length][String]
	 */
	public static byte[] toBytes(String s) {
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		return ByteHelper.mergeBytes(ByteHelper.toBytes(b.length), b);
	}
	
	/**
	 * Function that serializes a long
	 * @param l Long to be serialized
	 * @return The string in byte form, the bytes are ordered as so:
	 * [Long]
	 */
	public static byte[] toBytes(long l) {
		return ByteBuffer.allocate(8).putLong(l).array();
	}
	
	/**
	 * Function that serializes a float
	 * @param f Float to be serialized
	 * @return The string in byte form, the bytes are ordered as so:
	 * [Float]
	 */
	public static byte[] toBytes(float f) {
		return ByteBuffer.allocate(4).putFloat(f).array();
	}
	
	/**
	 * Function that serializes an integer
	 * @param i Int to be serialized
	 * @return The int in byte form, the bytes are ordered as so:
	 * [Int]
	 */
	public static byte[] toBytes(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}
	
	/**
	 * Function that serializes a short
	 * @param s Short to be serialized
	 * @return The short in byte form, the bytes are ordered as so:
	 * [Short]
	 */
	public static byte[] toBytes(short s) {
		return ByteBuffer.allocate(2).putShort(s).array();
	}
	
	/**
	 * Function that serializes a character
	 * @param c Int to be serialized
	 * @return The char in byte form, the bytes are ordered as so:
	 * [Char]
	 */
	public static byte[] toBytes(char c) {
		return ByteBuffer.allocate(2).putChar(c).array();
	}
	
	/**
	 * Function that serializes a boolean as an array
	 * @param b Bool to be serialized
	 * @return The bool in byte form, the bytes are ordered as so:
	 * [Bool]
	 */
	public static byte[] toBytes(boolean b) {
		return new byte[] {(byte) (b ? 1 : 0)};
	}
	
	/**
	 * Function that serializes a boolean
	 * @param b Bool to be serialized
	 * @return The bool in byte form, the bytes are ordered as so:
	 * [Bool]
	 */
	public static byte toByte(boolean b) {
		return (byte) (b ? 1 : 0);
	}
	
	private final ByteBuffer byteStream;
	
	public ByteHelper(byte[] bytes) {this.byteStream = ByteBuffer.wrap(bytes);}
	
	public boolean hasRemaining () {return this.byteStream.hasRemaining();}
	
	public byte[] get() {return this.byteStream.array();}
	
	// [Array Length][[Byte Length][Object], ...[Byte Length][Object]]
	@SuppressWarnings("unchecked")
	public <T extends FastSerializable<?>> T[] readObjArr(T proto) {
		T[] arr = (T[]) proto.getProtoArray(this.readInt());
		for (int i = 0; i < arr.length; i++) {
			arr[i] = this.readObject(proto);
		} return arr;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends EasySerializable<T>> T[] readObjArr(T prototype) {
		T[] rtrn = (T[]) java.lang.reflect.Array.newInstance(
			prototype.getClass(), this.byteStream.getInt()
		); for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readObj(prototype);
		} return rtrn;
	}
	
	public String[] readStringArr() {
		String[] rtrn = new String[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readString();
		} return rtrn;
	}
	
	public float[] readFloatArr() {
		float[] rtrn = new float[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readFloat();
		} return rtrn;
	}
	
	public int[] readIntArr() {
		int[] rtrn = new int[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readInt();
		} return rtrn;
	}
	
	public short[] readShortArr() {
		short[] rtrn = new short[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readShort();
		} return rtrn;
	}
	
	public char[] readCharArr() {
		char[] rtrn = new char[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readChar();
		} return rtrn;
	}
	
	public byte[] readByteArr() {
		byte[] b = new byte[this.byteStream.getInt()];
		this.byteStream.get(b); return b;
	}
	
	public boolean[] readBoolArr() {
		boolean[] rtrn = new boolean[this.byteStream.getInt()]; 
		for (int i = 0; i < rtrn.length; i++) {
			rtrn[i] = this.readBool();
		} return rtrn;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends FastSerializable<?>> T readObject(T proto) {
		byte[] bytes = new byte[this.readInt()]; this.byteStream.get(bytes);
		return (T) proto.deserialize(new ByteHelper(bytes), proto);
	}
	
	public <T extends EasySerializable<T>> T readObj(T prototype) {
		byte[] bytes = new byte[this.byteStream.getInt()];
		this.byteStream.get(bytes);
		return prototype.deserialize(new ByteHelper(bytes));
	}
	
	public String readString() {
		byte[] str = new byte[this.byteStream.getInt()]; 
		this.byteStream.get(str);
		return new String(str, StandardCharsets.UTF_8);
	}
	
	public long readLong() {
		return this.byteStream.getLong();
	}
	
	public float readFloat() {
		return this.byteStream.getFloat();
	}
	
	public int readInt() {
		return this.byteStream.getInt();
	}
	
	public short readShort() {
		return this.byteStream.getShort();
	}
	
	public char readChar() {
		return this.byteStream.getChar();
	}
	
	public boolean readBool() {
		return this.byteStream.get() == 1;
	}

}
