import { useState, useEffect } from 'react';
import { getAllContracts } from '../api/admin';
import './DashboardPage.css';

function formatDate(val) {
  if (!val) return '—';
  return new Date(val).toLocaleString('tr-TR');
}

export default function ContractsPage() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getAllContracts()
      .then((res) => setList(res.data || []))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  return (
    <>
      <h1 className="admin-page-title">Sözleşmeler</h1>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tür</th>
              <th>Başlık</th>
              <th>Versiyon</th>
              <th>Aktif</th>
              <th>Zorunlu onay</th>
              <th>Güncellenme</th>
            </tr>
          </thead>
          <tbody>
            {list.length === 0 ? (
              <tr>
                <td colSpan={7} className="muted">Kayıt yok</td>
              </tr>
            ) : (
              list.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td>{c.type}</td>
                  <td>{c.title}</td>
                  <td>{c.version ?? 1}</td>
                  <td>{c.active ? 'Evet' : 'Hayır'}</td>
                  <td>{c.requiredApproval ? 'Evet' : 'Hayır'}</td>
                  <td className="muted">{formatDate(c.updatedAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
