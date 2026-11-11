package com.project.ohl.category;

import com.project.ohl.section.Section;
import com.project.ohl.section.SectionDao;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CategoryDaoImpl implements CategoryDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SectionDao sectionDao;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public CategoryDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, SectionDao sectionDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.sectionDao = sectionDao;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("categories").usingGeneratedKeyColumns("id");
    }

    RowMapper<Category> categoryRowMapper = (resultSet, rowNumber) -> {
        Category parent = findById(resultSet.getObject("parent_id", Long.class)).orElse(null);
        Section section = sectionDao.findById(resultSet.getObject("section_id", Long.class)).orElse(null);
        return Category.builder()
              .id(resultSet.getLong("id"))
              .name(resultSet.getString("name"))
              .imageId(resultSet.getInt("image_id"))
              .isExpanded(resultSet.getBoolean("is_expanded"))
              .isFavorite(resultSet.getBoolean("is_favorite"))
              .parent(parent)
              .section(section)
              .build();
    };

    @Override
    public List<Category> findAll() {
        return jdbcTemplate.query("SELECT * FROM categories", categoryRowMapper);
    }

    @Override
    public Optional<Category> findById(Long id) {
        try (Stream<Category> stream = jdbcTemplate.queryForStream("SELECT * FROM categories WHERE id = ?", categoryRowMapper, id)) {
            return stream.findFirst();
        }
    }

    @Override
    @Transactional
    public Category save(Category category) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", category.getName());
        parameters.put("image_id", category.getImageId());
        parameters.put("is_expanded", category.getIsExpanded());
        parameters.put("is_favorite", category.getIsFavorite());
        if(category.getParent() != null) parameters.put("parent_id", category.getParent().getId());
        if(category.getSection() != null) parameters.put("section_id", category.getSection().getId());
        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        category.setId(generatedId.longValue());
        return category;
    }

    @Override
    @Transactional
    public Category update(Category category) {
        jdbcTemplate.update("UPDATE categories SET name = ?, image_id = ?, is_expanded = ?, is_favorite = ?, parent_id = ?, section_id = ? WHERE id = ?",
              category.getName(),
              category.getImageId(),
              category.getIsExpanded(),
              category.getIsFavorite(),
              category.getParent() != null ? category.getParent().getId() : null,
              category.getSection() != null ? category.getSection().getId() : null,
              category.getId());
        return category;
    }

    @Override
    @Transactional
    public void delete(Category category) {
        jdbcTemplate.update("DELETE FROM categories WHERE id = ?", category.getId());
    }

    @Override
    public List<Category> findByParentIdNullAndSectionId(Long sectionId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sectionId", sectionId);
        return namedParameterJdbcTemplate.query("SELECT * FROM categories WHERE parent_id IS NULL AND section_id = :sectionId", parameters, categoryRowMapper);
    }

    @Override
    public List<Category> findByParentIdNullAndSectionIdAndNameContaining(Long sectionId, String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sectionId", sectionId);
        parameters.put("name", "%" + name + "%");
        return namedParameterJdbcTemplate.query("SELECT * FROM categories WHERE parent_id IS NULL AND section_id = :sectionId AND name LIKE :name", parameters, categoryRowMapper);
    }

    @Override
    public List<Category> findByParentIdAndSectionId(Long parentId, Long sectionId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("parentId", parentId);
        parameters.put("sectionId", sectionId);
        return namedParameterJdbcTemplate.query("SELECT * FROM categories WHERE parent_id = :parentId AND section_id = :sectionId", parameters, categoryRowMapper);
    }

    @Override
    public List<Category> findByParentIdAndSectionIdAndNameContaining(Long parentId, Long sectionId, String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("parentId", parentId);
        parameters.put("sectionId", sectionId);
        parameters.put("name", "%" + name + "%");
        return namedParameterJdbcTemplate.query("SELECT * FROM categories WHERE parent_id = :parentId AND section_id = :sectionId AND name LIKE :name", parameters, categoryRowMapper);
    }

    @Override
    public List<Category> findFavorites() {
        return jdbcTemplate.query("SELECT * FROM categories WHERE is_favorite = TRUE", categoryRowMapper);
    }

    @Override
    public List<Category> findFavoritesByNameContaining(String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "%" + name + "%");
        return namedParameterJdbcTemplate.query("SELECT * FROM categories WHERE is_favorite = TRUE AND name LIKE :name", parameters, categoryRowMapper);
    }
}
