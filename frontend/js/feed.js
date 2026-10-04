/**
 * DailyBook Feed Controller
 * Manages timeline feed, explore public fallback, pagination, and user sidebar stats.
 */

class FeedController {
  constructor() {
    this.currentPage = 0;
    this.pageSize = (window.CONFIG && window.CONFIG.DEFAULT_PAGE_SIZE) || 10;
    this.currentMode = 'feed'; // 'feed' or 'explore'
    this.totalPages = 1;
    this.totalElements = 0;
    this.isLoading = false;

    this.container = null;
    this.paginationContainer = null;
    this.sidebarContainer = null;
  }

  async init() {
    this.container = document.getElementById('feed-posts-container');
    this.paginationContainer = document.getElementById('feed-pagination');
    this.sidebarContainer = document.getElementById('feed-sidebar-profile');

    const isAuthed = window.Auth && window.Auth.isAuthenticated();

    // Default to explore if guest
    if (!isAuthed) {
      this.currentMode = 'explore';
      const feedTabBtn = document.getElementById('tab-feed-btn');
      if (feedTabBtn) feedTabBtn.style.display = 'none';

      const exploreTabBtn = document.getElementById('tab-explore-btn');
      if (exploreTabBtn) exploreTabBtn.classList.add('active');

      const guestBanner = document.getElementById('guest-feed-banner');
      if (guestBanner) guestBanner.style.display = 'block';
    }

    this.attachEvents();
    this.loadSidebarStats();
    await this.loadPosts();
  }

  attachEvents() {
    // Mode switcher tabs
    const feedTab = document.getElementById('tab-feed-btn');
    const exploreTab = document.getElementById('tab-explore-btn');

    if (feedTab) {
      feedTab.addEventListener('click', () => {
        if (this.currentMode !== 'feed') {
          this.currentMode = 'feed';
          this.currentPage = 0;
          feedTab.classList.add('active');
          if (exploreTab) exploreTab.classList.remove('active');
          this.loadPosts();
        }
      });
    }

    if (exploreTab) {
      exploreTab.addEventListener('click', () => {
        if (this.currentMode !== 'explore') {
          this.currentMode = 'explore';
          this.currentPage = 0;
          exploreTab.classList.add('active');
          if (feedTab) feedTab.classList.remove('active');
          this.loadPosts();
        }
      });
    }
  }

  async loadSidebarStats() {
    if (!this.sidebarContainer) return;
    const isAuthed = window.Auth && window.Auth.isAuthenticated();

    if (!isAuthed) {
      this.sidebarContainer.innerHTML = `
        <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 24px; text-align: center;">
          <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--accent-subtle); color: var(--accent); display: inline-flex; align-items: center; justify-content: center; margin-bottom: 14px;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <h3 style="font-size: 1.1rem; font-weight: 600; margin-bottom: 6px;">Welcome to DailyBook</h3>
          <p style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 20px;">Join to build your personal timeline, follow writers, and share your perspective.</p>
          <div style="display: flex; flex-direction: column; gap: 8px;">
            <a href="register.html" class="btn btn-primary btn-sm" style="width: 100%;">Create Account</a>
            <a href="login.html" class="btn btn-outline btn-sm" style="width: 100%;">Sign In</a>
          </div>
        </div>
      `;
      return;
    }

    const cachedUser = window.Auth.getUser() || {};
    const username = cachedUser.username || 'You';
    const avatarTint = window.App.getAvatarClass(username);
    const initials = window.App.getInitials(username);

    // Initial sidebar render with cached values
    this.renderSidebarHtml(username, avatarTint, initials, '—', '—');

    // Fetch live user profile and follower counts asynchronously
    try {
      const [followers, following] = await Promise.allSettled([
        window.API.getMyFollowers(),
        window.API.getMyFollowing()
      ]);

      const followersCount = (followers.status === 'fulfilled' && Array.isArray(followers.value)) ? followers.value.length : 0;
      const followingCount = (following.status === 'fulfilled' && Array.isArray(following.value)) ? following.value.length : 0;

      this.renderSidebarHtml(username, avatarTint, initials, followersCount, followingCount);
    } catch (e) {
      console.warn('Could not load full follow counts:', e);
    }
  }

