package example.DearFuture.mail;

/**
 * HTML email template for future message delivery notification.
 * Sends a link to the recipient so they can view the message on a dedicated page.
 */
public final class FutureMessageEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Gelecekten Bir Mesajın Var</title>
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
                                    <div style="display:inline-block;padding:12px 24px;background-color:#f0f4ff;border-radius:8px;border:2px solid #4f46e5;margin-bottom:20px;">
                                        <span style="font-size:18px;font-weight:700;color:#4f46e5;">Yeni Mesaj</span>
                                    </div>
                                    <p style="margin:20px 0 12px 0;font-size:16px;color:#333;line-height:1.5;">
                                        Gelecekten bir mesajın var!
                                    </p>
                                    <p style="margin:0 0 24px 0;font-size:14px;color:#666;line-height:1.5;">
                                        %s sana bir mesaj bıraktı. Mesajını görüntülemek için aşağıdaki butona tıkla.
                                    </p>
                                    <a href="%s" target="_blank" style="display:inline-block;padding:14px 32px;background-color:#4f46e5;color:#ffffff;text-decoration:none;border-radius:8px;font-size:16px;font-weight:600;letter-spacing:0.5px;">
                                        Mesajımı Görüntüle
                                    </a>
                                    <p style="margin:28px 0 0 0;font-size:12px;color:#999;line-height:1.5;">
                                        Bu link sana özeldir. Başkalarıyla paylaşmaman önerilir.
                                    </p>
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
     * Returns HTML body for the future message delivery email.
     *
     * @param senderName name of the person who sent the message (or "Birisi" if unknown)
     * @param viewUrl    full URL to the message viewing page (e.g. https://example.com/message/view/{token})
     */
    public static String build(String senderName, String viewUrl) {
        String sender = (senderName != null && !senderName.isBlank()) ? senderName : "Birisi";
        return String.format(TEMPLATE, sender, viewUrl);
    }

    private FutureMessageEmailTemplate() {}
}
