package example.DearFuture.mail;

/**
 * HTML email template for successful subscription purchase / renewal.
 */
public final class SubscriptionSuccessEmailTemplate {

    private static final String TEMPLATE = """
        <!DOCTYPE html>
        <html lang="tr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Dear Future - Abonelik Onayı</title>
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
                                        <span style="font-size:18px;font-weight:700;color:#059669;">Ödeme Başarılı</span>
                                    </div>
                                    <p style="margin:20px 0 12px 0;font-size:15px;color:#333;line-height:1.5;">
                                        <strong>%s</strong> planınız başarıyla %s.
                                    </p>
                                    <table role="presentation" cellspacing="0" cellpadding="0" style="margin:20px auto;text-align:left;">
                                        <tr>
                                            <td style="padding:6px 12px;font-size:14px;color:#666;">Plan:</td>
                                            <td style="padding:6px 12px;font-size:14px;font-weight:600;color:#1a1a1a;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding:6px 12px;font-size:14px;color:#666;">Tutar:</td>
                                            <td style="padding:6px 12px;font-size:14px;font-weight:600;color:#1a1a1a;">%s TL</td>
                                        </tr>
                                        <tr>
                                            <td style="padding:6px 12px;font-size:14px;color:#666;">Sonraki ödeme:</td>
                                            <td style="padding:6px 12px;font-size:14px;font-weight:600;color:#1a1a1a;">%s</td>
                                        </tr>
                                    </table>
                                    <p style="margin:24px 0 0 0;font-size:13px;color:#888;">Aboneliğiniz otomatik olarak yenilenecektir.</p>
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
     * Returns HTML body for the subscription success email.
     *
     * @param planName    plan name (e.g. "Plus", "Premium")
     * @param actionLabel "aktif edildi" or "yenilendi"
     * @param amount      amount in TL (e.g. "100")
     * @param nextPayDate next payment date string (e.g. "10 Mart 2026")
     */
    public static String build(String planName, String actionLabel, String amount, String nextPayDate) {
        return String.format(TEMPLATE, planName, actionLabel, planName, amount, nextPayDate);
    }

    private SubscriptionSuccessEmailTemplate() {}
}
