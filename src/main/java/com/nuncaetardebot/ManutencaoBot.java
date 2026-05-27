package com.nuncaetardebot;

public class ManutencaoBot {
    public static void main(String[] args) {
        System.out.println("🔄 Enviando mensagem de manutenção para manter janela aberta...");

        boolean enviado = WhatsappService.sendMaintenanceMessage();

        if (enviado) {
            System.out.println("✅ Janela de 24h renovada com sucesso!");
        } else {
            System.out.println("❌ Falha ao enviar mensagem de manutenção");
        }
    }
}