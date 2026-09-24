import React, { useState, useEffect } from 'react';
import { userApi, deliveryPartnerApi, deliveryApi, inventoryApi, orderApi } from '../api/client';
import Alert from '../components/Alert';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('users');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Domain data
  const [users, setUsers] = useState([]);
  const [partners, setPartners] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [inventories, setInventories] = useState([]);

  // Order status patch state
  const [patchOrderId, setPatchOrderId] = useState('');
  const [patchStatus, setPatchStatus] = useState('CONFIRMED');

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await userApi.getAllUsers();
      setUsers(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const fetchPartners = async () => {
    setLoading(true);
    try {
      const res = await deliveryPartnerApi.getAll();
      setPartners(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch delivery partners');
    } finally {
      setLoading(false);
    }
  };

  const fetchDeliveries = async () => {
    setLoading(true);
    try {
      const res = await deliveryApi.getAll();
      setDeliveries(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch deliveries');
    } finally {
      setLoading(false);
    }
  };

  const fetchInventory = async () => {
    setLoading(true);
    try {
      const res = await inventoryApi.getAll();
      setInventories(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch inventory');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setError('');
    setSuccess('');
    if (activeTab === 'users') fetchUsers();
    if (activeTab === 'partners') fetchPartners();
    if (activeTab === 'deliveries') fetchDeliveries();
    if (activeTab === 'inventory') fetchInventory();
  }, [activeTab]);

  // Admin Actions
  const handleDeleteUser = async (id) => {
    if (!window.confirm(`Delete user #${id}?`)) return;
    try {
      await userApi.deleteUser(id);
      setSuccess(`User #${id} deleted.`);
      fetchUsers();
    } catch (err) {
      setError(err.message || 'Failed to delete user');
    }
  };

  const handlePartnerStatusChange = async (partnerId, status) => {
    try {
      await deliveryPartnerApi.updateStatus(partnerId, status);
      setSuccess(`Partner #${partnerId} status updated to ${status}`);
      fetchPartners();
    } catch (err) {
      setError(err.message || 'Failed to update partner status');
    }
  };

  const handleDeletePartner = async (id) => {
    if (!window.confirm(`Delete partner #${id}?`)) return;
    try {
      await deliveryPartnerApi.delete(id);
      setSuccess(`Partner #${id} deleted.`);
      fetchPartners();
    } catch (err) {
      setError(err.message || 'Failed to delete partner');
    }
  };

  const handleDeleteInventory = async (id) => {
    if (!window.confirm(`Delete inventory item #${id}?`)) return;
    try {
      await inventoryApi.delete(id);
      setSuccess(`Inventory #${id} deleted.`);
      fetchInventory();
    } catch (err) {
      setError(err.message || 'Failed to delete inventory');
    }
  };

  const handlePatchOrderStatus = async (e) => {
    e.preventDefault();
    if (!patchOrderId) return;
    try {
      await orderApi.updateStatus(Number(patchOrderId), patchStatus);
      setSuccess(`Order #${patchOrderId} status updated to ${patchStatus}`);
      setPatchOrderId('');
    } catch (err) {
      setError(err.message || 'Failed to update order status');
    }
  };

  return (
    <div className="container">
      <h2>FoodFlow Admin Console</h2>

      <Alert type="danger" message={error} onClose={() => setError('')} />
      <Alert type="success" message={success} onClose={() => setSuccess('')} />

      {/* Admin Tab Navigation */}
      <div style={{ display: 'flex', gap: 10, margin: '20px 0', borderBottom: '1px solid #dee2e6', paddingBottom: 10, flexWrap: 'wrap' }}>
        <button
          onClick={() => setActiveTab('users')}
          className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          Users Management
        </button>
        <button
          onClick={() => setActiveTab('partners')}
          className={`btn ${activeTab === 'partners' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          Delivery Partners
        </button>
        <button
          onClick={() => setActiveTab('deliveries')}
          className={`btn ${activeTab === 'deliveries' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          All Deliveries
        </button>
        <button
          onClick={() => setActiveTab('inventory')}
          className={`btn ${activeTab === 'inventory' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          Inventory System
        </button>
        <button
          onClick={() => setActiveTab('orders')}
          className={`btn ${activeTab === 'orders' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
        >
          Order Status Control
        </button>
      </div>

      {loading && <div className="state-box">Loading administrative data...</div>}

      {/* Tab 1: Users */}
      {!loading && activeTab === 'users' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>Registered Users ({users.length})</h3>
            <button onClick={fetchUsers} className="btn btn-secondary btn-sm">Refresh</button>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Role</th>
                  <th>Address</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>#{u.id}</td>
                    <td><strong>{u.name}</strong></td>
                    <td>{u.email}</td>
                    <td>{u.phone}</td>
                    <td><span className="badge badge-placed">{u.userRole}</span></td>
                    <td>{u.address}</td>
                    <td>
                      <button onClick={() => handleDeleteUser(u.id)} className="btn btn-outline-danger btn-sm">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: Delivery Partners */}
      {!loading && activeTab === 'partners' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>Delivery Partners ({partners.length})</h3>
            <button onClick={fetchPartners} className="btn btn-secondary btn-sm">Refresh</button>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Phone</th>
                  <th>Vehicle</th>
                  <th>License</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {partners.map((p) => (
                  <tr key={p.id}>
                    <td>#{p.id}</td>
                    <td><strong>{p.name}</strong></td>
                    <td>{p.phone}</td>
                    <td>{p.vehicleType}</td>
                    <td>{p.vehicleNumber}</td>
                    <td>
                      <span className={`badge ${p.status === 'AVAILABLE' ? 'badge-confirmed' : p.status === 'BUSY' ? 'badge-busy' : 'badge-offline'}`}>
                        {p.status}
                      </span>
                    </td>
                    <td>
                      <button
                        onClick={() => handlePartnerStatusChange(p.id, 'AVAILABLE')}
                        className="btn btn-success btn-sm"
                        style={{ marginRight: 4 }}
                      >
                        Set Available
                      </button>
                      <button
                        onClick={() => handlePartnerStatusChange(p.id, 'OFFLINE')}
                        className="btn btn-secondary btn-sm"
                        style={{ marginRight: 4 }}
                      >
                        Set Offline
                      </button>
                      <button onClick={() => handleDeletePartner(p.id)} className="btn btn-outline-danger btn-sm">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: All Deliveries */}
      {!loading && activeTab === 'deliveries' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>Deliveries Record ({deliveries.length})</h3>
            <button onClick={fetchDeliveries} className="btn btn-secondary btn-sm">Refresh</button>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Order #</th>
                  <th>Partner ID</th>
                  <th>Status</th>
                  <th>Pickup Address</th>
                  <th>Delivery Address</th>
                </tr>
              </thead>
              <tbody>
                {deliveries.map((d) => (
                  <tr key={d.id}>
                    <td>#{d.id}</td>
                    <td>#{d.orderId}</td>
                    <td>{d.deliveryPartnerId || 'Unassigned'}</td>
                    <td><span className="badge badge-out-for-delivery">{d.status}</span></td>
                    <td>{d.pickupAddress}</td>
                    <td>{d.deliveryAddress}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 4: Inventory */}
      {!loading && activeTab === 'inventory' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3>Inventory Items ({inventories.length})</h3>
            <button onClick={fetchInventory} className="btn btn-secondary btn-sm">Refresh</button>
          </div>
          <div className="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Menu Item ID</th>
                  <th>Item Name</th>
                  <th>Available Qty</th>
                  <th>Reserved Qty</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {inventories.map((inv) => (
                  <tr key={inv.id}>
                    <td>#{inv.id}</td>
                    <td>#{inv.menuItemId}</td>
                    <td>{inv.menuItemName || `Menu Item #${inv.menuItemId}`}</td>
                    <td><strong>{inv.availableQuantity}</strong></td>
                    <td>{inv.reservedQuantity}</td>
                    <td>
                      <button onClick={() => handleDeleteInventory(inv.id)} className="btn btn-outline-danger btn-sm">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 5: Order Status Control */}
      {!loading && activeTab === 'orders' && (
        <div className="card" style={{ maxWidth: 500 }}>
          <div className="card-header">Manual Order Status Override</div>
          <form onSubmit={handlePatchOrderStatus}>
            <div className="form-group">
              <label>Order ID</label>
              <input
                type="number"
                className="form-control"
                required
                value={patchOrderId}
                onChange={(e) => setPatchOrderId(e.target.value)}
                placeholder="Enter Order ID"
              />
            </div>
            <div className="form-group">
              <label>New Status</label>
              <select
                className="form-control"
                value={patchStatus}
                onChange={(e) => setPatchStatus(e.target.value)}
              >
                <option value="PLACED">PLACED</option>
                <option value="PAYMENT_PROCESSING">PAYMENT_PROCESSING</option>
                <option value="CONFIRMED">CONFIRMED</option>
                <option value="PREPARING">PREPARING</option>
                <option value="READY_FOR_PICKUP">READY_FOR_PICKUP</option>
                <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="PAYMENT_FAILED">PAYMENT_FAILED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <button type="submit" className="btn btn-primary">
              Update Order Status
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
