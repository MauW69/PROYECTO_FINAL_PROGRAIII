// java
package com.example.proyecto_final_prograiii.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;

public class RecuperarClave {
    // Genera un PIN pseudoaleatorio de 6 digitos
    public static String generarPin() {
        Random rnd = new Random();
        int pin = 100000 + rnd.nextInt(900000); // 6 digitos
        return String.valueOf(pin);
    }

    // Envia un correo con el PIN de recuperacion
    public static boolean enviarPinCorreo(String destinatario, String pin) {
        final String remitente = "mauricioguatemala13@gmail.com"; // Cambiar por tu correo
        final String clave = "gjrxkkzdminhirvl"; // Cambiar por la contraseña de app si es Gmail

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Recuperacion de PIN");
            message.setText("Tu PIN de recuperacion es: " + pin);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
