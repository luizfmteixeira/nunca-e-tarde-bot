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
        System.out.println("=== 🔍 DEBUG CREDENCIAIS ===");

        // 1. Tenta da variável de ambiente
        String jsonFromEnv = System.getenv("SERVICE_ACCOUNT_JSON");

        System.out.println("Variável SERVICE_ACCOUNT_JSON existe? " + (jsonFromEnv != null));

        if (jsonFromEnv != null) {
            System.out.println("Tamanho: " + jsonFromEnv.length() + " caracteres");
            System.out.println("Começa com: " + jsonFromEnv.substring(0, Math.min(50, jsonFromEnv.length())));

            if (!jsonFromEnv.trim().isEmpty()) {
                System.out.println("✅ Usando credenciais de variável de ambiente");
                try {
                    return GoogleCredential.fromStream(
                                    new ByteArrayInputStream(jsonFromEnv.getBytes()))
                            .createScoped(SCOPES);
                } catch (Exception e) {
                    System.out.println("❌ Erro ao processar JSON da variável: " + e.getMessage());
                    throw e;
                }
            } else {
                System.out.println("⚠️  Variável existe mas está vazia!");
            }
        } else {
            System.out.println("❌ Variável SERVICE_ACCOUNT_JSON NÃO encontrada!");
            System.out.println("Variáveis de ambiente disponíveis:");
            System.getenv().forEach((key, value) -> {
                if (key.contains("SERVICE") || key.contains("ACCOUNT") || key.contains("JSON")) {
                    System.out.println("  " + key + " = " + value.substring(0, Math.min(30, value.length())) + "...");
                }
            });
        }

        // 2. Fallback para arquivo (apenas dev)
        System.out.println("⚠️  Tentando arquivo local service-account-key.json");
        try {
            return GoogleCredential.fromStream(
                            new FileInputStream("service-account-key.json"))
                    .createScoped(SCOPES);
        } catch (IOException e) {
            System.out.println("💥 ERRO: Nenhuma credencial disponível!");
            System.out.println("   No GitHub Actions: Configure secret 'SERVICE_ACCOUNT_JSON'");
            System.out.println("   Localmente: Coloque service-account-key.json na pasta");
            throw e;
        }
    }
}