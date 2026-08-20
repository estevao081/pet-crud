package dev.estv.pet_crud_api.util;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import dev.estv.pet_crud_api.exception.exceptions.InvalidImageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

@Component
public class ReturnImageURL {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final IMap<String, CachedImage> imageCache;

    public ReturnImageURL(HazelcastInstance hazelcastInstance) {
        this.imageCache = hazelcastInstance.getMap("petImages");
    }

    public String imageUrl(MultipartFile image) {

        if (image.isEmpty() || image.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageException();
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

            if (bufferedImage == null) {
                throw new InvalidImageException();
            }

            if (bufferedImage.getWidth() > 4000 || bufferedImage.getHeight() > 4000) {
                throw new InvalidImageException();
            }

            String id = UUID.randomUUID().toString();
            String contentType = image.getContentType() != null
                    ? image.getContentType()
                    : "application/octet-stream";

            imageCache.put(id, new CachedImage(image.getBytes(), contentType));

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/images/")
                    .path(id)
                    .toUriString();

        } catch (IOException e) {
            throw new RuntimeException("Error on send image");
        }
    }
}