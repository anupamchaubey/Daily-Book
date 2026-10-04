/**
 * DailyBook Frontend Configuration
 * Centralized settings for backend API connectivity.
 */

const CONFIG = {
  // Default Spring Boot backend URL
  DEFAULT_API_BASE_URL: 'http://localhost:8080',

  // Retrieves the active API base URL (checks localStorage override, window override, or fallback)
  get API_BASE_URL() {
    return (
      (typeof window !== 'undefined' && window.DAILYBOOK_CONFIG && window.DAILYBOOK_CONFIG.API_BASE_URL) ||
      (typeof localStorage !== 'undefined' && localStorage.getItem('dailybook_api_url')) ||
      this.DEFAULT_API_BASE_URL
    ).replace(/\/+$/, ''); // Strip trailing slashes
  },

  // Allows dynamic reconfiguration (e.g. from developer settings or console)
  setApiBaseUrl(url) {
    if (url && typeof url === 'string') {
      localStorage.setItem('dailybook_api_url', url.trim().replace(/\/+$/, ''));
    } else {
      localStorage.removeItem('dailybook_api_url');
    }
  },

  APP_NAME: 'DailyBook',
  TAGLINE: 'Write. Reflect. Connect.',
  STORAGE_KEYS: {
    TOKEN: 'dailybook_token',
    USER: 'dailybook_user',
    THEME: 'dailybook_theme',
    EXPIRES_AT: 'dailybook_expires_at'
  },
  DEFAULT_PAGE_SIZE: 10
};

// Export to window for vanilla JS modularity
if (typeof window !== 'undefined') {
  window.CONFIG = CONFIG;
}
