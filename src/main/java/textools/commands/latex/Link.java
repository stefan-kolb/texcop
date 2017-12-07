package textools.commands.latex;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import textools.commands.ValidateLinks;
import textools.cop.Location;

public class Link {

    private static final Pattern DEFINITION_DETECTION = Pattern.compile("\\\\url\\{(?<url>[^\\}]*)\\}");

    public static List<Link> find(String line, int lineNumber, Path file) {
        final Matcher matcher = DEFINITION_DETECTION.matcher(line);

        List<Link> result = new ArrayList<>();
        while (matcher.find()) {
            int column = 0;
            int length = 0;

            if (matcher.groupCount() >= 1) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    column = matcher.start(i);
                    length = matcher.end(i) - matcher.start(i);
                }
            } else {
                column = matcher.start();
                length = matcher.end() - matcher.start();
            }
            Location location = new Location(line, lineNumber, column, length);
            result.add(new Link(matcher.group("url"), file, location));
        }

        return result;
    }

    public final String url;
    public final Path file;
    public final Location location;

    public Link(String url, Path file,Location location) {
        this.url = url.replace("\\#","#"); // replace some things
        this.location = location;
        this.file = file;
    }

    public void validateUrl() throws MalformedURLException {
        new URL(url);
    }

    public int getStatusCode() throws IOException {
        String finalUrl = getFinalURL(url);

        try {
            final URL url = new URL(finalUrl);
            HttpURLConnection http = null;
            try {
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("HEAD");
                http.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/536.26.17 (KHTML, like Gecko) Version/6.0.2 Safari/536.26.17");
                int statusCode = http.getResponseCode();
                return statusCode;
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        } catch (MalformedURLException e) {
            return -1;
        }
    }

    private static String getFinalURL(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setInstanceFollowRedirects(false);
            con.connect();
            con.getInputStream();

            if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                String redirectUrl = con.getHeaderField("Location");
                return getFinalURL(redirectUrl);
            }
            return url;
        } catch (IOException e) {
            return url;
        }
    }
}
