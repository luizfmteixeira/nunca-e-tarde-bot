package com.nuncaetardebot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeradorDeImagens {

    String outputPath = "C:/testes_imagens/aniversario_joao.png";

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1080;
    private static final int CIRCLE_X = 540; // Centro horizontal
    private static final int CIRCLE_Y = 383; // Centro vertical
    private static final int CIRCLE_RADIUS = 265; // Raio do círculo

    public byte[] createBirthdayImage(String profileImageUrl, String name) throws IOException {
        // 1. Carregar template
        BufferedImage template = loadTemplate();

        // 2. Carregar e processar foto do perfil
        BufferedImage profileImage = loadAndCropProfileImage(profileImageUrl);

        // 3. Combinar as imagens
        BufferedImage result = combineImages(template, profileImage, name);

        // 5. Retornar byte array para envio
        return convertToByteArray(result);
    }

    private BufferedImage loadTemplate() throws IOException {
        // Carregar do classpath ou arquivo
        return ImageIO.read(new File("template.jpeg"));
    }

    private BufferedImage loadAndCropProfileImage(String imageUrl) throws IOException {
        String linkconvertido = convertGoogleDriveUrl(imageUrl);
        BufferedImage original = ImageIO.read(new URL(linkconvertido));
        return cropToCircle(original);
    }

    private BufferedImage cropToCircle(BufferedImage image) {
        BufferedImage circleImage = new BufferedImage(
                CIRCLE_RADIUS * 2,
                CIRCLE_RADIUS * 2,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = circleImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Criar forma circular
        Shape circle = new Ellipse2D.Float(0, 0, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
        g2d.setClip(circle);

        // Redimensionar e centralizar a imagem
        Image scaled = image.getScaledInstance(
                CIRCLE_RADIUS * 2,
                CIRCLE_RADIUS * 2,
                Image.SCALE_SMOOTH
        );

        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return circleImage;
    }

    private BufferedImage combineImages(BufferedImage template, BufferedImage profile, String name) {
        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        // Desenhar template
        g2d.drawImage(template, 0, 0, null);

        // Posicionar foto do perfil (centralizada no círculo vazado)
        int x = CIRCLE_X - CIRCLE_RADIUS;
        int y = CIRCLE_Y - CIRCLE_RADIUS;
        g2d.drawImage(profile, x, y, null);


        g2d.dispose();
        return result;
    }

    private byte[] convertToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private void saveImageToFile(BufferedImage image, String filePath) throws IOException {
        File outputFile = new File(filePath);

        // Criar diretórios se não existirem
        outputFile.getParentFile().mkdirs();

        ImageIO.write(image, "PNG", outputFile);
        System.out.println("Imagem salva em: " + outputFile.getAbsolutePath());
    }

    private String convertGoogleDriveUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // Se já está no formato de download, retorna
        if (url.contains("uc?export=download") || url.contains("usercontent.google.com")) {
            return url;
        }

        String fileId = null;

        // Formato: https://drive.google.com/file/d/FILE_ID/view
        if (url.contains("/d/")) {
            Pattern pattern = Pattern.compile("/d/([a-zA-Z0-9_-]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                fileId = matcher.group(1);
            }
        }

        // Formato: https://drive.google.com/uc?id=FILE_ID&export=view
        if (fileId == null && url.contains("id=")) {
            fileId = url.substring(url.indexOf("id=") + 3);
            if (fileId.contains("&")) {
                fileId = fileId.substring(0, fileId.indexOf("&"));
            }
        }

        // Formato: https://drive.google.com/open?id=FILE_ID
        if (fileId == null && url.contains("open?id=")) {
            fileId = url.substring(url.indexOf("open?id=") + 8);
            if (fileId.contains("&")) {
                fileId = fileId.substring(0, fileId.indexOf("&"));
            }
        }

        if (fileId != null && !fileId.isEmpty()) {
            // Link que funciona atualmente para imagens públicas
            return "https://drive.usercontent.google.com/download?id=" + fileId + "&export=download";
        }

        return url;
    }
}
