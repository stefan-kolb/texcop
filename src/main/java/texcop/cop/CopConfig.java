package texcop.cop;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CopConfig {
    @JsonProperty("Enabled")
    private boolean enabled;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("Match")
    public List<String> matches;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
