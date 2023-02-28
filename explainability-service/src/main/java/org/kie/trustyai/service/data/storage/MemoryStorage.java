package org.kie.trustyai.service.data.storage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.kie.trustyai.service.config.storage.StorageConfig;
import org.kie.trustyai.service.data.exceptions.StorageReadException;
import org.kie.trustyai.service.data.exceptions.StorageWriteException;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;

@LookupIfProperty(name = "service.storage.format", stringValue = "MEMORY")
@ApplicationScoped
public class MemoryStorage extends Storage {

    private final Map<String, String> data = new HashMap<>();
    private final String dataFilename;

    public MemoryStorage(StorageConfig config) {
        this.dataFilename = config.dataFilename();
    }

    @Override
    public ByteBuffer getData() throws StorageReadException {
        if (data.containsKey(this.dataFilename)) {
            return ByteBuffer.wrap(data.get(this.dataFilename).getBytes());
        } else {
            throw new StorageReadException("Data file not found");
        }

    }

    @Override
    public boolean dataExists() throws StorageReadException {
        return data.containsKey(this.dataFilename);
    }

    @Override
    public void save(ByteBuffer data, String location) throws StorageWriteException {
        final String stringData = new String(data.array(), StandardCharsets.UTF_8);
        Log.info("Saving " + stringData + " to " + location);
        this.data.put(location, stringData);
    }

    @Override
    public void append(ByteBuffer data, String location) throws StorageWriteException {
        if (this.data.containsKey(location)) {
            final String existing = this.data.get(location);
            this.data.put(location, existing + new String(data.array(), StandardCharsets.UTF_8));
        } else {
            throw new StorageWriteException("Destination does not exist: " + location);
        }

    }

    @Override
    public void appendData(ByteBuffer data) throws StorageWriteException {
        append(data, this.dataFilename);
    }

    @Override
    public ByteBuffer read(String location) throws StorageReadException {
        if (data.containsKey(location)) {
            return ByteBuffer.wrap(data.get(location).getBytes());
        } else {
            throw new StorageReadException("File not found: " + location);
        }

    }

    @Override
    public void saveData(ByteBuffer data) throws StorageWriteException {
        save(data, this.dataFilename);
    }

    @Override
    public boolean fileExists(String location) throws StorageReadException {
        return data.containsKey(location);
    }

}
