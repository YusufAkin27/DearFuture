import { useState, useEffect } from 'react';
import { getPayments } from '../api/admin';
import './DashboardPage.css';

function formatDate(val) {
  if (!val) return '—';
  return new Date(val).toLocaleString('tr-TR');
}

function formatMoney(amount) {
  if (amount == null) return '—';
  return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(amount);
}

export default function PaymentsPage() {
  const [data, setData] = useState({ content: [], totalPages: 0, number: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const size = 20;

  useEffect(() => {
    setLoading(true);
    getPayments(page, size)
      .then((res) => {
        setData({
          content: res.data?.content ?? [],
          totalPages: res.data?.totalPages ?? 0,
          number: res.data?.number ?? 0,
        });
      })
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, [page]);

  if (error) return <div className="admin-error">{error}</div>;

  const list = data.content;

  return (
    <>
      <h1 className="admin-page-title">Ödemeler</h1>
      {loading ? (
        <p className="admin-loading">Yükleniyor…</p>
      ) : (
        <>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Kullanıcı</th>
                  <th>Plan</th>
                  <th>Tutar</th>
                  <th>Durum</th>
                  <th>Yenileme</th>
                  <th>Ödeme tarihi</th>
                  <th>Oluşturulma</th>
                </tr>
              </thead>
              <tbody>
                {list.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="muted">Kayıt yok</td>
                  </tr>
                ) : (
                  list.map((p) => (
                    <tr key={p.id}>
                      <td>{p.id}</td>
                      <td>{p.userEmail || `User #${p.userId}`}</td>
                      <td>{p.planCode || '—'}</td>
                      <td>{formatMoney(p.amount)}</td>
                      <td>{p.status || '—'}</td>
                      <td>{p.renewal ? 'Evet' : 'Hayır'}</td>
                      <td className="muted">{formatDate(p.paidAt)}</td>
                      <td className="muted">{formatDate(p.createdAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          {data.totalPages > 1 && (
            <div className="admin-pagination">
              <button
                type="button"
                disabled={page <= 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Önceki
              </button>
              <span>
                Sayfa {data.number + 1} / {data.totalPages}
              </span>
              <button
                type="button"
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Sonraki
              </button>
            </div>
          )}
        </>
      )}
    </>
  );
}
