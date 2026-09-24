import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderApi, paymentApi, deliveryApi, reviewApi, notificationApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const ORDER_STEPS = [
  'PLACED',
  'CONFIRMED',
  'PREPARING',
  'READY_FOR_PICKUP',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
];

const OrderDetails = () => {
  const { orderId } = useParams();
  const { user } = useAuth();

  const [order, setOrder] = useState(null);
  const [payment, setPayment] = useState(null);
  const [delivery, setDelivery] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Payment Form State
  const [paymentMethod, setPaymentMethod] = useState('UPI');

  // Review Form State
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [reviewSubmitted, setReviewSubmitted] = useState(false);

  const loadOrderData = useCallback(async () => {
    setError('');
    try {
      const orderRes = await orderApi.getById(orderId);
      setOrder(orderRes.data);

      // Attempt to load associated payment
      try {
        const payRes = await paymentApi.getByOrderId(orderId);
        setPayment(payRes.data);
      } catch {
        setPayment(null);
      }

      // Attempt to load associated delivery
      try {
        const delRes = await deliveryApi.getByOrderId(orderId);
        setDelivery(delRes.data);
      } catch {
        setDelivery(null);
      }

      // Attempt to load notifications for this order
      try {
        const notifRes = await notificationApi.getByOrder(orderId);
        setNotifications(notifRes.data || []);
      } catch {
        setNotifications([]);
      }
    } catch (err) {
      console.error('Failed to load order data', err);
      setError(err.message || 'Failed to load order details.');
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    loadOrderData();
  }, [loadOrderData]);

  // Payment creation and processing
  const handleInitiateAndProcessPayment = async () => {
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      // 1. Create Payment
      const createRes = await paymentApi.create({
        orderId: Number(orderId),
        paymentMethod,
      });
      const newPayment = createRes.data;
      setPayment(newPayment);

      // 2. Process Payment immediately
      const processRes = await paymentApi.process(newPayment.id);
      setPayment(processRes.data);

      setSuccess('Payment processed successfully!');
      // Refresh order to see updated status
      setTimeout(() => loadOrderData(), 1000);
    } catch (err) {
      console.error('Payment error', err);
      setError(err.message || 'Payment initiation failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleProcessExistingPayment = async () => {
    if (!payment) return;
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      const processRes = await paymentApi.process(payment.id);
      setPayment(processRes.data);
      setSuccess('Payment processed successfully!');
      setTimeout(() => loadOrderData(), 1000);
    } catch (err) {
      console.error('Payment process error', err);
      setError(err.message || 'Failed to process payment.');
    } finally {
      setActionLoading(false);
    }
  };

  // Review submission
  const handleSubmitReview = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      await reviewApi.create({
        orderId: Number(orderId),
        rating: Number(rating),
        comment,
      });
      setSuccess('Thank you! Your review has been submitted.');
      setReviewSubmitted(true);
    } catch (err) {
      console.error('Failed to submit review', err);
      setError(err.message || 'Failed to submit review.');
    } finally {
      setActionLoading(false);
    }
  };

  const getStepIndex = (status) => ORDER_STEPS.indexOf(status);

  if (loading) {
    return <div className="container state-box">Loading order details...</div>;
  }

  if (error && !order) {
    return (
      <div className="container">
        <Alert type="danger" message={error} />
        <Link to="/orders" className="btn btn-secondary">
          &larr; Back to Orders
        </Link>
      </div>
    );
  }

  const currentStep = getStepIndex(order.status);
  const isCancelled = order.status === 'CANCELLED' || order.status === 'PAYMENT_FAILED';

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div>
          <Link to="/orders" className="btn btn-secondary btn-sm" style={{ marginRight: 12 }}>
            &larr; My Orders
          </Link>
          <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Order #{order.id}</span>
        </div>
        <button onClick={loadOrderData} className="btn btn-secondary btn-sm">
          🔄 Refresh Order Status
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />
      <Alert type="success" message={success} onClose={() => setSuccess('')} />

      {/* Order Status & Progress Timeline */}
      <div className="card">
        <div className="card-header">
          <span>Current Status</span>
          <span className={`badge badge-${order.status?.toLowerCase().replace(/_/g, '-')}`}>
            {order.status}
          </span>
        </div>

        {isCancelled ? (
          <div className="alert alert-danger" style={{ margin: '10px 0' }}>
            This order is currently marked as <strong>{order.status}</strong>.
          </div>
        ) : (
          <div className="timeline">
            {ORDER_STEPS.map((step, idx) => {
              const isActive = idx === currentStep;
              const isCompleted = currentStep > idx;
              return (
                <div
                  key={step}
                  className={`timeline-step ${isActive ? 'active' : ''} ${isCompleted ? 'completed' : ''}`}
                >
                  <div className="timeline-step-circle">
                    {isCompleted ? '✓' : idx + 1}
                  </div>
                  <div className="timeline-step-label">{step.replace(/_/g, ' ')}</div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Order Items */}
      <div className="card">
        <div className="card-header">Order Summary</div>
        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>Item ID</th>
                <th>Item Name</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {order.items?.map((item, index) => (
                <tr key={item.id || index}>
                  <td>{item.menuItemId}</td>
                  <td>{item.menuItemName || `Menu Item #${item.menuItemId}`}</td>
                  <td>${Number(item.price).toFixed(2)}</td>
                  <td>{item.quantity}</td>
                  <td>${(item.price * item.quantity).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div style={{ textAlign: 'right', marginTop: 8 }}>
          <h3>Total: ${Number(order.totalAmount).toFixed(2)}</h3>
        </div>
      </div>

      {/* Payment Section */}
      <div className="card">
        <div className="card-header">Payment Information</div>
        {payment ? (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
              <div>
                <strong>Payment ID:</strong> #{payment.id}
              </div>
              <div>
                <strong>Method:</strong> {payment.paymentMethod}
              </div>
              <div>
                <strong>Status:</strong>{' '}
                <span className={`badge ${payment.status === 'SUCCESS' ? 'badge-confirmed' : payment.status === 'PENDING' ? 'badge-payment-processing' : 'badge-cancelled'}`}>
                  {payment.status}
                </span>
              </div>
              <div>
                <strong>Amount:</strong> ${Number(payment.amount).toFixed(2)}
              </div>
              {payment.transactionId && (
                <div>
                  <strong>Transaction ID:</strong> {payment.transactionId}
                </div>
              )}
            </div>

            {payment.status === 'PENDING' && (
              <div style={{ marginTop: 14 }}>
                <button
                  onClick={handleProcessExistingPayment}
                  disabled={actionLoading}
                  className="btn btn-success"
                >
                  {actionLoading ? 'Processing...' : 'Complete Payment Processing'}
                </button>
              </div>
            )}
          </div>
        ) : (
          <div>
            {order.status === 'PLACED' ? (
              <div style={{ maxWidth: 400 }}>
                <p style={{ marginBottom: 12 }}>Select a payment method to complete your order:</p>
                <div className="form-group">
                  <label>Payment Method</label>
                  <select
                    className="form-control"
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  >
                    <option value="UPI">UPI</option>
                    <option value="CARD">Credit/Debit Card</option>
                    <option value="CASH">Cash on Delivery</option>
                  </select>
                </div>
                <button
                  onClick={handleInitiateAndProcessPayment}
                  disabled={actionLoading}
                  className="btn btn-success"
                >
                  {actionLoading ? 'Processing Payment...' : `Pay $${Number(order.totalAmount).toFixed(2)}`}
                </button>
              </div>
            ) : (
              <p>No payment record found for this order.</p>
            )}
          </div>
        )}
      </div>

      {/* Delivery Tracking Section */}
      {delivery && (
        <div className="card">
          <div className="card-header">Delivery Tracking</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}>
            <div>
              <strong>Delivery ID:</strong> #{delivery.id}
            </div>
            <div>
              <strong>Delivery Status:</strong>{' '}
              <span className="badge badge-preparing">{delivery.status}</span>
            </div>
            <div>
              <strong>Partner ID:</strong> {delivery.deliveryPartnerId || 'Pending'}
            </div>
            <div>
              <strong>Pickup Address:</strong> {delivery.pickupAddress}
            </div>
            <div>
              <strong>Delivery Address:</strong> {delivery.deliveryAddress}
            </div>
          </div>
        </div>
      )}

      {/* Customer Review Section (Only when DELIVERED) */}
      {order.status === 'DELIVERED' && user?.role === 'CUSTOMER' && (
        <div className="card">
          <div className="card-header">Leave a Review</div>
          {reviewSubmitted ? (
            <div className="alert alert-success">Your review has been recorded!</div>
          ) : (
            <form onSubmit={handleSubmitReview} style={{ maxWidth: 500 }}>
              <div className="form-group">
                <label>Rating (1 to 5 Stars)</label>
                <select
                  className="form-control"
                  value={rating}
                  onChange={(e) => setRating(e.target.value)}
                >
                  <option value={5}>⭐⭐⭐⭐⭐ (5 - Excellent)</option>
                  <option value={4}>⭐⭐⭐⭐ (4 - Very Good)</option>
                  <option value={3}>⭐⭐⭐ (3 - Average)</option>
                  <option value={2}>⭐⭐ (2 - Poor)</option>
                  <option value={1}>⭐ (1 - Terrible)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Your Feedback</label>
                <textarea
                  className="form-control"
                  rows={3}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="How was the food and delivery service?"
                />
              </div>
              <button type="submit" disabled={actionLoading} className="btn btn-primary">
                {actionLoading ? 'Submitting...' : 'Submit Review'}
              </button>
            </form>
          )}
        </div>
      )}

      {/* Notifications Section */}
      {notifications.length > 0 && (
        <div className="card">
          <div className="card-header">Order Event Notifications ({notifications.length})</div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Event Type</th>
                  <th>Channel</th>
                  <th>Message</th>
                  <th>Status</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((n) => (
                  <tr key={n.id}>
                    <td><strong>{n.type}</strong></td>
                    <td>{n.channel}</td>
                    <td>{n.message}</td>
                    <td><span className="badge badge-confirmed">{n.status}</span></td>
                    <td>{n.sentAt ? new Date(n.sentAt).toLocaleTimeString() : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderDetails;
