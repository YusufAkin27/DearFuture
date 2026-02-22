import 'package:flutter/material.dart';

import '../services/auth_service.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key, this.onLoginSuccess});

  final VoidCallback? onLoginSuccess;

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _authService = AuthService();
  final _emailController = TextEditingController();
  final _codeController = TextEditingController();
  final _emailFocus = FocusNode();

  bool _codeSent = false;
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
    _codeController.dispose();
    _emailFocus.dispose();
    super.dispose();
  }

  Future<void> _sendCode() async {
    final email = _emailController.text.trim();
    if (email.isEmpty) {
      setState(() => _error = 'E-posta adresinizi girin.');
      return;
    }
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      await _authService.sendCode(email);
      if (mounted) {
        setState(() {
          _codeSent = true;
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

  Future<void> _verifyCode() async {
    final email = _emailController.text.trim();
    final code = _codeController.text.trim();
    if (email.isEmpty || code.isEmpty) {
      setState(() => _error = 'E-posta ve doğrulama kodunu girin.');
      return;
    }
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      await _authService.verifyCode(email, code);
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

  Future<void> _signInWithGoogle() async {
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      await _authService.signInWithGoogle();
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

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      body: Container(
        width: double.infinity,
        height: double.infinity,
        color: isDark ? colorScheme.surface : const Color(0xFFF7F9FB),
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 420),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const SizedBox(height: 20),
                    _LoginCard(
                      colorScheme: colorScheme,
                      isDark: isDark,
                      error: _error,
                      onClearError: () => setState(() => _error = null),
                      codeSent: _codeSent,
                      emailController: _emailController,
                      codeController: _codeController,
                      emailFocus: _emailFocus,
                      loading: _loading,
                      onSendCode: _sendCode,
                      onVerifyCode: _verifyCode,
                      onResendCode: _sendCode,
                      onGoogleSignIn: _signInWithGoogle,
                    ),
                    const SizedBox(height: 24),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.lock_outline, size: 16, color: colorScheme.onSurfaceVariant),
                        const SizedBox(width: 6),
                        Text(
                          'Uçtan uca şifreli & güvenli',
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: colorScheme.onSurfaceVariant,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _LoginCard extends StatefulWidget {
  const _LoginCard({
    required this.colorScheme,
    required this.isDark,
    required this.error,
    required this.onClearError,
    required this.codeSent,
    required this.emailController,
    required this.codeController,
    required this.emailFocus,
    required this.loading,
    required this.onSendCode,
    required this.onVerifyCode,
    required this.onResendCode,
    required this.onGoogleSignIn,
  });

  final ColorScheme colorScheme;
  final bool isDark;
  final String? error;
  final VoidCallback onClearError;
  final bool codeSent;
  final TextEditingController emailController;
  final TextEditingController codeController;
  final FocusNode emailFocus;
  final bool loading;
  final VoidCallback onSendCode;
  final VoidCallback onVerifyCode;
  final VoidCallback onResendCode;
  final VoidCallback onGoogleSignIn;

  @override
  State<_LoginCard> createState() => _LoginCardState();
}

class _LoginCardState extends State<_LoginCard> {
  bool _emailFocused = false;

  @override
  void initState() {
    super.initState();
    widget.emailFocus.addListener(_onFocusChange);
    widget.emailController.addListener(_onEmailChange);
  }

  @override
  void dispose() {
    widget.emailFocus.removeListener(_onFocusChange);
    widget.emailController.removeListener(_onEmailChange);
    super.dispose();
  }

  void _onFocusChange() {
    setState(() => _emailFocused = widget.emailFocus.hasFocus);
  }

  void _onEmailChange() {
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
      decoration: BoxDecoration(
        color: widget.colorScheme.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: widget.isDark
              ? Colors.white.withValues(alpha: 0.06)
              : const Color(0xFF0F172A).withValues(alpha: 0.08),
        ),
        boxShadow: [
          BoxShadow(
            color: widget.isDark
                ? Colors.black.withValues(alpha: 0.35)
                : const Color(0xFF142850).withValues(alpha: 0.08),
            blurRadius: 20,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Icon(
            Icons.mail_outline_rounded,
            size: 56,
            color: widget.colorScheme.primary,
          ),
          const SizedBox(height: 20),
          Text(
            'Hoş Geldiniz',
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: widget.colorScheme.onSurface,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          Text(
            'Zamanın ötesine bir not bırakmaya hazır mısın?',
            style: theme.textTheme.bodyLarge?.copyWith(
              fontWeight: FontWeight.w600,
              color: widget.colorScheme.onSurfaceVariant,
              height: 1.4,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 4),
          Text(
            widget.codeSent
                ? 'E-postanıza gelen 6 haneli kodu girin.'
                : 'Yolculuğuna başlamak için e-postanı gir.',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: widget.colorScheme.onSurfaceVariant.withValues(alpha: 0.9),
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),
          if (widget.error != null) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: widget.colorScheme.errorContainer,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Icon(Icons.error_outline, size: 20, color: widget.colorScheme.onErrorContainer),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      widget.error!,
                      style: TextStyle(
                        fontSize: 13,
                        color: widget.colorScheme.onErrorContainer,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
          ],
          if (!widget.codeSent) ...[
            _EmailInput(
              controller: widget.emailController,
              focusNode: widget.emailFocus,
              focused: _emailFocused,
              colorScheme: widget.colorScheme,
              onChanged: widget.onClearError,
            ),
            const SizedBox(height: 20),
            _GradientButton(
              onPressed: widget.loading ? null : widget.onSendCode,
              loading: widget.loading,
              label: 'Sihirli Bağlantı Gönder',
              icon: Icons.send_rounded,
            ),
          ] else ...[
            _CodeInput(
              controller: widget.codeController,
              colorScheme: widget.colorScheme,
              onChanged: widget.onClearError,
            ),
            const SizedBox(height: 16),
            _GradientButton(
              onPressed: widget.loading ? null : widget.onVerifyCode,
              loading: widget.loading,
              label: 'Doğrula ve giriş yap',
              icon: Icons.check_rounded,
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: widget.loading ? null : widget.onResendCode,
              child: const Text('Kodu tekrar gönder'),
            ),
          ],
          const SizedBox(height: 20),
          Row(
            children: [
              Expanded(child: Divider(color: widget.colorScheme.outlineVariant)),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Text(
                  'veya',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: widget.colorScheme.onSurfaceVariant,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
              Expanded(child: Divider(color: widget.colorScheme.outlineVariant)),
            ],
          ),
          const SizedBox(height: 20),
          OutlinedButton.icon(
            onPressed: widget.loading ? null : widget.onGoogleSignIn,
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 14),
              side: BorderSide(
                color: widget.isDark
                    ? Colors.white.withValues(alpha: 0.12)
                    : const Color(0xFF0F172A).withValues(alpha: 0.15),
              ),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            icon: const Text(
              'G',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Color(0xFF4285F4),
              ),
            ),
            label: const Text('Google ile giriş yap', style: TextStyle(fontWeight: FontWeight.w600)),
          ),
        ],
      ),
    );
  }
}

class _EmailInput extends StatelessWidget {
  const _EmailInput({
    required this.controller,
    required this.focusNode,
    required this.focused,
    required this.colorScheme,
    required this.onChanged,
  });

  final TextEditingController controller;
  final FocusNode focusNode;
  final bool focused;
  final ColorScheme colorScheme;
  final VoidCallback onChanged;

  @override
  Widget build(BuildContext context) {
    final borderColor = focused || controller.text.isNotEmpty
        ? colorScheme.primary
        : (colorScheme.brightness == Brightness.dark
            ? Colors.white.withValues(alpha: 0.1)
            : const Color(0xFF0F172A).withValues(alpha: 0.12));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'E-POSTA ADRESİ',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.06,
            color: focused || controller.text.isNotEmpty
                ? colorScheme.primary
                : colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 6),
        Container(
          decoration: BoxDecoration(
            color: colorScheme.brightness == Brightness.dark
                ? Colors.white.withValues(alpha: 0.04)
                : const Color(0xFFF7F9FB),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: borderColor),
            boxShadow: focused ? [
              BoxShadow(
                color: colorScheme.primary.withValues(alpha: 0.12),
                blurRadius: 0,
                spreadRadius: 2,
              ),
            ] : null,
          ),
          child: TextField(
            controller: controller,
            focusNode: focusNode,
            keyboardType: TextInputType.emailAddress,
            autocorrect: false,
            onChanged: (_) => onChanged(),
            style: const TextStyle(fontWeight: FontWeight.w500),
            decoration: InputDecoration(
              hintText: 'ornek@email.com',
              hintStyle: TextStyle(color: colorScheme.onSurfaceVariant),
              prefixIcon: Icon(
                Icons.email_outlined,
                size: 20,
                color: focused || controller.text.isNotEmpty
                    ? colorScheme.primary
                    : colorScheme.onSurfaceVariant,
              ),
              border: InputBorder.none,
              contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
            ),
          ),
        ),
      ],
    );
  }
}

class _CodeInput extends StatelessWidget {
  const _CodeInput({
    required this.controller,
    required this.colorScheme,
    required this.onChanged,
  });

  final TextEditingController controller;
  final ColorScheme colorScheme;
  final VoidCallback onChanged;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'DOĞRULAMA KODU',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.06,
            color: colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 6),
        Container(
          decoration: BoxDecoration(
            color: colorScheme.brightness == Brightness.dark
                ? Colors.white.withValues(alpha: 0.04)
                : const Color(0xFFF7F9FB),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: colorScheme.brightness == Brightness.dark
                  ? Colors.white.withValues(alpha: 0.1)
                  : const Color(0xFF0F172A).withValues(alpha: 0.12),
            ),
          ),
          child: TextField(
            controller: controller,
            keyboardType: TextInputType.number,
            maxLength: 6,
            onChanged: (_) => onChanged(),
            style: const TextStyle(fontWeight: FontWeight.w600, letterSpacing: 4),
            decoration: InputDecoration(
              hintText: '000000',
              counterText: '',
              prefixIcon: Icon(Icons.pin_outlined, size: 20, color: colorScheme.primary),
              border: InputBorder.none,
              contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
            ),
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
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                const Color(0xFF00A8CC),
                const Color(0xFF00D2FC),
              ],
            ),
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF00A8CC).withValues(alpha: 0.25),
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
