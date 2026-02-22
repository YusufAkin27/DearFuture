import 'dart:convert';

import '../config/api_config.dart';
import '../models/subscription_models.dart';
import 'api_client.dart';

class SubscriptionService {
  SubscriptionService([ApiClient? client]) : _client = client ?? ApiClient();

  final ApiClient _client;

  /// GET /api/subscription/plans — giriş gerekmez
  Future<List<PlanListItem>> getPlans() async {
    final res = await _client.get(ApiConfig.subscriptionPlans);
    if (res.statusCode != 200) return [];
    final list = jsonDecode(res.body);
    if (list is! List) return [];
    return list
        .map((e) => PlanListItem.fromJson(e as Map<String, dynamic>?))
        .whereType<PlanListItem>()
        .toList();
  }

  /// GET /api/subscription/plans/{code} — giriş gerekmez
  Future<PlanDetail?> getPlanByCode(String code) async {
    if (code.isEmpty) return null;
    final res = await _client.get(ApiConfig.subscriptionPlanByCode(code));
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return PlanDetail.fromJson(map);
  }

  /// POST /api/subscription/checkout/initialize — JWT gerekli
  Future<CheckoutInitializeResult?> initializeCheckout(String planCode) async {
    final res = await _client.post(
      ApiConfig.subscriptionCheckoutInitialize,
      body: {'planCode': planCode},
    );
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return CheckoutInitializeResult.fromJson(map);
  }

  /// POST /api/subscription/cancel — JWT gerekli
  Future<bool> cancelSubscription() async {
    final res = await _client.post(ApiConfig.subscriptionCancel, body: {});
    return res.statusCode == 200 || res.statusCode == 204;
  }
}
