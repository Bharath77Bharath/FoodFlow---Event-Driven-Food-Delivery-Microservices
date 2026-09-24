import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { orderApi } from '../api/client';
import Alert from '../components/Alert';

const CartPage = () => {
  const { restaurant, items, updateQuantity, removeItem, clearCart, totalAmount } = useCart();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleCreateOrder = async () => {
    if (!restaurant || items.length === 0) return;

    setLoading(true);
    setError('');

    const payload = {
      restaurantId: restaurant.id,
      items: items.map((item) => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
      })),
    };

    try {
      const response = await orderApi.create(payload);
      const createdOrder = response.data;
      clearCart();
      // Navigate to the newly created order details page for status & payment
      navigate(`/orders/${createdOrder.id}`);
    } catch (err) {
      console.error('Failed to create order', err);
      setError(err.message || 'Failed to place order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!restaurant || items.length === 0) {
    return (
      <div className="container">
        <h2>Your Shopping Cart</h2>
        <div className="state-box" style={{ marginTop: 20 }}>
          <p>Your cart is currently empty.</p>
          <Link to="/restaurants" className="btn btn-primary" style={{ marginTop: 12 }}>
            Browse Restaurants
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Order Cart</h2>
        <button onClick={clearCart} className="btn btn-outline-danger btn-sm">
          Clear Cart
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />

      <div className="card">
        <div className="card-header">
          <span>Restaurant: <strong>{restaurant.name}</strong></span>
          <Link to={`/restaurants/${restaurant.id}`} className="btn btn-secondary btn-sm">
            Add More Items
          </Link>
        </div>

        <div className="table-responsive">
          <table>
            <thead>
              <tr>
                <th>Item</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Total</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.menuItemId}>
                  <td><strong>{item.name}</strong></td>
                  <td>${Number(item.price).toFixed(2)}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <button
                        onClick={() => updateQuantity(item.menuItemId, -1)}
                        className="btn btn-secondary btn-sm"
                        style={{ padding: '2px 8px' }}
                      >
                        -
                      </button>
                      <span>{item.quantity}</span>
                      <button
                        onClick={() => updateQuantity(item.menuItemId, 1)}
                        className="btn btn-secondary btn-sm"
                        style={{ padding: '2px 8px' }}
                      >
                        +
                      </button>
                    </div>
                  </td>
                  <td>${(item.price * item.quantity).toFixed(2)}</td>
                  <td>
                    <button
                      onClick={() => removeItem(item.menuItemId)}
                      className="btn btn-outline-danger btn-sm"
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <div style={{ textAlign: 'right' }}>
            <h3 style={{ marginBottom: 12 }}>Subtotal: ${totalAmount.toFixed(2)}</h3>
            <button
              onClick={handleCreateOrder}
              disabled={loading}
              className="btn btn-success"
              style={{ padding: '10px 24px', fontSize: '1rem' }}
            >
              {loading ? 'Submitting Order...' : 'Place Order Now'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;
