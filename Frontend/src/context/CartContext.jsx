import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
  const [restaurant, setRestaurant] = useState(null); // { id, name }
  const [items, setItems] = useState([]); // [{ menuItemId, name, price, quantity }]

  useEffect(() => {
    try {
      const savedCart = localStorage.getItem('foodflow_cart');
      if (savedCart) {
        const parsed = JSON.parse(savedCart);
        setRestaurant(parsed.restaurant || null);
        setItems(parsed.items || []);
      }
    } catch (e) {
      console.error('Failed to load cart from storage', e);
    }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem('foodflow_cart', JSON.stringify({ restaurant, items }));
    } catch (e) {
      console.error('Failed to save cart to storage', e);
    }
  }, [restaurant, items]);

  const addItem = (restInfo, menuItem) => {
    // If cart has items from another restaurant, reset or warn
    if (restaurant && restaurant.id !== restInfo.id && items.length > 0) {
      const confirmReset = window.confirm(
        `Your cart contains items from "${restaurant.name}". Would you like to clear your cart to add items from "${restInfo.name}"?`
      );
      if (!confirmReset) return false;
      setRestaurant({ id: restInfo.id, name: restInfo.name });
      setItems([{ menuItemId: menuItem.id, name: menuItem.name, price: menuItem.price, quantity: 1 }]);
      return true;
    }

    setRestaurant({ id: restInfo.id, name: restInfo.name });
    setItems((prevItems) => {
      const existing = prevItems.find((item) => item.menuItemId === menuItem.id);
      if (existing) {
        return prevItems.map((item) =>
          item.menuItemId === menuItem.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      } else {
        return [...prevItems, { menuItemId: menuItem.id, name: menuItem.name, price: menuItem.price, quantity: 1 }];
      }
    });
    return true;
  };

  const updateQuantity = (menuItemId, delta) => {
    setItems((prevItems) => {
      const updated = prevItems
        .map((item) => {
          if (item.menuItemId === menuItemId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean);

      if (updated.length === 0) {
        setRestaurant(null);
      }
      return updated;
    });
  };

  const removeItem = (menuItemId) => {
    setItems((prevItems) => {
      const updated = prevItems.filter((item) => item.menuItemId !== menuItemId);
      if (updated.length === 0) {
        setRestaurant(null);
      }
      return updated;
    });
  };

  const clearCart = () => {
    setRestaurant(null);
    setItems([]);
    localStorage.removeItem('foodflow_cart');
  };

  const totalAmount = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const totalItemCount = items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <CartContext.Provider
      value={{
        restaurant,
        items,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
        totalAmount: Number(totalAmount.toFixed(2)),
        totalItemCount,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
