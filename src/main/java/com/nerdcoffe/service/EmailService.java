package com.nerdcoffe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) {
        log.info("Enviando e-mail de verificação para {}", to);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Confirmação de E-mail - Coffe Nerd");
            message.setText("Olá! Por favor, clique no link a seguir para verificar seu e-mail:\n\n"
                    + "http://localhost:3000/verify-email?token=" + token);
            message.setFrom("noreply@coffenerd.com");

            mailSender.send(message);
            log.info("E-mail de verificação enviado com sucesso para {}", to);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de verificação para {}", to, e);
        }
    }
}
