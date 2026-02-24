import { useState, useEffect } from 'react';
import { getAllPlans } from '../api/admin';
import './DashboardPage.css';

function formatPrice(val) {
  if (val == null) return '—';
  return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(val);
}

export default function PlansPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAllPlans()
      .then((res) => setList(res.data || []))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  return (
    <>
      <h1 className="admin-page-title">Planlar</h1>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Kod</th>
              <th>Ad</th>
              <th>Aylık fiyat</th>
              <th>Max mesaj</th>
              <th>Fotoğraf</th>
              <th>Dosya</th>
              <th>Ses</th>
              <th>Aktif</th>
              <th>Kullanıcı sayısı</th>
            </tr>
          </thead>
          <tbody>
            {list.length === 0 ? (
              <tr>
                <td colSpan={10} className="muted">Kayıt yok</td>
              </tr>
            ) : (
              list.map((p) => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.code}</td>
                  <td>{p.name}</td>
                  <td>{formatPrice(p.monthlyPrice)} {p.priceLabel || ''}</td>
                  <td>{p.maxMessages}</td>
                  <td>{p.allowPhoto ? 'Evet' : 'Hayır'}</td>
                  <td>{p.allowFile ? 'Evet' : 'Hayır'}</td>
                  <td>{p.allowVoice ? 'Evet' : 'Hayır'}</td>
                  <td>{p.active ? 'Evet' : 'Hayır'}</td>
                  <td>{p.userCount ?? 0}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
