package br.edu.fatecpg.cifa.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String para, String assunto, String texto, String html) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom("cifasuporte@gmail.com");
            helper.setTo(para);
            helper.setSubject(assunto);

            helper.setText(html, true);

            mailSender.send(mensagem);
            System.out.println("E-mail enviado com sucesso para: " + para);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            throw new RuntimeException("Falha no disparo do e-mail.");
        }
    }

    public void enviarEmailContato(String para, String assunto, String mensagem) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("cifasuporte@gmail.com", "Suporte CIFA");
        helper.setTo(para);
        helper.setSubject(assunto);

        String htmlTemplate = montarTemplateHtml(mensagem);
        helper.setText(htmlTemplate, true);

        mailSender.send(message);
    }

    private String montarTemplateHtml(String mensagem) {

        return  "<table border='0' cellpadding='0' cellspacing='0' width='100%' background='https://images.unsplash.com/photo-1620121692029-d088224ddc74?q=80&w=1332&auto=format&fit=crop' style='background-color: #0A102E; background-image: url(\"https://images.unsplash.com/photo-1620121692029-d088224ddc74?q=80&w=1332&auto=format&fit=crop\"); background-size: cover; background-position: center;'>" +
                "<tr><td style='padding: 60px 20px; font-family: \"Poppins\", sans-serif;'>" +
                "<table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width: 550px; background-color: rgba(10, 16, 46, 0.95); border: 1px solid #334155; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);'>" +
                "<tr><td style='padding: 32px 24px; text-align: center; border-bottom: 2px solid #4F46E5;'>" +
                "<h1 style='color: #ffffff; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: 3px;'>CIFA</h1>" +
                "<p style='color: #94a3b8; margin: 5px 0 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 1.5px;'>Suporte Técnico</p>" +
                "</td></tr><tr><td style='padding: 40px 32px;'>" +
                "<h2 style='color: #f8fafc; margin-top: 0; font-size: 20px; font-weight: 600;'>Resposta à sua solicitação</h2>" +
                "<div style='background-color: rgba(255, 255, 255, 0.05); border: 1px solid #334155; border-radius: 8px; padding: 20px; margin: 24px 0; color: #e2e8f0; font-size: 14px; line-height: 1.6; white-space: pre-wrap;'>" +
                mensagem +
                "</div>" +
                "</td></tr><tr><td style='padding: 24px 32px; background-color: rgba(0, 0, 0, 0.2); border-top: 1px solid #334155; text-align: center; font-size: 12px; color: #64748b; line-height: 1.5;'>" +
                "Este é um disparo automático do ecossistema CIFA.<br>Centro Paula Souza · <strong>FATEC Praia Grande</strong>" +
                "</td></tr></table></td></tr></table>";
    }
}
