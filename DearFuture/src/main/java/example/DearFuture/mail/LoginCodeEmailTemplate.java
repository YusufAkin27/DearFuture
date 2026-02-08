package example.DearFuture.mail;

/**
 * HTML email template for login/verification code.
 */
public final class LoginCodeEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Giriş Kodunuz</title>
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
                                    <p style="margin:0 0 20px 0;font-size:15px;color:#333;line-height:1.5;">Giriş kodunuz:</p>
                                    <div style="display:inline-block;padding:16px 28px;background-color:#f0f4ff;border-radius:8px;border:2px dashed #4f46e5;">
                                        <span style="font-size:28px;font-weight:700;letter-spacing:6px;color:#4f46e5;">%s</span>
                                    </div>
                                    <p style="margin:24px 0 0 0;font-size:13px;color:#888;">Bu kod %d dakika geçerlidir.</p>
                                    <p style="margin:32px 0 0 0;font-size:12px;color:#999;">Bu e-postayı siz talep etmediyseniz lütfen dikkate almayın.</p>
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
     * Returns HTML body for the login code email.
     * @param code 6-digit code
     * @param expiresInMinutes e.g. 5
     */
    public static String build(String code, int expiresInMinutes) {
        return String.format(TEMPLATE, code, expiresInMinutes);
    }

    private LoginCodeEmailTemplate() {}
}
