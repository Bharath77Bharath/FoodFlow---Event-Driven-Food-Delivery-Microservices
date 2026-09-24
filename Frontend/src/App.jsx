import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import RestaurantList from './pages/RestaurantList';
import RestaurantDetails from './pages/RestaurantDetails';
import CartPage from './pages/CartPage';
import MyOrders from './pages/MyOrders';
import OrderDetails from './pages/OrderDetails';
import OwnerDashboard from './pages/OwnerDashboard';
import DeliveryPartnerDashboard from './pages/DeliveryPartnerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import NotificationsPage from './pages/NotificationsPage';
import NotFound from './pages/NotFound';

const HomeRedirect = () => {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return <div className="container state-box">Loading FoodFlow...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  switch (role) {
    case 'RESTAURANT_OWNER':
      return <Navigate to="/owner" replace />;
    case 'DELIVERY_PARTNER':
      return <Navigate to="/delivery-partner" replace />;
    case 'ADMIN':
      return <Navigate to="/admin" replace />;
    case 'CUSTOMER':
    default:
      return <Navigate to="/restaurants" replace />;
  }
};

const App = () => {
  return (
    <div>
      <Navbar />
      <Routes>
        {/* Public / Landing routes */}
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Restaurant Browsing */}
        <Route
          path="/restaurants"
          element={
            <ProtectedRoute>
              <RestaurantList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/restaurants/:id"
          element={
            <ProtectedRoute>
              <RestaurantDetails />
            </ProtectedRoute>
          }
        />

        {/* Customer Flow */}
        <Route
          path="/cart"
          element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <CartPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute allowedRoles={['CUSTOMER', 'ADMIN']}>
              <MyOrders />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders/:orderId"
          element={
            <ProtectedRoute>
              <OrderDetails />
            </ProtectedRoute>
          }
        />

        {/* Restaurant Owner Flow */}
        <Route
          path="/owner"
          element={
            <ProtectedRoute allowedRoles={['RESTAURANT_OWNER', 'ADMIN']}>
              <OwnerDashboard />
            </ProtectedRoute>
          }
        />

        {/* Delivery Partner Flow */}
        <Route
          path="/delivery-partner"
          element={
            <ProtectedRoute allowedRoles={['DELIVERY_PARTNER', 'ADMIN']}>
              <DeliveryPartnerDashboard />
            </ProtectedRoute>
          }
        />

        {/* Admin Flow */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        {/* Common Notifications */}
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />

        {/* 404 Fallback */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </div>
  );
};

export default App;
