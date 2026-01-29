package com.nuncaetardebot;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    public static GoogleCredential getCredentials() throws IOException, GeneralSecurityException {
        // 🔥 PRIMEIRO: Tenta da variável de ambiente (GitHub Actions)
        String jsonFromEnv = System.getenv("SERVICE_ACCOUNT_JSON");

        if (jsonFromEnv != null && !jsonFromEnv.trim().isEmpty()) {
            System.out.println("✅ Usando credenciais de variável de ambiente (GitHub Actions)");
            return GoogleCredential.fromStream(
                            new ByteArrayInputStream(jsonFromEnv.getBytes()))
                    .createScoped(SCOPES);
        }

        // 🔥 SEGUNDO: Tenta do arquivo local (desenvolvimento)
        try {
            System.out.println("⚠️  Variável de ambiente não encontrada, tentando arquivo local...");
            return GoogleCredential.fromStream(
                            new FileInputStream("service-account-key.json"))
                    .createScoped(SCOPES);
        } catch (IOException e) {
            System.out.println("❌ Nenhuma credencial encontrada!");
            System.out.println("   Configure SERVICE_ACCOUNT_JSON no GitHub Secrets");
            System.out.println("   Ou coloque service-account-key.json na pasta do projeto");
            throw e;
        }
    }
}