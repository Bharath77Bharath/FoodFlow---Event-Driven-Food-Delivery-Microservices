import React, { useState, useEffect } from 'react';
import { notificationApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const NotificationsPage = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchNotifications = async () => {
    if (!user?.userId) return;
    setLoading(true);
    setError('');
    try {
      const response = await notificationApi.getByUser(user.userId);
      setNotifications(response.data || []);
    } catch (err) {
      console.error('Failed to load notifications', err);
      setError(err.message || 'Failed to fetch user notifications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [user?.userId]);

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>My Notifications</h2>
        <button onClick={fetchNotifications} className="btn btn-secondary btn-sm">
          Refresh
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />

      {loading && <div className="state-box">Loading notifications...</div>}

      {!loading && notifications.length === 0 && (
        <div className="state-box">
          <p>You have no notifications at this time.</p>
        </div>
      )}

      {!loading && notifications.length > 0 && (
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>Event Type</th>
                <th>Order #</th>
                <th>Channel</th>
                <th>Message</th>
                <th>Status</th>
                <th>Date / Time</th>
              </tr>
            </thead>
            <tbody>
              {notifications.map((n) => (
                <tr key={n.id}>
                  <td><strong>{n.type}</strong></td>
                  <td>{n.orderId ? `#${n.orderId}` : '-'}</td>
                  <td>{n.channel}</td>
                  <td>{n.message}</td>
                  <td><span className="badge badge-confirmed">{n.status}</span></td>
                  <td>{n.sentAt || n.createdAt ? new Date(n.sentAt || n.createdAt).toLocaleString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
