import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(email, password);
      // Route by role
      if (user.role === 'RESTAURANT_OWNER') {
        navigate('/owner');
      } else if (user.role === 'DELIVERY_PARTNER') {
        navigate('/delivery-partner');
      } else if (user.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/restaurants');
      }
    } catch (err) {
      console.error('Login error', err);
      if (err.status === 401 || err.status === 403) {
        setError('Invalid email or password.');
      } else {
        setError(err.message || 'Login failed. Please check the backend connection.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 440, marginTop: 40 }}>
      <div className="card">
        <h2 style={{ marginBottom: 16 }}>Sign In to FoodFlow</h2>

        <Alert type="danger" message={error} onClose={() => setError('')} />

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <input
              type="email"
              className="form-control"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. customer@example.com"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              className="form-control"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Your password"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: 8 }}
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div style={{ marginTop: 16, fontSize: '0.9rem', textAlign: 'center' }}>
          Don't have an account? <Link to="/register">Register here</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
