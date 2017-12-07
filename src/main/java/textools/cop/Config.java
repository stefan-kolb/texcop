package textools.cop;

import java.util.HashMap;

public class Config extends HashMap<String, CopConfig> {
    public CopConfig forCop(String copName) {
        return this.get(copName);
    }
}
