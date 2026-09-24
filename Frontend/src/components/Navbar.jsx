import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const { totalItemCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => (location.pathname === path ? 'active' : '');

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <span>🍔 FoodFlow</span>
      </Link>

      <ul className="navbar-links">
        {isAuthenticated ? (
          <>
            {/* Customer Navigation */}
            {user?.role === 'CUSTOMER' && (
              <>
                <li>
                  <Link to="/restaurants" className={isActive('/restaurants')}>
                    Restaurants
                  </Link>
                </li>
                <li>
                  <Link to="/cart" className={isActive('/cart')}>
                    Cart {totalItemCount > 0 && `(${totalItemCount})`}
                  </Link>
                </li>
                <li>
                  <Link to="/orders" className={isActive('/orders')}>
                    My Orders
                  </Link>
                </li>
              </>
            )}

            {/* Restaurant Owner Navigation */}
            {user?.role === 'RESTAURANT_OWNER' && (
              <>
                <li>
                  <Link to="/owner" className={isActive('/owner')}>
                    My Restaurant
                  </Link>
                </li>
                <li>
                  <Link to="/restaurants" className={isActive('/restaurants')}>
                    All Restaurants
                  </Link>
                </li>
              </>
            )}

            {/* Delivery Partner Navigation */}
            {user?.role === 'DELIVERY_PARTNER' && (
              <>
                <li>
                  <Link to="/delivery-partner" className={isActive('/delivery-partner')}>
                    Partner Dashboard
                  </Link>
                </li>
              </>
            )}

            {/* Admin Navigation */}
            {user?.role === 'ADMIN' && (
              <>
                <li>
                  <Link to="/admin" className={isActive('/admin')}>
                    Admin Console
                  </Link>
                </li>
                <li>
                  <Link to="/restaurants" className={isActive('/restaurants')}>
                    Restaurants
                  </Link>
                </li>
              </>
            )}

            {/* Common Notification Link */}
            <li>
              <Link to="/notifications" className={isActive('/notifications')}>
                Notifications
              </Link>
            </li>

            {/* User role & status */}
            <li>
              <span className="user-badge">{user?.role} (ID: {user?.userId})</span>
            </li>

            <li>
              <button onClick={handleLogout} className="btn btn-outline-danger btn-sm">
                Logout
              </button>
            </li>
          </>
        ) : (
          <>
            <li>
              <Link to="/login" className={isActive('/login')}>
                Login
              </Link>
            </li>
            <li>
              <Link to="/register" className={isActive('/register')}>
                Register
              </Link>
            </li>
          </>
        )}
      </ul>
    </nav>
  );
};

export default Navbar;
