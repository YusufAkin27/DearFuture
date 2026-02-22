import 'package:flutter/material.dart';

import 'screens/login_screen.dart';
import 'services/auth_service.dart';

void main() {
  runApp(const DearFutureApp());
}

class DearFutureApp extends StatelessWidget {
  const DearFutureApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dear Future',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF00A8CC), brightness: Brightness.light),
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF00A8CC), brightness: Brightness.dark),
        useMaterial3: true,
      ),
      home: const SplashWrapper(),
    );
  }
}

class SplashWrapper extends StatefulWidget {
  const SplashWrapper({super.key});

  @override
  State<SplashWrapper> createState() => _SplashWrapperState();
}

class _SplashWrapperState extends State<SplashWrapper> {
  final _authService = AuthService();
  bool _checked = false;
  bool _loggedIn = false;

  @override
  void initState() {
    super.initState();
    _checkAuth();
  }

  Future<void> _checkAuth() async {
    final loggedIn = await _authService.isLoggedIn;
    if (mounted) {
      setState(() {
        _checked = true;
        _loggedIn = loggedIn;
      });
    }
  }

  void _onLogout() async {
    await _authService.logout();
    if (mounted) setState(() => _loggedIn = false);
  }

  void _onLoginSuccess() async {
    await _checkAuth();
  }

  @override
  Widget build(BuildContext context) {
    if (!_checked) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }
    if (_loggedIn) {
      return _HomePlaceholder(onLogout: _onLogout);
    }
    return LoginScreen(onLoginSuccess: _onLoginSuccess);
  }
}

class _HomePlaceholder extends StatelessWidget {
  const _HomePlaceholder({required this.onLogout});
  final VoidCallback onLogout;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Dear Future')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.mail_outline, size: 64),
            const SizedBox(height: 16),
            Text(
              'Giriş yaptınız',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: onLogout,
              icon: const Icon(Icons.logout),
              label: const Text('Çıkış yap'),
            ),
          ],
        ),
      ),
    );
  }
}
