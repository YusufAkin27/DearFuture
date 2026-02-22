import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../services/auth_service.dart';
import '../theme/login_theme.dart';

class VerifyCodeScreen extends StatefulWidget {
  const VerifyCodeScreen({
    super.key,
    required this.email,
    this.onLoginSuccess,
  });

  final String email;
  final VoidCallback? onLoginSuccess;

  @override
  State<VerifyCodeScreen> createState() => _VerifyCodeScreenState();
}

class _VerifyCodeScreenState extends State<VerifyCodeScreen> {
  final _authService = AuthService();
  final _codeController = TextEditingController();

  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _verify() async {
    final code = _codeController.text.trim();
    if (code.length != 6) {
      setState(() => _error = '6 haneli kodu girin.');
      return;
    }
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      await _authService.verifyCode(widget.email, code);
      if (mounted) {
        widget.onLoginSuccess?.call();
        Navigator.of(context).popUntil((route) => route.isFirst);
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _loading = false;
          _error = e.toString().replaceFirst('Exception: ', '');
        });
      }
    }
  }

  Future<void> _resendCode() async {
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      await _authService.sendCode(widget.email);
      if (mounted) {
        setState(() {
          _loading = false;
          _error = null;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _loading = false;
          _error = e.toString().replaceFirst('Exception: ', '');
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Align(
                    alignment: Alignment.centerLeft,
                    child: IconButton(
                      onPressed: _loading ? null : () => Navigator.of(context).pop(),
                      icon: const Icon(
                        Icons.arrow_back_ios_new_rounded,
                        color: LoginColors.textWhite,
                        size: 22,
                      ),
                      style: IconButton.styleFrom(
                        backgroundColor: LoginColors.surface,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  _VerifyLogo(),
                  const SizedBox(height: 28),
                  Text(
                    'Hoş Geldiniz',
                    style: TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.w700,
                      color: LoginColors.textWhite,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'E-postanıza gelen 6 haneli kodu girin.',
                    style: TextStyle(
                      fontSize: 14,
                      color: LoginColors.textMuted,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 28),
                  if (_error != null) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.red.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.error_outline, size: 20, color: Colors.red.shade200),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              _error!,
                              style: TextStyle(fontSize: 13, color: Colors.red.shade200),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],
                  _CodeInputDark(
                    controller: _codeController,
                    onChanged: () => setState(() => _error = null),
                  ),
                  const SizedBox(height: 16),
                  _GradientButton(
                    onPressed: _loading ? null : _verify,
                    loading: _loading,
                    label: 'Doğrula ve giriş yap',
                    icon: Icons.check_rounded,
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: _loading ? null : _resendCode,
                    child: Text(
                      'Kodu tekrar gönder',
                      style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _VerifyLogo extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: 80,
        height: 80,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [LoginColors.primaryStart, LoginColors.primaryEnd],
          ),
          boxShadow: [
            BoxShadow(
              color: LoginColors.primaryStart.withValues(alpha: 0.4),
              blurRadius: 20,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: const Icon(
          Icons.arrow_forward_rounded,
          size: 40,
          color: Colors.white,
        ),
      ),
    );
  }
}

/// Her rakamın kendi kutucuğunda göründüğü 6 haneli kod alanı.
/// Tek TextField kullanır (focus hatası olmaz); yazılan her rakam ilgili kutuda gösterilir.
class _CodeInputDark extends StatefulWidget {
  const _CodeInputDark({
    required this.controller,
    required this.onChanged,
  });

  final TextEditingController controller;
  final VoidCallback onChanged;

  @override
  State<_CodeInputDark> createState() => _CodeInputDarkState();
}

class _CodeInputDarkState extends State<_CodeInputDark> {
  static const int _length = 6;
  static const double _boxSize = 48;
  static const double _gap = 10;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_onTextChanged);
  }

  @override
  void dispose() {
    widget.controller.removeListener(_onTextChanged);
    super.dispose();
  }

  void _onTextChanged() => setState(() {});

  void _filterAndSync(String value) {
    final digits = value.replaceAll(RegExp(r'\D'), '');
    final trimmed = digits.length > _length ? digits.substring(0, _length) : digits;
    if (widget.controller.text != trimmed) {
      widget.controller.text = trimmed;
      widget.controller.selection = TextSelection.collapsed(offset: trimmed.length);
    }
    widget.onChanged();
  }

  @override
  Widget build(BuildContext context) {
    final totalWidth = (_boxSize * _length) + (_gap * (_length - 1));
    final raw = widget.controller.text.replaceAll(RegExp(r'\D'), '');
    final text = raw.length > _length ? raw.substring(0, _length) : raw;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'DOĞRULAMA KODU',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.08,
            color: LoginColors.textLightGray,
          ),
        ),
        const SizedBox(height: 10),
        SizedBox(
          height: _boxSize + 2,
          width: totalWidth,
          child: Stack(
            alignment: Alignment.center,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                mainAxisSize: MainAxisSize.min,
                children: List.generate(_length, (index) {
                  final isActive = index == text.length;
                  return Container(
                    width: _boxSize,
                    height: _boxSize,
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: LoginColors.surface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: isActive
                            ? LoginColors.primaryStart
                            : LoginColors.border,
                        width: isActive ? 1.5 : 1,
                      ),
                    ),
                    child: Text(
                      index < text.length ? text[index] : '',
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.w700,
                        color: LoginColors.textWhite,
                      ),
                    ),
                  );
                }),
              ),
              Positioned.fill(
                child: TextField(
                  controller: widget.controller,
                  maxLength: _length,
                  keyboardType: TextInputType.number,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  onChanged: _filterAndSync,
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.w700,
                    color: Colors.transparent,
                    height: 1.0,
                  ),
                  decoration: const InputDecoration(
                    counterText: '',
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.zero,
                    isDense: true,
                    filled: false,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _GradientButton extends StatelessWidget {
  const _GradientButton({
    required this.onPressed,
    required this.loading,
    required this.label,
    required this.icon,
  });

  final VoidCallback? onPressed;
  final bool loading;
  final String label;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Material(
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onPressed,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [LoginColors.primaryStart, LoginColors.primaryEnd],
            ),
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: LoginColors.primaryStart.withValues(alpha: 0.35),
                blurRadius: 14,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: loading
              ? const Center(
                  child: SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  ),
                )
              : Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      label,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Icon(icon, size: 20, color: Colors.white),
                  ],
                ),
        ),
      ),
    );
  }
}
