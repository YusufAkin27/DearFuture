/// Backend GET /api/messages/public sayfalı yanıt öğesi (PublicMessageItemResponse).
class PublicMessageItem {
  PublicMessageItem({
    this.id,
    this.viewToken,
    this.scheduledAt,
    this.sentAt,
    this.senderName,
    this.textPreview,
    this.starredByMe = false,
  });

  final int? id; // Backend Long, star/unstar için messageId
  final String? viewToken;
  final DateTime? scheduledAt;
  final DateTime? sentAt;
  final String? senderName;
  final String? textPreview;
  final bool starredByMe;

  static DateTime? _parseDate(dynamic v) {
    if (v == null) return null;
    if (v is String) {
      final d = DateTime.tryParse(v);
      if (d != null) return d;
      return null;
    }
    if (v is num) return DateTime.fromMillisecondsSinceEpoch(v.toInt());
    if (v is List && v.length >= 3) {
      try {
        final y = (v[0] as num).toInt();
        final m = (v[1] as num).toInt();
        final day = (v[2] as num).toInt();
        final h = v.length > 3 ? (v[3] as num).toInt() : 0;
        final min = v.length > 4 ? (v[4] as num).toInt() : 0;
        final sec = v.length > 5 ? (v[5] as num).toInt() : 0;
        return DateTime(y, m, day, h, min, sec);
      } catch (_) {}
    }
    return null;
  }

  /// Gösterim: "22.02.2024 14:30" (iletilme/kaydedilme zamanı).
  static String formatDateTime(DateTime? d) {
    if (d == null) return '—';
    final y = d.year;
    final m = d.month.toString().padLeft(2, '0');
    final day = d.day.toString().padLeft(2, '0');
    final h = d.hour.toString().padLeft(2, '0');
    final min = d.minute.toString().padLeft(2, '0');
    return '$day.$m.$y $h:$min';
  }

  static PublicMessageItem? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return PublicMessageItem(
      id: (json['id'] as num?)?.toInt(),
      viewToken: json['viewToken'] as String?,
      scheduledAt: _parseDate(json['scheduledAt']),
      sentAt: _parseDate(json['sentAt']),
      senderName: json['senderName'] as String?,
      textPreview: json['textPreview'] as String?,
      starredByMe: json['starredByMe'] == true,
    );
  }
}

/// Sayfalı yanıt: { content: [], totalElements, totalPages, size, number }.
class PublicMessagePage {
  PublicMessagePage({
    this.content = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.size = 12,
    this.number = 0,
  });

  final List<PublicMessageItem> content;
  final int totalElements;
  final int totalPages;
  final int size;
  final int number;

  bool get hasNext => number + 1 < totalPages;
  int get nextPage => number + 1;

  static PublicMessagePage? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    List<PublicMessageItem> content = [];
    if (json['content'] is List) {
      content = (json['content'] as List)
          .map((e) => PublicMessageItem.fromJson(e as Map<String, dynamic>?))
          .whereType<PublicMessageItem>()
          .toList();
    }
    return PublicMessagePage(
      content: content,
      totalElements: (json['totalElements'] as num?)?.toInt() ?? 0,
      totalPages: (json['totalPages'] as num?)?.toInt() ?? 0,
      size: (json['size'] as num?)?.toInt() ?? 12,
      number: (json['number'] as num?)?.toInt() ?? 0,
    );
  }
}

/// Backend GET /api/messages/view/{token} → MessageViewResponse.
class MessageViewDetail {
  MessageViewDetail({
    this.scheduledAt,
    this.senderName,
    this.contents = const [],
  });

  final DateTime? scheduledAt;
  final String? senderName;
  final List<MessageViewContentItem> contents;

  static DateTime? _parseDate(dynamic v) {
    if (v == null) return null;
    if (v is String) return DateTime.tryParse(v);
    if (v is num) return DateTime.fromMillisecondsSinceEpoch(v.toInt());
    return null;
  }

  static MessageViewDetail? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    List<MessageViewContentItem> contents = [];
    if (json['contents'] is List) {
      contents = (json['contents'] as List)
          .map((e) => MessageViewContentItem.fromJson(e as Map<String, dynamic>?))
          .whereType<MessageViewContentItem>()
          .toList();
    }
    return MessageViewDetail(
      scheduledAt: _parseDate(json['scheduledAt']),
      senderName: json['senderName'] as String?,
      contents: contents,
    );
  }
}

class MessageViewContentItem {
  MessageViewContentItem({
    this.type,
    this.textContent,
    this.fileUrl,
    this.fileName,
    this.fileSize,
  });

  final String? type; // TEXT, IMAGE, VIDEO, FILE, AUDIO
  final String? textContent;
  final String? fileUrl;
  final String? fileName;
  final int? fileSize;

  static MessageViewContentItem? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return MessageViewContentItem(
      type: json['type'] as String?,
      textContent: json['textContent'] as String?,
      fileUrl: json['fileUrl'] as String?,
      fileName: json['fileName'] as String?,
      fileSize: (json['fileSize'] as num?)?.toInt(),
    );
  }
}
