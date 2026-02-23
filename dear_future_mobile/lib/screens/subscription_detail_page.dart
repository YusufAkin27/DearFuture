import 'package:flutter/material.dart';

import '../models/subscription_models.dart';
import '../services/subscription_service.dart';
import '../theme/login_theme.dart';

class SubscriptionDetailPage extends StatefulWidget {
  const SubscriptionDetailPage({
    super.key,
    required this.planCode,
    required this.currentPlanCode,
    required this.onPurchase,
  });

  final String planCode;
  final String currentPlanCode;
  final VoidCallback onPurchase;

  @override
  State<SubscriptionDetailPage> createState() => _SubscriptionDetailPageState();
}

class _SubscriptionDetailPageState extends State<SubscriptionDetailPage> {
  final SubscriptionService _service = SubscriptionService();
  PlanDetail? _plan;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadPlan();
  }

  Future<void> _loadPlan() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final plan = await _service.getPlanByCode(widget.planCode);
      if (!mounted) return;
      setState(() {
        _plan = plan;
        _loading = false;
        if (plan == null) _error = 'Plan bulunamadı.';
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Plan yüklenemedi.';
      });
    }
  }

  bool get _isCurrentPlan =>
      (widget.currentPlanCode.toUpperCase()) == (_plan?.id.toUpperCase() ?? '');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        leading: IconButton(
          icon: Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: LoginColors.surface,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: LoginColors.border),
            ),
            child: const Icon(Icons.arrow_back_rounded, size: 20),
          ),
          color: LoginColors.textWhite,
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          'Plan detayı',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w600,
            color: LoginColors.textWhite,
          ),
        ),
        centerTitle: true,
      ),
      body: _loading
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(
                    width: 40,
                    height: 40,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: LoginColors.primaryEnd,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'Yükleniyor...',
                    style: TextStyle(fontSize: 14, color: LoginColors.textMuted),
                  ),
                ],
              ),
            )
          : _error != null || _plan == null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(32),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.error_outline_rounded, size: 56, color: LoginColors.textMuted),
                        const SizedBox(height: 20),
                        Text(
                          _error ?? 'Plan bulunamadı.',
                          style: TextStyle(fontSize: 16, color: LoginColors.textLightGray),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 24),
                        FilledButton.icon(
                          onPressed: () => Navigator.of(context).pop(),
                          icon: const Icon(Icons.arrow_back_rounded, size: 20),
                          label: const Text('Fiyatlandırmaya dön'),
                          style: FilledButton.styleFrom(
                            backgroundColor: LoginColors.primaryEnd,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                          ),
                        ),
                      ],
                    ),
                  ),
                )
              : SingleChildScrollView(
                  physics: const BouncingScrollPhysics(),
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildSummaryCard(),
                      const SizedBox(height: 28),
                      _buildSectionTitle('Neler dahil?'),
                      const SizedBox(height: 14),
                      _buildFeaturesSection(),
                      const SizedBox(height: 28),
                      _buildSectionTitle('Limitler ve kotalar'),
                      const SizedBox(height: 14),
                      _buildLimitsSection(),
                      const SizedBox(height: 32),
                      _buildActionButton(),
                    ],
                  ),
                ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: TextStyle(
        fontSize: 17,
        fontWeight: FontWeight.w700,
        color: LoginColors.textWhite,
        letterSpacing: 0.2,
      ),
    );
  }

  Widget _buildSummaryCard() {
    final p = _plan!;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            LoginColors.surface,
            LoginColors.surface,
            LoginColors.primaryEnd.withValues(alpha: 0.08),
          ],
        ),
        border: Border.all(
          color: LoginColors.primaryEnd.withValues(alpha: 0.35),
          width: 1,
        ),
        boxShadow: [
          BoxShadow(
            color: LoginColors.primaryEnd.withValues(alpha: 0.06),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
            decoration: BoxDecoration(
              color: LoginColors.primaryEnd.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              p.id,
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: LoginColors.primaryEnd,
                letterSpacing: 0.5,
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            p.name,
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w800,
              color: LoginColors.textWhite,
              letterSpacing: -0.5,
            ),
          ),
          if (p.description != null && p.description!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              p.description!,
              style: TextStyle(
                fontSize: 14,
                color: LoginColors.textLightGray,
                height: 1.4,
              ),
            ),
          ],
          const SizedBox(height: 20),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: LoginColors.background.withValues(alpha: 0.5),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              p.isFree ? p.name : '${p.price} ${p.priceLabel}',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.w800,
                color: LoginColors.textWhite,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeaturesSection() {
    final p = _plan!;
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        children: p.features
            .map(
              (f) => Padding(
                padding: const EdgeInsets.only(bottom: 14),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 28,
                      height: 28,
                      decoration: BoxDecoration(
                        color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      alignment: Alignment.center,
                      child: Icon(Icons.check_rounded, size: 18, color: LoginColors.primaryEnd),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Text(
                        f,
                        style: TextStyle(
                          fontSize: 15,
                          color: LoginColors.textLightGray,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            )
            .toList(),
      ),
    );
  }

  Widget _buildLimitsSection() {
    final p = _plan!;
    final limits = <_LimitItem>[
      _LimitItem(Icons.send_rounded, 'Mesaj hakkı', '${p.maxMessages} mesaj'),
      _LimitItem(Icons.people_outline_rounded, 'Alıcı (mesaj başına)', '${p.maxRecipientsPerMessage} kişi'),
      _LimitItem(
        Icons.photo_camera_outlined,
        'Fotoğraf / video',
        p.allowPhoto ? '${p.maxPhotosPerMessage} adet' : 'Yok',
      ),
      _LimitItem(
        Icons.insert_drive_file_outlined,
        'Dosya',
        p.allowFile ? '${p.maxFilesPerMessage} adet' : 'Yok',
      ),
      _LimitItem(
        Icons.mic_none_rounded,
        'Ses kaydı',
        p.allowVoice ? 'Var' : 'Yok',
      ),
    ];
    return GridView.count(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisCount: 2,
      mainAxisSpacing: 14,
      crossAxisSpacing: 14,
      childAspectRatio: 1.0,
      children: limits
          .map(
            (item) => Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: LoginColors.surface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: LoginColors.border.withValues(alpha: 0.8)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.15),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: LoginColors.primaryEnd.withValues(alpha: 0.18),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: LoginColors.primaryEnd.withValues(alpha: 0.3),
                        width: 1,
                      ),
                    ),
                    alignment: Alignment.center,
                    child: Icon(item.icon, size: 24, color: LoginColors.primaryEnd),
                  ),
                  const SizedBox(height: 14),
                  Text(
                    item.title,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: LoginColors.textLightGray,
                      height: 1.25,
                    ),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    item.value,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: LoginColors.textWhite,
                      letterSpacing: 0.2,
                    ),
                  ),
                ],
              ),
            ),
          )
          .toList(),
    );
  }

  Widget _buildActionButton() {
    final p = _plan!;
    final isCurrent = _isCurrentPlan;
    if (p.isFree || isCurrent) {
      return SizedBox(
        width: double.infinity,
        child: OutlinedButton(
          onPressed: null,
          style: OutlinedButton.styleFrom(
            foregroundColor: LoginColors.textWhite,
            side: BorderSide(color: LoginColors.border),
            padding: const EdgeInsets.symmetric(vertical: 16),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
          ),
          child: Text(isCurrent ? 'Aktif plan' : p.name),
        ),
      );
    }
    return SizedBox(
      width: double.infinity,
      child: FilledButton(
        onPressed: widget.onPurchase,
        style: FilledButton.styleFrom(
          backgroundColor: LoginColors.primaryEnd,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 16),
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        ),
        child: const Text('Satın Al'),
      ),
    );
  }
}

class _LimitItem {
  const _LimitItem(this.icon, this.title, this.value);
  final IconData icon;
  final String title;
  final String value;
}
