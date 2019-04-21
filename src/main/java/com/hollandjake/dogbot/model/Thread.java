package com.hollandjake.dogbot.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Objects;

@Data
@Slf4j
@Builder
public class Thread {
    private Integer id;

    @NonNull
    private String url;

    @FindBy(className = "_17w2 _6ybr")
    private WebElement nameElement;

    public String getThreadName() {
        return nameElement.getText();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Thread thread = (Thread) o;
        return url.equals(thread.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
