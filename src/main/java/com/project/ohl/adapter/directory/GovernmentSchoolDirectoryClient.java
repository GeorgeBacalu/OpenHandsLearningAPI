package com.project.ohl.adapter.directory;

import com.project.ohl.domain.model.SchoolDirectory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import tools.jackson.databind.json.JsonMapper;

public class GovernmentSchoolDirectoryClient {
    private static final String API = "https://data.gov.ro/api/3/action/package_show?id=";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final JsonMapper json = JsonMapper.builder().build();

    public SchoolDirectory download() throws IOException, InterruptedException {
        byte[] geography = resource("siruta_s1-2026", "CSV");
        byte[] schools = resource("retea-scolara-2025-2026", "XLSX");
        var directory = new SchoolDirectoryParser().parse(geography, schools);
        if (directory.counties().size() != 42
                || directory.cities().size() < 13000 || directory.schools().size() < 15000)
            throw new IOException("The Romanian directory is incomplete; no records have been imported");
        return directory;
    }

    private byte[] resource(String dataset, String format) throws IOException, InterruptedException {
        var response = json.readTree(get(URI.create(API + dataset)));
        if (!response.path("success").asBoolean()) throw new IOException("Government catalog request failed");
        for (var resource : response.path("result").path("resources")) {
            if (format.equalsIgnoreCase(resource.path("format").asString())) {
                URI uri = URI.create(resource.path("url").asString());
                if (!"https".equals(uri.getScheme()) || !"data.gov.ro".equals(uri.getHost()))
                    throw new IOException("Unexpected government resource host");
                return get(uri);
            }
        }
        throw new IOException("Government dataset has no " + format + " resource");
    }

    private byte[] get(URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(90))
                .header("User-Agent", "OpenHandsLearning school directory importer").GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200 || response.body().length == 0 || response.body().length > 20_000_000)
            throw new IOException("Government resource download failed: HTTP " + response.statusCode());
        return response.body();
    }
}
