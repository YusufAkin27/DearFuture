import { useState, useEffect } from 'react';
import { FaTimes } from 'react-icons/fa';
import './BlogPage.css';

const POSTS = [
    {
        id: 1,
        title: 'Dijital Miras Neden Önemli?',
        excerpt: 'Geleceğe bırakacağınız dijital ayak izinizi hazırlamanın 21. yüzyılda neden her zamankinden daha anlamlı olduğunu keşfedin.',
        body: 'Geleceğe bırakacağınız dijital ayak izinizi hazırlamak, 21. yüzyılda her zamankinden daha anlamlı. Sosyal medya hesapları, e-postalar ve dijital dosyalar biriktikçe, bunların nasıl korunacağı ve sevdiklerinize nasıl aktarılacağı önem kazanıyor. Dear Future gibi araçlarla zamanlanmış mesajlar bırakarak, gelecekteki nesillere kendi sesinizle ulaşabilirsiniz. Dijital miras, yalnızca vasiyet değil; anıların, düşüncelerin ve duyguların zamanda yolculuğudur.',
        date: '15 Eki 2025',
        author: 'Ayşe Yılmaz',
        category: 'Miras',
        image: 'blog1.jpg',
    },
    {
        id: 2,
        title: 'Zaman Kapsüllerini Kullanmanın 5 Yolu',
        excerpt: 'Sevdiklerinizi şaşırtacak ve mutlu edecek zamanlanmış mesajlar için yaratıcı fikirler.',
        body: 'Zaman kapsülleri yalnızca doğum günleri için değil. Yıldönümlerinde eşinize, çocuğunuzun mezuniyetinde ona, kendinize on yıl sonrasına mektup yazın. Doğum günü sabahı açılacak bir video, evlilik yıldönümünde ulaşacak bir ses kaydı veya çocuğunuzun 18 yaşında okuyacağı bir mektup… Dear Future ile bu anları planlayabilir, fotoğraf ve videolarla zenginleştirebilirsiniz. Sevdiklerinizi şaşırtmak ve mutlu etmek için zamanı kullanın.',
        date: '2 Kas 2025',
        author: 'Mehmet Kaya',
        category: 'İpuçları',
        image: 'blog2.jpg',
    },
    {
        id: 3,
        title: 'Dear Future\'da Güvenlik',
        excerpt: 'Sıfır-bilgi şifreleme mimarimiz ve verilerinizi nasıl koruduğumuz hakkında teknik bir bakış.',
        body: 'Mesajlarınız bizim için kutsal. Sıfır-bilgi (zero-knowledge) prensibiyle, içeriğinizi yalnızca siz ve alıcı görebilir; sunucularımızda bile şifrelenmiş halde saklanır. Teslim tarihi gelene kadar içerik çözülemez. HTTPS, güçlü şifreleme ve düzenli güvenlik denetimleriyle verilerinizi koruyoruz. Dear Future\'da güvenlik, sadece bir özellik değil; temel taahhüdümüzdür.',
        date: '20 Kas 2025',
        author: 'Zeynep Demir',
        category: 'Teknoloji',
        image: 'blog3.jpg',
    },
];

const BlogPage = () => {
    const [modalPost, setModalPost] = useState(null);

    const openModal = (post) => setModalPost(post);
    const closeModal = () => setModalPost(null);

    useEffect(() => {
        if (!modalPost) return;
        const handleEscape = (e) => { if (e.key === 'Escape') setModalPost(null); };
        window.addEventListener('keydown', handleEscape);
        document.body.style.overflow = 'hidden';
        return () => {
            window.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = '';
        };
    }, [modalPost]);

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
                    {POSTS.map((post) => (
                        <article key={post.id} className="blog-card">
                            <div className="blog-image-wrap">
                                <img src={post.image} alt="" className="blog-image" loading="lazy" />
                            </div>
                            <div className="blog-content">
                                <div className="blog-meta">
                                    <span className="blog-category">{post.category}</span>
                                    <span className="blog-date">{post.date}</span>
                                </div>
                                <h2>{post.title}</h2>
                                <p>{post.excerpt}</p>
                                <div className="blog-card-footer">
                                    <span className="blog-author">{post.author} tarafından</span>
                                    <button
                                        type="button"
                                        className="blog-read-more"
                                        onClick={() => openModal(post)}
                                    >
                                        Devamını Oku →
                                    </button>
                                </div>
                            </div>
                        </article>
                    ))}
                </div>
            </div>

            {modalPost && (
                <div
                    className="blog-modal-backdrop"
                    onClick={closeModal}
                    role="dialog"
                    aria-modal="true"
                    aria-labelledby="blog-modal-title"
                >
                    <div className="blog-modal" onClick={(e) => e.stopPropagation()}>
                        <button
                            type="button"
                            className="blog-modal-close"
                            onClick={closeModal}
                            aria-label="Kapat"
                        >
                            <FaTimes />
                        </button>
                        <div className="blog-modal-image-wrap">
                            <img src={modalPost.image} alt="" className="blog-modal-image" />
                        </div>
                        <div className="blog-modal-content">
                            <div className="blog-modal-meta">
                                <span className="blog-category">{modalPost.category}</span>
                                <span className="blog-date">{modalPost.date}</span>
                            </div>
                            <h2 id="blog-modal-title" className="blog-modal-title">{modalPost.title}</h2>
                            <p className="blog-modal-author">{modalPost.author} tarafından</p>
                            <p className="blog-modal-body">{modalPost.body}</p>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
};

export default BlogPage;
