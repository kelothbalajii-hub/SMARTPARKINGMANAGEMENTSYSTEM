import React, { useState, useEffect } from 'react';
import { getAllActiveSessions, recordExit } from '../../api/api';
import LoadingSpinner from '../../components/LoadingSpinner';
import StatusBadge from '../../components/StatusBadge';
import ConfirmModal from '../../components/ConfirmModal';
import '../../styles/main.css';

const formatElapsed = (entryTime, now) => {
  const mins = Math.max(0, Math.floor((now - new Date(entryTime)) / 60000));
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
};

function AdminSessions() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [search, setSearch] = useState('');
  const [toExit, setToExit] = useState(null);
  const [now, setNow] = useState(new Date());

  const fetchSessions = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getAllActiveSessions();
      setSessions(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError('Failed to load active sessions. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSessions(); }, []);

  // Tick every 30s so "time parked" stays fresh
  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 30000);
    return () => clearInterval(t);
  }, []);

  const handleForceExit = async () => {
    try {
      const res = await recordExit(toExit.id);
      const fee = res.data?.totalAmount;
      setSuccess(`Session #${toExit.id} ended.${fee != null ? ` Fee charged: ₹${fee.toFixed(2)}` : ''}`);
      setToExit(null);
      fetchSessions();
      setTimeout(() => setSuccess(''), 5000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to end session.');
      setToExit(null);
    }
  };

  const q = search.toLowerCase();
  const filtered = sessions.filter((s) =>
    (s.user?.name || '').toLowerCase().includes(q) ||
    (s.vehicle?.vehicleNumber || '').toLowerCase().includes(q) ||
    (s.parkingSlot?.slotNumber || '').toLowerCase().includes(q) ||
    (s.parkingSlot?.parkingArea?.name || '').toLowerCase().includes(q)
  );

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1>Active Sessions</h1>
          <p>Vehicles currently parked in the system</p>
        </div>
        <button className="btn btn-primary" onClick={fetchSessions} disabled={loading}>
          🔄 {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </div>

      {error && <div className="alert alert-error">⚠️ {error}</div>}
      {success && <div className="alert alert-success">✅ {success}</div>}

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', gap: '12px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            <strong>{filtered.length}</strong> vehicle(s) currently parked
          </span>
          <input
            className="form-input"
            style={{ maxWidth: '260px' }}
            placeholder="🔍 Search user, vehicle, slot, area…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        {loading ? (
          <LoadingSpinner />
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🔴</div>
            <h3>No active sessions</h3>
            <p>Sessions appear here when a vehicle enters a parking area</p>
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
                  <th>Entry Time</th>
                  <th>Time Parked</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((s) => (
                  <tr key={s.id}>
                    <td style={{ color: 'var(--text-secondary)', fontFamily: 'monospace' }}>#{s.id}</td>
                    <td>
                      <div style={{ fontWeight: '500' }}>{s.user?.name || '—'}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>{s.user?.email}</div>
                    </td>
                    <td style={{ fontFamily: 'monospace', fontWeight: '700' }}>{s.vehicle?.vehicleNumber || '—'}</td>
                    <td><strong>{s.parkingSlot?.slotNumber || '—'}</strong></td>
                    <td style={{ fontSize: '0.85rem' }}>{s.parkingSlot?.parkingArea?.name || '—'}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {new Date(s.entryTime).toLocaleString('en-IN')}
                    </td>
                    <td className="history-duration">{formatElapsed(s.entryTime, now)}</td>
                    <td><StatusBadge status={s.status} /></td>
                    <td>
                      <button className="btn btn-warning btn-sm" onClick={() => setToExit(s)}>End Session</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={!!toExit}
        title="End Parking Session"
        message={toExit ? `Record exit for ${toExit.vehicle?.vehicleNumber} from slot ${toExit.parkingSlot?.slotNumber}? The fee will be calculated now and the slot will be freed.` : ''}
        confirmText="End Session"
        confirmClass="btn btn-warning"
        onConfirm={handleForceExit}
        onCancel={() => setToExit(null)}
      />
    </div>
  );
}

export default AdminSessions;
