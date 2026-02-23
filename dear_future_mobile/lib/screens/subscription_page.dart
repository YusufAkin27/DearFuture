import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../models/profile_models.dart';
import '../models/subscription_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../services/subscription_service.dart';
import '../theme/login_theme.dart';
import 'subscription_detail_page.dart';

class SubscriptionPage extends StatefulWidget {
  const SubscriptionPage({super.key, this.onLogout});

  final VoidCallback? onLogout;

  @override
  State<SubscriptionPage> createState() => _SubscriptionPageState();
}

class _SubscriptionPageState extends State<SubscriptionPage> {
  final AuthService _auth = AuthService();
  final SubscriptionService _subscriptionService = SubscriptionService();
  ApiClient? _apiClient;
  ProfileService? _profileService;
  SubscriptionService? _authSubscriptionService;

  List<PlanListItem> _plans = [];
  ProfileData? _profile;
  bool _loading = true;
  String? _error;
  bool _checkoutLoading = false;

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  Future<void> _initAndLoad() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    final token = _auth.token;
    if (token != null && token.isNotEmpty) {
      _apiClient = ApiClient(token: token, onUnauthorized: widget.onLogout);
      _profileService = ProfileService(_apiClient!);
      _authSubscriptionService = SubscriptionService(_apiClient!);
    }
    await _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final plans = await _subscriptionService.getPlans();
      ProfileData? profile;
      if (_profileService != null) {
        profile = await _profileService!.getProfile();
      }
      if (!mounted) return;
      setState(() {
        _plans = plans;
        _profile = profile;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Bilgiler yüklenemedi.';
      });
    }
  }

  String get _currentPlanCode => _profile?.subscriptionPlanCode?.toUpperCase() ?? 'FREE';

  Future<void> _openCheckout(PlanListItem plan) async {
    if (plan.isFree) return;
    if (_authSubscriptionService == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Giriş yaparak plan satın alabilirsiniz.')),
      );
      return;
    }
    setState(() => _checkoutLoading = true);
    try {
      final result = await _authSubscriptionService!.initializeCheckout(plan.id);
      if (!mounted) return;
      setState(() => _checkoutLoading = false);
      if (result == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ödeme başlatılamadı.'), backgroundColor: Colors.red),
        );
        return;
      }
      final url = result.paymentPageUrl;
      if (url == null || url.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ödeme sayfası alınamadı.'), backgroundColor: Colors.red),
        );
        return;
      }
      final uri = Uri.parse(url);
      try {
        final launched = await launchUrl(uri, mode: LaunchMode.externalApplication);
        if (mounted && launched) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Ödeme tamamlandıktan sonra uygulamaya dönün. Sayfayı yenilemek için aşağıyı çekin.'),
              duration: Duration(seconds: 4),
            ),
          );
        } else if (mounted && !launched) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Ödeme sayfası açılamadı. Lütfen tarayıcıyı güncelleyin veya linki kopyalayıp tarayıcıda açın.'), backgroundColor: Colors.red),
          );
        }
      } on Exception catch (_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Ödeme sayfası açılamadı. Lütfen izinleri kontrol edin veya linki tarayıcıda açın.'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() => _checkoutLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Hata: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  void _openPlanDetail(PlanListItem plan) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (context) => SubscriptionDetailPage(
          planCode: plan.id,
          currentPlanCode: _currentPlanCode,
          onPurchase: () => _openCheckout(plan),
        ),
      ),
    ).then((_) => _loadData());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: RefreshIndicator(
        onRefresh: _loadData,
        color: LoginColors.primaryEnd,
        child: _loading && _plans.isEmpty
            ? Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_error!, style: TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
                          const SizedBox(height: 16),
                          TextButton(
                            onPressed: _loadData,
                            child: Text('Tekrar dene', style: TextStyle(color: LoginColors.primaryEnd)),
                          ),
                        ],
                      ),
                    ),
                  )
     
    
                : CustomScrollView(
                    slivers: [
                      SliverToBoxAdapter(
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Aboneliği Yönet',
                                style: TextStyle(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w700,
                                  color: LoginColors.textWhite,
                                ),
                              ),
                              const SizedBox(height: 8),
                              RichText(
                                text: TextSpan(
                                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                                  children: [
                                    const TextSpan(text: 'Mevcut planınız: '),
                                    TextSpan(
                                      text: _currentPlanCode,
                                      style: TextStyle(
                                        color: LoginColors.primaryEnd,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      SliverPadding(
                        padding: const EdgeInsets.symmetric(horizontal: 20),
                        sliver: SliverList(
                          delegate: SliverChildBuilderDelegate(
                            (context, index) {
                              final plan = _plans[index];
                              final isCurrent = _currentPlanCode == plan.id.toUpperCase();
                              final isRecommended = plan.recommended;
                              return Padding(
                                padding: const EdgeInsets.only(bottom: 16),
                                child: _PlanCard(
                                  plan: plan,
                                  isCurrent: isCurrent,
                                  isRecommended: isRecommended,
                                  checkoutLoading: _checkoutLoading,
                                  onTap: () => _openPlanDetail(plan),
                                  onPurchase: () => _openCheckout(plan),
                                ),
                              );
                            },
                            childCount: _plans.length,
                          ),
                        ),
                      ),
                      SliverToBoxAdapter(
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Ödeme iyzico güvencesiyle alınır.',
                                style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'Abonelik her ay otomatik yenilenir; süre sonunda yenileme yapılmazsa plan Ücretsiz\'e döner.',
                                style: TextStyle(fontSize: 12, color: LoginColors.textMuted, height: 1.35),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'Ödeme için iyzico test kartlarını kullanabilirsiniz.',
                                style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
    );
  }
}

class _PlanCard extends StatelessWidget {
  const _PlanCard({
    required this.plan,
    required this.isCurrent,
    required this.isRecommended,
    required this.checkoutLoading,
    required this.onTap,
    required this.onPurchase,
  });

  final PlanListItem plan;
  final bool isCurrent;
  final bool isRecommended;
  final bool checkoutLoading;
  final VoidCallback onTap;
  final VoidCallback onPurchase;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: LoginColors.surface,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: isRecommended ? LoginColors.primaryEnd : LoginColors.border,
              width: isRecommended ? 2 : 1,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  if (isCurrent)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        'Mevcut Plan',
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                      ),
                    ),
                  if (isRecommended && !isCurrent) ...[
                    if (isCurrent) const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        'Önerilen',
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                      ),
                    ),
                  ],
                ],
              ),
              if (plan.description != null && plan.description!.isNotEmpty) ...[
                const SizedBox(height: 8),
                Text(
                  plan.description!,
                  style: TextStyle(fontSize: 13, color: LoginColors.textLightGray),
                ),
              ],
              const SizedBox(height: 12),
              Text(
                plan.name,
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: LoginColors.textWhite,
                ),
              ),
              if (!plan.isFree) ...[
                const SizedBox(height: 4),
                Text(
                  '${plan.price} ${plan.priceLabel}',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                ),
              ],
              const SizedBox(height: 12),
              ...plan.features.take(6).map(
                    (f) => Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Icon(Icons.check_circle_rounded, size: 18, color: LoginColors.primaryEnd),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              f,
                              style: TextStyle(fontSize: 13, color: LoginColors.textLightGray),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: plan.isFree
                    ? OutlinedButton(
                        onPressed: null,
                        style: OutlinedButton.styleFrom(
                          foregroundColor: LoginColors.textWhite,
                          side: BorderSide(color: LoginColors.border),
                        ),
                        child: const Text('Aktif'),
                      )
                    : isCurrent
                        ? OutlinedButton(
                            onPressed: null,
                            style: OutlinedButton.styleFrom(
                              foregroundColor: LoginColors.textWhite,
                              side: BorderSide(color: LoginColors.primaryEnd),
                            ),
                            child: const Text('Aktif'),
                          )
                        : FilledButton(
                            onPressed: checkoutLoading ? null : onPurchase,
                            style: FilledButton.styleFrom(
                              backgroundColor: isRecommended ? LoginColors.primaryEnd : LoginColors.surface,
                              foregroundColor: isRecommended ? Colors.white : LoginColors.primaryEnd,
                              side: BorderSide(color: LoginColors.primaryEnd),
                            ),
                            child: checkoutLoading
                                ? SizedBox(
                                    height: 20,
                                    width: 20,
                                    child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd),
                                  )
                                : const Text('Satın Al'),
                          ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
