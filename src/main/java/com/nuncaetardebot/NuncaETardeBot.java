package com.nuncaetardebot;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NuncaETardeBot {
    private static final String APPLICATION_NAME = "Nunca É Tarde Bot";
    private static final String CALENDAR_ID = "comunidadesantacruz@gmail.com";
    static GeradorDeImagens geradorDeImagens = new GeradorDeImagens();

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando verificação de aniversários...");

            List<Aniversariante> aniversariantes = getAniversariosHoje();

            if (aniversariantes.isEmpty()) {
                System.out.println("Nenhum aniversariante hoje.");
                return;
            }

            System.out.println("🎉 Aniversariantes encontrados: " + aniversariantes.size());

            for (Aniversariante aniversariante : aniversariantes) {
                // Gerar a imagem
                byte[] imagemBytes = geradorDeImagens.createBirthdayImage(
                        aniversariante.getImageUrl(),
                        aniversariante.getNome()
                );

                // Fazer upload da imagem
                String mediaId = MediaUploadService.uploadMedia(imagemBytes, "image/png");
                System.out.println("✅ Imagem upload realizada. Media ID: " + mediaId);

                String caption = null;

                if(aniversariante.getNome().contains("Casamento")){
                    String nomeCasal = extrairNomeCasal(aniversariante.getNome());
                    caption = "Parabéns "+ nomeCasal+"! \n" +
                            "\n" +
                            "O matrimônio de vocês é exemplo de amor e doação. \n" +
                            "\n" +
                            "Que Deus abençoe está união hoje e sempre. \uD83D\uDE4F\uD83C\uDFFB\n" +
                            "\n" +
                            "“Assim, eles já não são dois, mas sim uma só carne. Portanto o que Deus uniu, ninguém separa.” Mateus 19:6\n" +
                            "\n" +
                            "Amamos vocês ❤\uFE0F";
                }else{
                    caption = "Parabéns, "+ aniversariante.getNome() + "!"+" \n" +
                        "\n" +
                        "Que seu dia seja tão especial quanto você é para nós. \n" +
                        "\n" +
                        " Louvamos a Deus pelo dom da sua vida,  que ela seja sempre abençoada e repleta de felicidades! \n" +
                        "\n" +
                        "Amamos você!";
                }
                boolean enviado = WhatsappService.sendImageMessage(mediaId, caption);

                if (enviado) {
                    System.out.println("✅ Mensagem enviada para: " + aniversariante.getNome());
                } else {
                    System.out.println("❌ Falha ao enviar mensagem para: " + aniversariante.getNome());
                }
            }

            System.out.println("Processo de envio concluído!");

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Aniversariante> getAniversariosHoje() throws IOException, GeneralSecurityException {
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                Utils.JSON_FACTORY,
                Utils.getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();

        LocalDate hoje = LocalDate.now();
        DateTime timeMin = new DateTime(hoje.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(hoje.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());

        Events events = service.events().list(CALENDAR_ID)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        List<Aniversariante> aniversariantes = new ArrayList<>();

        for (Event event : items) {
            String summary = event.getSummary();
            if (summary != null && isAniversario(summary)) {
                String name = extrairNome(summary);
                String imageUrl = null;

                // Buscar URL da imagem se existir
                if (event.getAttachments() != null && !event.getAttachments().isEmpty()) {
                    for (EventAttachment attachment : event.getAttachments()) {
                        if (attachment != null && attachment.getFileUrl() != null) {
                            imageUrl = attachment.getFileUrl();
                            break;
                        }
                    }
                }

                aniversariantes.add(new Aniversariante(name, imageUrl));
            }
        }

        return aniversariantes;
    }

    private static boolean isAniversario(String summary) {
        String lowerSummary = summary.toLowerCase();
        return lowerSummary.contains("aniversário") ||
                lowerSummary.contains("aniversario");
    }

    private static String extrairNome(String summary) {
        // Converte para maiúsculo para comparar
        String upper = summary.toUpperCase().trim();

        // Verifica se começa com ANIVERSÁRIO (qualquer variação)
        if (upper.startsWith("ANIVERS")) {
            // Pega a partir da posição depois de "ANIVERSÁRIO"
            String nome = summary.substring(summary.indexOf(" ") + 1).trim();

            // Capitaliza
            String[] partes = nome.split("\\s+");
            StringBuilder resultado = new StringBuilder();

            for (String parte : partes) {
                if (!parte.isEmpty()) {
                    resultado.append(parte.substring(0, 1).toUpperCase())
                            .append(parte.substring(1).toLowerCase())
                            .append(" ");
                }
            }

            return resultado.toString().trim();
        }

        return summary; // Se não tiver "ANIVERSÁRIO", retorna original
    }

    private static String extrairNomeCasal(String summary) {
        // Converte para maiúsculo para comparar
        String upper = summary.toUpperCase().trim();

        // Verifica se começa com ANIVERSÁRIO (qualquer variação)
        if (upper.startsWith("CASAMENTO")) {
            // Pega a partir da posição depois de "ANIVERSÁRIO"
            String nome = summary.substring(summary.indexOf(" ") + 1).trim();

            // Capitaliza
            String[] partes = nome.split("\\s+");
            StringBuilder resultado = new StringBuilder();

            for (String parte : partes) {
                if (!parte.isEmpty()) {
                    resultado.append(parte.substring(0, 1).toUpperCase())
                            .append(parte.substring(1).toLowerCase())
                            .append(" ");
                }
            }

            return resultado.toString().trim();
        }

        return summary; // Se não tiver "ANIVERSÁRIO", retorna original
    }

    // Classe interna para representar um aniversariante
    static class Aniversariante {
        private String nome;
        private String imageUrl;

        public Aniversariante(String nome, String imageUrl) {
            this.nome = nome;
            this.imageUrl = imageUrl;
        }

        public String getNome() {
            return nome;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        @Override
        public String toString() {
            return nome;
        }
    }
}