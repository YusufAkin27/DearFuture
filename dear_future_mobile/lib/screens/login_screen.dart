import 'package:flutter/material.dart';

import '../services/auth_service.dart';
import '../theme/login_theme.dart';
import 'verify_code_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key, this.onLoginSuccess});

  final VoidCallback? onLoginSuccess;

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _authService = AuthService();
  final _emailController = TextEditingController();
  final _emailFocus = FocusNode();

  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _emailController.dispose();
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
        setState(() => _loading = false);
        Navigator.of(context).push(
          MaterialPageRoute<void>(
            builder: (context) => VerifyCodeScreen(
              email: email,
              onLoginSuccess: widget.onLoginSuccess,
            ),
          ),
        );
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
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _LoginCard(
                    error: _error,
                    onClearError: () => setState(() => _error = null),
                    emailController: _emailController,
                    emailFocus: _emailFocus,
                    loading: _loading,
                    onSendCode: _sendCode,
                    onGoogleSignIn: _signInWithGoogle,
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

class _LoginCard extends StatefulWidget {
  const _LoginCard({
    required this.error,
    required this.onClearError,
    required this.emailController,
    required this.emailFocus,
    required this.loading,
    required this.onSendCode,
    required this.onGoogleSignIn,
  });

  final String? error;
  final VoidCallback onClearError;
  final TextEditingController emailController;
  final FocusNode emailFocus;
  final bool loading;
  final VoidCallback onSendCode;
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
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _LoginLogo(),
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
          'Zamanın ötesine bir not bırakmaya hazır mısın?',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w500,
            color: LoginColors.textLightGray,
            height: 1.4,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 6),
        Text(
          'Yolculuğuna başlamak için e-postanı gir.',
          style: TextStyle(
            fontSize: 14,
            color: LoginColors.textMuted,
            height: 1.5,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 28),
        if (widget.error != null) ...[
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
                    widget.error!,
                    style: TextStyle(fontSize: 13, color: Colors.red.shade200),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
        ],
        _EmailInputDark(
          controller: widget.emailController,
          focusNode: widget.emailFocus,
          focused: _emailFocused,
          onChanged: widget.onClearError,
        ),
        const SizedBox(height: 20),
        _GradientButton(
          onPressed: widget.loading ? null : widget.onSendCode,
          loading: widget.loading,
          label: 'Sihirli Bağlantı Gönder',
          icon: Icons.send_rounded,
        ),
        const SizedBox(height: 24),
        Text(
          'veya',
          style: TextStyle(
            fontSize: 14,
            color: LoginColors.textMuted,
            fontWeight: FontWeight.w500,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 20),
        _GoogleButton(
          onPressed: widget.loading ? null : widget.onGoogleSignIn,
        ),
      ],
    );
  }
}

class _LoginLogo extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: 80,
        height: 80,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: LinearGradient(
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

class _GoogleButton extends StatelessWidget {
  const _GoogleButton({required this.onPressed});

  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: LoginColors.surface,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onPressed,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: LoginColors.border),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'G',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF4285F4),
                ),
              ),
              const SizedBox(width: 10),
              Text(
                'Google ile Giriş Yap',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: LoginColors.textWhite,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _EmailInputDark extends StatelessWidget {
  const _EmailInputDark({
    required this.controller,
    required this.focusNode,
    required this.focused,
    required this.onChanged,
  });

  final TextEditingController controller;
  final FocusNode focusNode;
  final bool focused;
  final VoidCallback onChanged;

  @override
  Widget build(BuildContext context) {
    final isActive = focused || controller.text.isNotEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'E-POSTA ADRESI',
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.08,
            color: LoginColors.textLightGray,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          decoration: BoxDecoration(
            color: LoginColors.surface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: isActive ? LoginColors.primaryStart : LoginColors.border,
              width: isActive ? 1.5 : 1,
            ),
          ),
          child: TextField(
            controller: controller,
            focusNode: focusNode,
            keyboardType: TextInputType.emailAddress,
            autocorrect: false,
            onChanged: (_) => onChanged(),
            style: const TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
              color: LoginColors.textWhite,
            ),
            decoration: InputDecoration(
              hintText: 'ornek@email.com',
              hintStyle: TextStyle(color: LoginColors.textMuted, fontSize: 16),
              prefixIcon: Padding(
                padding: const EdgeInsets.only(left: 14, right: 10),
                child: Icon(
                  Icons.email_outlined,
                  size: 20,
                  color: isActive ? LoginColors.primaryEnd : LoginColors.textLightGray,
                ),
              ),
              border: InputBorder.none,
              contentPadding: const EdgeInsets.symmetric(horizontal: 4, vertical: 16),
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
