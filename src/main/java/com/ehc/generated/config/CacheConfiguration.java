package com.ehc.generated.config;

import java.io.File;
import java.net.URI;
import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.ehc.generated.domain.Authority;
import com.ehc.generated.domain.Book;
import com.ehc.generated.domain.User;

import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.builders.*;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.Eh107Configuration;

import org.hibernate.cache.jcache.ConfigSettings;
import io.github.jhipster.config.JHipsterProperties;
import org.ehcache.jsr107.EhcacheCachingProvider;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.jhipster.config.cache.PrefixedKeyGenerator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfiguration {
    private GitProperties gitProperties;
    private BuildProperties buildProperties;
    private JCacheCacheManager jCacheCacheManager;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        createCaches(createManager(), jHipsterProperties);
    }

    private <K, V> javax.cache.configuration.Configuration<K, V> creatConfig(Class<K> key, Class<V> value,
            JHipsterProperties.Cache.Ehcache ehcache) {
        return Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder
                .newCacheConfigurationBuilder(key, value,
                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries()).offheap(5, MemoryUnit.MB)
                                // .disk(10, MemoryUnit.MB, true)
                                .with(ClusteredResourcePoolBuilder.clusteredShared("resource-pool-a")))
                .withExpiry(
                        ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build());
    }

    private CacheManager createManager() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        EhcacheCachingProvider ehcacheProvider = (EhcacheCachingProvider) cachingProvider;
        DefaultConfiguration configuration = new DefaultConfiguration(ehcacheProvider.getDefaultClassLoader(),
                // new DefaultPersistenceConfiguration(new File("D:/training/EHCache/cache"))
                ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9410/clustered"))
                        .autoCreate(c -> c.defaultServerResource("default-resource").resourcePool("resource-pool-a", 10,
                                MemoryUnit.MB, "default-resource"))
                        .build());
        CacheManager cacheManager = ehcacheProvider.getCacheManager(ehcacheProvider.getDefaultURI(), configuration);
        jCacheCacheManager = new JCacheCacheManager(cacheManager);
        return cacheManager;
    } 

    private void createCaches(javax.cache.CacheManager cm, JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache = jHipsterProperties.getCache().getEhcache();
        createCache(cm, com.ehc.generated.repository.UserRepository.USERS_BY_LOGIN_CACHE,
                creatConfig(String.class, Object.class, ehcache));
        createCache(cm, com.ehc.generated.repository.UserRepository.USERS_BY_EMAIL_CACHE,
                creatConfig(String.class, Object.class, ehcache));
        createCache(cm, com.ehc.generated.domain.User.class.getName(),
                creatConfig(Object.class, Object.class, ehcache));
        createCache(cm, com.ehc.generated.domain.Authority.class.getName(),
                creatConfig(Object.class, Object.class, ehcache));
        createCache(cm, com.ehc.generated.domain.User.class.getName() + ".authorities",
                creatConfig(Object.class, Object.class, ehcache));
        createCache(cm, com.ehc.generated.domain.Book.class.getName(),
                creatConfig(Object.class, Object.class, ehcache));
        createCache(cm, "byId", creatConfig(Long.class, String.class, ehcache));
        createCache(cm, "combine", creatConfig(Long.class, String.class, ehcache));
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        return jCacheCacheManager;
    }

    @Bean
    public CacheManager cacheManagers() {
        return jCacheCacheManager.getCacheManager();
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName,
            javax.cache.configuration.Configuration<?, ?> configuration) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache == null) {
            cm.createCache(cacheName, configuration);
        }
    }

    @Autowired(required = false)
    public void setGitProperties(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }
}
