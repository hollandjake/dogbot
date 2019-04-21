package com.hollandjake.dogbot;

import com.hollandjake.dogbot.controller.WebController;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Controller
@EnableCaching
@SpringBootApplication
public class ChatbotApplication {
    private final Environment env;
    private final WebController webController;
    @Getter
    private String version;
    @Getter
    private LocalDateTime startup;

    @Autowired
    public ChatbotApplication(Environment env,
                              WebController webController) {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> errorHandler(this, e));
        this.env = env;
        this.webController = webController;
        setVersion();
        this.startup = LocalDateTime.now();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ChatbotApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

    public void errorHandler(Object sender, Throwable e) {
        log.error("Unhandled Exception from {}", sender, e);
        webController.screenshot();
        webController.close();
    }

    private void setVersion() {
        try {
            this.version = (new MavenXpp3Reader()).read(new FileReader(new File("pom.xml"))).getVersion();
        } catch (IOException | XmlPullParserException var2) {
            this.version = "";
        }
    }
}
