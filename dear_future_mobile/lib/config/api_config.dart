/// Dear Future API yapılandırması.
/// Tüm REST istekleri bu base URL üzerinden yapılır.
class ApiConfig {
  ApiConfig._();

  static const String baseUrl = 'https://api.dearfuture.info';

  static const String authSendCode = '/api/auth/send-code';
  static const String authVerify = '/api/auth/verify';
  static const String authResendCode = '/api/auth/resend-code';
  static const String authGoogle = '/api/auth/google';

  static const String userProfile = '/api/user/profile';
  static const String userMessageQuota = '/api/user/message-quota';
  static const String userProfilePhoto = '/api/user/profile/photo';
  static const String userSettings = '/api/user/settings';
  static const String userAccount = '/api/user/account';
  static const String userAccountDeactivate = '/api/user/account/deactivate';

  static const String messagesDelivered = '/api/messages';
  static const String messagesPublic = '/api/messages/public';
  static const String messagesPublicStarred = '/api/messages/public/starred';
  static String messageViewByToken(String token) => '/api/messages/view/$token';
  static String publicMessageStar(int messageId) => '/api/messages/public/$messageId/star';

  static const String subscriptionPlans = '/api/subscription/plans';
  static String subscriptionPlanByCode(String code) => '/api/subscription/plans/$code';
  static const String subscriptionCheckoutInitialize = '/api/subscription/checkout/initialize';
  static const String subscriptionCancel = '/api/subscription/cancel';
}
