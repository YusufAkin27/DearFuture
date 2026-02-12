import './BlogPage.css';

const BlogPage = () => {
    const posts = [
        {
            id: 1,
            title: 'Dijital Miras Neden Önemli?',
            excerpt: 'Geleceğe bırakacağınız dijital ayak izinizi hazırlamanın 21. yüzyılda neden her zamankinden daha anlamlı olduğunu keşfedin.',
            date: '15 Eki 2025',
            author: 'Ayşe Yılmaz',
            category: 'Miras',
            image: 'linear-gradient(135deg, #00a8cc, #00d2fc)',
        },
        {
            id: 2,
            title: 'Zaman Kapsüllerini Kullanmanın 5 Yolu',
            excerpt: 'Sevdiklerinizi şaşırtacak ve mutlu edecek zamanlanmış mesajlar için yaratıcı fikirler.',
            date: '2 Kas 2025',
            author: 'Mehmet Kaya',
            category: 'İpuçları',
            image: 'linear-gradient(135deg, #0c7b93, #00a8cc)',
        },
        {
            id: 3,
            title: 'Dear Future\'da Güvenlik',
            excerpt: 'Sıfır-bilgi şifreleme mimarimiz ve verilerinizi nasıl koruduğumuz hakkında teknik bir bakış.',
            date: '20 Kas 2025',
            author: 'Zeynep Demir',
            category: 'Teknoloji',
            image: 'linear-gradient(135deg, #142850, #00a8cc)',
        },
    ];

    return (
        <section className="blog-container">
            <div className="blog-inner">
                <header className="blog-hero">
                    <span className="blog-pill">Blog</span>
                    <h1>Zaman, anı ve gelecek hakkında yazılar</h1>
                    <p>
                        Geleceğe mektup yazmak, dijital miras ve anıları saklamak üzerine düşünceler ve ipuçları.
                    </p>
                </header>

                <div className="blog-grid">
                    {posts.map((post) => (
                        <article key={post.id} className="blog-card">
                            <div className="blog-image" style={{ background: post.image }} />
                            <div className="blog-content">
                                <div className="blog-meta">
                                    <span className="blog-category">{post.category}</span>
                                    <span className="blog-date">{post.date}</span>
                                </div>
                                <h2>{post.title}</h2>
                                <p>{post.excerpt}</p>
                                <div className="blog-card-footer">
                                    <span className="blog-author">{post.author} tarafından</span>
                                    <button type="button" className="blog-read-more">Devamını Oku →</button>
                                </div>
                            </div>
                        </article>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default BlogPage;
