import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaStar, FaRegStar, FaHeart } from 'react-icons/fa';
import { getPublicMessages, getMyStarredMessages, starPublicMessage, unstarPublicMessage } from '../api/message';
import './PublicMessagesPage.css';

const PAGE_SIZE = 12;

const PublicMessagesPage = () => {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const tabParam = searchParams.get('tab');
    const [activeTab, setActiveTab] = useState(tabParam === 'starred' ? 'starred' : 'public');
    const [publicPage, setPublicPage] = useState(0);
    const [publicData, setPublicData] = useState({ content: [], totalPages: 0, totalElements: 0 });
    const [starredList, setStarredList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [starringId, setStarringId] = useState(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem('token');
        setIsLoggedIn(!!token);
    }, []);

    useEffect(() => {
        setActiveTab(tabParam === 'starred' ? 'starred' : 'public');
    }, [tabParam]);

    useEffect(() => {
        if (activeTab === 'public') {
            loadPublicPage(publicPage);
        } else {
            loadStarred();
        }
    }, [activeTab, publicPage]);

    const loadPublicPage = async (page) => {
        setLoading(true);
        try {
            const res = await getPublicMessages(page, PAGE_SIZE);
            const data = res.data;
            setPublicData({
                content: data.content || [],
                totalPages: data.totalPages ?? 0,
                totalElements: data.totalElements ?? 0,
            });
        } catch (err) {
            toast.error('Mesajlar yüklenemedi.');
            setPublicData({ content: [], totalPages: 0, totalElements: 0 });
        } finally {
            setLoading(false);
        }
    };

    const loadStarred = async () => {
        setLoading(true);
        try {
            const res = await getMyStarredMessages();
            setStarredList(Array.isArray(res.data) ? res.data : []);
        } catch (err) {
            if (err.response?.status === 401) {
                setIsLoggedIn(false);
                setStarredList([]);
            } else {
                toast.error('Yıldızlı mesajlar yüklenemedi.');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleStar = async (msg) => {
        if (!isLoggedIn) {
            toast.info('Yıldızlamak için giriş yapın.');
            return;
        }
        setStarringId(msg.id);
        try {
            const starred = msg.starredByMe === true;
            if (starred) {
                await unstarPublicMessage(msg.id);
                toast.success('Yıldız kaldırıldı.');
            } else {
                await starPublicMessage(msg.id);
                toast.success('Mesaj yıldızlandı.');
            }
            if (activeTab === 'public') {
                loadPublicPage(publicPage);
            } else {
                loadStarred();
            }
        } catch (err) {
            const msg_err = err.response?.data?.message || (err.response?.status === 401 ? 'Giriş yapın.' : 'İşlem başarısız.');
            toast.error(msg_err);
        } finally {
            setStarringId(null);
        }
    };

    const formatDate = (instant) => {
        if (!instant) return '—';
        return new Date(instant).toLocaleDateString('tr-TR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const renderCard = (msg) => (
        <div key={msg.id} className="public-msg-card">
            <div className="public-msg-card-header">
                <span className="public-msg-sender">{msg.senderName || 'Anonim'}</span>
                <span className="public-msg-date">{formatDate(msg.sentAt)}</span>
            </div>
            <div className="public-msg-preview">
                {msg.textPreview || '—'}
            </div>
            <div className="public-msg-actions">
                <button
                    type="button"
                            className={`star-btn ${msg.starredByMe ? 'starred' : ''}`}
                    onClick={() => handleStar(msg)}
                    disabled={starringId === msg.id}
                    title={msg.starredByMe ? 'Yıldızı kaldır' : 'Yıldızla'}
                >
                    {msg.starredByMe ? <FaStar /> : <FaRegStar />}
                    <span>{msg.starredByMe ? 'Yıldızlı' : 'Yıldızla'}</span>
                </button>
                {msg.viewToken && (
                    <a
                        href={`/api/messages/view/${msg.viewToken}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="view-link"
                    >
                        Mesajı görüntüle
                    </a>
                )}
            </div>
        </div>
    );

    return (
        <div className="public-messages-container">
            <header className="public-messages-header">
                <h1>Herkese Açık Mesajlar</h1>
                <p className="public-messages-subtitle">
                    İletildikten sonra herkese açık bırakılan mesajlar. Beğendiklerinizi yıldızlayabilirsiniz.
                </p>
            </header>

            <div className="public-messages-tabs">
                <button
                    type="button"
                    className={`tab-btn ${activeTab === 'public' ? 'active' : ''}`}
                    onClick={() => {
                        setActiveTab('public');
                        setSearchParams({});
                    }}
                >
                    Herkese Açık
                </button>
                <button
                    type="button"
                    className={`tab-btn ${activeTab === 'starred' ? 'active' : ''}`}
                    onClick={() => {
                        setActiveTab('starred');
                        setSearchParams({ tab: 'starred' });
                    }}
                >
                    <FaHeart /> Yıldızlılarım
                </button>
            </div>

            {activeTab === 'starred' && !isLoggedIn && (
                <div className="public-messages-login-prompt">
                    <p>Yıldızlı mesajlarınızı görmek için giriş yapın.</p>
                    <Link to="/login" className="login-link-btn">Giriş Yap</Link>
                </div>
            )}

            {loading ? (
                <div className="public-messages-loading">
                    <div className="public-messages-spinner" />
                    <p>Yükleniyor...</p>
                </div>
            ) : activeTab === 'public' ? (
                <>
                    <div className="public-messages-grid">
                        {publicData.content.length === 0 ? (
                            <div className="public-messages-empty">
                                <p>Henüz herkese açık mesaj yok.</p>
                            </div>
                        ) : (
                            publicData.content.map(renderCard)
                        )}
                    </div>
                    {publicData.totalPages > 1 && (
                        <div className="public-messages-pagination">
                            <button
                                type="button"
                                className="pagination-btn"
                                disabled={publicPage === 0}
                                onClick={() => setPublicPage((p) => Math.max(0, p - 1))}
                            >
                                Önceki
                            </button>
                            <span className="pagination-info">
                                Sayfa {publicPage + 1} / {publicData.totalPages} (toplam {publicData.totalElements} mesaj)
                            </span>
                            <button
                                type="button"
                                className="pagination-btn"
                                disabled={publicPage >= publicData.totalPages - 1}
                                onClick={() => setPublicPage((p) => p + 1)}
                            >
                                Sonraki
                            </button>
                        </div>
                    )}
                </>
            ) : (
                <div className="public-messages-grid">
                    {starredList.length === 0 ? (
                        <div className="public-messages-empty">
                            <p>Henüz yıldızlı mesajınız yok. Herkese açık mesajlardan yıldız ekleyebilirsiniz.</p>
                        </div>
                    ) : (
                        starredList.map(renderCard)
                    )}
                </div>
            )}
        </div>
    );
};

export default PublicMessagesPage;
