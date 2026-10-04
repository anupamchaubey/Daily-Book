/**
 * DailyBook Authentication & Session Manager
 * Manages JWT tokens, user state, route guards, and session life cycle.
 */

const Auth = {
  TOKEN_KEY: 'dailybook_token',
  USER_KEY: 'dailybook_user',
  EXPIRES_KEY: 'dailybook_expires_at',

  /**
   * Check if a valid, unexpired token exists
   */
  isAuthenticated() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return false;

    // Check expiration timestamp if stored
    const expiresAt = localStorage.getItem(this.EXPIRES_KEY);
    if (expiresAt) {
      const expTime = parseInt(expiresAt, 10);
      if (!isNaN(expTime) && Date.now() >= expTime) {
        this.clearSession();
        return false;
      }
    }

    return true;
  },

  /**
   * Get cached user object
   */
  getUser() {
    try {
      const userStr = localStorage.getItem(this.USER_KEY);
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  /**
   * Get the current JWT token
   */
  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  /**
   * Fetch latest profile from backend and update cache
   */
  async fetchCurrentUser() {
    if (!this.isAuthenticated()) return null;
    try {
      const user = await window.API.getCurrentUser();
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      window.dispatchEvent(new CustomEvent('auth:userUpdated', { detail: user }));
      return user;
    } catch (err) {
      if (err.status === 401) {
        this.clearSession();
      }
      return null;
    }
  },

  /**
   * Process login flow
   */
  async login(username, password) {
    const response = await window.API.login({ username, password });
    if (!response || !response.token) {
      throw new Error('Invalid response from authentication server');
    }

    localStorage.setItem(this.TOKEN_KEY, response.token);
    if (response.expiresAt) {
      localStorage.setItem(this.EXPIRES_KEY, response.expiresAt.toString());
    }

    // Now fetch full user profile
    try {
      const user = await window.API.getCurrentUser();
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    } catch {
      // Fallback minimal user object if profile fetch fails
      localStorage.setItem(this.USER_KEY, JSON.stringify({ username }));
    }

    window.dispatchEvent(new CustomEvent('auth:stateChanged', { detail: { authenticated: true } }));
    return response;
  },

  /**
   * Process registration flow
   */
  async register(username, email, password) {
    return await window.API.register({ username, email, password });
  },

  /**
   * Clear session and broadcast change
   */
  clearSession() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.EXPIRES_KEY);
    window.dispatchEvent(new CustomEvent('auth:stateChanged', { detail: { authenticated: false } }));
  },

  /**
   * Standard user logout
   */
  logout(redirectUrl = 'index.html') {
    this.clearSession();
    if (redirectUrl) {
      window.location.href = redirectUrl;
    }
  },

  /**
   * Handle session expiry (called from API client upon 401)
   */
  handleSessionExpired() {
    this.clearSession();
    if (typeof window.Toast !== 'undefined') {
      window.Toast.warning('Your session has expired. Please log in again.');
    }
    // Only redirect if on protected page
    const protectedPages = ['create-post.html', 'feed.html'];
    const currentPath = window.location.pathname;
    if (protectedPages.some(page => currentPath.endsWith(page))) {
      setTimeout(() => {
        window.location.href = `login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
      }, 1200);
    }
  },

  /**
   * Route guard: redirect to login if not authenticated
   */
  requireAuth(redirectAfter = null) {
    if (!this.isAuthenticated()) {
      const target = redirectAfter || (window.location.pathname + window.location.search);
      window.location.href = `login.html?redirect=${encodeURIComponent(target)}`;
      return false;
    }
    return true;
  },

  /**
   * Route guard: redirect to feed/home if already logged in (for login/register pages)
   */
  requireGuest(redirect = 'feed.html') {
    if (this.isAuthenticated()) {
      window.location.href = redirect;
      return false;
    }
    return true;
  }
};

window.Auth = Auth;
