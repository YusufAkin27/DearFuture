package example.DearFuture.mail;

/**
 * HTML email template for subscription cancellation due to repeated payment failures.
 */
public final class SubscriptionCancelledEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Abonelik İptal Edildi</title>
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
                                    <div style="display:inline-block;padding:12px 24px;background-color:#fef2f2;border-radius:8px;border:2px solid #ef4444;margin-bottom:20px;">
                                        <span style="font-size:18px;font-weight:700;color:#dc2626;">Abonelik İptal Edildi</span>
                                    </div>
                                    <p style="margin:20px 0 12px 0;font-size:15px;color:#333;line-height:1.5;">
                                        <strong>%s</strong> planınız, 7 gün boyunca ödeme alınamadığı için iptal edilmiştir.
                                    </p>
                                    <p style="margin:0 0 20px 0;font-size:14px;color:#666;line-height:1.5;">
                                        Hesabınız <strong>Ücretsiz</strong> plana geçirilmiştir.
                                    </p>
                                    <div style="margin:20px auto;padding:16px;background-color:#f0f4ff;border-radius:8px;border:1px solid #4f46e5;max-width:360px;">
                                        <p style="margin:0;font-size:14px;color:#3730a3;line-height:1.5;">
                                            Aboneliğinizi yeniden aktif etmek için hesabınızdan yeni bir plan satın alabilirsiniz.
                                        </p>
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
     * Returns HTML body for the subscription cancelled email.
     *
     * @param planName the cancelled plan name (e.g. "Plus", "Premium")
     */
    public static String build(String planName) {
        return String.format(TEMPLATE, planName);
    }

    private SubscriptionCancelledEmailTemplate() {}
}
