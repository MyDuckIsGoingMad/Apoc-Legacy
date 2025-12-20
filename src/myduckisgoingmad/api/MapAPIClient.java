package myduckisgoingmad.api;

import org.json.JSONException;
import org.json.JSONObject;

import myduckisgoingmad.api.entities.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MapAPIClient {
    private final String apiBase;

    public MapAPIClient(String apiBase) {
        this.apiBase = apiBase.endsWith("/") ? apiBase.substring(0, apiBase.length() - 1) : apiBase;
    }

    public Player checkout(String username, double x, double y) throws MapAPIException {
        return checkout(username, x, y, null, null);
    }

    public Player checkout(String username, double x, double y, Boolean track, Boolean newSession)
            throws MapAPIException {
        try {
            Player player = new Player(username, x, y);
            JSONObject requestJson = createPlayerDTO(player);

            addNonNullField(requestJson, "track", track);
            addNonNullField(requestJson, "newSession", newSession);

            return parsePlayer(postRequest("/players/checkout", requestJson));
        } catch (Exception e) {
            throw new MapAPIException("Failed to checkout", e);
        }
    }

    public Location createClaim(double x, double y, ClaimType type) throws MapAPIException {
        try {
            Location location = new Location(x, y);
            location.claim = new Claim(type);
            JSONObject requestJson = createLocationDTO(location);

            return parseLocation(postRequest("/locations", requestJson));
        } catch (Exception e) {
            throw new MapAPIException("Failed to create claim", e);
        }
    }

    public Location createPOI(double x, double y, String category) throws MapAPIException {
        try {
            Location location = new Location(x, y);
            location.poi = new PointOfInterest(category);
            JSONObject requestJson = createLocationDTO(location);

            return parseLocation(postRequest("/locations", requestJson));
        } catch (Exception e) {
            throw new MapAPIException("Failed to create POI", e);
        }
    }

    public Location createResource(double x, double y, String type, int q) throws MapAPIException {
        try {
            Location location = new Location(x, y);
            Resource resource = new Resource(type, q);
            location.resource = resource;
            JSONObject requestJson = createLocationDTO(location);

            return parseLocation(postRequest("/locations", requestJson));
        } catch (Exception e) {
            throw new MapAPIException("Failed to create resource", e);
        }
    }

    public Location createLocation(Location location) throws MapAPIException {
        try {
            JSONObject requestJson = createLocationDTO(location);

            return parseLocation(postRequest("/locations", requestJson));
        } catch (Exception e) {
            throw new MapAPIException("Failed to create location", e);
        }
    }

    private void setRequestHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
    }

    private JSONObject postRequest(String endpoint, JSONObject requestJson) throws Exception {
        URL url = new URL(apiBase + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            setRequestHeaders(conn);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readResponse(conn.getInputStream());
                return new JSONObject(responseBody);
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new Exception("API request failed with code " + responseCode + ": " + error);
            }
        } finally {
            conn.disconnect();
        }
    }

    private String readResponse(java.io.InputStream stream) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            return response.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readNonNullField(JSONObject json, String key) throws JSONException {
        return json.has(key) && !json.isNull(key) ? (T) json.get(key) : null;
    }

    private <T> void addNonNullField(JSONObject json, String key, T value) throws JSONException {
        if (value != null) {
            json.put(key, value);
        }
    }

    private JSONObject createPlayerDTO(Player player) throws JSONException {
        JSONObject requestJson = new JSONObject();

        requestJson.put("username", player.username);
        requestJson.put("lastX", player.lastX);
        requestJson.put("lastY", player.lastY);

        return requestJson;
    }

    private JSONObject createLocationDTO(Location location) throws JSONException, IllegalArgumentException {
        JSONObject requestJson = new JSONObject();

        requestJson.put("x", location.x);
        requestJson.put("y", location.y);

        addNonNullField(requestJson, "title", location.title);
        addNonNullField(requestJson, "icon", location.icon);
        addNonNullField(requestJson, "color", location.color);
        addNonNullField(requestJson, "notes", location.notes);

        if (location.resource != null) {
            if (location.resource.type == null) {
                throw new IllegalArgumentException("Resource type must not be null");
            }

            JSONObject resourceJson = new JSONObject();

            resourceJson.put("type", location.resource.type);
            resourceJson.put("q", location.resource.q);

            requestJson.put("resource", resourceJson);
        }

        if (location.claim != null) {
            if (location.claim.type == null) {
                throw new IllegalArgumentException("Claim type must not be null");
            }

            JSONObject claimJson = new JSONObject();

            claimJson.put("type", location.claim.type.name());
            addNonNullField(claimJson, "status", location.claim.status.name());
            addNonNullField(claimJson, "ownerName", location.claim.ownerName);
            addNonNullField(claimJson, "width", location.claim.width);
            addNonNullField(claimJson, "height", location.claim.height);

            requestJson.put("claim", claimJson);
        }

        if (location.poi != null) {
            if (location.poi.category == null) {
                throw new IllegalArgumentException("PointOfInterest category must not be null");
            }
            JSONObject poiJson = new JSONObject();

            poiJson.put("category", location.poi.category);

            requestJson.put("poi", poiJson);
        }

        return requestJson;
    }

    private Player parsePlayer(JSONObject json) throws JSONException {
        String id = json.getString("id");
        String username = json.getString("username");
        double lastX = json.getDouble("lastX");
        double lastY = json.getDouble("lastY");

        Player player = new Player(username, lastX, lastY);
        player.id = id;

        return player;
    }

    private Location parseLocation(JSONObject json) throws JSONException {
        Double x = json.getDouble("x");
        Double y = json.getDouble("y");

        Location location = new Location(x, y);
        location.id = json.getString("id");

        location.title = readNonNullField(json, "title");
        location.icon = readNonNullField(json, "icon");
        location.color = readNonNullField(json, "color");
        location.notes = readNonNullField(json, "notes");

        if (json.has("resource") && !json.isNull("resource")) {
            location.resource = parseResource(json.getJSONObject("resource"));
        }
        if (json.has("claim") && !json.isNull("claim")) {
            location.claim = parseClaim(json.getJSONObject("claim"));
        }
        if (json.has("poi") && !json.isNull("poi")) {
            location.poi = parsePointOfInterest(json.getJSONObject("poi"));
        }

        return location;
    }

    private Resource parseResource(JSONObject json) throws JSONException {

        String type = json.getString("type");
        int q = json.getInt("q");

        Resource resource = new Resource(type, q);

        return resource;
    }

    private Claim parseClaim(JSONObject json) throws JSONException {
        ClaimType type = ClaimType.valueOf(json.getString("type"));
        Claim claim = new Claim(type);

        claim.status = ClaimStatus.valueOf(json.getString("status"));
        claim.ownerName = readNonNullField(json, "ownerName");
        claim.width = readNonNullField(json, "width");
        claim.height = readNonNullField(json, "height");

        return claim;
    }

    private PointOfInterest parsePointOfInterest(JSONObject json) throws JSONException {
        String category = json.getString("category");

        PointOfInterest poi = new PointOfInterest(category);

        return poi;
    }
}
