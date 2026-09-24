import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return <div className="container state-box">Checking authentication...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    return (
      <div className="container">
        <div className="alert alert-danger" style={{ marginTop: 20 }}>
          <strong>Access Denied:</strong> Your role ({role}) does not have permission to view this page. Required role(s): {allowedRoles.join(', ')}.
        </div>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;
