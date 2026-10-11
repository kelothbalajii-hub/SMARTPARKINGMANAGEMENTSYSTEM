import React, { useState, useEffect } from 'react';
import { getAllReservations, cancelReservation } from '../../api/api';
import LoadingSpinner from '../../components/LoadingSpinner';
import StatusBadge from '../../components/StatusBadge';
import ConfirmModal from '../../components/ConfirmModal';
import '../../styles/main.css';

function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [toCancel, setToCancel] = useState(null);

  const fetchReservations = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getAllReservations();
      const data = Array.isArray(res.data) ? res.data : [];
      // Newest first
      data.sort((a, b) => new Date(b.reservationTime) - new Date(a.reservationTime));
      setReservations(data);
    } catch (err) {
      setError('Failed to load reservations. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchReservations(); }, []);

  const handleCancel = async () => {
    try {
      await cancelReservation(toCancel.id);
      setSuccess(`Reservation #${toCancel.id} cancelled. Slot is available again.`);
      setToCancel(null);
      fetchReservations();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel reservation.');
      setToCancel(null);
    }
  };

  const q = search.toLowerCase();
  const filtered = reservations.filter((r) => {
    const matchesSearch =
      (r.user?.name || '').toLowerCase().includes(q) ||
      (r.vehicle?.vehicleNumber || '').toLowerCase().includes(q) ||
      (r.parkingSlot?.slotNumber || '').toLowerCase().includes(q) ||
      (r.parkingSlot?.parkingArea?.name || '').toLowerCase().includes(q);
    const matchesStatus = statusFilter === 'ALL' || r.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div>
      <div className="page-header">
        <h1>Reservation Management</h1>
        <p>Monitor all slot reservations and cancel active ones if needed</p>
      </div>

      {error && <div className="alert alert-error">⚠️ {error}</div>}
      {success && <div className="alert alert-success">✅ {success}</div>}

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', gap: '12px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Showing <strong>{filtered.length}</strong> of <strong>{reservations.length}</strong> reservations
          </span>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <select className="form-select" style={{ width: 'auto' }} value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="ALL">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <input
              className="form-input"
              style={{ maxWidth: '260px' }}
              placeholder="🔍 Search user, vehicle, slot, area…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {loading ? (
          <LoadingSpinner />
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📋</div>
            <h3>No reservations found</h3>
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
                  <th>Reserved At</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((r) => (
                  <tr key={r.id}>
                    <td style={{ color: 'var(--text-secondary)', fontFamily: 'monospace' }}>#{r.id}</td>
                    <td>
                      <div style={{ fontWeight: '500' }}>{r.user?.name || '—'}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>{r.user?.email}</div>
                    </td>
                    <td style={{ fontFamily: 'monospace', fontWeight: '700' }}>{r.vehicle?.vehicleNumber || '—'}</td>
                    <td><strong>{r.parkingSlot?.slotNumber || '—'}</strong></td>
                    <td style={{ fontSize: '0.85rem' }}>{r.parkingSlot?.parkingArea?.name || '—'}</td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {r.reservationTime ? new Date(r.reservationTime).toLocaleString('en-IN') : '—'}
                    </td>
                    <td><StatusBadge status={r.status} /></td>
                    <td>
                      {r.status === 'ACTIVE' ? (
                        <button className="btn btn-danger btn-sm" onClick={() => setToCancel(r)}>Cancel</button>
                      ) : (
                        <span style={{ color: 'var(--text-secondary)' }}>—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={!!toCancel}
        title="Cancel Reservation"
        message={toCancel ? `Cancel reservation #${toCancel.id} for slot ${toCancel.parkingSlot?.slotNumber}? The slot will become available again.` : ''}
        confirmText="Yes, Cancel"
        onConfirm={handleCancel}
        onCancel={() => setToCancel(null)}
      />
    </div>
  );
}

export default AdminReservations;
