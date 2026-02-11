package example.DearFuture.mail;

/**
 * HTML email template for welcome / new account (e.g. after first login or signup).
 */
public final class WelcomeEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Hoş Geldiniz</title>
        </head>
        <body style="margin:0;padding:0;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background-color:#f5f5f5;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f5f5f5;">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:480px;background-color:#ffffff;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.08);">
                            <tr>
                                <td style="padding:40px 32px;text-align:center;">
                                    <h1 style="margin:0 0 8px 0;font-size:24px;font-weight:600;color:#1a1a1a;">Dear Future</h1>
                                    <p style="margin:0 0 28px 0;font-size:14px;color:#666;">Geleceğe mektubunuz</p>
                                    <div style="display:inline-block;padding:12px 24px;background-color:#ecfdf5;border-radius:8px;border:2px solid #10b981;margin-bottom:20px;">
                                        <span style="font-size:18px;font-weight:700;color:#059669;">Hoş Geldiniz</span>
                                    </div>
                                    <p style="margin:20px 0 8px 0;font-size:16px;color:#333;line-height:1.5;">%s</p>
                                    <p style="margin:0 0 20px 0;font-size:16px;color:#333;line-height:1.5;">
                                        Dear Future ailesine katıldığınız için teşekkür ederiz.
                                    </p>
                                    <p style="margin:0 0 20px 0;font-size:14px;color:#666;line-height:1.6;">
                                        Geleceğe yazdığınız mesajları sevdiklerinize ulaştırabilir, zamanlanmış mektuplar oluşturabilirsiniz.
                                    </p>
                                    <ul style="margin:0 auto 24px auto;padding:0;list-style:none;text-align:left;max-width:320px;">
                                        <li style="margin:0 0 10px 0;font-size:14px;color:#333;padding-left:20px;position:relative;">• İlk mesajınızı oluşturun</li>
                                        <li style="margin:0 0 10px 0;font-size:14px;color:#333;padding-left:20px;position:relative;">• Gönderim tarihini seçin</li>
                                        <li style="margin:0 0 0 0;font-size:14px;color:#333;padding-left:20px;position:relative;">• Geleceğe mektubunuz ulaşsın</li>
                                    </ul>
                                    <p style="margin:24px 0 0 0;font-size:13px;color:#888;">Sorularınız için destek ekibimiz yanınızda.</p>
                                    <p style="margin:24px 0 0 0;font-size:14px;font-weight:600;color:#1a1a1a;">Dear Future</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """;

    /**
     * Returns HTML body for the welcome email.
     *
     * @param greetingLine optional greeting (e.g. "Merhaba Ayşe," or empty string for no extra line)
     */
    public static String build(String greetingLine) {
        String line = (greetingLine != null && !greetingLine.isBlank())
                ? "<span style=\"color:#1a1a1a;\">" + greetingLine.trim() + "</span>"
                : "";
        return String.format(TEMPLATE, line);
    }

    private WelcomeEmailTemplate() {}
}
