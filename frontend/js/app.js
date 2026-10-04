/**
 * DailyBook Shared Application Layer
 * Global Navigation, Dark Mode, Toasts, Safe Markdown Parser, Date Formatting
 */

// ==========================================================================
// Toast Notification Engine
// ==========================================================================
class ToastEngine {
  constructor() {
    this.container = null;
    this.init();
  }

  init() {
    if (!document.body) {
      document.addEventListener('DOMContentLoaded', () => this.init());
      return;
    }
    let container = document.getElementById('dailybook-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'dailybook-toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    this.container = container;
  }

  show(message, type = 'info', duration = 4000) {
    if (!this.container) this.init();

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    // SVG icon mapping
    let iconSvg = '';
    if (type === 'success') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>';
    } else if (type === 'error') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>';
    } else if (type === 'warning') {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>';
    } else {
      iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="12 16v-4"/><path d="12 8h.01"/></svg>';
    }

    toast.innerHTML = `
      <div class="toast-icon">${iconSvg}</div>
      <div class="toast-content">${App.escapeHtml(message)}</div>
      <button class="toast-close" aria-label="Close">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
      </button>
    `;

    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.addEventListener('click', () => this.dismiss(toast));

    this.container.appendChild(toast);

    if (duration > 0) {
      setTimeout(() => this.dismiss(toast), duration);
    }
  }

  dismiss(toast) {
    if (!toast || !toast.parentNode) return;
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(12px)';
    setTimeout(() => {
      if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, 200);
  }

  success(msg, duration) { this.show(msg, 'success', duration); }
  error(msg, duration) { this.show(msg, 'error', duration); }
  warning(msg, duration) { this.show(msg, 'warning', duration); }
  info(msg, duration) { this.show(msg, 'info', duration); }
}

window.Toast = new ToastEngine();

