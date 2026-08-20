package dev.estv.pet_crud_api.controller;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import dev.estv.pet_crud_api.util.CachedImage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    private final IMap<String, CachedImage> imageCache;

    public ImageController(HazelcastInstance hazelcastInstance) {
        this.imageCache = hazelcastInstance.getMap("petImages");
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        CachedImage image = imageCache.get(id);

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(image.getData());
    }
}