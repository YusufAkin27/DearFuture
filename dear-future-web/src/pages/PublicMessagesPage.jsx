import { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaStar, FaRegStar, FaHeart } from 'react-icons/fa';
import { getPublicMessages, getMyStarredMessages, starPublicMessage, unstarPublicMessage } from '../api/message';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './PublicMessagesPage.css';

const PAGE_SIZE = 12;

const PublicMessagesPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const tabParam = searchParams.get('tab');
    const [activeTab, setActiveTab] = useState(tabParam === 'starred' ? 'starred' : 'public');
    const [publicData, setPublicData] = useState({ content: [], totalPages: 0, totalElements: 0 });
    const [nextPage, setNextPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [starredList, setStarredList] = useState([]);
    const [starringId, setStarringId] = useState(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const sentinelRef = useRef(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        setIsLoggedIn(!!token);
    }, []);

    useEffect(() => {
        setActiveTab(tabParam === 'starred' ? 'starred' : 'public');
    }, [tabParam]);

    const loadFirstPage = useCallback(async () => {
        setLoading(true);
        setNextPage(0);
        try {
            const res = await getPublicMessages(0, PAGE_SIZE);
            const data = res.data;
            setPublicData({
                content: data.content || [],
                totalPages: data.totalPages ?? 0,
                totalElements: data.totalElements ?? 0,
            });
            setNextPage(1);
        } catch (err) {
            toast.error('Mesajlar yüklenemedi.');
            setPublicData({ content: [], totalPages: 0, totalElements: 0 });
        } finally {
            setLoading(false);
        }
    }, []);

    const loadMore = useCallback(async () => {
        if (loadingMore || nextPage >= publicData.totalPages) return;
        setLoadingMore(true);
        try {
            const res = await getPublicMessages(nextPage, PAGE_SIZE);
            const data = res.data;
            const newContent = data.content || [];
            setPublicData((prev) => ({
                ...prev,
                content: [...prev.content, ...newContent],
            }));
            setNextPage((p) => p + 1);
        } catch (err) {
            toast.error('Daha fazla mesaj yüklenemedi.');
        } finally {
            setLoadingMore(false);
        }
    }, [loadingMore, nextPage, publicData.totalPages]);

    const loadStarred = useCallback(async () => {
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
    }, []);

    useEffect(() => {
        if (activeTab === 'public') {
            loadFirstPage();
        } else {
            loadStarred();
        }
    }, [activeTab, loadFirstPage, loadStarred]);

    useEffect(() => {
        if (activeTab !== 'public' || loading || loadingMore || nextPage >= publicData.totalPages || publicData.totalPages === 0) {
            return;
        }
        const el = sentinelRef.current;
        if (!el) return;
        const observer = new IntersectionObserver(
            (entries) => {
                if (entries[0]?.isIntersecting) loadMore();
            },
            { rootMargin: '200px', threshold: 0.1 }
        );
        observer.observe(el);
        return () => observer.disconnect();
    }, [activeTab, loading, loadingMore, nextPage, publicData.totalPages, loadMore]);

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
                setPublicData((prev) => ({
                    ...prev,
                    content: prev.content.map((m) =>
                        m.id === msg.id ? { ...m, starredByMe: !starred } : m
                    ),
                }));
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

    const debouncedStar = useDebouncedCallback(handleStar, 500);

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

    const previewImageUrl = (url) => {
        if (!url) return null;
        if (url.startsWith('http://') || url.startsWith('https://')) return url;
        const base = (import.meta.env.VITE_BACKEND_URL || 'https://api.dearfuture.info').replace(/\/$/, '');
        return url.startsWith('/') ? base + url : `${base}/${url}`;
    };

    const renderCard = (msg) => (
        <div key={msg.id} className="public-msg-card">
            {msg.previewImageUrl && (
                <div className="public-msg-card-media">
                    <img
                        src={previewImageUrl(msg.previewImageUrl)}
                        alt=""
                        className="public-msg-preview-img"
                        loading="lazy"
                    />
                    {msg.attachmentCount > 1 && (
                        <span className="public-msg-media-badge" title={`${msg.attachmentCount} ek`}>
                            +{msg.attachmentCount}
                        </span>
                    )}
                </div>
            )}
            <div className="public-msg-card-body">
                <div className="public-msg-card-header">
                    <span className="public-msg-sender">{msg.senderName || 'Anonim'}</span>
                    <span className="public-msg-date">{formatDate(msg.sentAt)}</span>
                </div>
                <div className="public-msg-preview">
                    {msg.textPreview || (msg.previewImageUrl ? 'Fotoğraflı mesaj' : '—')}
                </div>
            </div>
            <div className="public-msg-actions">
                <button
                    type="button"
                            className={`star-btn ${msg.starredByMe ? 'starred' : ''}`}
                    onClick={() => debouncedStar(msg)}
                    disabled={starringId === msg.id}
                    title={msg.starredByMe ? 'Yıldızı kaldır' : 'Yıldızla'}
                >
                    {msg.starredByMe ? <FaStar /> : <FaRegStar />}
                    <span>{msg.starredByMe ? 'Yıldızlı' : 'Yıldızla'}</span>
                </button>
                {msg.viewToken && (
                    <Link
                        to={`/message/view/${msg.viewToken}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="view-link"
                    >
                        Mesajı görüntüle
                    </Link>
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
                    {publicData.content.length > 0 && (
                        <div ref={sentinelRef} className="public-messages-sentinel" aria-hidden="true">
                            {loadingMore && (
                                <div className="public-messages-load-more">
                                    <div className="public-messages-spinner public-messages-spinner--small" />
                                    <span>Daha fazla yükleniyor...</span>
                                </div>
                            )}
                            {!loadingMore && publicData.content.length < publicData.totalElements && (
                                <p className="public-messages-scroll-hint">Aşağı kaydırarak daha fazla mesaj görüntüleyebilirsiniz.</p>
                            )}
                            {!loadingMore && publicData.content.length >= publicData.totalElements && publicData.totalElements > 0 && (
                                <p className="public-messages-count">Toplam {publicData.totalElements} mesaj</p>
                            )}
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
