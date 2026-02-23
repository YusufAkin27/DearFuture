/// Backend GET /api/user/profile (UserResponse) ile uyumlu.
class ProfileData {
  ProfileData({
    this.firstName,
    this.lastName,
    this.email,
    this.emailVerified = false,
    this.profilePictureUrl,
    this.createdAt,
    this.subscriptionPlanCode,
    this.subscriptionPlanName,
    this.subscriptionEndsAt,
    this.maxMessagesPerPlan = 3,
    this.maxRecipientsPerMessage = 1,
    this.maxPhotosPerMessage = 0,
    this.maxPhotoSizeBytes = 0,
    this.maxFilesPerMessage = 0,
    this.maxFileSizeBytes = 0,
    this.allowVoice = false,
    this.maxAudioPerMessage = 0,
    this.maxAudioSizeBytes = 0,
    this.locale = 'tr',
    this.emailNotifications = true,
    this.marketingEmails = false,
  });

  final String? firstName;
  final String? lastName;
  final String? email;
  final bool emailVerified;
  final String? profilePictureUrl;
  final DateTime? createdAt;
  final String? subscriptionPlanCode;
  final String? subscriptionPlanName;
  final DateTime? subscriptionEndsAt;
  final int maxMessagesPerPlan;
  final int maxRecipientsPerMessage;
  final int maxPhotosPerMessage;
  final int maxPhotoSizeBytes;
  final int maxFilesPerMessage;
  final int maxFileSizeBytes;
  final bool allowVoice;
  final int maxAudioPerMessage;
  final int maxAudioSizeBytes;
  final String locale;
  final bool emailNotifications;
  final bool marketingEmails;

  String get displayName {
    if ((firstName ?? '').isEmpty && (lastName ?? '').isEmpty) return email ?? '';
    return [firstName, lastName].where((s) => s != null && s.isNotEmpty).join(' ').trim();
  }

  String get initial {
    if ((firstName ?? '').isNotEmpty) return firstName!.substring(0, 1).toUpperCase();
    if ((email ?? '').isNotEmpty) return email!.substring(0, 1).toUpperCase();
    return '?';
  }

  static ProfileData? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    DateTime? createdAt;
    if (json['createdAt'] != null) {
      if (json['createdAt'] is String) createdAt = DateTime.tryParse(json['createdAt'] as String);
    }
    DateTime? subscriptionEndsAt;
    if (json['subscriptionEndsAt'] != null) {
      if (json['subscriptionEndsAt'] is String) subscriptionEndsAt = DateTime.tryParse(json['subscriptionEndsAt'] as String);
    }
    return ProfileData(
      firstName: json['firstName'] as String?,
      lastName: json['lastName'] as String?,
      email: json['email'] as String?,
      emailVerified: json['emailVerified'] == true,
      profilePictureUrl: json['profilePictureUrl'] as String?,
      createdAt: createdAt,
      subscriptionPlanCode: json['subscriptionPlanCode'] as String? ?? 'FREE',
      subscriptionPlanName: json['subscriptionPlanName'] as String? ?? 'Ücretsiz',
      subscriptionEndsAt: subscriptionEndsAt,
      maxMessagesPerPlan: (json['maxMessagesPerPlan'] as num?)?.toInt() ?? 3,
      maxRecipientsPerMessage: (json['maxRecipientsPerMessage'] as num?)?.toInt() ?? 1,
      maxPhotosPerMessage: (json['maxPhotosPerMessage'] as num?)?.toInt() ?? 0,
      maxPhotoSizeBytes: (json['maxPhotoSizeBytes'] as num?)?.toInt() ?? 0,
      maxFilesPerMessage: (json['maxFilesPerMessage'] as num?)?.toInt() ?? 0,
      maxFileSizeBytes: (json['maxFileSizeBytes'] as num?)?.toInt() ?? 0,
      allowVoice: json['allowVoice'] == true,
      maxAudioPerMessage: (json['maxAudioPerMessage'] as num?)?.toInt() ?? 0,
      maxAudioSizeBytes: (json['maxAudioSizeBytes'] as num?)?.toInt() ?? 0,
      locale: json['locale'] as String? ?? 'tr',
      emailNotifications: json['emailNotifications'] == true,
      marketingEmails: json['marketingEmails'] == true,
    );
  }
}

/// Backend GET /api/messages (iletilen mesajlar) öğesi.
class DeliveredMessageItem {
  DeliveredMessageItem({
    this.id,
    this.viewToken,
    this.scheduledAt,
    this.sentAt,
    this.status,
    this.contentPreview,
    this.contentTypes = const [],
    this.previewImageUrl,
  });

  final int? id;
  /// Mesaj detayı için GET /api/messages/view/{viewToken}
  final String? viewToken;
  final DateTime? scheduledAt;
  final DateTime? sentAt;
  final String? status;
  final String? contentPreview;
  /// İçerik türleri: TEXT, IMAGE, VIDEO, FILE, AUDIO (backend yoksa boş liste)
  final List<String> contentTypes;
  /// Liste önizlemesi için ilk fotoğraf URL'i (varsa)
  final String? previewImageUrl;

  static DateTime? _parseDate(dynamic v) {
    if (v == null) return null;
    if (v is String) return DateTime.tryParse(v);
    if (v is num) {
      final n = v.toInt();
      // Backend saniye cinsinden Unix timestamp gönderebilir (10 haneli); milisaniye 13 haneli
      if (n > 0 && n < 10000000000) return DateTime.fromMillisecondsSinceEpoch(n * 1000);
      return DateTime.fromMillisecondsSinceEpoch(n);
    }
    return null;
  }

  static List<String> _parseContentTypes(dynamic v) {
    if (v == null) return [];
    if (v is List) return v.map((e) => e?.toString() ?? '').where((s) => s.isNotEmpty).toList();
    return [];
  }

  static DeliveredMessageItem? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return DeliveredMessageItem(
      id: (json['id'] as num?)?.toInt(),
      viewToken: json['viewToken'] as String?,
      scheduledAt: _parseDate(json['scheduledAt']),
      sentAt: _parseDate(json['sentAt']),
      status: json['status'] as String?,
      contentPreview: json['content'] as String?,
      contentTypes: _parseContentTypes(json['contentTypes']),
      previewImageUrl: json['previewImageUrl'] as String?,
    );
  }
}

/// Backend GET /api/user/message-quota (MessageQuotaResponse) ile uyumlu.
class MessageQuotaData {
  MessageQuotaData({
    this.limit = 3,
    this.used = 0,
    this.remaining = 3,
    this.planCode = 'FREE',
    this.planName = 'Ücretsiz',
  });

  final int limit;
  final int used;
  final int remaining;
  final String planCode;
  final String planName;

  static MessageQuotaData? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return MessageQuotaData(
      limit: (json['limit'] as num?)?.toInt() ?? 3,
      used: (json['used'] as num?)?.toInt() ?? 0,
      remaining: (json['remaining'] as num?)?.toInt() ?? 3,
      planCode: json['planCode'] as String? ?? 'FREE',
      planName: json['planName'] as String? ?? 'Ücretsiz',
    );
  }
}
