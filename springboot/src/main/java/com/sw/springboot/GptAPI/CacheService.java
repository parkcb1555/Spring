package com.sw.springboot.GptAPI;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CacheService {


    private CacheRepository cacheRepository;


    @Autowired
    public CacheService(CacheRepository cacheRepository){
        this.cacheRepository = cacheRepository;
    }


    public void saveCache(String name, String types, String response) {
        Cache cache = Cache.builder()
                .name(name)
                .types(types)
                .response(response)
                .build();
        cacheRepository.save(cache);  // 데이터를 저장
    }

    public void getCache(Long id) {
        Cache cache = cacheRepository.findById(id).orElse(null);
        if (cache != null) {
            System.out.println("Cache found: " + cache.getResponse());
        } else {
            System.out.println("Cache not found");
        }
    }

    // name과 types로 Cache를 찾아서 response를 출력하는 메서드
    public Optional<Cache> getCacheByNameAndTypes(String name, String types) {
        Optional<Cache> cacheOptional = cacheRepository.findByNameAndTypes(name, types);

        if (cacheOptional.isPresent()) {
            Cache cache = cacheOptional.get();
            System.out.println("Cache found: " + cache.getResponse());

        } else {
            System.out.println("Cache not found for name: " + name + " and types: " + types);
        }

        return cacheOptional;
    }
}