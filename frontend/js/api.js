/**
 * DailyBook Centralized API Client
 * Clean abstraction over the Spring Boot backend REST endpoints.
 * Handles headers, JWT injection, response parsing, and standard error mapping.
 */

class ApiClient {
  constructor() {
    this.tokenKey = (window.CONFIG && window.CONFIG.STORAGE_KEYS.TOKEN) || 'dailybook_token';
  }

  get baseUrl() {
    return (window.CONFIG && window.CONFIG.API_BASE_URL) || 'http://localhost:8080';
  }

  getToken() {
    return localStorage.getItem(this.tokenKey);
  }

  setToken(token) {
    if (token) {
      localStorage.setItem(this.tokenKey, token);
    } else {
      localStorage.removeItem(this.tokenKey);
    }
  }

  removeToken() {
    localStorage.removeItem(this.tokenKey);
  }

  /**
   * Core request wrapper
   */
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...options.headers
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      // Handle 204 No Content
      if (response.status === 204) {
        return null;
      }

      // Read response body safely
      let data = null;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        const text = await response.text();
        try {
          data = JSON.parse(text);
        } catch {
          data = text;
        }
      }

      if (!response.ok) {
        // Build descriptive error object
        let errorMessage = 'An unexpected error occurred';
        if (data && typeof data === 'object') {
          if (typeof data.message === 'string') {
            errorMessage = data.message;
          } else if (typeof data.message === 'object' && data.message !== null) {
            // Spring Boot validation errors map { field: errorMsg }
            errorMessage = Object.values(data.message).join(', ');
          } else if (data.error) {
            errorMessage = `${data.error}: ${data.status || response.status}`;
          }
        } else if (typeof data === 'string' && data.length > 0) {
          errorMessage = data;
        }

        const error = new Error(errorMessage);
        error.status = response.status;
        error.data = data;

        // Auto logout and clear storage on 401 unauthorized
        if (response.status === 401 && token) {
          console.warn('[ApiClient] Token expired or invalid (401). Triggering session cleanup.');
          if (window.Auth && typeof window.Auth.handleSessionExpired === 'function') {
            window.Auth.handleSessionExpired();
          }
        }

        throw error;
      }

      return data;
    } catch (err) {
      // Network failure or CORS connection issue
      if (!err.status) {
        console.error('[ApiClient] Network/Connection error:', err);
        const netErr = new Error('Cannot connect to DailyBook backend. Please verify the server is running at ' + this.baseUrl);
        netErr.status = 0;
        netErr.isNetworkError = true;
        throw netErr;
      }
      throw err;
    }
  }

  // ==========================================
  // AUTHENTICATION (/api/auth)
  // ==========================================

  /**
   * Register a new user
   * @param {Object} data - { username, email, password }
   */
  async register(data) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  /**
   * Log in user and receive JWT
   * @param {Object} data - { username, password }
   */
  async login(data) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  // ==========================================
  // USER PROFILES (/api/users)
  // ==========================================

  /**
   * Fetch authenticated user's profile
   */
  async getCurrentUser() {
    return this.request('/api/users/me', {
      method: 'GET'
    });
  }

  /**
   * Fetch public user profile by username
   */
  async getUserByUsername(username) {
    return this.request(`/api/users/${encodeURIComponent(username)}`, {
      method: 'GET'
    });
  }

  // ==========================================
  // POSTS (/api/posts)
  // ==========================================

  /**
   * Explore / list public posts paged
   */
  async getPublicPosts(page = 0, size = 10) {
    return this.request(`/api/posts?page=${page}&size=${size}`, {
      method: 'GET'
    });
  }

  /**
   * Get post by ID (authenticated or public viewer)
   */
  async getPostById(id) {
    return this.request(`/api/posts/${encodeURIComponent(id)}`, {
      method: 'GET'
    });
  }

  /**
   * Create a new post (authenticated)
   * @param {Object} postData - { title, content, tags, visibility }
   */
  async createPost(postData) {
    return this.request('/api/posts', {
      method: 'POST',
      body: JSON.stringify(postData)
    });
  }

  /**
   * Update own post
   * @param {string} id
   * @param {Object} postData - { title, content, tags, visibility }
   */
  async updatePost(id, postData) {
    return this.request(`/api/posts/${encodeURIComponent(id)}`, {
      method: 'PUT',
      body: JSON.stringify(postData)
    });
  }

  /**
   * Delete own post
   * @param {string} id
   */
  async deletePost(id) {
    return this.request(`/api/posts/${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  }

  /**
   * Get authenticated user's own posts
   */
  async getMyPosts(page = 0, size = 10) {
    return this.request(`/api/posts/me?page=${page}&size=${size}`, {
      method: 'GET'
    });
  }

  /**
   * Get author's posts (visibility filtered by viewer relationship)
   */
  async getPostsByAuthor(username, page = 0, size = 10) {
    return this.request(`/api/posts/user/${encodeURIComponent(username)}?page=${page}&size=${size}`, {
      method: 'GET'
    });
  }

  /**
   * Get personalized timeline feed (followed authors + self)
   */
  async getFeed(page = 0, size = 10) {
    return this.request(`/api/posts/feed?page=${page}&size=${size}`, {
      method: 'GET'
    });
  }

  /**
   * Search public posts by keyword
   */
  async searchPosts(query, page = 0, size = 10) {
    return this.request(`/api/posts/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`, {
      method: 'GET'
    });
  }

  // ==========================================
  // FOLLOWING (/api/follow)
  // ==========================================

  /**
   * Follow a user
   */
  async followUser(username) {
    return this.request(`/api/follow/${encodeURIComponent(username)}`, {
      method: 'POST'
    });
  }

  /**
   * Unfollow a user
   */
  async unfollowUser(username) {
    return this.request(`/api/follow/${encodeURIComponent(username)}`, {
      method: 'DELETE'
    });
  }

  /**
   * Get list of followers for authenticated user
   */
  async getMyFollowers() {
    return this.request('/api/follow/followers', {
      method: 'GET'
    });
  }

  /**
   * Get list of usernames authenticated user follows
   */
  async getMyFollowing() {
    return this.request('/api/follow/following', {
      method: 'GET'
    });
  }
}

// Global API singleton
window.API = new ApiClient();
