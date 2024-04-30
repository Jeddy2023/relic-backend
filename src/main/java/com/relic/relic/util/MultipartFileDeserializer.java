package com.relic.relic.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MultipartFileDeserializer extends StdDeserializer<MultipartFile> {

    protected MultipartFileDeserializer() {
        super(MultipartFile.class);
    }

    @Override
    public MultipartFile deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        // Implement logic to convert JSON node to MultipartFile
        // This might involve reading the file content, filename, etc.
        // For simplicity, let's assume the file URL is provided in the JSON
        String fileUrl = node.asText();
        // You may need to fetch the file content using this URL
        // and create a MultipartFile object
        // For now, let's return null
        return null;
    }
}