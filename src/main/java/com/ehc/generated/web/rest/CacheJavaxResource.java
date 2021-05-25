package com.ehc.generated.web.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;

import com.ehc.generated.service.dto.BookDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/javax")
public class CacheJavaxResource {

    private final static Logger log = LoggerFactory.getLogger(CacheJavaxResource.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * Cacheable testing.
     * 
     * @param id
     * @return
     */
    @GetMapping("/setCache/{id}")
    @Cacheable(cacheNames = "byId", unless = "#id==3")
    public String testCacheable(@PathVariable Long id) {
        Date date = new Date();
        log.debug(
                "\n*******************************\n\n Testing cache, id : {} , data create time : {} \n \n*******************************\n",
                id, date);

        return "{'Success':" + date + "}";
    }

    /**
     * CachePut Testing
     * 
     * @param id
     * @return
     */
    @PutMapping("/setCache/{id}")
    @CachePut(cacheNames = "byId")
    public String testCachePut(@PathVariable Long id) {
        Date date = new Date();
        log.debug(
                "\n*******************************\n\n Testing cache, id : {} , data create time : {} \n \n*******************************\n",
                id, date);

        return "{'Success':" + date + "}";
    }



    /**
     * Clear Cache
     * 
     * @param name
     * @param id
     * @return
     */
    @GetMapping("/cleanCache/{name}/{id}")
    public ResponseEntity<Void> cleanCache(@PathVariable String name, @PathVariable(required = false) String id) {
        if (id != null) {
            this.cacheManager.getCache(name).remove(id);
            log.debug(
                    "\n*******************************\n\n Delete cache, name : {} , id : {} \n \n*******************************\n",
                    name, id);
        } else {
            log.debug(
                    "\n*******************************\n\n Delete cache, name : {}\n \n*******************************\n",
                    name);
            this.cacheManager.getCache(name).clear();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Get Cache
     * 
     * @param name
     * @return
     */
    @GetMapping("/getCache/{name}")
    public ResponseEntity<Iterator<Cache.Entry<Object, Object>>> getCache(@PathVariable String name) {
        return ResponseEntity.ok().body(this.cacheManager.getCache(name).iterator());
    }

    /**
     * Get All Cache
     * 
     * @return
     */
    @GetMapping("/getAllCache")
    public ResponseEntity<Map<String,Iterator<Cache.Entry<Object,Object>>>> getAllCache() {
        Map<String,Iterator<Cache.Entry<Object,Object>>> map=new HashMap<>();
        
        this.cacheManager.getCacheNames().forEach(item->{
            map.put(item, this.cacheManager.getCache(item).iterator());
        });
        return ResponseEntity.ok(map);
    }

    /**
     * Clear All Cache
     * 
     * @return
     */
    @DeleteMapping("/clearAll")
    public ResponseEntity<Void> deleteAll() {
        this.cacheManager.getCacheNames().forEach(item -> this.cacheManager.getCache(item).clear());
        return ResponseEntity.ok().build();
    }

    /**
     * Clear All Cache of ById
     * 
     * @return
     */
    @DeleteMapping("/evict/all")
    @CacheEvict(cacheNames = "byId", allEntries = true)
    public ResponseEntity<Void> evictAll() {
        return ResponseEntity.ok().build();
    }

    /**
     * Clear All Cache of ById with condition
     * 
     * @param dto
     * @return
     */
    @DeleteMapping("/evict/all/condition")
    @CacheEvict(cacheNames = "byId", allEntries = true, condition = "#dto.id==2")
    public ResponseEntity<Void> evict(@RequestBody BookDTO dto) {
        return ResponseEntity.ok().build();
    }

    /**
     * Delete cache with name is byId and id is ${id}
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/evict/{id}")
    @CacheEvict(cacheNames = "byId")
    public ResponseEntity<Void> evict(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    /**
     * Delete cache before function,with exception
     * 
     * @return
     * @throws Exception
     */
    @DeleteMapping("/evict/before/true")
    @CacheEvict(cacheNames = "byId", allEntries = true, beforeInvocation = true)
    public ResponseEntity<Void> evictError() throws Exception {
        throw new Exception();
    }

    /**
     * Delete cache after function,with exception
     * 
     * @return
     * @throws Exception
     */
    @DeleteMapping("/evict/before/false")
    @CacheEvict(cacheNames = "byId", allEntries = true)
    public ResponseEntity<Void> evictErrorFalse() throws Exception {
        throw new Exception();
    }

    @GetMapping("/evict/combine/{id}")
    @Cacheable(cacheNames = "combine")
    @CachePut(cacheNames = "combine", condition = "#p0==1")
    @CacheEvict(cacheNames = "combine", condition = "#p0==2", allEntries = true)
    public String testcombine(@PathVariable Long id) {
        Date date = new Date();
        log.debug(
                "\n*******************************\n\n Testing combine cache, id : {} , data create time : {} \n \n*******************************\n",
                id, date);

        return "{'Success':" + date + "}";
    }

}
