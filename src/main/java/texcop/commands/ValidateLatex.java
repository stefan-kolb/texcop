package texcop.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import texcop.FileTask;
import texcop.cop.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates all .tex files within the current directory and its descendants.
 * <p/>
 * Rules adopted by chktex (http://baruch.ev-en.org/proj/chktex/)
 */
public class ValidateLatex implements FileTask {

    private Config config;
    private List<RegexCop> cops = new ArrayList<>();

    public ValidateLatex() {
        loadConfig();
        loadRules();
    }

    @Override
    public String getName() {
        return "validate-latex";
    }

    @Override
    public String getDescription() {
        return "validates .tex files";
    }

    @Override
    public List<Offense> execute(Path filePath) {
        final List<Offense> offenses = new ArrayList<>();

        for (RegexCop cop : cops) {
            CopConfig cc = config.forCop(cop.getName());
            if (cc == null || cc.isEnabled()) {
                offenses.addAll(cop.execute(filePath));
            }
        }

        return offenses;
    }

    private void loadConfig() {
        final String configFile = ".texcop.yml";

        if (!Files.exists(Paths.get(configFile))) {
            config = new Config();
            return;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            config = mapper.readValue(Paths.get(configFile).toFile(), Config.class);
        } catch (Exception e) {
            System.err.println("Error reading .texcop.yml: " + e.getMessage());
        }
    }

    private void loadRules() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("texcop/cop/style/rules.yml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(is, Config.class);
            for (Map.Entry<String, CopConfig> entry : config.entrySet()) {
                cops.add(new RegexCop(entry.getKey(), entry.getValue().matches, entry.getValue().message));
            }
        } catch (Exception e) {
            System.err.println("Error reading rules.yml: " + e.getMessage());
        }
    }

}
