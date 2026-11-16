package com.estv.pet_crud_auth.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class Bucket4jConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {

        InputStream configFile =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("hazelcast.xml");

        if (configFile == null) {
            throw new IllegalStateException(
                    "hazelcast.xml não encontrado no classpath"
            );
        }

        Config config = new XmlConfigBuilder(configFile).build();

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public ProxyManager<String> proxyManager(
            HazelcastInstance hazelcastInstance) {

        IMap<String, byte[]> map =
                hazelcastInstance.getMap("rateLimiter");

        return new HazelcastProxyManager<>(map);
    }
}