package com.hollandjake.dogbot;

import com.hollandjake.dogbot.model.Thread;
import com.hollandjake.dogbot.model.*;
import com.hollandjake.dogbot.module.EightBall;
import com.hollandjake.dogbot.module.ExtraGoodDogs;
import com.hollandjake.dogbot.module.Gran;
import com.hollandjake.dogbot.module.Reacts;
import com.hollandjake.dogbot.module.core.Boot;
import com.hollandjake.dogbot.module.reddit.Birds;
import com.hollandjake.dogbot.module.reddit.Cats;
import com.hollandjake.dogbot.module.reddit.Dogs;
import com.hollandjake.dogbot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@SpringBootApplication
@EnableConfigurationProperties(DatabaseConfiguration.class)
@Slf4j
@ComponentScan(basePackages = {"com.hollandjake.dogbot"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        value = {Controller.class, Service.class}),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        value = {Boot.class})
        })
public class SystemLoader implements CommandLineRunner {
    private final Environment env;
    private final JdbcTemplate template;
    private final MessageService messageService;
    private final MessageComponentService messageComponentService;
    private final ImageService imageService;
    private final TextService textService;
    private final HumanService humanService;
    private final ThreadService threadService;
    private final ModuleService moduleService;

    @Value("${image.max-size:1MB}")
    public DataSize maxImageBytes;
    private Thread thread;
    private Human johnSmith;
    private Human rexAtkinson;

