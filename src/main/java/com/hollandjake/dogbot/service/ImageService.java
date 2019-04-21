package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.model.Image;
import com.hollandjake.dogbot.model.MessageComponent;
import com.hollandjake.dogbot.repository.ImageRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Blob;

@Slf4j
@Service
@CacheConfig(cacheNames = "images")
public class ImageService implements ComponentService<Image> {
    @Getter
    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Cacheable(key = "#id")
    public Image findById(Integer id) {
        return imageRepository.findById(id);
    }

    @Cacheable(key = "#blob")
    public Image findByImageBlob(Blob blob) {
        return imageRepository.findByImageBlob(blob);
    }

    @Cacheable(key = "#object.blob")
    @Transactional
    public Image save(Image object) {
        return imageRepository.save(object);
    }

    public MessageComponent getRandomImageForModule(Integer moduleId) {
        return imageRepository.getRandomImageForModule(moduleId);
    }
}
