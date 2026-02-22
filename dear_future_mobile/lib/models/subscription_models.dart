/// Backend GET /api/subscription/plans → PlanResponse
class PlanListItem {
  PlanListItem({
    required this.id,
    required this.name,
    this.description,
    this.price = 0,
    this.priceLabel = '₺/ay',
    this.features = const [],
    this.recommended = false,
  });

  final String id;
  final String name;
  final String? description;
  final int price;
  final String priceLabel;
  final List<String> features;
  final bool recommended;

  bool get isFree => price == 0;

  static PlanListItem? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    List<String> features = [];
    if (json['features'] != null && json['features'] is List) {
      features = (json['features'] as List).map((e) => e.toString()).toList();
    }
    return PlanListItem(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      description: json['description'] as String?,
      price: (json['price'] as num?)?.toInt() ?? 0,
      priceLabel: json['priceLabel'] as String? ?? '₺/ay',
      features: features,
      recommended: json['recommended'] == true,
    );
  }
}

/// Backend GET /api/subscription/plans/{code} → PlanDetailResponse
class PlanDetail {
  PlanDetail({
    required this.id,
    required this.name,
    this.description,
    this.price = 0,
    this.priceLabel = '₺/ay',
    this.features = const [],
    this.recommended = false,
    this.active = true,
    this.maxMessages = 3,
    this.maxRecipientsPerMessage = 1,
    this.allowPhoto = false,
    this.allowFile = false,
    this.allowVoice = false,
    this.maxPhotosPerMessage = 0,
    this.maxPhotoSizeBytes = 0,
    this.maxFilesPerMessage = 0,
    this.maxFileSizeBytes = 0,
    this.maxAudioPerMessage = 0,
    this.maxAudioSizeBytes = 0,
    this.displayOrder = 0,
  });

  final String id;
  final String name;
  final String? description;
  final int price;
  final String priceLabel;
  final List<String> features;
  final bool recommended;
  final bool active;
  final int maxMessages;
  final int maxRecipientsPerMessage;
  final bool allowPhoto;
  final bool allowFile;
  final bool allowVoice;
  final int maxPhotosPerMessage;
  final int maxPhotoSizeBytes;
  final int maxFilesPerMessage;
  final int maxFileSizeBytes;
  final int maxAudioPerMessage;
  final int maxAudioSizeBytes;
  final int displayOrder;

  bool get isFree => price == 0;

  static PlanDetail? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    List<String> features = [];
    if (json['features'] != null && json['features'] is List) {
      features = (json['features'] as List).map((e) => e.toString()).toList();
    }
    return PlanDetail(
      id: json['id'] as String? ?? '',
      name: json['name'] as String? ?? '',
      description: json['description'] as String?,
      price: (json['price'] as num?)?.toInt() ?? 0,
      priceLabel: json['priceLabel'] as String? ?? '₺/ay',
      features: features,
      recommended: json['recommended'] == true,
      active: json['active'] != false,
      maxMessages: (json['maxMessages'] as num?)?.toInt() ?? 3,
      maxRecipientsPerMessage: (json['maxRecipientsPerMessage'] as num?)?.toInt() ?? 1,
      allowPhoto: json['allowPhoto'] == true,
      allowFile: json['allowFile'] == true,
      allowVoice: json['allowVoice'] == true,
      maxPhotosPerMessage: (json['maxPhotosPerMessage'] as num?)?.toInt() ?? 0,
      maxPhotoSizeBytes: (json['maxPhotoSizeBytes'] as num?)?.toInt() ?? 0,
      maxFilesPerMessage: (json['maxFilesPerMessage'] as num?)?.toInt() ?? 0,
      maxFileSizeBytes: (json['maxFileSizeBytes'] as num?)?.toInt() ?? 0,
      maxAudioPerMessage: (json['maxAudioPerMessage'] as num?)?.toInt() ?? 0,
      maxAudioSizeBytes: (json['maxAudioSizeBytes'] as num?)?.toInt() ?? 0,
      displayOrder: (json['displayOrder'] as num?)?.toInt() ?? 0,
    );
  }
}

/// Backend POST /api/subscription/checkout/initialize → CheckoutInitializeResponse
class CheckoutInitializeResult {
  CheckoutInitializeResult({
    this.paymentPageUrl,
    this.checkoutFormContent,
    this.token,
  });

  final String? paymentPageUrl;
  final String? checkoutFormContent;
  final String? token;

  static CheckoutInitializeResult? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return CheckoutInitializeResult(
      paymentPageUrl: json['paymentPageUrl'] as String?,
      checkoutFormContent: json['checkoutFormContent'] as String?,
      token: json['token'] as String?,
    );
  }
}
