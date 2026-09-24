import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const MyOrders = () => {
  const { userId } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = async () => {
    if (!userId) return;
    setLoading(true);
    setError('');
    try {
      const response = await orderApi.getByUser(userId);
      setOrders(response.data || []);
    } catch (err) {
      console.error('Failed to load orders', err);
      setError(err.message || 'Failed to load your orders.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [userId]);

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'PLACED': return 'badge-placed';
      case 'PAYMENT_PROCESSING': return 'badge-payment-processing';
      case 'CONFIRMED': return 'badge-confirmed';
      case 'PREPARING': return 'badge-preparing';
      case 'READY_FOR_PICKUP': return 'badge-ready-for-pickup';
      case 'OUT_FOR_DELIVERY': return 'badge-out-for-delivery';
      case 'DELIVERED': return 'badge-delivered';
      case 'PAYMENT_FAILED':
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-placed';
    }
  };

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>My Orders</h2>
        <button onClick={fetchOrders} className="btn btn-secondary btn-sm">
          Refresh
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />

      {loading && <div className="state-box">Loading your orders...</div>}

      {!loading && orders.length === 0 && (
        <div className="state-box">
          <p>You have not placed any orders yet.</p>
          <Link to="/restaurants" className="btn btn-primary" style={{ marginTop: 12 }}>
            Start Ordering
          </Link>
        </div>
      )}

      {!loading && orders.length > 0 && (
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>Order #</th>
                <th>Restaurant ID</th>
                <th>Items Count</th>
                <th>Total Amount</th>
                <th>Status</th>
                <th>Placed At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td><strong>#{order.id}</strong></td>
                  <td>{order.restaurantId}</td>
                  <td>{order.items ? order.items.length : 0} items</td>
                  <td><strong>${Number(order.totalAmount).toFixed(2)}</strong></td>
                  <td>
                    <span className={`badge ${getStatusBadgeClass(order.status)}`}>
                      {order.status}
                    </span>
                  </td>
                  <td>{order.createdAt ? new Date(order.createdAt).toLocaleString() : '-'}</td>
                  <td>
                    <Link to={`/orders/${order.id}`} className="btn btn-primary btn-sm">
                      Details & Payment
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default MyOrders;
