package com.hollandjake.dogbot.repository;

import com.hollandjake.dogbot.model.Image;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.unit.DataSize;

import java.sql.Blob;
import java.util.Optional;

@Slf4j
@Repository
@CacheConfig(cacheNames = "images")
public class ImageRepository implements ComponentRepository<Image> {
    private final JdbcTemplate template;
    @Value("${image.max-size:1MB}")
    public DataSize maxImageBytes;

    public ImageRepository(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.blob")
    })
    public Image findById(Integer id) {
        return template.query(
                "CALL GetImage(?)",
                getMapper(),
                id
        ).stream()
                .findAny()
                .orElse(null);
    }

    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.blob")
    })
    public Image findByImageBlob(Blob data) {
        return template.query(
                "CALL GetOrCreateImage(?)",
                getMapper(),
                data).stream()
                .findAny()
                .orElse(null);
    }

    @Override
    @Caching(put = {
            @CachePut(key = "#result.id"),
            @CachePut(key = "#result.blob")
    })
    public Image save(Image object) {
        if (object.getId() != null) {
            return object;
        } else {
            return findByImageBlob(object.getBlob());
        }
    }

    public RowMapper<Image> getMapper() {
        return (resultSet, i) ->
                Image.builder()
                        .id(resultSet.getInt("image_id"))
                        .blob(resultSet.getBlob("data"))
                        .maxBytes(maxImageBytes.toBytes())
                        .build();
    }

    @Caching(put = {
            @CachePut(key = "#result.id", condition = "#result instanceof T(com.hollandjake.dogbot.model.Image)"),
            @CachePut(key = "#result.blob", condition = "#result instanceof T(com.hollandjake.dogbot.model.Image)")
    })
    public MessageComponent getRandomImageForModule(Integer moduleId) {
        return Optional.ofNullable((MessageComponent) template.queryForObject(
                "SELECT i.image_id, data "
                        + "FROM image i "
                        + "JOIN module_image mi on i.image_id = mi.image_id "
                        + "WHERE module_id = ? "
                        + "ORDER BY RAND() "
                        + "LIMIT 1",
                getMapper(),
                moduleId)
        ).orElseGet(() -> Text.fromString("Image couldn't load"));
    }
}
