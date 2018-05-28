package texcop.cop;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CopConfig {
    @JsonProperty("Enabled")
    private boolean enabled = true;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("Match")
    public List<String> matches;

    @JsonProperty("Exclude")
    public List<String> exclude;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
