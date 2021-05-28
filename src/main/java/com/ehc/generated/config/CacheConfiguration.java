package com.ehc.generated.config;

import java.io.File;
import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.ehc.generated.domain.User;

import org.ehcache.config.builders.*;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.Eh107Configuration;

import org.hibernate.cache.jcache.ConfigSettings;
import io.github.jhipster.config.JHipsterProperties;
import org.ehcache.jsr107.EhcacheCachingProvider;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import io.github.jhipster.config.cache.PrefixedKeyGenerator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfiguration {
    private GitProperties gitProperties;
    private BuildProperties buildProperties;
    private final javax.cache.configuration.Configuration<String, User> jcacheConfiguration;
    private JCacheCacheManager jCacheCacheManager;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration
                .fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(String.class, User.class,
                                        ResourcePoolsBuilder.heap(ehcache.getMaxEntries()).disk(10, MemoryUnit.MB,
                                                true))
                                .withExpiry(ExpiryPolicyBuilder
                                        .timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                                .build());
        CachingProvider cachingProvider = Caching.getCachingProvider();
        EhcacheCachingProvider ehcacheProvider = (EhcacheCachingProvider) cachingProvider;
        DefaultConfiguration configuration = new DefaultConfiguration(ehcacheProvider.getDefaultClassLoader(),
                new DefaultPersistenceConfiguration(new File("D:/training/EHCache/cache")));
        CacheManager cacheManager = ehcacheProvider.getCacheManager(ehcacheProvider.getDefaultURI(), configuration);

        createCache(cacheManager, com.ehc.generated.repository.UserRepository.USERS_BY_LOGIN_CACHE);
        createCache(cacheManager, com.ehc.generated.repository.UserRepository.USERS_BY_EMAIL_CACHE);
        cacheManager.createCache(com.ehc.generated.domain.Book.class.getName(),  Eh107Configuration
        .fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(Object.class, Object.class,
                                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                        .withExpiry(ExpiryPolicyBuilder
                                .timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                        .build()));
        jCacheCacheManager = new JCacheCacheManager(cacheManager);
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

    // createCache(cm,
    // com.ehc.generated.repository.UserRepository.USERS_BY_LOGIN_CACHE);
    // createCache(cm,
    // com.ehc.generated.repository.UserRepository.USERS_BY_EMAIL_CACHE);
    // createCache(cm, com.ehc.generated.domain.User.class.getName());
    // createCache(cm, com.ehc.generated.domain.Authority.class.getName());
    // createCache(cm, com.ehc.generated.domain.User.class.getName() +
    // ".authorities");
    // createCache(cm, com.ehc.generated.domain.Book.class.getName());

    // @Bean
    // public JCacheManagerCustomizer cacheManagerCustomizer() {
    // return new JCacheManagerCustomizers(jcacheConfiguration);
    // }

    // private static class JCacheManagerCustomizers implements
    // JCacheManagerCustomizer {

    // private final javax.cache.configuration.Configuration<String, User>
    // jcacheConfiguration;

    // public
    // JCacheManagerCustomizers(javax.cache.configuration.Configuration<String,
    // User> jcacheConfiguration) {
    // this.jcacheConfiguration = jcacheConfiguration;
    // }

    // @Override
    // public void customize(CacheManager cacheManager) {
    // EhcacheCachingProvider ehcacheCachingProvider = (EhcacheCachingProvider)
    // cacheManager.getCachingProvider();
    // DefaultConfiguration configuration = new DefaultConfiguration(
    // ehcacheCachingProvider.getDefaultClassLoader(),
    // new DefaultPersistenceConfiguration(new File("D:/training/EHCache/cache")));
    // cacheManager =
    // ehcacheCachingProvider.getCacheManager(ehcacheCachingProvider.getDefaultURI(),
    // configuration);

    // createCache(cacheManager,
    // com.ehc.generated.repository.UserRepository.USERS_BY_LOGIN_CACHE);
    // createCache(cacheManager,
    // com.ehc.generated.repository.UserRepository.USERS_BY_EMAIL_CACHE);

    // }

    // }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache == null) {
            cm.createCache(cacheName, jcacheConfiguration);
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
