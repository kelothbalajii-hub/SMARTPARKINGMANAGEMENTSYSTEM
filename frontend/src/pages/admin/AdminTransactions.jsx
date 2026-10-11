import React, { useState, useEffect } from 'react';
import { getAllCompletedSessions } from '../../api/api';
import LoadingSpinner from '../../components/LoadingSpinner';
import StatusBadge from '../../components/StatusBadge';
import '../../styles/main.css';

const formatDuration = (minutes) => {
  if (!minutes) return '—';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
};

const formatRupees = (n) =>
  '₹' + Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

function AdminTransactions() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const res = await getAllCompletedSessions();
        const data = Array.isArray(res.data) ? res.data : [];
        data.sort((a, b) => new Date(b.exitTime) - new Date(a.exitTime));
        setSessions(data);
      } catch (err) {
        setError('Failed to load transactions. Please try again.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const q = search.toLowerCase();
  const filtered = sessions.filter((s) =>
    (s.user?.name || '').toLowerCase().includes(q) ||
    (s.vehicle?.vehicleNumber || '').toLowerCase().includes(q) ||
    (s.parkingSlot?.slotNumber || '').toLowerCase().includes(q) ||
    (s.parkingSlot?.parkingArea?.name || '').toLowerCase().includes(q)
  );

  const totalRevenue = sessions.reduce((sum, s) => sum + (s.totalAmount || 0), 0);
  const totalMinutes = sessions.reduce((sum, s) => sum + (s.durationMinutes || 0), 0);
  const avgFee = sessions.length ? totalRevenue / sessions.length : 0;

  return (
    <div>
      <div className="page-header">
        <h1>Transaction History</h1>
        <p>All completed parking sessions and revenue collected</p>
      </div>

      {error && <div className="alert alert-error">⚠️ {error}</div>}

      {loading ? (
        <LoadingSpinner />
      ) : (
        <>
          <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', marginBottom: '24px' }}>
            <div className="stat-card">
              <div className="stat-icon green">💰</div>
              <div className="stat-info">
                <div className="stat-value">{formatRupees(totalRevenue)}</div>
                <div className="stat-label">Total Revenue</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon blue">🧾</div>
              <div className="stat-info">
                <div className="stat-value">{sessions.length}</div>
                <div className="stat-label">Transactions</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon purple">📈</div>
              <div className="stat-info">
                <div className="stat-value">{formatRupees(avgFee)}</div>
                <div className="stat-label">Average Fee</div>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon teal">⏱️</div>
              <div className="stat-info">
                <div className="stat-value">{formatDuration(totalMinutes)}</div>
                <div className="stat-label">Total Time Parked</div>
              </div>
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', gap: '12px', flexWrap: 'wrap' }}>
              <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                Showing <strong>{filtered.length}</strong> of <strong>{sessions.length}</strong> transactions
              </span>
              <input
                className="form-input"
                style={{ maxWidth: '260px' }}
                placeholder="🔍 Search user, vehicle, slot, area…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            {filtered.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">💰</div>
                <h3>No transactions found</h3>
                <p>Completed parking sessions will appear here</p>
              </div>
            ) : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>User</th>
                      <th>Vehicle</th>
                      <th>Slot</th>
                      <th>Area</th>
                      <th>Entry</th>
                      <th>Exit</th>
                      <th>Duration</th>
                      <th>Amount</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((s) => (
                      <tr key={s.id}>
                        <td style={{ color: 'var(--text-secondary)', fontFamily: 'monospace' }}>#{s.id}</td>
                        <td>{s.user?.name || '—'}</td>
                        <td style={{ fontFamily: 'monospace', fontWeight: '700' }}>{s.vehicle?.vehicleNumber || '—'}</td>
                        <td><strong>{s.parkingSlot?.slotNumber || '—'}</strong></td>
                        <td style={{ fontSize: '0.8rem' }}>{s.parkingSlot?.parkingArea?.name || '—'}</td>
                        <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                          {new Date(s.entryTime).toLocaleString('en-IN')}
                        </td>
                        <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                          {s.exitTime ? new Date(s.exitTime).toLocaleString('en-IN') : '—'}
                        </td>
                        <td className="history-duration">{formatDuration(s.durationMinutes)}</td>
                        <td className="history-amount">{formatRupees(s.totalAmount)}</td>
                        <td><StatusBadge status={s.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default AdminTransactions;
