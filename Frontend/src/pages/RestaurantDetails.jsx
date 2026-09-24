import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { restaurantApi, menuApi, reviewApi } from '../api/client';
import { useCart } from '../context/CartContext';
import Alert from '../components/Alert';

const RestaurantDetails = () => {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cartNotice, setCartNotice] = useState('');

  const { addItem, items: cartItems } = useCart();

  useEffect(() => {
    const loadDetails = async () => {
      setLoading(true);
      setError('');
      try {
        const [restRes, menuRes, reviewRes] = await Promise.all([
          restaurantApi.getById(id),
          menuApi.getByRestaurant(id),
          reviewApi.getByRestaurant(id).catch(() => ({ data: [] })),
        ]);

        setRestaurant(restRes.data);
        setMenuItems(menuRes.data || []);
        setReviews(reviewRes.data || []);
      } catch (err) {
        console.error('Failed to load restaurant details', err);
        setError(err.message || 'Failed to load restaurant details.');
      } finally {
        setLoading(false);
      }
    };

    loadDetails();
  }, [id]);

  const handleAddToCart = (item) => {
    if (!item.available) return;
    const added = addItem(restaurant, item);
    if (added) {
      setCartNotice(`Added "${item.name}" to cart!`);
      setTimeout(() => setCartNotice(''), 3000);
    }
  };

  const getItemQuantityInCart = (itemId) => {
    const found = cartItems.find((ci) => ci.menuItemId === itemId);
    return found ? found.quantity : 0;
  };

  if (loading) {
    return <div className="container state-box">Loading restaurant details...</div>;
  }

  if (error || !restaurant) {
    return (
      <div className="container">
        <Alert type="danger" message={error || 'Restaurant not found.'} />
        <Link to="/restaurants" className="btn btn-secondary">
          &larr; Back to Restaurants
        </Link>
      </div>
    );
  }

  return (
    <div className="container">
      <Link to="/restaurants" className="btn btn-secondary btn-sm" style={{ marginBottom: 16 }}>
        &larr; Back to Restaurants
      </Link>

      <Alert type="success" message={cartNotice} onClose={() => setCartNotice('')} />

      {/* Restaurant Header */}
      <div className="card">
        <div className="card-header">
          <h2>{restaurant.name}</h2>
          <span className={`badge ${restaurant.active ? 'badge-confirmed' : 'badge-cancelled'}`}>
            {restaurant.active ? 'Active' : 'Inactive'}
          </span>
        </div>
        <p style={{ color: '#555', marginBottom: 10 }}>{restaurant.description}</p>
        <div style={{ fontSize: '0.9rem', color: '#666' }}>
          <div>📍 <strong>Address:</strong> {restaurant.address}, {restaurant.city}</div>
          <div>📞 <strong>Phone:</strong> {restaurant.phone}</div>
          {restaurant.email && <div>✉️ <strong>Email:</strong> {restaurant.email}</div>}
        </div>
      </div>

      {/* Menu Section */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '24px 0 12px' }}>
        <h3>Menu Items</h3>
        <Link to="/cart" className="btn btn-primary btn-sm">
          Go to Cart &rarr;
        </Link>
      </div>

      {menuItems.length === 0 ? (
        <div className="state-box">No menu items currently available for this restaurant.</div>
      ) : (
        <div className="grid-2">
          {menuItems.map((item) => {
            const qty = getItemQuantityInCart(item.id);
            return (
              <div key={item.id} className="card">
                <div className="card-header">
                  <span>{item.name}</span>
                  <strong>${Number(item.price).toFixed(2)}</strong>
                </div>
                <p style={{ fontSize: '0.85rem', color: '#666', marginBottom: 12 }}>
                  {item.description || 'No description available'}
                </p>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span className={`badge ${item.available ? 'badge-confirmed' : 'badge-cancelled'}`}>
                    {item.available ? 'Available' : 'Sold Out'}
                  </span>

                  <div>
                    {qty > 0 && (
                      <span style={{ marginRight: 8, fontSize: '0.85rem', color: '#198754', fontWeight: 'bold' }}>
                        In Cart: {qty}
                      </span>
                    )}
                    <button
                      onClick={() => handleAddToCart(item)}
                      disabled={!item.available}
                      className="btn btn-primary btn-sm"
                    >
                      {qty > 0 ? '+ Add More' : 'Add to Cart'}
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Reviews Section */}
      <div style={{ marginTop: 36 }}>
        <h3>Customer Reviews ({reviews.length})</h3>
        {reviews.length === 0 ? (
          <div className="state-box" style={{ marginTop: 12 }}>
            No reviews yet for this restaurant.
          </div>
        ) : (
          <div style={{ marginTop: 12 }}>
            {reviews.map((rev) => (
              <div key={rev.id} className="card" style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <div>
                    {'⭐'.repeat(rev.rating)}
                    <span style={{ fontSize: '0.85rem', color: '#666', marginLeft: 8 }}>
                      ({rev.rating}/5)
                    </span>
                  </div>
                  <span style={{ fontSize: '0.8rem', color: '#888' }}>
                    {rev.createdAt ? new Date(rev.createdAt).toLocaleDateString() : ''}
                  </span>
                </div>
                <p style={{ fontSize: '0.9rem', color: '#444' }}>{rev.comment}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default RestaurantDetails;
