import { useState, useEffect } from 'react';
import { getContactMessages } from '../api/admin';
import './DashboardPage.css';

function formatDate(val) {
  if (!val) return '—';
  return new Date(val).toLocaleString('tr-TR');
}

export default function ContactMessagesPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getContactMessages()
      .then((res) => setList(res.data || []))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  return (
    <>
      <h1 className="admin-page-title">İletişim Mesajları</h1>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Ad</th>
              <th>E-posta</th>
              <th>Telefon</th>
              <th>Konu</th>
              <th>Mesaj (özet)</th>
              <th>Doğrulandı</th>
              <th>Tarih</th>
            </tr>
          </thead>
          <tbody>
            {list.length === 0 ? (
              <tr>
                <td colSpan={8} className="muted">Kayıt yok</td>
              </tr>
            ) : (
              list.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.name}</td>
                  <td>{c.email}</td>
                  <td>{c.phone || '—'}</td>
                  <td>{c.subject}</td>
                  <td className="muted" style={{ maxWidth: 200 }}>
                    {c.message ? (c.message.length > 80 ? c.message.slice(0, 80) + '…' : c.message) : '—'}
                  </td>
                  <td>{c.verified ? 'Evet' : 'Hayır'}</td>
                  <td className="muted">{formatDate(c.createdAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
