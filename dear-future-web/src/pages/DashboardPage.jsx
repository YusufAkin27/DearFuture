import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaTrash, FaClock, FaPlus, FaPen, FaPaperPlane, FaHourglassStart } from 'react-icons/fa';
import { getProfile } from '../api/profile';
import { getPendingMessages, getDeliveredMessages, deleteMessage } from '../api/message';
import EditMessageModal from '../components/EditMessageModal';
import './DashboardPage.css';

const DashboardPage = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('pending');
    const [messages, setMessages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingMessage, setEditingMessage] = useState(null);
    const [effectivePlan, setEffectivePlan] = useState('FREE');

    useEffect(() => {
        loadProfileAndMessages();
    }, [activeTab]);

    const loadProfileAndMessages = async () => {
        setLoading(true);
        try {
            const [profileRes, messagesRes] = await Promise.all([
                getProfile().catch(() => ({ data: null })),
                activeTab === 'pending' ? getPendingMessages() : getDeliveredMessages(),
            ]);
            if (profileRes?.data) {
                const user = profileRes.data;
                const isExpired = user.subscriptionEndsAt && new Date(user.subscriptionEndsAt) < new Date();
                setEffectivePlan(isExpired ? 'FREE' : (user.subscriptionPlan || 'FREE'));
            }
            setMessages(Array.isArray(messagesRes.data) ? messagesRes.data : []);
        } catch (error) {
            console.error(error);
            toast.error('Mesajlar yüklenemedi. Lütfen tekrar giriş yapın.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Bu mesajı silmek istediğinize emin misiniz?")) return;

        try {
            await deleteMessage(id);
            toast.success("Mesaj silindi");
            loadProfileAndMessages();
        } catch (error) {
            const msg = error.response?.data?.message || "Mesaj silinemedi";
            toast.error(msg);
        }
    }

    return (
        <div className="dashboard-container">
            {/* Background elements */}
            <div className="dashboard-bg-glow"></div>

            <header className="dashboard-header">
                <div className="header-text">
                    <h2>Zaman Kapsülüm</h2>
                    <p>Geleceğe bıraktığın izleri buradan yönet.</p>
                </div>
                <Link to="/new" className="create-msg-btn">
                    <FaPlus /> <span>Yeni Mesaj Yaz</span>
                </Link>
            </header>

            <div className="dashboard-tabs">
                <button
                    className={`tab-btn ${activeTab === 'pending' ? 'active' : ''}`}
                    onClick={() => setActiveTab('pending')}
                >
                    <FaHourglassStart /> Bekleyenler
                </button>
                <button
                    className={`tab-btn ${activeTab === 'delivered' ? 'active' : ''}`}
                    onClick={() => setActiveTab('delivered')}
                >
                    <FaPaperPlane /> İletilenler
                </button>
            </div>

            {loading ? (
                <div className="loading-state">
                    <div className="spinner"></div>
                    <p>Mesajlar yükleniyor...</p>
                </div>
            ) : (
                <div className="message-grid">
                    {messages.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">
                                {activeTab === 'pending' ? <FaHourglassStart /> : <FaPaperPlane />}
                            </div>
                            <h3>Burada Henüz Bir Şey Yok</h3>
                            <p>
                                {activeTab === 'pending'
                                    ? 'Henüz geleceğe gönderilmek üzere bekleyen bir mesajın yok.'
                                    : 'Henüz teslim edilmiş bir mesajın yok.'}
                            </p>
                            {activeTab === 'pending' && (
                                <Link to="/new" className="empty-cta-btn">
                                    İlk Mesajını Yaz
                                </Link>
                            )}
                        </div>
                    ) : (
                        messages.map((msg) => (
                            <div key={msg.id} className="message-card">
                                <div className="card-top">
                                    <span className="message-date">
                                        <FaClock />
                                        {new Date(msg.scheduledAt).toLocaleDateString('tr-TR', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </span>
                                    <span className={`status-badge ${activeTab}`}>
                                        {activeTab === 'pending' ? 'Bekliyor' : 'İletildi'}
                                    </span>
                                </div>

                                <div className="message-preview">
                                    {(msg.content && msg.content.substring(0, 150)) || 'Metin içeriği yok'}
                                    {msg.content && msg.content.length > 150 && '...'}
                                </div>

                                <div className="message-actions">
                                    {activeTab === 'pending' && effectivePlan !== 'FREE' && (
                                        <>
                                            <button
                                                onClick={() => setEditingMessage(msg)}
                                                className="action-btn edit-btn"
                                                title="Düzenle"
                                            >
                                                <FaPen /> Düzenle
                                            </button>
                                            <button
                                                onClick={() => handleDelete(msg.id)}
                                                className="action-btn delete-btn"
                                                title="Sil"
                                            >
                                                <FaTrash /> Sil
                                            </button>
                                        </>
                                    )}
                                    {activeTab === 'pending' && effectivePlan === 'FREE' && (
                                        <span className="free-plan-hint">Ücretsiz hesapta bekleyen mesajlar düzenlenemez veya silinemez.</span>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}

            {editingMessage && (
                <EditMessageModal
                    message={editingMessage}
                    onClose={() => setEditingMessage(null)}
                    onUpdate={loadProfileAndMessages}
                />
            )}
        </div>
    );
};

export default DashboardPage;
