package com.project.llt.section;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SectionDaoImpl implements SectionDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public SectionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("sections").usingGeneratedKeyColumns("id");
    }

    RowMapper<Section> sectionRowMapper = (resultSet, rowNumber) -> Section.builder()
          .id(resultSet.getLong("id"))
          .name(resultSet.getString("name"))
          .iconId(resultSet.getInt("icon_id"))
          .imageId(resultSet.getInt("image_id"))
          .build();

    @Override
    public List<Section> findAll() {
        return jdbcTemplate.query("SELECT * FROM sections", sectionRowMapper);
    }

    @Override
    public Optional<Section> findById(Long id) {
        try(Stream<Section> stream = jdbcTemplate.queryForStream("SELECT * FROM sections WHERE id = ?", sectionRowMapper, id)) {
            return stream.findFirst();
        }
    }

    @Override
    @Transactional
    public Section save(Section section) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", section.getName());
        parameters.put("icon_id", section.getIconId());
        parameters.put("image_id", section.getImageId());
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        section.setId(generatedId.longValue());
        return section;
    }

    @Override
    @Transactional
    public Section update(Section section) {
        jdbcTemplate.update("UPDATE sections SET name = ?, icon_id = ?, image_id = ? WHERE id = ?", section.getName(), section.getIconId(), section.getImageId(), section.getId());
        return section;
    }

    @Override
    @Transactional
    public void delete(Section section) {
        jdbcTemplate.update("DELETE FROM sections WHERE id = ?", section.getId());
    }
}
