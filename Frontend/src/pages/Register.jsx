import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';

const Register = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    userRole: 'CUSTOMER',
    address: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Phone validation (10 digits)
    if (!/^[0-9]{10}$/.test(formData.phone)) {
      setError('Phone number must contain exactly 10 digits.');
      return;
    }

    setLoading(true);

    try {
      await register(formData);
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (err) {
      console.error('Registration error', err);
      if (err.data && typeof err.data === 'object') {
        const messages = Object.values(err.data).join('; ');
        setError(messages || err.message || 'Registration failed.');
      } else {
        setError(err.message || 'Registration failed.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: 500, marginTop: 30 }}>
      <div className="card">
        <h2 style={{ marginBottom: 16 }}>Create an Account</h2>

        <Alert type="danger" message={error} onClose={() => setError('')} />
        <Alert type="success" message={success} />

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              name="name"
              className="form-control"
              required
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g. Jane Doe"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Email Address</label>
              <input
                type="email"
                name="email"
                className="form-control"
                required
                value={formData.email}
                onChange={handleChange}
                placeholder="e.g. jane@example.com"
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                name="password"
                className="form-control"
                required
                value={formData.password}
                onChange={handleChange}
                placeholder="Password"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Phone Number (10 digits)</label>
              <input
                type="tel"
                name="phone"
                maxLength={10}
                className="form-control"
                required
                value={formData.phone}
                onChange={handleChange}
                placeholder="9876543210"
              />
            </div>
            <div className="form-group">
              <label>Role</label>
              <select
                name="userRole"
                className="form-control"
                value={formData.userRole}
                onChange={handleChange}
              >
                <option value="CUSTOMER">Customer</option>
                <option value="RESTAURANT_OWNER">Restaurant Owner</option>
                <option value="DELIVERY_PARTNER">Delivery Partner</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Address</label>
            <textarea
              name="address"
              className="form-control"
              required
              rows={2}
              value={formData.address}
              onChange={handleChange}
              placeholder="Enter full physical address"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: 8 }}
            disabled={loading}
          >
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>

        <div style={{ marginTop: 16, fontSize: '0.9rem', textAlign: 'center' }}>
          Already have an account? <Link to="/login">Login here</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
