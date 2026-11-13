package com.project.ohl.adapter.persistence.directory;

import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.project.ohl.domain.model.SchoolDirectory.sourceId;

@Repository
@Transactional(readOnly = true)
public class SchoolDirectoryPersistenceAdapter implements SchoolDirectoryPort {
    private final JdbcTemplate database;

    public SchoolDirectoryPersistenceAdapter(JdbcTemplate database) {
        this.database = database;
    }

    @Override
    public List<SchoolDirectory.County> counties() {
        return database.query("SELECT id, name FROM counties ORDER BY name, id",
                (row, index) -> new SchoolDirectory.County(row.getObject(1, UUID.class), row.getString(2)));
    }

    @Override
    public List<SchoolDirectory.City> cities(UUID countyId, String name, int offset, int limit) {
        validatePage(offset, limit);
        return database.query("SELECT id, name, county_id FROM cities WHERE county_id = ? AND lower(name) LIKE ? ESCAPE '!' ORDER BY name, id LIMIT ? OFFSET ?",
                (row, index) -> new SchoolDirectory.City(row.getObject(1, UUID.class), row.getString(2), row.getObject(3, UUID.class)),
                countyId, pattern(name), limit, offset);
    }

    @Override
    public List<SchoolDirectory.School> schools(UUID cityId, String name, int offset, int limit) {
        validatePage(offset, limit);
        return database.query("SELECT id, name, city_id FROM schools WHERE city_id = ? AND lower(name) LIKE ? ESCAPE '!' ORDER BY name, id LIMIT ? OFFSET ?",
                (row, index) -> new SchoolDirectory.School(row.getObject(1, UUID.class), row.getString(2), row.getObject(3, UUID.class)),
                cityId, pattern(name), limit, offset);
    }

    private static String pattern(String value) {
        return "%" + value.strip().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private static void validatePage(int offset, int limit) {
        if (offset < 0 || limit < 1 || limit > 200) throw new InvalidRequestException("Use offset >= 0 and limit between 1 and 200.");
    }

    @Override
    @Transactional
    public void importDirectory(SchoolDirectory data) {
        if (data.counties().size() != 42 || data.cities().size() < 13000 || data.schools().size() < 15000)
            throw new IllegalArgumentException("A complete Romania directory is required");
        database.execute("SELECT pg_advisory_xact_lock(782134920)");
        database.batchUpdate("INSERT INTO counties (id, name) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name WHERE counties.name IS DISTINCT FROM EXCLUDED.name",
                data.counties().stream().map(row -> new Object[]{row.id(), row.name()}).toList());
        upsert("cities", "county_id", data.cities().stream().map(row -> new Object[]{row.id(), row.name(), row.countyId()}).toList());
        upsert("schools", "city_id", data.schools().stream().map(row -> new Object[]{row.id(), row.name(), row.cityId()}).toList());
        replacePlaceholders();
        database.execute("ALTER TABLE schools VALIDATE CONSTRAINT schools_city_required");
    }

    private void upsert(String table, String parent, List<Object[]> rows) {
        String sql = "INSERT INTO " + table + " (id, name, " + parent + ") VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, "
                + parent + " = EXCLUDED." + parent + " WHERE " + table + ".name IS DISTINCT FROM EXCLUDED.name OR "
                + table + "." + parent + " IS DISTINCT FROM EXCLUDED." + parent;
        for (int start = 0; start < rows.size(); start += 500) database.batchUpdate(sql, rows.subList(start, Math.min(start + 500, rows.size())));
    }

    private void replacePlaceholders() {
        var replacements = Map.of(
                UUID.fromString("103ac659-e577-388b-a794-67fff7f04ca3"), new String[]{"Scoala Gimnaziala Nr. 1", "2361101277"},
                UUID.fromString("d507dc18-15f4-35b8-83ec-548ee8b2ffc6"), new String[]{"Scoala Gimnaziala Nr. 2", "2362101883"},
                UUID.fromString("e5bfa376-f632-3683-8207-91fc4924cc50"), new String[]{"Scoala Gimnaziala Nr. 3", "2362100534"});
        replacements.forEach((oldId, school) -> {
            var existing = database.queryForList("SELECT name FROM schools WHERE id = ? FOR UPDATE", String.class, oldId);
            if (existing.isEmpty()) return;
            if (!existing.getFirst().equals(school[0])) throw new IllegalStateException("Placeholder school has been changed; review its replacement");
            UUID replacement = sourceId("school", school[1]);
            if (database.queryForObject("SELECT COUNT(*) FROM schools WHERE id = ?", Integer.class, replacement) != 1)
                throw new IllegalStateException("Replacement school is absent from the government dataset");
            database.update("UPDATE users SET school_id = ? WHERE school_id = ?", replacement, oldId);
            database.update("DELETE FROM schools WHERE id = ?", oldId);
        });
    }
}