  renderSidebarHtml(username, avatarTint, initials, followersCount, followingCount) {
    if (!this.sidebarContainer) return;
    this.sidebarContainer.innerHTML = `
      <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-xs);">
        <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 20px;">
          <a href="profile.html?user=${encodeURIComponent(username)}" class="avatar avatar-lg ${avatarTint}">
            ${initials}
          </a>
          <div style="overflow: hidden;">
            <a href="profile.html?user=${encodeURIComponent(username)}" style="font-weight: 700; font-size: 1.05rem; display: block; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
              @${window.App.escapeHtml(username)}
            </a>
            <span style="font-size: 0.8125rem; color: var(--text-muted);">Writer & Thinker</span>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 12px 0; border-top: 1px solid var(--border-light); border-bottom: 1px solid var(--border-light); text-align: center; margin-bottom: 20px;">
          <a href="profile.html?user=${encodeURIComponent(username)}&tab=followers" style="color: inherit;">
            <div style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary);">${followersCount}</div>
            <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Followers</div>
          </a>
          <a href="profile.html?user=${encodeURIComponent(username)}&tab=following" style="color: inherit;">
            <div style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary);">${followingCount}</div>
            <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Following</div>
          </a>
        </div>

        <a href="create-post.html" class="btn btn-primary" style="width: 100%;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
          Write New Story
        </a>
      </div>
    `;
  }

  async loadPosts() {
    if (this.isLoading || !this.container) return;
    this.isLoading = true;

    // Show skeletons
    this.container.innerHTML = window.App.renderSkeletons(4);
    if (this.paginationContainer) this.paginationContainer.innerHTML = '';

    try {
      let pageData;
      if (this.currentMode === 'feed' && window.Auth.isAuthenticated()) {
        pageData = await window.API.getFeed(this.currentPage, this.pageSize);
      } else {
        pageData = await window.API.getPublicPosts(this.currentPage, this.pageSize);
      }

      const posts = (pageData && pageData.content) ? pageData.content : [];
      this.totalPages = (pageData && pageData.totalPages) || 1;
      this.totalElements = (pageData && pageData.totalElements) || posts.length;

      if (posts.length === 0) {
        this.renderEmptyState();
        return;
      }

      this.container.innerHTML = posts.map(post => window.App.renderPostCard(post)).join('');
      this.renderPagination();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      console.error('Failed to load posts:', err);
      this.container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon" style="color: var(--danger);">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <h3 class="empty-state-title">Unable to load stories</h3>
          <p class="empty-state-desc">${window.App.escapeHtml(err.message || 'Server error')}</p>
          <button class="btn btn-secondary btn-sm" onclick="feedController.loadPosts()">Try Again</button>
        </div>
      `;
    } finally {
      this.isLoading = false;
    }
  }

  renderEmptyState() {
    if (this.currentMode === 'feed') {
      this.container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/><path d="M6 6h10"/></svg>
          </div>
          <h3 class="empty-state-title">Your timeline is quiet</h3>
          <p class="empty-state-desc">Your feed collects stories from authors you follow and your own published entries. Follow authors or create your first story.</p>
          <div style="display: flex; justify-content: center; gap: 12px;">
            <a href="create-post.html" class="btn btn-primary btn-sm">Write Story</a>
            <button class="btn btn-secondary btn-sm" onclick="document.getElementById('tab-explore-btn').click()">Explore Community Stories</button>
          </div>
        </div>
      `;
    } else {
      this.container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/></svg>
          </div>
          <h3 class="empty-state-title">No public stories available</h3>
          <p class="empty-state-desc">There are no public stories published on the platform yet.</p>
          <a href="create-post.html" class="btn btn-primary btn-sm">Publish the First Story</a>
        </div>
      `;
    }
  }

  renderPagination() {
    if (!this.paginationContainer || this.totalPages <= 1) {
      if (this.paginationContainer) this.paginationContainer.innerHTML = '';
      return;
    }

    let buttons = '';
    
    // Prev Button
    buttons += `
      <button class="page-btn" ${this.currentPage === 0 ? 'disabled' : ''} onclick="feedController.goToPage(${this.currentPage - 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
      </button>
    `;

    // Page numbers
    for (let i = 0; i < this.totalPages; i++) {
      if (
        i === 0 || 
        i === this.totalPages - 1 || 
        (i >= this.currentPage - 1 && i <= this.currentPage + 1)
      ) {
        buttons += `
          <button class="page-btn ${i === this.currentPage ? 'active' : ''}" onclick="feedController.goToPage(${i})">
            ${i + 1}
          </button>
        `;
      } else if (
        (i === this.currentPage - 2 && i > 0) ||
        (i === this.currentPage + 2 && i < this.totalPages - 1)
      ) {
        buttons += `<span style="padding: 0 4px; color: var(--text-muted);">&hellip;</span>`;
      }
    }

    // Next Button
    buttons += `
      <button class="page-btn" ${this.currentPage >= this.totalPages - 1 ? 'disabled' : ''} onclick="feedController.goToPage(${this.currentPage + 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </button>
    `;

    this.paginationContainer.innerHTML = `
      <div class="pagination">
        ${buttons}
      </div>
      <div style="text-align: center; font-size: 0.8125rem; color: var(--text-muted); margin-top: 8px;">
        Page ${this.currentPage + 1} of ${this.totalPages} (${this.totalElements} stories)
      </div>
    `;
  }

  goToPage(page) {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadPosts();
    }
  }
}

window.feedController = new FeedController();