    @Autowired
    public SystemLoader(Environment env,
                        MessageService messageService,
                        JdbcTemplate template,
                        MessageComponentService messageComponentService,
                        ImageService imageService,
                        TextService textService,
                        HumanService humanService,
                        ThreadService threadService,
                        ModuleService moduleService) {
        this.env = env;
        this.template = template;
        this.messageService = messageService;
        this.messageComponentService = messageComponentService;
        this.imageService = imageService;
        this.textService = textService;
        this.humanService = humanService;
        this.threadService = threadService;
        this.moduleService = moduleService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SystemLoader.class, args);
    }

    @Override
    public void run(String... args) {
        createDB();
        createBots();
        this.thread = threadService.findByUrl(env.getRequiredProperty("thread.name"));
        createBirds();
        createBoot();
        createCats();
        createDogs();
        createEightBall();
        createExtraGoodDogs();
        createGran();
        createReacts();
        createMessages();
    }

    private void createBots() {
        this.rexAtkinson = humanService.findByName("Rex Atkinson");
        this.johnSmith = humanService.findByName("John Smith");
        log.info("Saved Bots");
    }

    @Transactional(rollbackFor = Exception.class)
    protected void createMessagesForFile(Path p) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        InputStreamReader r = new InputStreamReader(new FileInputStream(p.toFile()), StandardCharsets.UTF_8);
        JSONObject file = (JSONObject) jsonParser.parse(r);
        r.close();
        file.remove("participants");
        file.remove("title");
        file.remove("is_still_participant");
        file.remove("thread_type");
        file.remove("thread_path");

        JSONArray messages = (JSONArray) file.get("messages");
        Arrays.stream(messages.toArray())
                .map(jm -> {
                    JSONObject jsonMessage = (JSONObject) jm;
                    String senderName = (String) jsonMessage.get("sender_name");
                    if (senderName.equals(rexAtkinson.getName())
                            || senderName.equals(johnSmith.getName())) {
                        return null;
                    } else if (jsonMessage.containsKey("content")) {
                        String text = ((String) jsonMessage.get("content"));
                        if (text.matches(".+? voted for \".+?\" in the poll: .+?") ||
                                text.matches(".+? set the emoji to .+?") ||
                                text.matches(".+? set the nickname for .+?") ||
                                text.matches(".+? left the group.") ||
                                text.matches(".+? named the group .+?") ||
                                text.matches(".+? changed the group photo.") ||
                                text.matches(".+? responded .+? to .+?") ||
                                text.matches(".+? created a poll: .+?") ||
                                text.matches(".+? removed their vote for .+?")) {
                            return null;
                        }
                    } else if (!jsonMessage.containsKey("photos")) {
                        return null;
                    }
                    jsonMessage.remove("type");
                    jsonMessage.remove("share");
                    jsonMessage.remove("reactions");
                    if (jsonMessage.containsKey("timestamp_ms")) {
                        return jsonMessage;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(a -> (long) a.get("timestamp_ms")))
                .map(jsonMessage -> {
                    Human sender = humanService.findByName((String) jsonMessage.get("sender_name"));
                    LocalDateTime date = new Timestamp((long) jsonMessage.get("timestamp_ms")).toLocalDateTime();
                    List<MessageComponent> components = new ArrayList<>();
                    if (jsonMessage.containsKey("content")) {
                        Text text = Text.fromString((String) jsonMessage.get("content"));
                        components.add(text);
                    }
                    if (jsonMessage.containsKey("photos")) {
                        for (Object ph : (JSONArray) jsonMessage.get("photos")) {
                            JSONObject photo = (JSONObject) ph;
                            String photoName = ((String) photo.get("uri")).replaceAll(".+(/photos/.+)", "$1");
                            MessageComponent image = Image.fromInputStream(getClass().getResourceAsStream(
                                    "/Messages/" + thread.getUrl() + photoName), maxImageBytes.toBytes());
                            if (image != null) {
                                components.add(image);
                            }
                        }
                    }

                    if (!components.isEmpty()) {
                        return Message.builder()
                                .thread(thread)
                                .sender(sender)
                                .messageComponents(components)
                                .timestamp(date)
                                .build();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEachOrdered(x -> log.info("Saved {}", messageService.save(x).prettyPrint()));
        p.toFile().renameTo(new File(p.toFile().getPath().replace(".json", ".json_processed")));
    }

    private void createMessages() {
        log.info("Saving messages");
        String path = new File(getClass().getResource("/Messages/" + thread.getUrl()).getPath()).getPath();
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            walk.filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(a -> Integer.valueOf(a.toString()
                            .replaceAll(".+message_(\\d+).json", "$1")))
                            .reversed())
                    .forEach(p -> {
                        try {
                            createMessagesForFile(p);
                        } catch (IOException | ParseException e) {
                            log.error("Error", e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error", e);
        }

    }

    private void createBirds() {
        String moduleName = Birds.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            saveSubreddits(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createBoot() {
        String moduleName = Boot.class.getSimpleName();

        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            saveResponseImages(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createCats() {
        String moduleName = Cats.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            saveSubreddits(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createDogs() {
        String moduleName = Dogs.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            saveSubreddits(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createEightBall() {
        String moduleName = EightBall.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createExtraGoodDogs() {
        String moduleName = ExtraGoodDogs.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseImages(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createGran() {
        String moduleName = Gran.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseText(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void createReacts() {
        String moduleName = Reacts.class.getSimpleName();
        if (moduleService.doesModuleExist(moduleName) == null) {
            saveResponseImages(moduleName);
            log.info("Saved {}", moduleName);
        }
    }

    private void saveResponseImages(String moduleName) {
        Arrays.stream(readFile("/" + moduleName + "/images.txt").replaceAll("\r", "").split("\n"))
                .forEachOrdered(url -> {
                    MessageComponent component = Image.fromUrl(url, maxImageBytes.toBytes());
                    if (component instanceof Image) {
                        template.update(
                                "INSERT IGNORE INTO module_image (module_id, image_id) VALUES (GetOrCreateModuleId(?), GetOrCreateImageId(?))",
                                moduleName,
                                ((Image) component).getBlob());
                    } else if (component instanceof Text) {
                        saveResponseText(moduleName, ((Text) component).getData());
                    }
                });
    }

    private void saveResponseText(String moduleName, String content) {
        template.update(
                "INSERT IGNORE INTO module_text (module_id, text_id) VALUES (GetOrCreateModuleId(?), GetOrCreateTextId(?))",
                moduleName,
                content);
    }

    private void saveResponseText(String moduleName) {
        List<Object[]> statements = Arrays.stream(readFile("/" + moduleName + "/responses.txt").split("\n"))
                .map(line -> new Object[]{moduleName, line})
                .collect(Collectors.toList());
        template.batchUpdate(
                "INSERT IGNORE INTO module_text (module_id, text_id) VALUES (GetOrCreateModuleId(?), GetOrCreateTextId(?))",
                statements);
    }

    private void saveSubreddits(String moduleName) {
        List<Object[]> statements = Arrays.stream(readFile("/" + moduleName + "/subreddits.txt").split("\n"))
                .map(line -> new Object[]{moduleName, line})
                .collect(Collectors.toList());

        template.batchUpdate("INSERT IGNORE INTO subreddit (module_id, link) VALUES (GetOrCreateModuleId(?), ?)",
                statements);
    }

    private void createDB() {
        String sql = readFile("/sql/core/tables.sql") + "\n" +
                readFile("/sql/core/human.sql") + "\n" +
                readFile("/sql/core/image.sql") + "\n" +
                readFile("/sql/core/message.sql") + "\n" +
                readFile("/sql/core/text.sql") + "\n" +
                readFile("/sql/core/thread.sql") + "\n" +
                readFile("/sql/modules/module.sql") + "\n" +
                readFile("/sql/modules/quotes.sql");
        template.execute(sql);
        log.info("Created DB");
    }

    private String readFile(String url) {
        try (InputStream stream = this.getClass().getResourceAsStream(url)) {
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            return "";
        }
    }
}
