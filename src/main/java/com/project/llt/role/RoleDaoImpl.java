package com.project.llt.role;

import com.project.llt.role.enums.Authority;
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
public class RoleDaoImpl implements RoleDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("roles").usingGeneratedKeyColumns("id");
    }

    RowMapper<Role> roleRowMapper = (resultSet, rowNumber) -> Role.builder()
          .id(resultSet.getLong("id"))
          .authority(Authority.valueOf(resultSet.getString("authority")))
          .build();

    @Override
    public List<Role> findAll() {
        return jdbcTemplate.query("SELECT * FROM roles", roleRowMapper);
    }

    @Override
    public Optional<Role> findById(Long id) {
        try(Stream<Role> stream = jdbcTemplate.queryForStream("SELECT * FROM roles WHERE id = ?", roleRowMapper, id)) {
            return stream.findFirst();
        }
    }

    @Override
    @Transactional
    public Role save(Role role) {
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(Map.of("authority", role.getAuthority()));
        role.setId(generatedId.longValue());
        return role;
    }

    @Override
    @Transactional
    public Role update(Role role) {
        jdbcTemplate.update("UPDATE roles SET authority = ? WHERE id = ?", String.valueOf(role.getAuthority()), role.getId());
        return role;
    }

    @Override
    @Transactional
    public void delete(Role role) {
        jdbcTemplate.update("DELETE FROM roles WHERE id = ?", role.getId());
    }
}
