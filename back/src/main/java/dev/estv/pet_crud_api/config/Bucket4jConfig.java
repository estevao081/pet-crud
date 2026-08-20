package dev.estv.pet_crud_api.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4jConfig {

    @Bean
    public ProxyManager<String> proxyManager(HazelcastInstance hazelcastInstance) {

        IMap<String, byte[]> map =
                hazelcastInstance.getMap("rateLimiter");

        return new HazelcastProxyManager<>(map);
    }
}