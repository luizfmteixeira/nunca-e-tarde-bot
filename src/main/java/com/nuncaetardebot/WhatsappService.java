package com.nuncaetardebot;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.List;

public class WhatsappService {

    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/v22.0/";
    private static final String PHONE_NUMBER_ID = "707371722470584";
    private static final String ACCESS_TOKEN = "EAAPEJa5xexIBQlPQQptl2fFsYxZB4nsuGs3WJ6ZC9QT6KM8q0XR2vJnYS1H8jdndh6CXL0i2MvzDF8ZAvCUo6Bfo1ctFOmMeh8vHm7Yyh1gJDIAOh5yOAe80Mfhxs2CZAlhxx8zWHHEjc7sROYkgbTedpth00ChYn6I9X93y7gYYWuwESKQUbBLRERuZCny2zxwZDZD";
    private static final String DESTINO_TESTE = "5541984980021";

    public static boolean sendImageMessage(String mediaId, String caption) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String messageUrl = WHATSAPP_API_URL + PHONE_NUMBER_ID + "/messages";

            HttpPost post = new HttpPost(messageUrl);
            post.setHeader("Authorization", "Bearer " + ACCESS_TOKEN);
            post.setHeader("Content-Type", "application/json");

            JSONObject payload = new JSONObject();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", DESTINO_TESTE);

            JSONObject imageObject = new JSONObject();
            imageObject.put("id", mediaId);
            imageObject.put("caption", caption);

            JSONObject messageObject = new JSONObject();
            messageObject.put("type", "image");
            messageObject.put("image", imageObject);

            payload.put("type", "image");
            payload.put("image", imageObject);

            String jsonPayload = payload.toString();
            System.out.println("📤 Enviando payload: " + jsonPayload);

            post.setEntity(new StringEntity(jsonPayload, "UTF-8"));

            HttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());

            System.out.println("📥 Resposta da API: " + responseString);

            return response.getStatusLine().getStatusCode() == 200;

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar mensagem com imagem: " + e.getMessage());
            return false;
        }
    }

    public static boolean sendTextMessage(String message) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {


                String messageUrl = WHATSAPP_API_URL + PHONE_NUMBER_ID + "/messages";

                HttpPost post = new HttpPost(messageUrl);
                post.setHeader("Authorization", "Bearer " + ACCESS_TOKEN);
                post.setHeader("Content-Type", "application/json");

                String jsonPayload = String.format("""
            {
                "messaging_product": "whatsapp",
                "to": "%s",
                "type": "text",
                "text": {
                    "body": "%s"
                }
            }
            """, DESTINO_TESTE, message.replace("\"", "\\\"").replace("\n", "\\n"));

                System.out.println("📤 Enviando payload: " + jsonPayload);

                post.setEntity(new StringEntity(jsonPayload, "UTF-8"));

                HttpResponse response = client.execute(post);
                String responseString = EntityUtils.toString(response.getEntity());

                System.out.println("📥 Resposta da API: " + responseString);

                if (response.getStatusLine().getStatusCode() != 200) {
                    return false;
                }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar mensagem: " + e.getMessage());
            return false;
        }
    }

    public static String criarMensagem(String aniversario) {
        StringBuilder mensagem = new StringBuilder("🎉 *TESTE - Parabéns ");
        mensagem.append(aniversario);
        return mensagem.toString();
    }
}