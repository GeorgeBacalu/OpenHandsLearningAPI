package com.project.llt.institution;

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
public class InstitutionDaoImpl implements InstitutionDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public InstitutionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("institutions").usingGeneratedKeyColumns("id");
    }

    RowMapper<Institution> institutionRowMapper = (resultSet, rowNumber) -> Institution.builder()
          .id(resultSet.getLong("id"))
          .school(resultSet.getString("school"))
          .classroom(resultSet.getString("classroom"))
          .build();

    @Override
    public List<Institution> findAll() {
        return jdbcTemplate.query("SELECT * FROM institutions", institutionRowMapper);
    }

    @Override
    public Optional<Institution> findById(Long id) {
        try(Stream<Institution> stream = jdbcTemplate.queryForStream("SELECT * FROM institutions WHERE id = ?", institutionRowMapper, id)) {
            return stream.findFirst();
        }
    }

    @Override
    @Transactional
    public Institution save(Institution institution) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("school", institution.getSchool());
        parameters.put("classroom", institution.getClassroom());
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        institution.setId(generatedId.longValue());
        return institution;
    }

    @Override
    @Transactional
    public Institution update(Institution institution) {
        jdbcTemplate.update("UPDATE institutions SET school = ?, classroom = ? WHERE id = ?", institution.getSchool(), institution.getClassroom(), institution.getId());
        return institution;
    }

    @Override
    @Transactional
    public void delete(Institution institution) {
        jdbcTemplate.update("DELETE FROM institutions WHERE id = ?", institution.getId());
    }
}
