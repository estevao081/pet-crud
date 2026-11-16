package dev.estv.petcrud.pet.util;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import dev.estv.petcrud.pet.exception.InvalidImageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

@Component
public class ReturnImageURL {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final IMap<String, CachedImage> imageCache;
    private final String publicBaseUrl;

    /**
     * @param publicBaseUrl URL pública do GATEWAY (ex.: http://localhost:8080). Vazio gera URL relativa
     *                      (/images/{id}), usada na imagem única, onde o Nginx faz o proxy.
     */
    public ReturnImageURL(HazelcastInstance hazelcastInstance,
                          @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.imageCache = hazelcastInstance.getMap("petImages");
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
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

            // A URL é montada a partir de configuração, e não do Host da requisição: este serviço
            // está atrás do gateway (o Host que ele vê é "pet-service:8083") e headers X-Forwarded-*
            // podem ser forjados por clientes.
            return publicBaseUrl + "/images/" + id;

        } catch (IOException e) {
            throw new RuntimeException("Error on send image");
        }
    }
}
