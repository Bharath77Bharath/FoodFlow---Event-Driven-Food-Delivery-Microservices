import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/client';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const storedToken = localStorage.getItem('foodflow_token');
      const storedUser = localStorage.getItem('foodflow_user');
      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      }
    } catch (e) {
      console.error('Failed to parse stored auth user', e);
      localStorage.removeItem('foodflow_token');
      localStorage.removeItem('foodflow_user');
    } finally {
      setLoading(false);
    }
  }, []);

  const login = async (email, password) => {
    const response = await authApi.login({ email, password });
    const { token: jwtToken, userId, role } = response.data;

    const userData = { userId, role, email };
    setToken(jwtToken);
    setUser(userData);

    localStorage.setItem('foodflow_token', jwtToken);
    localStorage.setItem('foodflow_user', JSON.stringify(userData));

    return userData;
  };

  const register = async (registerData) => {
    return await authApi.register(registerData);
  };

  const logout = () => {
    localStorage.removeItem('foodflow_token');
    localStorage.removeItem('foodflow_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        role: user?.role,
        userId: user?.userId,
        login,
        register,
        logout,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
