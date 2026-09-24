import axios from 'axios';

// Base URL configuration:
// - In development: leave VITE_API_BASE_URL empty so requests are relative (/api/v1/...)
//   and go through the Vite dev proxy (configured in vite.config.js) to avoid CORS issues.
// - In production: set VITE_API_BASE_URL to the API Gateway URL (e.g. http://localhost:8080).
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach Authorization header if token exists
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('foodflow_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401 and 403 errors appropriately
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response ? error.response.status : null;
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';

    if (status === 401) {
      // Clear token and redirect to login if session is invalid
      localStorage.removeItem('foodflow_token');
      localStorage.removeItem('foodflow_user');
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }

    return Promise.reject({
      status,
      message,
      data: error.response?.data,
      rawError: error,
    });
  }
);

// ==========================================
// 1. Authentication APIs
// ==========================================
export const authApi = {
  login: (data) => client.post('/api/v1/auth/login', data),
  register: (data) => client.post('/api/v1/auth/register', data),
};

// ==========================================
// 2. User Management APIs
// ==========================================
export const userApi = {
  getUserById: (id) => client.get(`/api/v1/users/${id}`),
  getAllUsers: () => client.get('/api/v1/users'),
  updateUser: (id, data) => client.put(`/api/v1/users/${id}`, data),
  deleteUser: (id) => client.delete(`/api/v1/users/${id}`),
};

// ==========================================
// 3. Restaurant APIs
// ==========================================
export const restaurantApi = {
  getAll: () => client.get('/api/v1/restaurants'),
  getById: (id) => client.get(`/api/v1/restaurants/${id}`),
  checkAvailability: (id) => client.get(`/api/v1/restaurants/${id}/availability`),
  searchByCity: (city) => client.get('/api/v1/restaurants/search/city', { params: { city } }),
  searchByName: (name) => client.get('/api/v1/restaurants/search/name', { params: { name } }),
  create: (data) => client.post('/api/v1/restaurants', data),
  update: (id, data) => client.put(`/api/v1/restaurants/${id}`, data),
  delete: (id) => client.delete(`/api/v1/restaurants/${id}`),
};

// ==========================================
// 4. Menu Item APIs
// ==========================================
export const menuApi = {
  getByRestaurant: (restaurantId) => client.get(`/api/v1/restaurants/${restaurantId}/menu-items`),
  getById: (menuItemId) => client.get(`/api/v1/menu-items/${menuItemId}`),
  create: (restaurantId, data) => client.post(`/api/v1/restaurants/${restaurantId}/menu-items`, data),
  update: (menuItemId, data) => client.put(`/api/v1/menu-items/${menuItemId}`, data),
  delete: (menuItemId) => client.delete(`/api/v1/menu-items/${menuItemId}`),
};

// ==========================================
// 5. Order APIs
// ==========================================
export const orderApi = {
  create: (data) => client.post('/api/v1/order', data),
  getById: (orderId) => client.get(`/api/v1/order/${orderId}`),
  getByUser: (userId) => client.get(`/api/v1/order/user/${userId}`),
  getStatus: (orderId) => client.get(`/api/v1/order/${orderId}/status`),
  updateStatus: (orderId, status) => client.patch(`/api/v1/order/${orderId}/status`, { status }),
};

// ==========================================
// 6. Payment APIs
// ==========================================
export const paymentApi = {
  create: (data) => client.post('/api/v1/payments', data),
  process: (paymentId) => client.post(`/api/v1/payments/${paymentId}/process`),
  getById: (paymentId) => client.get(`/api/v1/payments/${paymentId}`),
  getByOrderId: (orderId) => client.get(`/api/v1/payments/order/${orderId}`),
};

// ==========================================
// 7. Delivery & Delivery Partner APIs
// ==========================================
export const deliveryPartnerApi = {
  create: (data) => client.post('/api/v1/delivery-partners', data),
  getById: (partnerId) => client.get(`/api/v1/delivery-partners/${partnerId}`),
  getAll: () => client.get('/api/v1/delivery-partners'),
  update: (partnerId, data) => client.put(`/api/v1/delivery-partners/${partnerId}`, data),
  updateStatus: (partnerId, status) =>
    client.put(`/api/v1/delivery-partners/${partnerId}/status`, null, { params: { status } }),
  updateOwnStatus: (status) =>
    client.put('/api/v1/delivery-partners/my-status', null, { params: { status } }),
  delete: (partnerId) => client.delete(`/api/v1/delivery-partners/${partnerId}`),
};

export const deliveryApi = {
  create: (data) => client.post('/api/v1/deliveries', data),
  getById: (deliveryId) => client.get(`/api/v1/deliveries/${deliveryId}`),
  getAll: () => client.get('/api/v1/deliveries'),
  getByOrderId: (orderId) => client.get(`/api/v1/deliveries/order/${orderId}`),
  updateStatus: (deliveryId, status) =>
    client.put(`/api/v1/deliveries/${deliveryId}/status`, null, { params: { status } }),
};

// ==========================================
// 8. Inventory APIs
// ==========================================
export const inventoryApi = {
  create: (data) => client.post('/api/v1/inventory', data),
  getById: (id) => client.get(`/api/v1/inventory/${id}`),
  getByMenuItem: (menuItemId) => client.get(`/api/v1/inventory/menu-item/${menuItemId}`),
  getAll: () => client.get('/api/v1/inventory'),
  update: (id, data) => client.put(`/api/v1/inventory/${id}`, data),
  delete: (id) => client.delete(`/api/v1/inventory/${id}`),
  checkAvailability: (data) => client.post('/api/v1/inventory/check-availability', data),
};

// ==========================================
// 9. Review APIs
// ==========================================
export const reviewApi = {
  create: (data) => client.post('/api/v1/review', data),
  getById: (id) => client.get(`/api/v1/review/${id}`),
  getByRestaurant: (restaurantId) => client.get(`/api/v1/review/restaurants/${restaurantId}`),
  getByUser: (userId) => client.get(`/api/v1/review/users/${userId}`),
  update: (id, data) => client.put(`/api/v1/review/${id}`, data),
  delete: (id) => client.delete(`/api/v1/review/${id}`),
};

// ==========================================
// 10. Notification APIs
// ==========================================
export const notificationApi = {
  getById: (id) => client.get(`/api/v1/notification/${id}`),
  getByUser: (userId) => client.get(`/api/v1/notification/users/${userId}`),
  getByOrder: (orderId) => client.get(`/api/v1/notification/orders/${orderId}`),
};

export default client;
