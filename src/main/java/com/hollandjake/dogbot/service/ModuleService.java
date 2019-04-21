package com.hollandjake.dogbot.service;

import com.hollandjake.dogbot.repository.ModuleRepository;
import com.hollandjake.dogbot.util.module.Module;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@CacheConfig(cacheNames = "modules")
public class ModuleService {
    @Getter
    private final ModuleRepository moduleRepository;

    @Autowired
    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Cacheable(key = "#moduleName")
    public Integer findByName(String moduleName) {
        return moduleRepository.findByName(moduleName);
    }

    public Boolean doesModuleExist(String moduleName) {
        return moduleRepository.doesModuleExist(moduleName);
    }

    @Cacheable(key = "#object.class.simpleName")
    public Integer save(Module object) {
        return moduleRepository.save(object);
    }
}
