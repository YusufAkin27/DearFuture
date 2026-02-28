#!/usr/bin/env node
/**
 * Build öncesi SEO dosyalarını üretir.
 * VITE_APP_URL veya SITE_URL ile site adresi verilebilir (örn: https://dearfuture.com.tr)
 */
import { writeFileSync, mkdirSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = join(__dirname, '..');
const publicDir = join(root, 'public');

const baseUrl = (process.env.VITE_APP_URL || process.env.SITE_URL || 'https://dearfuture.com.tr').replace(/\/$/, '');

const paths = [
  { path: '/', changefreq: 'weekly', priority: '1.0' },
  { path: '/welcome', changefreq: 'weekly', priority: '0.9' },
  { path: '/features', changefreq: 'monthly', priority: '0.8' },
  { path: '/pricing', changefreq: 'monthly', priority: '0.8' },
  { path: '/blog', changefreq: 'weekly', priority: '0.8' },
  { path: '/about', changefreq: 'monthly', priority: '0.7' },
  { path: '/sss', changefreq: 'monthly', priority: '0.7' },
  { path: '/contact', changefreq: 'monthly', priority: '0.7' },
  { path: '/download', changefreq: 'monthly', priority: '0.7' },
  { path: '/public-messages', changefreq: 'daily', priority: '0.8' },
  { path: '/privacy', changefreq: 'yearly', priority: '0.5' },
  { path: '/terms', changefreq: 'yearly', priority: '0.5' },
  { path: '/cookie-policy', changefreq: 'yearly', priority: '0.5' },
  { path: '/security', changefreq: 'monthly', priority: '0.6' },
  { path: '/login', changefreq: 'monthly', priority: '0.5' },
];

const sitemap = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
${paths.map(({ path, changefreq, priority }) => `  <url><loc>${baseUrl}${path}</loc><changefreq>${changefreq}</changefreq><priority>${priority}</priority></url>`).join('\n')}
</urlset>
`;

const robots = `# Dear Future - robots.txt
# Google ve diğer arama motorlarının hangi sayfaları tarayıp taramayacağını belirler.

User-agent: *
Allow: /

Disallow: /profile
Disallow: /settings
Disallow: /new
Disallow: /change-subscription
Disallow: /api/
Disallow: /oauth2/
Disallow: /auth/callback

Sitemap: ${baseUrl}/sitemap.xml
`;

mkdirSync(publicDir, { recursive: true });
writeFileSync(join(publicDir, 'sitemap.xml'), sitemap, 'utf8');
writeFileSync(join(publicDir, 'robots.txt'), robots, 'utf8');
console.log('SEO: sitemap.xml ve robots.txt üretildi (baseUrl: %s)', baseUrl);