// ==========================================================================
// Core App Utilities & UI Helpers
// ==========================================================================
const App = {
  /**
   * Escape HTML to prevent XSS injection
   */
  escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  },

  /**
   * Safe Markdown parser: escapes raw HTML, then transforms safe markdown syntax
   */
  renderMarkdown(rawText) {
    if (!rawText) return '';

    // Step 1: Escape HTML entities strictly
    let text = this.escapeHtml(rawText);

    // Step 2: Code blocks (```lang\ncode\n```)
    text = text.replace(/```([\w-]*)\n([\s\S]*?)```/g, (match, lang, code) => {
      return `<pre><code class="language-${lang || 'plaintext'}">${code.trim()}</code></pre>`;
    });

    // Step 3: Inline code (`code`)
    text = text.replace(/`([^`]+)`/g, '<code>$1</code>');

    // Step 4: Headers (#, ##, ###)
    text = text.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    text = text.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    text = text.replace(/^# (.*$)/gim, '<h1>$1</h1>');

    // Step 5: Blockquotes (> quote)
    text = text.replace(/^\> (.*$)/gim, '<blockquote>$1</blockquote>');

    // Step 6: Bold and Italic
    text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>');

    // Step 7: Safe links [text](https://...)
    text = text.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+|\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

    // Step 8: Bullet lists (- or *)
    text = text.replace(/^\s*[-*]\s+(.*)$/gim, '<li>$1</li>');
    text = text.replace(/(<li>.*<\/li>)/gms, '<ul>$1</ul>');
    // Clean nested ul artifacts
    text = text.replace(/<\/ul>\s*<ul>/g, '');

    // Step 9: Paragraphs for remaining text blocks
    const lines = text.split(/\n{2,}/);
    text = lines.map(block => {
      const trimmed = block.trim();
      if (!trimmed) return '';
      if (/^<(h1|h2|h3|pre|blockquote|ul|ol|li)/i.test(trimmed)) {
        return trimmed;
      }
      return `<p>${trimmed.replace(/\n/g, '<br>')}</p>`;
    }).join('\n');

    return text;
  },

  /**
   * Calculate human-friendly reading time
   */
  getReadingTime(text) {
    if (!text) return '1 min read';
    const words = text.trim().split(/\s+/).length;
    const minutes = Math.max(1, Math.ceil(words / 200));
    return `${minutes} min read`;
  },

  /**
   * Format ISO date strings into clean editorial dates (e.g. "Oct 4, 2026")
   */
  formatDate(isoString) {
    if (!isoString) return '';
    try {
      const date = new Date(isoString);
      if (isNaN(date.getTime())) return '';

      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffSecs = Math.floor(diffMs / 1000);
      const diffHours = Math.floor(diffSecs / 3600);
      const diffDays = Math.floor(diffHours / 24);

      if (diffSecs < 60) return 'Just now';
      if (diffHours < 1) return `${Math.floor(diffSecs / 60)}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;

      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
      });
    } catch {
      return '';
    }
  },

  /**
   * Deterministic avatar color gradient class based on username
   */
  getAvatarClass(username) {
    if (!username) return 'avatar-tint-0';
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
      hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % 8;
    return `avatar-tint-${index}`;
  },

  /**
   * Get user initial(s)
   */
  getInitials(name) {
    if (!name) return 'U';
    return name.slice(0, 2).toUpperCase();
  },

  /**
   * Accessible confirmation dialog modal
   */
  confirmModal({ title = 'Confirm Action', message = 'Are you sure?', confirmText = 'Confirm', cancelText = 'Cancel', isDanger = false }) {
    return new Promise((resolve) => {
      const modal = document.createElement('div');
      modal.className = 'modal-backdrop';
      modal.innerHTML = `
        <div class="modal-box" role="dialog" aria-modal="true">
          <div class="modal-header">
            <h3 class="modal-title">${this.escapeHtml(title)}</h3>
          </div>
          <div class="modal-body">${this.escapeHtml(message)}</div>
          <div class="modal-actions">
            <button class="btn btn-secondary modal-cancel-btn">${this.escapeHtml(cancelText)}</button>
            <button class="btn ${isDanger ? 'btn-danger' : 'btn-primary'} modal-confirm-btn">${this.escapeHtml(confirmText)}</button>
          </div>
        </div>
      `;

      document.body.appendChild(modal);
      requestAnimationFrame(() => modal.classList.add('open'));

      const cleanup = (result) => {
        modal.classList.remove('open');
        setTimeout(() => {
          if (modal.parentNode) modal.parentNode.removeChild(modal);
          resolve(result);
        }, 200);
      };

      modal.querySelector('.modal-confirm-btn').addEventListener('click', () => cleanup(true));
      modal.querySelector('.modal-cancel-btn').addEventListener('click', () => cleanup(false));
      modal.addEventListener('click', (e) => {
        if (e.target === modal) cleanup(false);
      });
    });
  },

  /**
   * Theme Management (Light / Dark)
   */
  initTheme() {
    const savedTheme = localStorage.getItem('dailybook_theme');
    const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initialTheme = savedTheme || (systemPrefersDark ? 'dark' : 'light');

    document.documentElement.setAttribute('data-theme', initialTheme);
    this.updateThemeToggleIcon(initialTheme);
  },

  toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('dailybook_theme', next);
    this.updateThemeToggleIcon(next);
  },

  updateThemeToggleIcon(theme) {
    const buttons = document.querySelectorAll('.theme-toggle-btn');
    buttons.forEach(btn => {
      if (theme === 'dark') {
        btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/></svg>`;
        btn.setAttribute('aria-label', 'Switch to light mode');
      } else {
        btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>`;
        btn.setAttribute('aria-label', 'Switch to dark mode');
      }
    });
  },

  /**
   * Render Standard Site Navbar
   */
  renderHeader(activeNav = '') {
    const header = document.querySelector('.site-header');
    if (!header) return;

    const isAuthed = window.Auth && window.Auth.isAuthenticated();
    const user = (window.Auth && window.Auth.getUser()) || {};
    const username = user.username || 'You';

    header.innerHTML = `
      <div class="container nav-container">
        <!-- Logo -->
        <a href="index.html" class="nav-brand">
          <div class="brand-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/>
              <path d="M6 6h10"/>
              <path d="M6 10h10"/>
            </svg>
          </div>
          <span>DailyBook</span>
        </a>

        <!-- Search Bar -->
        <div class="nav-search">
          <svg class="nav-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
          </svg>
          <input type="text" class="nav-search-input" placeholder="Search stories, topics, authors..." id="global-search-input" />
          <span class="nav-search-kbd">/</span>
        </div>

        <!-- Navigation Links -->
        <div class="nav-actions">
          <div class="nav-links-desktop">
            <a href="feed.html" class="nav-link ${activeNav === 'feed' ? 'active' : ''}">Feed</a>
            <a href="search.html" class="nav-link ${activeNav === 'search' ? 'active' : ''}">Explore</a>
            ${isAuthed ? `
              <a href="create-post.html" class="btn btn-sm btn-outline">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
                Write
              </a>
            ` : ''}
          </div>

          <!-- Theme Toggle -->
          <button class="btn-icon theme-toggle-btn" title="Toggle color theme">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/></svg>
          </button>

          <!-- User Authentication Section -->
          ${isAuthed ? `
            <div class="dropdown" id="nav-user-dropdown">
              <button class="user-menu-trigger" id="user-menu-btn" aria-label="User menu">
                <div class="avatar avatar-sm ${this.getAvatarClass(username)}">${this.getInitials(username)}</div>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
              </button>
              <div class="dropdown-menu" id="user-menu-list">
                <a href="profile.html?user=${encodeURIComponent(username)}" class="dropdown-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="5"/><path d="M20 21a8 8 0 0 0-16 0"/></svg>
                  My Profile (@${this.escapeHtml(username)})
                </a>
                <a href="create-post.html" class="dropdown-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
                  New Story
                </a>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item" id="nav-logout-btn" style="color: var(--danger); width: 100%; border: none; background: none; text-align: left;">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                  Sign Out
                </button>
              </div>
            </div>
          ` : `
            <a href="login.html" class="btn btn-sm btn-ghost">Log in</a>
            <a href="register.html" class="btn btn-sm btn-primary">Start Writing</a>
          `}

          <!-- Mobile Menu Button -->
          <button class="mobile-menu-btn" id="mobile-menu-toggle" aria-label="Toggle navigation">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="18" x2="20" y2="18"/></svg>
          </button>
        </div>
      </div>

      <!-- Mobile Navigation Drawer -->
      <div class="mobile-drawer" id="mobile-nav-drawer">
        <a href="index.html" class="nav-link">Home</a>
        <a href="feed.html" class="nav-link">Feed</a>
        <a href="search.html" class="nav-link">Search & Explore</a>
        ${isAuthed ? `
          <a href="create-post.html" class="nav-link">Write a Story</a>
          <a href="profile.html?user=${encodeURIComponent(username)}" class="nav-link">My Profile (@${this.escapeHtml(username)})</a>
          <div class="dropdown-divider"></div>
          <button class="btn btn-danger-outline btn-sm" id="mobile-logout-btn" style="width: 100%;">Sign Out</button>
        ` : `
          <div style="display: flex; gap: 10px; margin-top: 12px;">
            <a href="login.html" class="btn btn-secondary" style="flex: 1;">Log In</a>
            <a href="register.html" class="btn btn-primary" style="flex: 1;">Register</a>
          </div>
        `}
      </div>
    `;

    // Attach Header Listeners
    this.attachHeaderListeners();
  },

  attachHeaderListeners() {
    // Theme toggle
    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
      btn.addEventListener('click', () => this.toggleTheme());
    });

    // Global Search Bar Input
    const searchInput = document.getElementById('global-search-input');
    if (searchInput) {
      searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && searchInput.value.trim()) {
          window.location.href = `search.html?q=${encodeURIComponent(searchInput.value.trim())}`;
        }
      });
    }

    // Keyboard shortcut '/' to focus search
    window.addEventListener('keydown', (e) => {
      if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
        e.preventDefault();
        const input = document.getElementById('global-search-input');
        if (input) input.focus();
      }
    });

    // User Dropdown toggle
    const userBtn = document.getElementById('user-menu-btn');
    const userMenu = document.getElementById('user-menu-list');
    if (userBtn && userMenu) {
      userBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        userMenu.classList.toggle('show');
      });
      document.addEventListener('click', () => userMenu.classList.remove('show'));
    }

    // Logout actions
    const logoutBtn = document.getElementById('nav-logout-btn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        window.Auth.logout('index.html');
      });
    }

    const mobileLogoutBtn = document.getElementById('mobile-logout-btn');
    if (mobileLogoutBtn) {
      mobileLogoutBtn.addEventListener('click', () => {
        window.Auth.logout('index.html');
      });
    }

    // Mobile menu toggle
    const mobileBtn = document.getElementById('mobile-menu-toggle');
    const mobileDrawer = document.getElementById('mobile-nav-drawer');
    if (mobileBtn && mobileDrawer) {
      mobileBtn.addEventListener('click', () => {
        mobileDrawer.classList.toggle('open');
      });
    }
  },

  /**
   * Render Standard Site Footer
   */
  renderFooter() {
    const footer = document.querySelector('.site-footer');
    if (!footer) return;

    footer.innerHTML = `
      <div class="container">
        <div class="footer-grid">
          <div>
            <div class="nav-brand">
              <div class="brand-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/>
                  <path d="M6 6h10"/>
                  <path d="M6 10h10"/>
                </svg>
              </div>
              <span>DailyBook</span>
            </div>
            <p class="footer-brand-tagline">Write. Reflect. Connect. A calm, editorial personal publishing platform.</p>
          </div>
          <div>
            <h4 class="footer-heading">Platform</h4>
            <ul class="footer-links">
              <li><a href="feed.html" class="footer-link">Feed</a></li>
              <li><a href="search.html" class="footer-link">Explore Stories</a></li>
              <li><a href="create-post.html" class="footer-link">Write a Story</a></li>
            </ul>
          </div>
          <div>
            <h4 class="footer-heading">Community</h4>
            <ul class="footer-links">
              <li><a href="register.html" class="footer-link">Join DailyBook</a></li>
              <li><a href="login.html" class="footer-link">Sign In</a></li>
            </ul>
          </div>
          <div>
            <h4 class="footer-heading">Technology</h4>
            <ul class="footer-links">
              <li><span class="footer-link" style="color: var(--text-muted)">Java 17 & Spring Boot 3</span></li>
              <li><span class="footer-link" style="color: var(--text-muted)">MongoDB & JWT</span></li>
              <li><span class="footer-link" style="color: var(--text-muted)">HTML5 / CSS3 / Vanilla JS</span></li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <span>&copy; ${new Date().getFullYear()} DailyBook. All rights reserved.</span>
          <span>Crafted for thoughtful minds.</span>
        </div>
      </div>
    `;
  },

  /**
   * Helper to render a Post Card HTML string
   */
  renderPostCard(post) {
    const author = post.authorId || 'Anonymous';
    const initials = this.getInitials(author);
    const avatarTint = this.getAvatarClass(author);
    const dateFormatted = this.formatDate(post.createdAt);
    const readingTime = this.getReadingTime(post.content);

    // Visibility Badge
    let visibilityBadge = '';
    if (post.visibility === 'PUBLIC') {
      visibilityBadge = '<span class="badge badge-public">Public</span>';
    } else if (post.visibility === 'FOLLOWERS_ONLY') {
      visibilityBadge = '<span class="badge badge-followers">Followers</span>';
    } else if (post.visibility === 'PRIVATE') {
      visibilityBadge = '<span class="badge badge-private">Private</span>';
    }

    // Tags
    const tagsHtml = (post.tags && Array.isArray(post.tags) && post.tags.length > 0)
      ? post.tags.map(tag => `<a href="search.html?q=${encodeURIComponent(tag)}" class="tag-pill">#${this.escapeHtml(tag)}</a>`).join('')
      : '';

    // Excerpt (stripped of markdown headers/symbols)
    const rawExcerpt = (post.content || '').replace(/[#*`>]/g, '').trim();
    const excerpt = rawExcerpt.length > 210 ? rawExcerpt.slice(0, 210) + '...' : rawExcerpt;

    return `
      <article class="post-card" data-post-id="${post.id}">
        <div class="post-card-header">
          <div class="post-author-info">
            <a href="profile.html?user=${encodeURIComponent(author)}" class="avatar avatar-sm ${avatarTint}">
              ${initials}
            </a>
            <div>
              <a href="profile.html?user=${encodeURIComponent(author)}" class="post-author-name">
                @${this.escapeHtml(author)}
              </a>
              <div class="post-meta-sub">
                <span>${dateFormatted}</span>
                <span class="post-dot-sep">&middot;</span>
                <span>${readingTime}</span>
              </div>
            </div>
          </div>
          <div>${visibilityBadge}</div>
        </div>

        <h3 class="post-card-title">
          <a href="post.html?id=${encodeURIComponent(post.id)}">${this.escapeHtml(post.title)}</a>
        </h3>

        <p class="post-card-excerpt">${this.escapeHtml(excerpt)}</p>

        <div class="post-card-footer">
          <div class="post-tags-list">${tagsHtml}</div>
          <a href="post.html?id=${encodeURIComponent(post.id)}" class="post-read-more">
            Read story
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
          </a>
        </div>
      </article>
    `;
  },

  /**
   * Helper to render Skeleton post cards while loading
   */
  renderSkeletons(count = 3) {
    let html = '';
    for (let i = 0; i < count; i++) {
      html += `
        <div class="skeleton-card">
          <div style="display: flex; align-items: center; gap: 10px;">
            <div class="skeleton skeleton-avatar"></div>
            <div style="flex: 1; display: flex; flex-direction: column; gap: 6px;">
              <div class="skeleton skeleton-text" style="width: 120px;"></div>
              <div class="skeleton skeleton-text-short"></div>
            </div>
          </div>
          <div class="skeleton skeleton-title"></div>
          <div class="skeleton skeleton-text"></div>
          <div class="skeleton skeleton-text" style="width: 85%;"></div>
        </div>
      `;
    }
    return html;
  }
};

window.App = App;

// Auto initialize themes on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  App.initTheme();
});
