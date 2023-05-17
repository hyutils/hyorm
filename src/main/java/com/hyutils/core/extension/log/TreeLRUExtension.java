package com.hyutils.core.extension.log;

import com.fasterxml.jackson.databind.util.LRUMap;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TreeLRUExtension {

    public ConcurrentHashMap<String, LRUMap<Object, String>> tableModelCache;

    @PostConstruct
    private void init(){
        tableModelCache = new ConcurrentHashMap<>();
    }

}
