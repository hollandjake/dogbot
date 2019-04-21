package com.hollandjake.dogbot.util.module;

import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.model.Text;
import com.hollandjake.dogbot.service.ImageService;
import com.hollandjake.dogbot.service.MessageService;
import com.hollandjake.dogbot.service.TextService;
import com.hollandjake.dogbot.util.exceptions.MissingModuleException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public abstract class DatabaseModule extends Module {
    @NonNull
    protected final JdbcTemplate template;
    @NonNull
    @Getter(value = AccessLevel.PACKAGE)
    private final Integer moduleId;
    private final ImageService imageService;
    private final TextService textService;

    public DatabaseModule(
            MessageService messageService,
            JdbcTemplate template) {
        super(messageService);
        this.template = template;
        this.moduleId = getModuleIdFromDB(template);
        this.imageService = messageService.getImageService();
        this.textService = messageService.getTextService();
    }

    private String getModuleName() {
        return getClass().getSimpleName();
    }

    private Integer getModuleIdFromDB(JdbcTemplate template) {
        return Optional.ofNullable(template.queryForObject(
                "SELECT GetOrCreateModuleId(?)",
                Integer.class,
                getModuleName()
        )).orElseThrow(() -> new MissingModuleException(
                "The module " + getModuleName() + " cannot be found in the database")
        );
    }

    public MessageComponent getRandomImage() {
        return imageService.getRandomImageForModule(moduleId);
    }

    public Text getRandomResponse() {
        return textService.getRandomResponseForModule(moduleId);
    }

    public RowMapper<Integer> getMapper() {
        return (resultSet, i) -> resultSet.getInt("module_id");
    }
}
