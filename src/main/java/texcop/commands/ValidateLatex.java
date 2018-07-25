package texcop.commands;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import texcop.FileTask;
import texcop.cop.Config;
import texcop.cop.CopConfig;
import texcop.cop.Offense;
import texcop.cop.RegexCop;
import texcop.cop.latex.TextInsideMathMode;
import texcop.cop.style.Capitalization;

/**
 * Validates all .tex files within the current directory and its descendants.
 * <p/>
 * Rules adopted by chktex (http://baruch.ev-en.org/proj/chktex/)
 */
public class ValidateLatex implements FileTask {

    protected static final List<String> topics = Arrays.asList("style", "layout", "math", "latex");

    private Config config;
    private List<FileTask> cops = new ArrayList<>();

    public ValidateLatex() {
        config = Config.load();
        loadCops();
        topics.stream().forEach(t -> loadRules(t));
        // FIXME other cops like bibtex will raise an error here, load cops globally?!
        warnUnrecognizedCopConfigs();
    }

    private void warnUnrecognizedCopConfigs() {
        config.keySet().stream()
                .filter(opt -> cops.stream().map(c -> c.getName()).noneMatch(c -> c.equals(opt)))
                .forEach(opt -> System.err.println("Warning: Unrecognized cop config: " + opt));
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

        for (FileTask cop : cops) {
            CopConfig cc = config.forCop(cop.getName());
            if (cc == null || (cc.isEnabled() && !excludesFile(cc.exclude, filePath))) {
                offenses.addAll(cop.execute(filePath));
            }
        }

        return offenses;
    }

    private boolean excludesFile(List<String> excludePaths, Path filePath) {
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }
        final Path workingDirectory = Paths.get("").toAbsolutePath();
        List<Path> excludes = excludePaths.stream().map(p -> workingDirectory.resolve(p).toAbsolutePath()).collect(Collectors.toList());
        return excludes.stream().anyMatch(p -> p.equals(filePath));
    }

    private void loadRules(String topic) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(String.format("texcop/cop/%s.yml", topic));

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(is, Config.class);
            for (Map.Entry<String, CopConfig> entry : config.entrySet()) {
                cops.add(new RegexCop(entry.getKey(), entry.getValue().matches, entry.getValue().message));
            }
        } catch (Exception e) {
            System.err.println(String.format("Error reading %s.yml: %s", topic, e.getMessage()));
        }
    }

    private void loadCops() {
        // TODO maybe use reflection later
        cops.add(new TextInsideMathMode());
        cops.add(new Capitalization());
    }
}
