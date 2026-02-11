package example.DearFuture.mail;

/**
 * HTML email template for general notifications (announcements, updates, marketing with consent).
 */
public final class GeneralNotificationEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - %s</title>
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
                                        <span style="font-size:18px;font-weight:700;color:#4f46e5;">%s</span>
                                    </div>
                                    <div style="margin:20px 0;text-align:left;font-size:15px;color:#333;line-height:1.6;">
                                        %s
                                    </div>
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
     * Returns HTML body for a general notification.
     *
     * @param title   notification title (e.g. "Yeni Özellikler", "Bakım Duyurusu")
     * @param content HTML-safe body content (e.g. "&lt;p&gt;Merhaba, ...&lt;/p&gt;" or plain text wrapped in paragraphs)
     */
    public static String build(String title, String content) {
        String t = (title != null && !title.isBlank()) ? title.trim() : "Bildirim";
        String c = (content != null && !content.isBlank()) ? content.trim() : "";
        if (c.isEmpty()) {
            c = "<p style=\"margin:0;color:#333;\">Bu e-posta Dear Future tarafından gönderilmiştir.</p>";
        } else if (!c.contains("<")) {
            c = "<p style=\"margin:0 0 12px 0;color:#333;\">" + c.replace("\n", "</p><p style=\"margin:0 0 12px 0;color:#333;\">") + "</p>";
        }
        return String.format(TEMPLATE, t, t, c);
    }

    private GeneralNotificationEmailTemplate() {}
}
