import { useState, useEffect } from 'react';
import { getAllMessages } from '../api/admin';
import './DashboardPage.css';

function formatDate(val) {
  if (!val) return '—';
  return new Date(val).toLocaleString('tr-TR');
}

export default function MessagesPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAllMessages()
      .then((res) => setList(res.data || []))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  return (
    <>
      <h1 className="admin-page-title">Tüm Mesajlar</h1>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Kullanıcı</th>
              <th>Durum</th>
              <th>Planlanan</th>
              <th>Gönderilen</th>
              <th>Alıcılar</th>
              <th>İçerik önizleme</th>
            </tr>
          </thead>
          <tbody>
            {list.length === 0 ? (
              <tr>
                <td colSpan={7} className="muted">Kayıt yok</td>
              </tr>
            ) : (
              list.map((m) => (
                <tr key={m.id}>
                  <td>{m.id}</td>
                  <td>
                    <span>{m.userEmail}</span>
                    {m.userName && <span className="muted"> ({m.userName})</span>}
                  </td>
                  <td>{m.status || '—'}</td>
                  <td className="muted">{formatDate(m.scheduledAt)}</td>
                  <td className="muted">{formatDate(m.sentAt)}</td>
                  <td>{m.recipientEmails?.length ? m.recipientEmails.join(', ') : '—'}</td>
                  <td className="muted">
                    {m.contents?.length
                      ? m.contents.map((c) => c.textPreview || c.fileName || c.type).filter(Boolean).slice(0, 2).join(' | ')
                      : '—'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
