package texcop.cop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Config extends HashMap<String, CopConfig> {
    public static final String configFile = ".texcop.yml";

    public CopConfig forCop(String copName) {
        return this.get(copName);
    }

    public Config addCop(String copName, CopConfig config) {
        this.putIfAbsent(copName, config);
        return this;
    }

    public static Config load() {
        if (!Files.exists(Paths.get(Config.configFile))) {
            return new Config();
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(Paths.get(Config.configFile).toFile(), Config.class);
        } catch (IOException e) {
            System.err.println(String.format("Error reading %s: %s", Config.configFile, e.getMessage()));
            return new Config();
        }
    }

    public void save() {
        if (Files.exists(Paths.get(Config.configFile))) {
            System.err.println(String.format("Error writing %s: file already exists", Config.configFile));
            return;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            mapper.writerFor(Config.class).writeValue(Paths.get(Config.configFile).toFile(), this);
        } catch (IOException e) {
            System.err.println(String.format("Error writing %s: %s", Config.configFile, e.getMessage()));
        }
    }
}
