import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { restaurantApi } from '../api/client';
import Alert from '../components/Alert';

const RestaurantList = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchCity, setSearchCity] = useState('');
  const [searchName, setSearchName] = useState('');

  const fetchRestaurants = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await restaurantApi.getAll();
      setRestaurants(response.data || []);
    } catch (err) {
      console.error('Failed to load restaurants', err);
      setError(err.message || 'Could not load restaurants. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRestaurants();
  }, []);

  const handleSearchByCity = async (e) => {
    e.preventDefault();
    if (!searchCity.trim()) {
      fetchRestaurants();
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await restaurantApi.searchByCity(searchCity.trim());
      setRestaurants(response.data || []);
    } catch (err) {
      setError(err.message || 'Search by city failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchByName = async (e) => {
    e.preventDefault();
    if (!searchName.trim()) {
      fetchRestaurants();
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await restaurantApi.searchByName(searchName.trim());
      setRestaurants(response.data || []);
    } catch (err) {
      setError(err.message || 'Search by name failed');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setSearchCity('');
    setSearchName('');
    fetchRestaurants();
  };

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Available Restaurants</h2>
        <button onClick={fetchRestaurants} className="btn btn-secondary btn-sm">
          Refresh List
        </button>
      </div>

      <Alert type="danger" message={error} onClose={() => setError('')} />

      {/* Search Controls */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <form onSubmit={handleSearchByCity} style={{ display: 'flex', gap: 8, flex: 1, minWidth: 240 }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search by city..."
              value={searchCity}
              onChange={(e) => setSearchCity(e.target.value)}
            />
            <button type="submit" className="btn btn-primary btn-sm">
              Search City
            </button>
          </form>

          <form onSubmit={handleSearchByName} style={{ display: 'flex', gap: 8, flex: 1, minWidth: 240 }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search by restaurant name..."
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
            />
            <button type="submit" className="btn btn-primary btn-sm">
              Search Name
            </button>
          </form>

          {(searchCity || searchName) && (
            <button onClick={handleReset} className="btn btn-secondary btn-sm">
              Reset
            </button>
          )}
        </div>
      </div>

      {/* Loading state */}
      {loading && <div className="state-box">Loading restaurants...</div>}

      {/* Empty state */}
      {!loading && restaurants.length === 0 && (
        <div className="state-box">
          <p>No restaurants found.</p>
          {(searchCity || searchName) && (
            <button onClick={handleReset} className="btn btn-secondary btn-sm" style={{ marginTop: 10 }}>
              Clear Search Filters
            </button>
          )}
        </div>
      )}

      {/* Restaurant Grid */}
      <div className="grid-2">
        {restaurants.map((rest) => (
          <div key={rest.id} className="card">
            <div className="card-header">
              <span>{rest.name}</span>
              <span className={`badge ${rest.active ? 'badge-confirmed' : 'badge-cancelled'}`}>
                {rest.active ? 'Open' : 'Closed'}
              </span>
            </div>
            <p style={{ color: '#555', fontSize: '0.9rem', marginBottom: 8 }}>
              {rest.description || 'No description provided'}
            </p>
            <div style={{ fontSize: '0.85rem', color: '#666', marginBottom: 12 }}>
              <div>📍 {rest.address}, {rest.city}</div>
              <div>📞 {rest.phone}</div>
              {rest.email && <div>✉️ {rest.email}</div>}
            </div>
            <Link to={`/restaurants/${rest.id}`} className="btn btn-primary" style={{ width: '100%' }}>
              View Menu & Order
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RestaurantList;
