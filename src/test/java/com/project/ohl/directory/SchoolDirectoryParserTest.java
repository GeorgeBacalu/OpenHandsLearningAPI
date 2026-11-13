package com.project.ohl.directory;

import com.project.ohl.adapter.directory.SchoolDirectoryParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import static com.project.ohl.domain.model.SchoolDirectory.sourceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchoolDirectoryParserTest {
    private final SchoolDirectoryParser parser = new SchoolDirectoryParser();
    private final byte[] geography = ("\uFEFFSIRUTA;DENLOC;JUD;NIV\n"
            + "10;JUDEŢUL ALBA;1;1\n1017;MUNICIPIUL ALBA IULIA;1;2\n"
            + "1026;ALBA IULIA;1;3\n1035;BĂRĂBANŢ;1;3\n").getBytes(StandardCharsets.UTF_8);

    @Test void linksSchoolsByOfficialCodesAndProducesRepeatableUuids() throws IOException {
        byte[] workbook = workbook("1035", false);
        var first = parser.parse(geography, workbook);
        assertThat(first).isEqualTo(parser.parse(geography, workbook));
        assertThat(first.counties()).singleElement().satisfies(county -> assertThat(county.name()).isEqualTo("ALBA"));
        assertThat(first.cities()).hasSize(2);
        assertThat(first.schools()).singleElement().satisfies(school -> {
            assertThat(school.id()).isEqualTo(sourceId("school", "0161102892"));
            assertThat(school.cityId()).isEqualTo(sourceId("city", "1035"));
            assertThat(school.name()).isEqualTo("ȘCOALA DE TEST");
        });
    }

    @Test void rejectsUnmatchedLocationsInsteadOfGuessing() throws IOException {
        byte[] workbook = workbook("999999", false);
        assertThatThrownBy(() -> parser.parse(geography, workbook)).isInstanceOf(IOException.class)
                .hasMessageContaining("absent from SIRUTA");
    }

    @Test void rejectsDuplicateOfficialSchoolIds() throws IOException {
        byte[] workbook = workbook("1035", true);
        assertThatThrownBy(() -> parser.parse(geography, workbook)).isInstanceOf(IOException.class)
                .hasMessageContaining("Duplicate school code");
    }

    private byte[] workbook(String city, boolean duplicate) throws IOException {
        try (var book = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = book.createSheet();
            var header = sheet.createRow(3);
            header.createCell(8).setCellValue("Cod SIRUTA unitate");
            header.createCell(11).setCellValue("Cod SIIIR unitate");
            header.createCell(13).setCellValue("Denumire lunga unitate");
            for (int i = 4; i <= (duplicate ? 5 : 4); i++) {
                var row = sheet.createRow(i);
                row.createCell(8).setCellValue(city);
                row.createCell(11).setCellValue("0161102892");
                row.createCell(13).setCellValue("ŞCOALA DE TEST");
            }
            book.write(output);
            return output.toByteArray();
        }
    }
}
