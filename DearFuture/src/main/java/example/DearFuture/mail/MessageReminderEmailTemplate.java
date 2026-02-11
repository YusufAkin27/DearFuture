package example.DearFuture.mail;

/**
 * HTML email template for reminding the user that a scheduled message will be sent soon.
 */
public final class MessageReminderEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Mesaj Hatırlatması</title>
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
                                    <div style="display:inline-block;padding:12px 24px;background-color:#fef3c7;border-radius:8px;border:2px solid #f59e0b;margin-bottom:20px;">
                                        <span style="font-size:18px;font-weight:700;color:#b45309;">Mesaj Hatırlatması</span>
                                    </div>
                                    <p style="margin:20px 0 12px 0;font-size:15px;color:#333;line-height:1.5;">
                                        Zamanlanmış mesajınız <strong>%s</strong> tarihinde alıcıya iletilecektir.
                                    </p>
                                    <p style="margin:0 0 20px 0;font-size:14px;color:#666;line-height:1.5;">
                                        %s
                                    </p>
                                    <p style="margin:24px 0 0 0;font-size:13px;color:#888;">Bu mesajı düzenlemek veya iptal etmek için hesabınıza giriş yapabilirsiniz.</p>
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
     * Returns HTML body for the message reminder email.
     *
     * @param scheduledDateFormatted formatted date/time when the message will be sent (e.g. "15 Mart 2026, 14:00")
     * @param detailLine             optional detail (e.g. "1 alıcıya gönderilecek" or "Mesaj: Doğum günün kutlu olsun!")
     */
    public static String build(String scheduledDateFormatted, String detailLine) {
        String date = scheduledDateFormatted != null ? scheduledDateFormatted : "-";
        String detail = (detailLine != null && !detailLine.isBlank()) ? detailLine : "Zamanı geldiğinde mesajınız iletilecektir.";
        return String.format(TEMPLATE, date, detail);
    }

    private MessageReminderEmailTemplate() {}
}
