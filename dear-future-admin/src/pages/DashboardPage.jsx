import { useState, useEffect } from 'react';
import { getDashboard } from '../api/admin';
import './DashboardPage.css';

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getDashboard()
      .then((res) => setStats(res.data))
      .catch((err) => setError(err.response?.status === 403 ? 'Yetkiniz yok.' : err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="admin-loading">Yükleniyor…</p>;
  if (error) return <div className="admin-error">{error}</div>;

  const usersPerPlan = stats?.usersPerPlan || {};
  const messagesPerStatus = stats?.messagesPerStatus || {};

  return (
    <>
      <h1 className="admin-page-title">Dashboard</h1>
      <div className="dashboard-cards">
        <div className="stat-card">
          <span className="stat-value">{stats?.totalUsers ?? 0}</span>
          <span className="stat-label">Toplam kullanıcı</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{stats?.totalMessages ?? 0}</span>
          <span className="stat-label">Toplam mesaj</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{stats?.totalPayments ?? 0}</span>
          <span className="stat-label">Toplam ödeme</span>
        </div>
      </div>
      <div className="dashboard-grid">
        <section className="dashboard-section">
          <h2>Kullanıcılar (plana göre)</h2>
          <ul className="stat-list">
            {Object.entries(usersPerPlan).map(([plan, count]) => (
              <li key={plan}>
                <span className="stat-list-label">{plan}</span>
                <span className="stat-list-value">{count}</span>
              </li>
            ))}
            {Object.keys(usersPerPlan).length === 0 && <li className="stat-list-empty">Veri yok</li>}
          </ul>
        </section>
        <section className="dashboard-section">
          <h2>Mesajlar (duruma göre)</h2>
          <ul className="stat-list">
            {Object.entries(messagesPerStatus).map(([status, count]) => (
              <li key={status}>
                <span className="stat-list-label">{status}</span>
                <span className="stat-list-value">{count}</span>
              </li>
            ))}
            {Object.keys(messagesPerStatus).length === 0 && <li className="stat-list-empty">Veri yok</li>}
          </ul>
        </section>
      </div>
    </>
  );
}
