/**
 * AuthContext.js - Global authentication state management using React Context API.
 *
 * React Context allows us to share data (like the logged-in user info) across
 * all components without "prop drilling" (passing data through every component level).
 *
 * How it works:
 * 1. AuthProvider wraps the entire app
 * 2. Any component can call useAuth() to get the current user
 * 3. When user logs in, all components automatically see the updated user info
 *
 * We store user info in localStorage so the user stays logged in even after
 * refreshing the page.
 */
import React, { createContext, useContext, useState, useEffect } from 'react';

// Create the context object
// This is like creating a "container" that will hold user data
const AuthContext = createContext(null);

/**
 * AuthProvider - Wraps the entire application to provide auth state.
 * All child components can access the user data through useAuth().
 */
export const AuthProvider = ({ children }) => {
  // user: Holds the logged-in user's data (id, name, email, role)
  // useState(null) means initially there's no logged-in user
  const [user, setUser] = useState(null);

  /**
   * useEffect runs once when the app first loads.
   * It checks if user data was saved in localStorage (from a previous session).
   * If found, it restores the user state so the user stays logged in.
   */
  useEffect(() => {
    const savedUser = localStorage.getItem('parkingUser');
    if (savedUser) {
      setUser(JSON.parse(savedUser)); // Parse the JSON string back to an object
    }
  }, []); // Empty array [] means this runs only once on component mount

  /**
   * login() - Called after successful login/registration.
   * Saves user data to state AND localStorage.
   *
   * @param userData - The user object from the backend { id, name, email, role }
   */
  const login = (userData) => {
    setUser(userData);
    // Save to localStorage so data persists after page refresh
    localStorage.setItem('parkingUser', JSON.stringify(userData));
  };

  /**
   * logout() - Called when user clicks Logout.
   * Clears user data from state and localStorage.
   */
  const logout = () => {
    setUser(null);
    localStorage.removeItem('parkingUser');
  };

  /**
   * isAdmin() - Helper to check if the current user is an admin.
   * Used to show/hide admin-only features.
   */
  const isAdmin = () => user && user.role === 'ADMIN';

  /**
   * isLoggedIn() - Helper to check if any user is currently logged in.
   */
  const isLoggedIn = () => user !== null;

  // The value object is what all components will receive when they call useAuth()
  const value = {
    user,      // The logged-in user object (or null)
    login,     // Function to log in
    logout,    // Function to log out
    isAdmin,   // Function to check admin status
    isLoggedIn // Function to check if logged in
  };

  return (
    // AuthContext.Provider makes the value available to all child components
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * useAuth() - Custom hook to access auth context in any component.
 *
 * Usage in any component:
 * const { user, login, logout, isAdmin } = useAuth();
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
