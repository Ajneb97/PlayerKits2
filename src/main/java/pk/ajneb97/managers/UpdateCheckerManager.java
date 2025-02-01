package pk.ajneb97.managers;

import pk.ajneb97.model.internal.UpdateCheckerResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateCheckerManager {

    private final String version;
    private String latestVersion;

    public UpdateCheckerManager(String version) {
        this.version = version;
    }

    public UpdateCheckerResult check() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=112616").openConnection();
            int timeout = 1500;
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                latestVersion = reader.readLine();
            }

            if (latestVersion != null && latestVersion.length() <= 7) {
                if (!version.equals(latestVersion)) {
                    return UpdateCheckerResult.noErrors(latestVersion);
                }
            }
            return UpdateCheckerResult.noErrors(null);
        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
