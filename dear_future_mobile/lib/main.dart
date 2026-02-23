import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'screens/home_screen.dart';
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
      locale: const Locale('tr', 'TR'),
      supportedLocales: const [
        Locale('tr', 'TR'),
        Locale('en', 'US'),
      ],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
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
      return Scaffold(
        backgroundColor: Colors.black,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Image.asset(
                'assets/images/logo.png',
                height: 120,
                filterQuality: FilterQuality.medium,
              ),
              const SizedBox(height: 32),
              const CircularProgressIndicator(
                color: Color(0xFF00A8CC),
                strokeWidth: 2,
              ),
            ],
          ),
        ),
      );
    }
    if (_loggedIn) {
      return HomeScreen(onLogout: _onLogout);
    }
    return LoginScreen(onLoginSuccess: _onLoginSuccess);
  }
}

