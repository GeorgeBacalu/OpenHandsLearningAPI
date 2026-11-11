package com.project.ohl.adapter.directory;

import com.project.ohl.domain.model.SchoolDirectory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import static com.project.ohl.domain.model.SchoolDirectory.sourceId;

public class SchoolDirectoryParser {
    public SchoolDirectory parse(byte[] geography, byte[] schools) throws IOException {
        var counties = new LinkedHashMap<String, SchoolDirectory.County>();
        var cities = new LinkedHashMap<String, SchoolDirectory.City>();
        var rows = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader().setSkipHeaderRecord(true).get()
                .parse(new StringReader(new String(geography, StandardCharsets.UTF_8).replaceFirst("^\uFEFF", ""))).getRecords();
        for (var row : rows) {
            if (row.get("NIV").equals("1")) {
                String name = text(row.get("DENLOC")).replaceFirst("^(JUDEȚUL|MUNICIPIUL) ", "");
                var county = new SchoolDirectory.County(sourceId("county", row.get("SIRUTA")), name);
                if (counties.put(row.get("JUD"), county) != null) throw new IOException("Duplicate county code");
            }
        }
        for (var row : rows) {
            if (row.get("NIV").equals("3")) {
                var county = counties.get(row.get("JUD"));
                if (county == null) throw new IOException("Locality has no Romanian county");
                var city = new SchoolDirectory.City(sourceId("city", row.get("SIRUTA")), text(row.get("DENLOC")), county.id());
                if (cities.put(row.get("SIRUTA"), city) != null) throw new IOException("Duplicate locality code");
            }
        }
        return new SchoolDirectory(new ArrayList<>(counties.values()), new ArrayList<>(cities.values()),
                parseSchools(schools, cities));
    }

    private List<SchoolDirectory.School> parseSchools(byte[] bytes, Map<String, SchoolDirectory.City> cities) throws IOException {
        var schools = new ArrayList<SchoolDirectory.School>();
        var ids = new HashSet<UUID>();
        var formatter = new DataFormatter();
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            var header = sheet.getRow(3);
            Map<Integer, String> expected = Map.of(8, "Cod SIRUTA unitate", 11, "Cod SIIIR unitate", 13, "Denumire lunga unitate");
            for (var entry : expected.entrySet()) {
                if (header == null || !formatter.formatCellValue(header.getCell(entry.getKey())).equals(entry.getValue()))
                    throw new IOException("School workbook format changed");
            }
            for (int index = 4; index <= sheet.getLastRowNum(); index++) {
                var row = sheet.getRow(index);
                if (row == null) continue;
                String code = formatter.formatCellValue(row.getCell(11)).strip();
                String name = text(formatter.formatCellValue(row.getCell(13)));
                String cityCode = formatter.formatCellValue(row.getCell(8)).strip();
                if (code.isEmpty() && name.isEmpty() && cityCode.isEmpty()) continue;
                if (!code.matches("[0-9]{10}") || name.isBlank() || name.length() > 255)
                    throw new IOException("Invalid school at workbook row " + (index + 1));
                var city = cities.get(cityCode);
                if (city == null) throw new IOException("School locality is absent from SIRUTA: " + cityCode);
                UUID id = sourceId("school", code);
                if (!ids.add(id)) throw new IOException("Duplicate school code: " + code);
                schools.add(new SchoolDirectory.School(id, name, city.id()));
            }
        }
        return schools;
    }

    private static String text(String value) {
        return value.strip().replace('ş', 'ș').replace('Ş', 'Ș').replace('ţ', 'ț').replace('Ţ', 'Ț');
    }
}
