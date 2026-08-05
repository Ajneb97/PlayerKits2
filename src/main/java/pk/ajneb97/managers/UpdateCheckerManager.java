package pk.ajneb97.managers;

import pk.ajneb97.model.internal.UpdateCheckerResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class UpdateCheckerManager {

    private static final String UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=112616";
    private static final int TIMEOUT_MILLIS = 1500;

    private final String version;
    private volatile String latestVersion;

    public UpdateCheckerManager(String version){
        this.version = version;
    }

    public UpdateCheckerResult check(){
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(UPDATE_URL).toURL().openConnection();
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);

            String read;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                read = reader.readLine();
            }

            if (read == null) {
                return UpdateCheckerResult.error();
            }

            latestVersion = read;
            if (read.length() <= 7 && !version.equals(read)) {
                return UpdateCheckerResult.noErrors(read);
            }
            return UpdateCheckerResult.noErrors(null);
        } catch (Exception ignored) {
            return UpdateCheckerResult.error();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
