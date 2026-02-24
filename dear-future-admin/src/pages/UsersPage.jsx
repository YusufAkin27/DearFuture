import { useState, useEffect } from 'react';
import { getAllUsers } from '../api/admin';
import './DashboardPage.css';

function formatDate(val) {
  if (!val) return '—';
  const d = new Date(val);
  return d.toLocaleString('tr-TR');
}

export default function UsersPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAllUsers()
      .then((res) => setList(res.data || []))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  return (
    <>
      <h1 className="admin-page-title">Tüm Kullanıcılar</h1>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>E-posta</th>
              <th>Ad Soyad</th>
              <th>Plan</th>
              <th>Roller</th>
              <th>Aktif</th>
              <th>Kayıt</th>
            </tr>
          </thead>
          <tbody>
            {list.length === 0 ? (
              <tr>
                <td colSpan={7} className="muted">Kayıt yok</td>
              </tr>
            ) : (
              list.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.email}</td>
                  <td>{(u.firstName || '') + (u.lastName ? ' ' + u.lastName : '') || '—'}</td>
                  <td>{u.subscriptionPlanCode || '—'}</td>
                  <td>{u.roles && u.roles.length ? u.roles.join(', ') : '—'}</td>
                  <td>{u.enabled ? 'Evet' : 'Hayır'}</td>
                  <td className="muted">{formatDate(u.createdAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
