package com.nuncaetardebot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MediaUploadService {

    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/v22.0/";
    private static final String PHONE_NUMBER_ID = "707371722470584";
    private static final String ACCESS_TOKEN = "EAAPEJa5xexIBQlPQQptl2fFsYxZB4nsuGs3WJ6ZC9QT6KM8q0XR2vJnYS1H8jdndh6CXL0i2MvzDF8ZAvCUo6Bfo1ctFOmMeh8vHm7Yyh1gJDIAOh5yOAe80Mfhxs2CZAlhxx8zWHHEjc7sROYkgbTedpth00ChYn6I9X93y7gYYWuwESKQUbBLRERuZCny2zxwZDZD";

    public static String uploadMedia(byte[] imageData, String mimeType) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String uploadUrl = WHATSAPP_API_URL + PHONE_NUMBER_ID + "/media";

            HttpPost post = new HttpPost(uploadUrl);
            post.setHeader("Authorization", "Bearer " + ACCESS_TOKEN);

            // Criar o corpo multipart/form-data
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("messaging_product", "whatsapp", ContentType.TEXT_PLAIN);
            builder.addBinaryBody("file",
                    new ByteArrayInputStream(imageData),
                    ContentType.create(mimeType),
                    "birthday_image.png");

            HttpEntity multipart = builder.build();
            post.setEntity(multipart);

            HttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(responseString);
                return jsonResponse.getString("id");
            } else {
                throw new IOException("Falha no upload: " + responseString);
            }
        }
    }
}