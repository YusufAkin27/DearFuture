/**
 * Kurucu ortaklar: Hakkımızda sayfası ve detay sayfaları için veri.
 * slug: URL'de kullanılır (/about/team/:slug)
 */
export const TEAM_MEMBERS = [
    {
        id: 1,
        slug: 'yusuf-akin',
        name: 'Yusuf Akin',
        role: 'Kurucu Ortağı',
        image: '/uye1.jpeg',
        bio: `Yusuf Akin, yazılım geliştirme ve ürün tasarımı alanında deneyimli bir kurucu ortaktır. 
Dear Future'un teknik altyapısı ve ürün vizyonunun şekillenmesinde öncü rol üstlenmiştir. 
Geleceğe dokunan anlamlı dijital deneyimler oluşturma tutkusuyla projeyi hayata geçirmiştir.`,
        projects: [
            { name: 'Dear Future', description: 'Geleceğe mesaj ve zaman kapsülü platformu.' },
            { name: 'Diğer projeler', description: 'Web ve mobil uygulama geliştirme projeleri.' },
        ],
        githubUsername: 'YusufAkin27',
        linkedInUrl: 'https://www.linkedin.com/in/yusufakin1',
        email: 'ysufakin23@gmail.com',
    },
    {
        id: 2,
        slug: 'melisa-cicek-soyubey',
        name: 'Melisa Çiçek Soyubey',
        role: 'Kurucu Ortağı',
        image: '/uye2.jpeg',
        bio: `Melisa Çiçek Soyubey, Dear Future'un kurucu ortağı olarak ürün ve kullanıcı deneyimi tarafında 
sorumluluk almaktadır. İnsan odaklı tasarım ve sürdürülebilir dijital hizmetler konusunda çalışmaktadır. 
Anlamlı anıları geleceğe taşıma misyonuna gönül vermiştir.`,
        projects: [
            { name: 'Dear Future', description: 'Zaman kapsülü ve geleceğe mesaj platformu.' },
            { name: 'Diğer projeler', description: 'Ürün tasarımı ve kullanıcı deneyimi projeleri.' },
        ],
        /** Backend'den GitHub projeleri çekilir (GET /api/public/team/github/melisacicek) */
        githubUsername: 'melisacicek',
        portfolioUrl: 'https://melisaciceksoyubey.com/',
        linkedInUrl: 'https://www.linkedin.com/in/melisaciceksoyubey',
        instagramUrl: 'https://www.instagram.com/dailycicek/',
    },
];

export function getTeamMemberBySlug(slug) {
    return TEAM_MEMBERS.find((m) => m.slug === slug) ?? null;
}
