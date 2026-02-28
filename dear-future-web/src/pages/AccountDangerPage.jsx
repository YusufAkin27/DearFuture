import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { FaArrowLeft } from 'react-icons/fa';
import { deleteAccount, deactivateAccount } from '../api/profile';
import { useDebouncedCallback } from '../hooks/useDebouncedCallback';
import './SettingsPage.css';

const AccountDangerPage = () => {
    const navigate = useNavigate();
    const [confirmDelete, setConfirmDelete] = useState('');
    const [confirmDeactivate, setConfirmDeactivate] = useState('');
    const [deactivating, setDeactivating] = useState(false);

    const handleDeactivateAccount = async () => {
        if (confirmDeactivate !== 'DONDUR') {
            toast.error('Onaylamak için "DONDUR" yazın.');
            return;
        }
        setDeactivating(true);
        try {
            await deactivateAccount();
            toast.success('Hesabınız donduruldu. Giriş yapamazsınız.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.message || 'Hesap dondurulamadı.');
        } finally {
            setDeactivating(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (confirmDelete !== 'SİL') {
            toast.error('Onaylamak için "SİL" yazın.');
            return;
        }
        try {
            await deleteAccount();
            toast.info('Hesabınız silindi.');
            localStorage.removeItem('token');
            navigate('/login', { replace: true });
        } catch (err) {
            toast.error(err.response?.data?.message || 'Hesap silinemedi.');
        }
    };

    const debouncedDeactivate = useDebouncedCallback(handleDeactivateAccount, 500);
    const debouncedDeleteAccount = useDebouncedCallback(handleDeleteAccount, 500);

    return (
        <div className="settings-container">
            <div className="settings-header" style={{ textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <Link to="/settings" className="settings-back-link">
                    <FaArrowLeft /> Ayarlara dön
                </Link>
                <h1>Hesap dondurma ve silme</h1>
                <p>Hesabınızı dondurma veya kalıcı olarak silme işlemleri.</p>
            </div>

            <div className="settings-grid" style={{ maxWidth: '640px' }}>
                {/* Hesap dondurma */}
                <div className="settings-card danger-zone danger-zone-warn">
                    <div className="danger-zone-header">
                        <h2>Hesabı dondur</h2>
                        <p className="danger-zone-desc">
                            Hesabınız devre dışı bırakılır, giriş yapamazsınız. Verileriniz silinmez.
                            Yeniden açmak için destek ile iletişime geçin.
                        </p>
                    </div>
                    <div className="danger-actions">
                        <div className="danger-block">
                            <label id="deactivate-hint" className="confirm-delete-label">
                                Onaylamak için <strong>DONDUR</strong> yazın
                            </label>
                            <input
                                type="text"
                                className="confirm-delete-input"
                                placeholder="DONDUR"
                                value={confirmDeactivate}
                                onChange={(e) => setConfirmDeactivate(e.target.value.toUpperCase())}
                                aria-describedby="deactivate-hint"
                            />
                            <button
                                type="button"
                                className="deactivate-btn"
                                onClick={debouncedDeactivate}
                                disabled={confirmDeactivate !== 'DONDUR' || deactivating}
                            >
                                {deactivating ? 'İşleniyor...' : 'Hesabı dondur'}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Hesap silme */}
                <div className="settings-card danger-zone">
                    <div className="danger-zone-header">
                        <h2>Hesap silme</h2>
                        <p className="danger-zone-desc">
                            Tüm verileriniz kalıcı olarak silinir. Bu işlem geri alınamaz.
                        </p>
                    </div>
                    <div className="danger-actions">
                        <div className="danger-block danger-block-delete">
                            <h3 className="danger-block-title">Hesabı kalıcı sil</h3>
                            <label id="delete-hint" className="confirm-delete-label">
                                Kalıcı silmek için <strong>SİL</strong> yazın
                            </label>
                            <input
                                type="text"
                                className="confirm-delete-input"
                                placeholder="SİL"
                                value={confirmDelete}
                                onChange={(e) => setConfirmDelete(e.target.value.toUpperCase())}
                                aria-describedby="delete-hint"
                            />
                            <button
                                type="button"
                                className="delete-btn"
                                onClick={debouncedDeleteAccount}
                                disabled={confirmDelete !== 'SİL'}
                            >
                                Hesabı kalıcı sil
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AccountDangerPage;
