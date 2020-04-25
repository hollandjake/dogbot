package com.hollandjake.dogbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RestService {
    private final RestTemplate rest;
    private String channelId = "UCcXhhVwCT6_WqjkEniejRJQ";
    private String apiKey = "AIzaSyB6WS9oYO-h4k4Ihk9X9V9Stm4wPbgM8yo";
    private String s = "https://www.googleapis.com/youtube/v3/activities?part=contentDetails&maxResults=10";
    private String x = "&channelId=%s&key=%s";

    @Autowired
    public RestService(RestTemplate rest) {
        this.rest = rest;
    }

    public List<Object> videos() {
        Map data = rest.getForObject(URI.create(s + String.format(x, channelId, apiKey)), Map.class);
        List<Map> items = (List<Map>) data.get("items");
        log.info("{}", items);
        return null;
    }

}
