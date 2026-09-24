import React, { useState, useEffect, useCallback } from 'react';
import { restaurantApi, menuApi, inventoryApi } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const OwnerDashboard = () => {
  const { user } = useAuth();

  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Forms
  const [showCreateRest, setShowCreateRest] = useState(false);
  const [restForm, setRestForm] = useState({
    name: '',
    description: '',
    address: '',
    city: '',
    phone: '',
    email: '',
  });

  const [showMenuForm, setShowMenuForm] = useState(false);
  const [editingMenuItemId, setEditingMenuItemId] = useState(null);
  const [menuForm, setMenuForm] = useState({
    name: '',
    description: '',
    price: '',
    available: true,
  });

  // Inventory modal / form
  const [inventoryItem, setInventoryItem] = useState(null); // { menuItemId, quantity }
  const [inventoryQty, setInventoryQty] = useState('');

  const fetchOwnerRestaurants = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await restaurantApi.getAll();
      const allRests = response.data || [];
      // Filter by current owner's userId
      const myRests = allRests.filter((r) => r.ownerId === user?.userId);
      setRestaurants(myRests);

      if (myRests.length > 0 && !selectedRestaurant) {
        setSelectedRestaurant(myRests[0]);
      }
    } catch (err) {
      console.error('Failed to load owner restaurants', err);
      setError(err.message || 'Failed to load your restaurants.');
    } finally {
      setLoading(false);
    }
  }, [user?.userId, selectedRestaurant]);

  useEffect(() => {
    fetchOwnerRestaurants();
  }, [fetchOwnerRestaurants]);

  const fetchMenuItems = useCallback(async (restaurantId) => {
    if (!restaurantId) return;
    try {
      const response = await menuApi.getByRestaurant(restaurantId);
      setMenuItems(response.data || []);
    } catch (err) {
      console.error('Failed to fetch menu items', err);
      setError(err.message || 'Failed to fetch menu items.');
    }
  }, []);

  useEffect(() => {
    if (selectedRestaurant?.id) {
      fetchMenuItems(selectedRestaurant.id);
    }
  }, [selectedRestaurant, fetchMenuItems]);

  // Create Restaurant
  const handleCreateRestaurant = async (e) => {
    e.preventDefault();
    if (!/^[0-9]{10}$/.test(restForm.phone)) {
      setError('Phone number must contain exactly 10 digits.');
      return;
    }
    setActionLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await restaurantApi.create(restForm);
      setSuccess(`Restaurant "${response.data.name}" created successfully!`);
      setShowCreateRest(false);
      setRestForm({ name: '', description: '', address: '', city: '', phone: '', email: '' });
      setSelectedRestaurant(response.data);
      fetchOwnerRestaurants();
    } catch (err) {
      setError(err.message || 'Failed to create restaurant.');
    } finally {
      setActionLoading(false);
    }
  };

  // Deactivate Restaurant
  const handleDeleteRestaurant = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this restaurant?')) return;
    setActionLoading(true);
    try {
      await restaurantApi.delete(id);
      setSuccess('Restaurant deactivated successfully.');
      setSelectedRestaurant(null);
      fetchOwnerRestaurants();
    } catch (err) {
      setError(err.message || 'Failed to deactivate restaurant.');
    } finally {
      setActionLoading(false);
    }
  };

  // Create or Update Menu Item
  const handleSaveMenuItem = async (e) => {
    e.preventDefault();
    if (!selectedRestaurant) return;

    setActionLoading(true);
    setError('');
    setSuccess('');

    const payload = {
      name: menuForm.name,
      description: menuForm.description,
      price: parseFloat(menuForm.price),
      available: menuForm.available,
    };

    try {
      if (editingMenuItemId) {
        await menuApi.update(editingMenuItemId, payload);
        setSuccess('Menu item updated successfully.');
      } else {
        await menuApi.create(selectedRestaurant.id, payload);
        setSuccess('Menu item added successfully.');
      }

      setShowMenuForm(false);
      setEditingMenuItemId(null);
      setMenuForm({ name: '', description: '', price: '', available: true });
      fetchMenuItems(selectedRestaurant.id);
    } catch (err) {
      setError(err.message || 'Failed to save menu item.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleEditMenuItem = (item) => {
    setEditingMenuItemId(item.id);
    setMenuForm({
      name: item.name,
      description: item.description || '',
      price: item.price,
      available: item.available,
    });
    setShowMenuForm(true);
  };

  const handleDeleteMenuItem = async (menuItemId) => {
    if (!window.confirm('Delete this menu item?')) return;
    setActionLoading(true);
    try {
      await menuApi.delete(menuItemId);
      setSuccess('Menu item removed.');
      fetchMenuItems(selectedRestaurant.id);
    } catch (err) {
      setError(err.message || 'Failed to delete menu item.');
    } finally {
      setActionLoading(false);
    }
  };

  // Manage Inventory
  const handleOpenInventory = async (item) => {
    setInventoryItem(item);
    setInventoryQty('');
    setError('');
    try {
      const res = await inventoryApi.getByMenuItem(item.id);
      if (res.data) {
        setInventoryQty(res.data.availableQuantity?.toString() || '');
      }
    } catch {
      // Inventory might not exist yet
      setInventoryQty('');
    }
  };

  const handleSaveInventory = async (e) => {
    e.preventDefault();
    if (!inventoryItem) return;
    setActionLoading(true);
    setError('');
    setSuccess('');

    try {
      // Check if inventory already exists
      let existingInv = null;
      try {
        const res = await inventoryApi.getByMenuItem(inventoryItem.id);
        existingInv = res.data;
      } catch {
        existingInv = null;
      }

      if (existingInv && existingInv.id) {
        await inventoryApi.update(existingInv.id, { quantity: parseInt(inventoryQty, 10) });
      } else {
        await inventoryApi.create({
          menuItemId: inventoryItem.id,
          quantity: parseInt(inventoryQty, 10),
        });
      }

      setSuccess(`Inventory updated for "${inventoryItem.name}"!`);
      setInventoryItem(null);
    } catch (err) {
      setError(err.message || 'Failed to update inventory.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Restaurant Owner Management</h2>
        <button
          onClick={() => setShowCreateRest(!showCreateRest)}
          className="btn btn-primary btn-sm"
        >
          {showCreateRest ? 'Cancel' : '+ Register New Restaurant'}
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />
      <Alert type="success" message={success} onClose={() => setSuccess('')} />

      {/* Register New Restaurant Form */}
      {showCreateRest && (
        <div className="card" style={{ marginBottom: 24, backgroundColor: '#fff' }}>
          <div className="card-header">Register a New Restaurant</div>
          <form onSubmit={handleCreateRestaurant}>
            <div className="form-row">
              <div className="form-group">
                <label>Restaurant Name</label>
                <input
                  type="text"
                  className="form-control"
                  required
                  value={restForm.name}
                  onChange={(e) => setRestForm({ ...restForm, name: e.target.value })}
                  placeholder="e.g. Bella Italia"
                />
              </div>
              <div className="form-group">
                <label>City</label>
                <input
                  type="text"
                  className="form-control"
                  required
                  value={restForm.city}
                  onChange={(e) => setRestForm({ ...restForm, city: e.target.value })}
                  placeholder="e.g. New York"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                className="form-control"
                required
                value={restForm.address}
                onChange={(e) => setRestForm({ ...restForm, address: e.target.value })}
                placeholder="Full street address"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Phone Number (10 digits)</label>
                <input
                  type="tel"
                  maxLength={10}
                  className="form-control"
                  required
                  value={restForm.phone}
                  onChange={(e) => setRestForm({ ...restForm, phone: e.target.value })}
                  placeholder="9876543210"
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  className="form-control"
                  value={restForm.email}
                  onChange={(e) => setRestForm({ ...restForm, email: e.target.value })}
                  placeholder="contact@restaurant.com"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                className="form-control"
                rows={2}
                value={restForm.description}
                onChange={(e) => setRestForm({ ...restForm, description: e.target.value })}
                placeholder="Cuisine, specialties, etc."
              />
            </div>

            <button type="submit" disabled={actionLoading} className="btn btn-success">
              {actionLoading ? 'Registering...' : 'Save Restaurant'}
            </button>
          </form>
        </div>
      )}

      {loading && <div className="state-box">Loading restaurant profiles...</div>}

      {!loading && restaurants.length === 0 && !showCreateRest && (
        <div className="state-box">
          <p>You have not registered any restaurants yet.</p>
          <button
            onClick={() => setShowCreateRest(true)}
            className="btn btn-primary"
            style={{ marginTop: 10 }}
          >
            Register Your Restaurant Now
          </button>
        </div>
      )}

      {/* Select Restaurant Tabs if multiple */}
      {restaurants.length > 0 && (
        <div style={{ marginBottom: 16 }}>
          <label style={{ fontWeight: 'bold', marginRight: 10 }}>Select Your Restaurant:</label>
          <select
            className="form-control"
            style={{ display: 'inline-block', width: 'auto' }}
            value={selectedRestaurant?.id || ''}
            onChange={(e) => {
              const r = restaurants.find((x) => x.id === Number(e.target.value));
              setSelectedRestaurant(r);
            }}
          >
            {restaurants.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name} ({r.city}) - {r.active ? 'Active' : 'Inactive'}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Current Restaurant Info */}
      {selectedRestaurant && (
        <div className="card">
          <div className="card-header">
            <span>{selectedRestaurant.name}</span>
            <div>
              <span
                className={`badge ${selectedRestaurant.active ? 'badge-confirmed' : 'badge-cancelled'}`}
                style={{ marginRight: 8 }}
              >
                {selectedRestaurant.active ? 'Active' : 'Inactive'}
              </span>
              <button
                onClick={() => handleDeleteRestaurant(selectedRestaurant.id)}
                className="btn btn-outline-danger btn-sm"
              >
                Deactivate
              </button>
            </div>
          </div>
          <div style={{ fontSize: '0.9rem', color: '#555' }}>
            <p>{selectedRestaurant.description}</p>
            <div>📍 {selectedRestaurant.address}, {selectedRestaurant.city} | 📞 {selectedRestaurant.phone}</div>
          </div>
        </div>
      )}

      {/* Menu Items Management */}
      {selectedRestaurant && (
        <div style={{ marginTop: 24 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3>Menu Items for {selectedRestaurant.name}</h3>
            <button
              onClick={() => {
                setEditingMenuItemId(null);
                setMenuForm({ name: '', description: '', price: '', available: true });
                setShowMenuForm(!showMenuForm);
              }}
              className="btn btn-primary btn-sm"
            >
              {showMenuForm ? 'Close Menu Form' : '+ Add New Menu Item'}
            </button>
          </div>

          {/* Menu Item Form (Add / Edit) */}
          {showMenuForm && (
            <div className="card" style={{ marginBottom: 20 }}>
              <div className="card-header">
                {editingMenuItemId ? 'Edit Menu Item' : 'Add New Menu Item'}
              </div>
              <form onSubmit={handleSaveMenuItem}>
                <div className="form-row">
                  <div className="form-group">
                    <label>Item Name</label>
                    <input
                      type="text"
                      className="form-control"
                      required
                      value={menuForm.name}
                      onChange={(e) => setMenuForm({ ...menuForm, name: e.target.value })}
                      placeholder="e.g. Pepperoni Pizza"
                    />
                  </div>
                  <div className="form-group">
                    <label>Price ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      className="form-control"
                      required
                      value={menuForm.price}
                      onChange={(e) => setMenuForm({ ...menuForm, price: e.target.value })}
                      placeholder="9.99"
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    className="form-control"
                    rows={2}
                    value={menuForm.description}
                    onChange={(e) => setMenuForm({ ...menuForm, description: e.target.value })}
                    placeholder="Ingredients, portion size, etc."
                  />
                </div>

                <div className="form-group">
                  <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={menuForm.available}
                      onChange={(e) => setMenuForm({ ...menuForm, available: e.target.checked })}
                    />
                    Available for customers to order
                  </label>
                </div>

                <button type="submit" disabled={actionLoading} className="btn btn-success">
                  {actionLoading ? 'Saving...' : editingMenuItemId ? 'Update Item' : 'Add Item'}
                </button>
              </form>
            </div>
          )}

          {/* Menu Items Table */}
          {menuItems.length === 0 ? (
            <div className="state-box">No menu items created yet for this restaurant.</div>
          ) : (
            <div className="table-responsive">
              <table>
                <thead>
                  <tr>
                    <th>Item ID</th>
                    <th>Name</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {menuItems.map((item) => (
                    <tr key={item.id}>
                      <td>#{item.id}</td>
                      <td>
                        <strong>{item.name}</strong>
                        <div style={{ fontSize: '0.8rem', color: '#666' }}>{item.description}</div>
                      </td>
                      <td>${Number(item.price).toFixed(2)}</td>
                      <td>
                        <span className={`badge ${item.available ? 'badge-confirmed' : 'badge-cancelled'}`}>
                          {item.available ? 'Available' : 'Unavailable'}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => handleEditMenuItem(item)}
                          className="btn btn-secondary btn-sm"
                          style={{ marginRight: 6 }}
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleOpenInventory(item)}
                          className="btn btn-primary btn-sm"
                          style={{ marginRight: 6 }}
                        >
                          Inventory
                        </button>
                        <button
                          onClick={() => handleDeleteMenuItem(item.id)}
                          className="btn btn-outline-danger btn-sm"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Inventory Modal / Dialog */}
      {inventoryItem && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div className="card" style={{ width: 380, backgroundColor: '#fff' }}>
            <div className="card-header">
              <span>Set Inventory for "{inventoryItem.name}"</span>
              <button
                onClick={() => setInventoryItem(null)}
                style={{ border: 'none', background: 'none', cursor: 'pointer', fontWeight: 'bold' }}
              >
                ×
              </button>
            </div>
            <form onSubmit={handleSaveInventory}>
              <div className="form-group">
                <label>Available Quantity</label>
                <input
                  type="number"
                  min="0"
                  className="form-control"
                  required
                  value={inventoryQty}
                  onChange={(e) => setInventoryQty(e.target.value)}
                  placeholder="e.g. 50"
                />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                <button
                  type="button"
                  onClick={() => setInventoryItem(null)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button type="submit" disabled={actionLoading} className="btn btn-primary">
                  {actionLoading ? 'Updating...' : 'Save Quantity'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default OwnerDashboard;
