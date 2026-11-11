package com.project.llt.user;

import com.project.llt.institution.Institution;
import com.project.llt.institution.InstitutionDao;
import com.project.llt.role.Role;
import com.project.llt.role.RoleDao;
import java.time.LocalDate;
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
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private InstitutionDao institutionDao;
    private RoleDao roleDao;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public UserDaoImpl(JdbcTemplate jdbcTemplate, InstitutionDao institutionDao, RoleDao roleDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.institutionDao = institutionDao;
        this.roleDao = roleDao;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("id");
    }

    RowMapper<User> userRowMapper = (resultSet, rowNumber) -> {
        Institution institution = institutionDao.findById(resultSet.getObject("institution_id", Long.class)).orElse(null);
        Role role = roleDao.findById(resultSet.getObject("role_id", Long.class)).orElse(null);
        return User.builder()
              .id(resultSet.getLong("id"))
              .name(resultSet.getString("name"))
              .email(resultSet.getString("email"))
              .password(resultSet.getString("password"))
              .mobile(resultSet.getString("mobile"))
              .address(resultSet.getString("address"))
              .birthday(resultSet.getObject("birthday", LocalDate.class))
              .institution(institution)
              .role(role)
              .build();
    };

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        try(Stream<User> stream = jdbcTemplate.queryForStream("SELECT * FROM users WHERE id = ?", userRowMapper, id)) {
            return stream.findFirst();
        }
    }

    @Override
    @Transactional
    public User save(User user) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", user.getName());
        parameters.put("email", user.getEmail());
        parameters.put("password", user.getPassword());
        parameters.put("mobile", user.getMobile());
        parameters.put("address", user.getAddress());
        parameters.put("birthday", user.getBirthday());
        if(user.getInstitution() != null) parameters.put("institution_id", user.getInstitution().getId());
        if(user.getRole() != null) parameters.put("role_id", user.getRole().getId());
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(generatedId.longValue());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        jdbcTemplate.update("UPDATE users SET name = ?, email = ?, password = ?, mobile = ?, address = ?, birthday = ?, institution_id = ?, role_id = ? WHERE id = ?",
              user.getName(),
              user.getEmail(),
              user.getPassword(),
              user.getMobile(),
              user.getAddress(),
              user.getBirthday(),
              user.getInstitution() != null ? user.getInstitution().getId() : null,
              user.getRole() != null ? user.getRole().getId() : null,
              user.getId());
        return user;
    }

    @Override
    @Transactional
    public void delete(User user) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
    }
}
