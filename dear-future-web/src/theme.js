const THEME_KEY = 'dearfuture-theme';

export function getTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  if (saved === 'dark' || saved === 'light') return saved;
  return 'dark';
}

export function setTheme(theme) {
  if (theme !== 'light' && theme !== 'dark') return;
  localStorage.setItem(THEME_KEY, theme);
  document.body.className = theme === 'dark' ? 'theme-dark' : 'theme-light';
}

export function toggleTheme() {
  const next = getTheme() === 'dark' ? 'light' : 'dark';
  setTheme(next);
  return next;
}

export function isDark() {
  return document.body.classList.contains('theme-dark');
}
