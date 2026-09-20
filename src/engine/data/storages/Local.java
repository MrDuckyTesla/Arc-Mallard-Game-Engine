package engine.data.storages;

import java.io.IOException;
import java.nio.file.*;

import engine.data.Storage;
import engine.data.serializations.FastSerializable;
import engine.data.util.ByteHelper;

public class Local implements Storage {
	
	private final Path path;
	
	public Local() {
		this.path = Paths.get("data/unnamed.mdt");
	}

	public Local(String fileLocation) {
		this.path = Paths.get(fileLocation);
		try {
			Path parent = this.path.getParent();
			if (parent != null) {Files.createDirectories(parent);}
			if (!Files.exists(this.path)) {Files.createFile(this.path);}
	    } catch (IOException e) {e.printStackTrace();}
	}

	@Override
	public void save(byte[] bytes) throws IOException {
		Files.write(this.path, bytes);
	}

	@Override
	public byte[] load() throws IOException {
		return Files.readAllBytes(this.path);
	}

	@Override
	public byte[] serialize() {
		return ByteHelper.toBytes(this.path.toString());
	}

	@Override
	public Storage deserialize(ByteHelper b, FastSerializable<?>... prototypes) {
		return new Local(b.readString());
	}

	@Override
	public Storage[] getProtoArray(int length) {
		return new Local[length];
	}

}
