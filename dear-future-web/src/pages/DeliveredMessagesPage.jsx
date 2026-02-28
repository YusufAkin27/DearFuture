import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaPlus, FaPaperPlane, FaClock, FaExternalLinkAlt, FaArrowLeft } from 'react-icons/fa';
import { getProfile } from '../api/profile';
import { getDeliveredMessages } from '../api/message';
import './SettingsPage.css';

const DeliveredMessagesPage = () => {
    const [profile, setProfile] = useState(null);
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [messagesLoading, setMessagesLoading] = useState(false);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            try {
                const res = await getProfile();
                if (!cancelled) setProfile(res.data);
            } catch (err) {
                if (!cancelled) {
                    toast.error('Profil yüklenemedi.');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        load();
        return () => { cancelled = true; };
    }, []);

    useEffect(() => {
        if (profile) loadMessages();
    }, [profile]);

    const loadMessages = async () => {
        setMessagesLoading(true);
        try {
            const res = await getDeliveredMessages();
            setMessages(Array.isArray(res.data) ? res.data : []);
        } catch (err) {
            setMessages([]);
        } finally {
            setMessagesLoading(false);
        }
    };

    const planCode = profile?.subscriptionPlanCode ?? profile?.subscriptionPlan ?? 'FREE';
    const planName = profile?.subscriptionPlanName ?? planCode;
    const subscriptionExpired = profile?.subscriptionEndsAt && new Date(profile.subscriptionEndsAt) < new Date();

    if (loading) {
        return (
            <div className="settings-container">
                <div className="settings-loading">
                    <div className="spinner" />
                    <p>Yükleniyor...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="settings-container">
            <div className="settings-header" style={{ textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <Link to="/settings" className="settings-back-link">
                    <FaArrowLeft /> Ayarlara dön
                </Link>
                <h1>İletilen mesajlar</h1>
                <p>Teslim edilmiş mesajlarınızı görüntüleyin.</p>
            </div>

            <div className="settings-messages-wrap">
                <div className="settings-messages-card">
                    <div className="settings-messages-head">
                        <h2>Mesajlarım</h2>
                        <Link to="/new" className="settings-new-msg-btn">
                            <FaPlus /> Yeni Mesaj
                        </Link>
                    </div>
                    <p className="settings-plan-badge">
                        Planınız: <strong>{planName}</strong>
                        {subscriptionExpired && planCode !== 'FREE' && (
                            <span className="settings-plan-expired"> (süre doldu, Ücretsiz geçerli)</span>
                        )}
                    </p>
                    {messagesLoading ? (
                        <div className="settings-messages-loading">
                            <div className="spinner" />
                            <p>Mesajlar yükleniyor...</p>
                        </div>
                    ) : (
                        <div className="settings-message-grid">
                            {messages.length === 0 ? (
                                <div className="settings-messages-empty">
                                    <div className="settings-messages-empty-icon">
                                        <FaPaperPlane />
                                    </div>
                                    <h3>Burada henüz bir şey yok</h3>
                                    <p>Henüz teslim edilmiş bir mesajın yok.</p>
                                    <Link to="/new" className="settings-empty-cta">Yeni mesaj yaz</Link>
                                </div>
                            ) : (
                                messages.map((msg) => (
                                    <div key={msg.id} className="settings-message-card">
                                        <div className="settings-message-card-top">
                                            <span className="settings-message-date">
                                                <FaClock />
                                                {new Date(msg.scheduledAt).toLocaleDateString('tr-TR', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric',
                                                    hour: '2-digit',
                                                    minute: '2-digit',
                                                })}
                                            </span>
                                            <span className="settings-status-badge delivered">İletildi</span>
                                        </div>
                                        <div className="settings-message-preview">
                                            {(msg.content && msg.content.substring(0, 150)) || 'Metin içeriği yok'}
                                            {msg.content && msg.content.length > 150 && '...'}
                                        </div>
                                        <div className="settings-message-actions">
                                            {msg.viewToken && (
                                                <Link
                                                    to={`/message/view/${msg.viewToken}`}
                                                    className="settings-action-btn view-link"
                                                    title="Mesajın sayfasına git"
                                                >
                                                    <FaExternalLinkAlt /> Mesajı görüntüle
                                                </Link>
                                            )}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DeliveredMessagesPage;
