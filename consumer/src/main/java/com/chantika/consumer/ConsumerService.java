package com.chantika.consumer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class ConsumerService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = "myQueue")
    public void receivedMessage(String text) {
        System.out.println("LOG: Pesan diterima dari RabbitMQ: " + text);
        sendEmail(text);
    }

    public void sendEmail(String jsonText) {
        try {
            // Parse JSON values
            Long idOrder = getJsonValue(jsonText, "idOrder");
            Long idPelanggan = getJsonValue(jsonText, "idPelanggan");
            Long idProduk = getJsonValue(jsonText, "idProduk");
            Integer jumlah = getJsonIntValue(jsonText, "jumlah");
            Double harga = getJsonDoubleValue(jsonText, "harga");
            Double totalHarga = getJsonDoubleValue(jsonText, "totalHarga");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("chantikaade146@gmail.com");
            helper.setTo("raditya.rafif1106@gmail.com");
            helper.setSubject("Notifikasi Order Baru - Order #" + idOrder);

            String html = "<div style='font-family:Arial,sans-serif; padding:20px; background-color:#f5f5f5;'>" +
                    "<h2 style='color:#2ecc71;'>✅ Order Sudah Masuk Dari chantika Tampan</h2>" +
                    "<table style='border-collapse:collapse; width:100%; background-color:white;'>" +
                    "<tr style='background-color:#3498db; color:white;'>" +
                    "<th style='padding:12px; border:1px solid #ddd; text-align:left;'>Field</th>" +
                    "<th style='padding:12px; border:1px solid #ddd; text-align:left;'>Nilai</th>" +
                    "</tr>" +
                    "<tr><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>ID Order</td><td style='padding:12px; border:1px solid #ddd;'>" + idOrder + "</td></tr>" +
                    "<tr style='background-color:#ecf0f1;'><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>ID Pelanggan</td><td style='padding:12px; border:1px solid #ddd;'>" + idPelanggan + "</td></tr>" +
                    "<tr><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>ID Produk</td><td style='padding:12px; border:1px solid #ddd;'>" + idProduk + "</td></tr>" +
                    "<tr style='background-color:#ecf0f1;'><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>Jumlah</td><td style='padding:12px; border:1px solid #ddd;'>" + jumlah + "</td></tr>" +
                    "<tr><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>Harga Satuan</td><td style='padding:12px; border:1px solid #ddd;'>Rp " + String.format("%,.2f", harga) + "</td></tr>" +
                    "<tr style='background-color:#f39c12; color:white;'><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>Total Harga</td><td style='padding:12px; border:1px solid #ddd; font-weight:bold;'>Rp " + String.format("%,.2f", totalHarga) + "</td></tr>" +
                    "</table>" +
                    "<br><p style='color:#7f8c8d;'>Terima kasih telah melakukan order! </p>" +
                    "</div>";

            helper.setText(html, true);
            mailSender.send(mimeMessage);
            System.out.println("✅ Email berhasil dikirim");

        } catch (Exception e) {
            System.out.println("❌ Error email: " + e.getMessage());
        }
    }

    private Long getJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private Integer getJsonIntValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private Double getJsonDoubleValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    }
}