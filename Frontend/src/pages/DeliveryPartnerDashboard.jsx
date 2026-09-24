import React, { useState, useEffect } from 'react';
import { deliveryPartnerApi, deliveryApi, orderApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const DeliveryPartnerDashboard = () => {
  const { user } = useAuth();

  const [partnerProfile, setPartnerProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Registration Form
  const [regForm, setRegForm] = useState({
    name: '',
    phone: '',
    vehicleType: 'BIKE',
    vehicleNumber: '',
  });

  // Delivery lookup state
  const [orderIdInput, setOrderIdInput] = useState('');
  const [activeDelivery, setActiveDelivery] = useState(null);
  const [activeOrder, setActiveOrder] = useState(null);

  // Initialize partner profile from localStorage if saved
  useEffect(() => {
    const savedPartnerId = localStorage.getItem(`foodflow_partner_${user?.userId}`);
    if (savedPartnerId) {
      deliveryPartnerApi
        .getById(savedPartnerId)
        .then((res) => {
          setPartnerProfile(res.data);
        })
        .catch(() => {
          localStorage.removeItem(`foodflow_partner_${user?.userId}`);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [user?.userId]);

  // Create Partner Profile
  const handleRegisterProfile = async (e) => {
    e.preventDefault();
    if (!/^[0-9]{10}$/.test(regForm.phone)) {
      setError('Phone number must contain exactly 10 digits.');
      return;
    }

    setActionLoading(true);
    setError('');
    setSuccess('');

    try {
      const res = await deliveryPartnerApi.create(regForm);
      setPartnerProfile(res.data);
      localStorage.setItem(`foodflow_partner_${user?.userId}`, res.data.id);
      setSuccess('Delivery partner profile registered successfully!');
    } catch (err) {
      console.error('Failed to register partner profile', err);
      setError(err.message || 'Failed to create delivery partner profile.');
    } finally {
      setActionLoading(false);
    }
  };

  // Toggle Status: AVAILABLE vs OFFLINE
  const handleStatusChange = async (newStatus) => {
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await deliveryPartnerApi.updateOwnStatus(newStatus);
      setPartnerProfile(res.data);
      setSuccess(`Your status is now ${newStatus}`);
    } catch (err) {
      setError(err.message || 'Failed to update status.');
    } finally {
      setActionLoading(false);
    }
  };

  // Lookup Delivery by Order ID
  const handleLookupDelivery = async (e) => {
    e.preventDefault();
    if (!orderIdInput) return;
    setActionLoading(true);
    setError('');
    setActiveDelivery(null);
    setActiveOrder(null);

    try {
      const delRes = await deliveryApi.getByOrderId(Number(orderIdInput));
      setActiveDelivery(delRes.data);

      try {
        const ordRes = await orderApi.getById(Number(orderIdInput));
        setActiveOrder(ordRes.data);
      } catch {
        setActiveOrder(null);
      }
    } catch (err) {
      setError(err.message || 'No delivery found for this order ID.');
    } finally {
      setActionLoading(false);
    }
  };

  // Update Delivery Status (PICKED_UP, OUT_FOR_DELIVERY, DELIVERED)
  const handleUpdateDeliveryStatus = async (status) => {
    if (!activeDelivery) return;
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await deliveryApi.updateStatus(activeDelivery.id, status);
      setActiveDelivery(res.data);
      setSuccess(`Delivery status changed to ${status}`);
    } catch (err) {
      setError(err.message || 'Failed to update delivery status.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div className="container state-box">Loading delivery partner profile...</div>;
  }

  return (
    <div className="container">
      <h2>Delivery Partner Console</h2>

      <Alert type="danger" message={error} onClose={() => setError('')} />
      <Alert type="success" message={success} onClose={() => setSuccess('')} />

      {/* Profile Registration if not created yet */}
      {!partnerProfile ? (
        <div className="card" style={{ maxWidth: 500, marginTop: 20 }}>
          <div className="card-header">Register Delivery Partner Profile</div>
          <p style={{ fontSize: '0.9rem', color: '#555', marginBottom: 16 }}>
            Please register your vehicle details before accepting deliveries.
          </p>

          <form onSubmit={handleRegisterProfile}>
            <div className="form-group">
              <label>Full Name</label>
              <input
                type="text"
                className="form-control"
                required
                value={regForm.name}
                onChange={(e) => setRegForm({ ...regForm, name: e.target.value })}
                placeholder="Rider name"
              />
            </div>

            <div className="form-group">
              <label>Phone Number (10 digits)</label>
              <input
                type="tel"
                maxLength={10}
                className="form-control"
                required
                value={regForm.phone}
                onChange={(e) => setRegForm({ ...regForm, phone: e.target.value })}
                placeholder="9876543210"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Vehicle Type</label>
                <select
                  className="form-control"
                  value={regForm.vehicleType}
                  onChange={(e) => setRegForm({ ...regForm, vehicleType: e.target.value })}
                >
                  <option value="BIKE">Motorbike</option>
                  <option value="SCOOTER">Scooter</option>
                  <option value="CAR">Car</option>
                </select>
              </div>

              <div className="form-group">
                <label>Vehicle License Plate</label>
                <input
                  type="text"
                  className="form-control"
                  required
                  value={regForm.vehicleNumber}
                  onChange={(e) => setRegForm({ ...regForm, vehicleNumber: e.target.value })}
                  placeholder="e.g. KA-01-AB-1234"
                />
              </div>
            </div>

            <button type="submit" disabled={actionLoading} className="btn btn-primary" style={{ width: '100%' }}>
              {actionLoading ? 'Registering...' : 'Register Profile'}
            </button>
          </form>
        </div>
      ) : (
        <>
          {/* Profile Card & Availability Toggle */}
          <div className="card" style={{ marginTop: 20 }}>
            <div className="card-header">
              <span>Partner: {partnerProfile.name} (Partner ID: #{partnerProfile.id})</span>
              <span className={`badge ${partnerProfile.status === 'AVAILABLE' ? 'badge-confirmed' : 'badge-offline'}`}>
                {partnerProfile.status}
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12, marginBottom: 16 }}>
              <div>📞 <strong>Phone:</strong> {partnerProfile.phone}</div>
              <div>🛵 <strong>Vehicle:</strong> {partnerProfile.vehicleType} ({partnerProfile.vehicleNumber})</div>
            </div>

            <div>
              <strong>Update Your Availability:</strong>{' '}
              <button
                onClick={() => handleStatusChange('AVAILABLE')}
                disabled={actionLoading || partnerProfile.status === 'AVAILABLE'}
                className="btn btn-success btn-sm"
                style={{ marginRight: 8 }}
              >
                Go Online (AVAILABLE)
              </button>
              <button
                onClick={() => handleStatusChange('OFFLINE')}
                disabled={actionLoading || partnerProfile.status === 'OFFLINE'}
                className="btn btn-secondary btn-sm"
              >
                Go Offline
              </button>
            </div>
          </div>

          {/* Delivery Lookup & Management */}
          <div className="card" style={{ marginTop: 20 }}>
            <div className="card-header">Manage Assigned Delivery</div>
            <form onSubmit={handleLookupDelivery} style={{ display: 'flex', gap: 10, maxWidth: 400, marginBottom: 16 }}>
              <input
                type="number"
                className="form-control"
                placeholder="Enter Order ID..."
                required
                value={orderIdInput}
                onChange={(e) => setOrderIdInput(e.target.value)}
              />
              <button type="submit" disabled={actionLoading} className="btn btn-primary">
                Lookup
              </button>
            </form>

            {activeDelivery && (
              <div style={{ borderTop: '1px solid #dee2e6', paddingTop: 14 }}>
                <h4>Delivery Details for Order #{activeDelivery.orderId}</h4>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12, margin: '12px 0' }}>
                  <div><strong>Delivery ID:</strong> #{activeDelivery.id}</div>
                  <div>
                    <strong>Status:</strong>{' '}
                    <span className="badge badge-out-for-delivery">{activeDelivery.status}</span>
                  </div>
                  <div>📍 <strong>Pickup:</strong> {activeDelivery.pickupAddress}</div>
                  <div>🏠 <strong>Delivery To:</strong> {activeDelivery.deliveryAddress}</div>
                  {activeOrder && (
                    <div>💵 <strong>Order Amount:</strong> ${Number(activeOrder.totalAmount).toFixed(2)}</div>
                  )}
                </div>

                <div style={{ marginTop: 16 }}>
                  <strong>Update Status:</strong>
                  <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
                    <button
                      onClick={() => handleUpdateDeliveryStatus('PICKED_UP')}
                      disabled={actionLoading || activeDelivery.status !== 'ASSIGNED'}
                      className="btn btn-secondary btn-sm"
                    >
                      Mark Picked Up
                    </button>
                    <button
                      onClick={() => handleUpdateDeliveryStatus('OUT_FOR_DELIVERY')}
                      disabled={actionLoading || activeDelivery.status !== 'PICKED_UP'}
                      className="btn btn-primary btn-sm"
                    >
                      Mark Out For Delivery
                    </button>
                    <button
                      onClick={() => handleUpdateDeliveryStatus('DELIVERED')}
                      disabled={actionLoading || activeDelivery.status !== 'OUT_FOR_DELIVERY'}
                      className="btn btn-success btn-sm"
                    >
                      Mark Delivered ✓
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default DeliveryPartnerDashboard;
