package ro.faur.apollo.libs.images.analyzer;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Service
public class ImageProcessorService {

    public boolean isImage(String mediaUrl) {
        return mediaUrl.matches(".*\\.(jpeg|jpg|png)$");
    }

    public String downloadAndConvertToBase64(String mediaUrl) {
        try {
            URL url = new URL(mediaUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
