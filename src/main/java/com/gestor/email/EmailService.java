package com.gestor.email;

import io.github.cdimascio.dotenv.Dotenv;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String EMAIL_FROM     = dotenv.get("SMTP_USER",     "");
    private static final String EMAIL_PASSWORD = dotenv.get("SMTP_PASSWORD", "");
    private static final String SMTP_HOST      = "smtp.gmail.com";
    private static final String SMTP_PORT      = "587";

    private static Session crearSesion() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",             SMTP_HOST);
        props.put("mail.smtp.port",             SMTP_PORT);
        props.put("mail.smtp.ssl.trust",        SMTP_HOST);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });
    }

    public static boolean enviarCorreoGenerico(String destinatario, String asunto, String cuerpoHTML) {
        try {
            Session sesion = crearSesion();
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(EMAIL_FROM, "Gestor de Usuarios"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(cuerpoHTML, "text/html; charset=UTF-8");
            Transport.send(mensaje);
            System.out.println("  Correo enviado a: " + destinatario);
            return true;
        } catch (Exception e) {
            System.out.println("  Advertencia: no se pudo enviar correo - " + e.getMessage());
            return false;
        }
    }

    public static boolean probarConexion() {
        try {
            Session sesion = crearSesion();
            Transport transport = sesion.getTransport("smtp");
            transport.connect(SMTP_HOST, EMAIL_FROM, EMAIL_PASSWORD);
            transport.close();
            System.out.println("  Conexion SMTP exitosa");
            return true;
        } catch (Exception e) {
            System.out.println("  Error SMTP: " + e.getMessage());
            return false;
        }
    }

    public void enviarBienvenida(String nombre, String email, String passwordTemporal) {
        String cuerpo = """
                <html>
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                  <div style="background:#003366;padding:20px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:20px;">Gestor de Usuarios</h1>
                    <p style="color:#adc8e8;margin:4px 0;font-size:13px;">Sistema de Administracion</p>
                  </div>
                  <div style="padding:28px;background:#f9f9f9;">
                    <h2 style="color:#003366;">Bienvenido/a, %s</h2>
                    <p>Tu cuenta ha sido creada exitosamente.</p>
                    <div style="background:white;border:1px solid #ddd;border-radius:8px;padding:16px;margin:18px 0;">
                      <p style="margin:0;"><strong>Email:</strong> %s</p>
                      <p style="margin:8px 0 0;"><strong>Contrasena temporal:</strong>
                        <code style="background:#f4f4f4;padding:4px 8px;border-radius:4px;">%s</code>
                      </p>
                    </div>
                    <p style="color:#e74c3c;font-weight:bold;">Por seguridad, cambia tu contrasena en el primer inicio de sesion.</p>
                    <p style="color:#999;font-size:12px;margin-top:24px;">Este correo fue generado automaticamente · Gestor de Usuarios 2026</p>
                  </div>
                  <div style="background:#b40000;padding:10px;text-align:center;">
                    <p style="color:white;margin:0;font-size:11px;">Gestor de Usuarios | Sistema Administrativo | 2026</p>
                  </div>
                </body></html>
                """.formatted(nombre, email, passwordTemporal);
        enviarCorreoGenerico(email, "Bienvenido al Sistema de Usuarios", cuerpo);
    }

    public void enviarResetPassword(String nombre, String email, String token) {
        String cuerpo = """
                <html>
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                  <div style="background:#003366;padding:20px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:20px;">Gestor de Usuarios</h1>
                    <p style="color:#adc8e8;margin:4px 0;font-size:13px;">Sistema de Administracion</p>
                  </div>
                  <div style="padding:28px;background:#f9f9f9;">
                    <h2 style="color:#b40000;">Restablecimiento de Contrasena</h2>
                    <p>Hola <strong>%s</strong>, recibimos una solicitud para restablecer tu contrasena.</p>
                    <p><strong>Tu token de restablecimiento:</strong></p>
                    <div style="background:#f4f4f4;padding:12px;border-radius:6px;font-family:monospace;font-size:14px;word-break:break-all;">%s</div>
                    <p style="margin-top:16px;">Este token expira en <strong>1 hora</strong>.</p>
                    <p style="color:#666;">Si no solicitaste esto, ignora este correo.</p>
                    <p style="color:#999;font-size:12px;margin-top:24px;">Este correo fue generado automaticamente · Gestor de Usuarios 2026</p>
                  </div>
                  <div style="background:#b40000;padding:10px;text-align:center;">
                    <p style="color:white;margin:0;font-size:11px;">Gestor de Usuarios | Sistema Administrativo | 2026</p>
                  </div>
                </body></html>
                """.formatted(nombre, token);
        enviarCorreoGenerico(email, "Restablecimiento de Contrasena", cuerpo);
    }

    public void enviarNotificacionCambio(String nombre, String email, String accion) {
        String cuerpo = """
                <html>
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;">
                  <div style="background:#003366;padding:20px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:20px;">Gestor de Usuarios</h1>
                    <p style="color:#adc8e8;margin:4px 0;font-size:13px;">Sistema de Administracion</p>
                  </div>
                  <div style="padding:28px;background:#f9f9f9;">
                    <h2 style="color:#003366;">Actividad en tu cuenta</h2>
                    <p>Hola <strong>%s</strong>, se realizo la siguiente accion en tu cuenta:</p>
                    <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:15px;margin:18px 0;">
                      <p style="margin:0;font-size:15px;"><strong>%s</strong></p>
                    </div>
                    <p style="color:#666;">Si no reconoces esta actividad, contacta al administrador.</p>
                    <p style="color:#999;font-size:12px;margin-top:24px;">Este correo fue generado automaticamente · Gestor de Usuarios 2026</p>
                  </div>
                  <div style="background:#b40000;padding:10px;text-align:center;">
                    <p style="color:white;margin:0;font-size:11px;">Gestor de Usuarios | Sistema Administrativo | 2026</p>
                  </div>
                </body></html>
                """.formatted(nombre, accion);
        enviarCorreoGenerico(email, "Notificacion de cambio en tu cuenta", cuerpo);
    }
}