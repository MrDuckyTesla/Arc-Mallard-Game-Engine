package engine.data.util;

import java.nio.file.*;

import java.io.IOException;

import engine.data.serializations.FastSerializable;

public class ChunkedDB<T extends FastSerializable<T>> implements AutoCloseable {
	
	private final FastSerializable<?>[] prototypes;
	private final String location;
	private final int totalChunks;
	
	private Chunk<T> loadedChunk = new Chunk<>();
	private int currentLoadedChunk = -1;

	public ChunkedDB(int totalChunks, String fileLocation, FastSerializable<?>... prototypes) {
		this.totalChunks = totalChunks;
		this.location = fileLocation;
		this.prototypes = prototypes;
	}
	
	public void remove(String key) throws IOException {
		if (this.currentLoadedChunk != Math.floorMod(key.hashCode(), this.totalChunks)) {
			this.unloadChunk(); this.loadChunk(Math.floorMod(key.hashCode(), this.totalChunks));
		} this.loadedChunk.remove(key);
	}
	
	public void put(String key, T payload) throws IOException {
		if (this.currentLoadedChunk != Math.floorMod(key.hashCode(), this.totalChunks)) {
			this.unloadChunk(); this.loadChunk(Math.floorMod(key.hashCode(), this.totalChunks));
		} this.loadedChunk.put(key, payload);
	}
	
	public T get(String key) throws IOException {
		if (this.currentLoadedChunk != Math.floorMod(key.hashCode(), this.totalChunks)) {
			this.unloadChunk(); this.loadChunk(Math.floorMod(key.hashCode(), this.totalChunks));
		} return this.loadedChunk.get(key);
	}
	
	private void loadChunk(int chunk) throws IOException {
		if (this.currentLoadedChunk != chunk) {
			Path path = Paths.get(this.location + "chunk" + chunk + ".mdb");
			if (!Files.exists(path)) {this.loadedChunk = new Chunk<>();}
			else {this.loadedChunk = this.loadedChunk.deserialize(new ByteHelper(Files.readAllBytes(path)), this.prototypes);}
			this.currentLoadedChunk = chunk;
		}
	}
	
	private void unloadChunk() throws IOException {
		this.writeChunk(); this.loadedChunk = new Chunk<>(); this.currentLoadedChunk = -1;
	}
	
	private void writeChunk() throws IOException {
		Path path = Paths.get(this.location+"chunk"+this.currentLoadedChunk+".mdb");
		if (this.loadedChunk.size() == 0) {Files.deleteIfExists(path);} 
		else {
			Path parent = path.getParent();
		    if (parent != null) {Files.createDirectories(parent);}
			Files.write(path, this.loadedChunk.serialize());
		} 
	}
	
	public void emptyFolder() throws IOException {
		if (Files.exists(Path.of(this.location))) {
			Files.list(Path.of(this.location)).forEach(path -> {
				try {Files.delete(path);} 
				catch (IOException e) {e.printStackTrace();} 
			}); 
		}
	}

	@Override
	public void close() throws IOException {
		if (this.currentLoadedChunk != -1) {this.unloadChunk();}
	}

}
