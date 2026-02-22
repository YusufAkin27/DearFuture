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
    this.scheduledAt,
    this.sentAt,
    this.status,
  });

  final int? id;
  final DateTime? scheduledAt;
  final DateTime? sentAt;
  final String? status;

  static DateTime? _parseDate(dynamic v) {
    if (v == null) return null;
    if (v is String) return DateTime.tryParse(v);
    if (v is num) return DateTime.fromMillisecondsSinceEpoch(v.toInt());
    return null;
  }

  static DeliveredMessageItem? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return DeliveredMessageItem(
      id: (json['id'] as num?)?.toInt(),
      scheduledAt: _parseDate(json['scheduledAt']),
      sentAt: _parseDate(json['sentAt']),
      status: json['status'] as String?,
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
