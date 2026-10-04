/**
 * DailyBook User Profile Controller
 * Manages user profile display, authored stories, followers/following tabs, and follow actions.
 */

class ProfileController {
  constructor() {
    this.targetUsername = null;
    this.isOwnProfile = false;
    this.isFollowing = false;
    this.currentTab = 'posts'; // 'posts', 'followers', 'following'
    this.currentPage = 0;
    this.totalPages = 1;
    this.followersList = [];
    this.followingList = [];
  }

  async init() {
    const params = new URLSearchParams(window.location.search);
    const userParam = params.get('user');
    const tabParam = params.get('tab');

    const currentUser = window.Auth.getUser();

    if (!userParam) {
      if (currentUser && currentUser.username) {
        this.targetUsername = currentUser.username;
      } else {
        window.location.href = 'login.html?redirect=profile.html';
        return;
      }
    } else {
      this.targetUsername = userParam;
    }

    this.isOwnProfile = !!(currentUser && currentUser.username === this.targetUsername);
    if (tabParam && ['posts', 'followers', 'following'].includes(tabParam)) {
      this.currentTab = tabParam;
    }

    this.attachTabEvents();
    await this.loadProfileHeader();
    await this.loadActiveTabContent();
  }

  attachTabEvents() {
    const tabs = ['posts', 'followers', 'following'];
    tabs.forEach(tab => {
      const btn = document.getElementById(`tab-${tab}-btn`);
      if (btn) {
        btn.addEventListener('click', () => {
          if (this.currentTab !== tab) {
            this.switchTab(tab);
          }
        });
      }
    });
  }

  switchTab(tab) {
    this.currentTab = tab;
    this.currentPage = 0;

    ['posts', 'followers', 'following'].forEach(t => {
      const btn = document.getElementById(`tab-${t}-btn`);
      if (btn) {
        if (t === tab) btn.classList.add('active');
        else btn.classList.remove('active');
      }
    });

    this.loadActiveTabContent();
  }

  async loadProfileHeader() {
    const headerContainer = document.getElementById('profile-header-container');
    if (!headerContainer) return;

    document.title = `@${this.targetUsername} — DailyBook`;

    const avatarTint = window.App.getAvatarClass(this.targetUsername);
    const initials = window.App.getInitials(this.targetUsername);

    // Initial render
    this.renderHeaderUi(initials, avatarTint, '—', '—');

    // Fetch user details & follow status
    try {
      const user = await window.API.getUserByUsername(this.targetUsername);

      // If viewing own profile, fetch our followers & following counts
      if (this.isOwnProfile && window.Auth.isAuthenticated()) {
        const [followers, following] = await Promise.allSettled([
          window.API.getMyFollowers(),
          window.API.getMyFollowing()
        ]);

        this.followersList = (followers.status === 'fulfilled' && Array.isArray(followers.value)) ? followers.value : [];
        this.followingList = (following.status === 'fulfilled' && Array.isArray(following.value)) ? following.value : [];

        this.renderHeaderUi(initials, avatarTint, this.followersList.length, this.followingList.length);
      } else if (window.Auth.isAuthenticated()) {
        // Viewing someone else's profile: check if logged-in user follows this target
        try {
          const myFollowing = await window.API.getMyFollowing();
          this.isFollowing = Array.isArray(myFollowing) && myFollowing.includes(this.targetUsername);
        } catch {
          this.isFollowing = false;
        }

        // Hide followers/following tabs for other users if backend restricts lists to current user
        const followersTabBtn = document.getElementById('tab-followers-btn');
        const followingTabBtn = document.getElementById('tab-following-btn');
        if (followersTabBtn) followersTabBtn.style.display = 'none';
        if (followingTabBtn) followingTabBtn.style.display = 'none';

        this.renderHeaderUi(initials, avatarTint, null, null);
      } else {
        // Guest viewing profile
        const followersTabBtn = document.getElementById('tab-followers-btn');
        const followingTabBtn = document.getElementById('tab-following-btn');
        if (followersTabBtn) followersTabBtn.style.display = 'none';
        if (followingTabBtn) followingTabBtn.style.display = 'none';

        this.renderHeaderUi(initials, avatarTint, null, null);
      }
    } catch (err) {
      console.error('Failed to load profile user:', err);
      headerContainer.innerHTML = `
        <div class="empty-state">
          <h2 class="empty-state-title">User Not Found</h2>
          <p class="empty-state-desc">The author @${window.App.escapeHtml(this.targetUsername)} does not exist.</p>
          <a href="feed.html" class="btn btn-primary btn-sm">Return to Feed</a>
        </div>
      `;
    }
  }

  renderHeaderUi(initials, avatarTint, followersCount, followingCount) {
    const headerContainer = document.getElementById('profile-header-container');
    if (!headerContainer) return;

    let actionBtnHtml = '';
    if (this.isOwnProfile) {
      actionBtnHtml = `
        <div style="display: flex; gap: 10px; align-items: center;">
          <a href="create-post.html" class="btn btn-primary btn-sm">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
            Write Story
          </a>
        </div>
      `;
    } else {
      if (this.isFollowing) {
        actionBtnHtml = `
          <button class="btn btn-outline btn-sm" id="profile-follow-btn">
            Following
          </button>
        `;
      } else {
        actionBtnHtml = `
          <button class="btn btn-primary btn-sm" id="profile-follow-btn">
            Follow
          </button>
        `;
      }
    }

    let statsHtml = '';
    if (followersCount !== null && followingCount !== null) {
      statsHtml = `
        <div class="profile-stats-row" style="display: flex; gap: 24px; font-size: 0.9375rem;">
          <span style="color: var(--text-secondary); cursor: pointer;" onclick="profileController.switchTab('followers')">
            <strong style="color: var(--text-primary);">${followersCount}</strong> Followers
          </span>
          <span style="color: var(--text-secondary); cursor: pointer;" onclick="profileController.switchTab('following')">
            <strong style="color: var(--text-primary);">${followingCount}</strong> Following
          </span>
        </div>
      `;
    }

    headerContainer.innerHTML = `
      <div class="profile-header-card" style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 36px 32px; display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 32px; box-shadow: var(--shadow-xs);">
        <div style="display: flex; align-items: center; gap: 20px;">
          <div class="avatar avatar-xl ${avatarTint}">${initials}</div>
          <div>
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 4px;">
              <h1 style="font-size: 1.6rem; font-weight: 700; color: var(--text-primary);">
                @${window.App.escapeHtml(this.targetUsername)}
              </h1>
              ${this.isOwnProfile ? '<span class="badge badge-public">You</span>' : ''}
            </div>
            <p style="color: var(--text-secondary); font-size: 0.9375rem; margin-bottom: 12px;">Author & Thinker on DailyBook</p>
            ${statsHtml}
          </div>
        </div>

        <div>
          ${actionBtnHtml}
        </div>
      </div>
    `;

    // Attach follow listener
    const followBtn = document.getElementById('profile-follow-btn');
    if (followBtn) {
      followBtn.addEventListener('click', () => this.handleFollowToggle());
    }
  }

  async handleFollowToggle() {
    if (!window.Auth.isAuthenticated()) {
      window.location.href = `login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
      return;
    }

    const followBtn = document.getElementById('profile-follow-btn');
    if (followBtn) followBtn.disabled = true;

    try {
      if (this.isFollowing) {
        await window.API.unfollowUser(this.targetUsername);
        this.isFollowing = false;
        window.Toast.info(`Unfollowed @${this.targetUsername}`);
      } else {
        await window.API.followUser(this.targetUsername);
        this.isFollowing = true;
        window.Toast.success(`Now following @${this.targetUsername}`);
      }
      await this.loadProfileHeader();
    } catch (err) {
      window.Toast.error(err.message || 'Follow action failed');
      if (followBtn) followBtn.disabled = false;
    }
  }

  async loadActiveTabContent() {
    const listContainer = document.getElementById('profile-content-container');
    const paginationContainer = document.getElementById('profile-pagination');
    if (!listContainer) return;

    if (this.currentTab === 'posts') {
      listContainer.innerHTML = window.App.renderSkeletons(3);
      if (paginationContainer) paginationContainer.innerHTML = '';

      try {
        const pageData = await window.API.getPostsByAuthor(this.targetUsername, this.currentPage, 10);
        const posts = (pageData && pageData.content) ? pageData.content : [];
        this.totalPages = (pageData && pageData.totalPages) || 1;

        if (posts.length === 0) {
          listContainer.innerHTML = `
            <div class="empty-state">
              <div class="empty-state-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/></svg>
              </div>
              <h3 class="empty-state-title">No stories yet</h3>
              <p class="empty-state-desc">@${window.App.escapeHtml(this.targetUsername)} has not published any stories yet.</p>
              ${this.isOwnProfile ? '<a href="create-post.html" class="btn btn-primary btn-sm">Write Your First Story</a>' : ''}
            </div>
          `;
          return;
        }

        listContainer.innerHTML = posts.map(post => window.App.renderPostCard(post)).join('');
        this.renderPagination();
      } catch (err) {
        listContainer.innerHTML = `<div class="empty-state"><p class="empty-state-desc">${window.App.escapeHtml(err.message || 'Error loading stories')}</p></div>`;
      }
    } else if (this.currentTab === 'followers') {
      this.renderUsernamesList(this.followersList, 'No followers yet', 'When people follow you, they will appear here.');
    } else if (this.currentTab === 'following') {
      this.renderUsernamesList(this.followingList, 'Not following anyone yet', 'Explore public stories to find inspiring authors to follow.');
    }
  }

  renderUsernamesList(usernames, emptyTitle, emptyDesc) {
    const listContainer = document.getElementById('profile-content-container');
    const paginationContainer = document.getElementById('profile-pagination');
    if (paginationContainer) paginationContainer.innerHTML = '';

    if (!usernames || usernames.length === 0) {
      listContainer.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
          </div>
          <h3 class="empty-state-title">${emptyTitle}</h3>
          <p class="empty-state-desc">${emptyDesc}</p>
        </div>
      `;
      return;
    }

    listContainer.innerHTML = `
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
        ${usernames.map(username => {
          const initials = window.App.getInitials(username);
          const tint = window.App.getAvatarClass(username);
          return `
            <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; display: flex; align-items: center; justify-content: space-between;">
              <a href="profile.html?user=${encodeURIComponent(username)}" style="display: flex; align-items: center; gap: 12px; color: inherit;">
                <div class="avatar avatar-md ${tint}">${initials}</div>
                <div>
                  <strong style="display: block; font-size: 0.9375rem;">@${window.App.escapeHtml(username)}</strong>
                  <span style="font-size: 0.8125rem; color: var(--text-muted);">View Profile</span>
                </div>
              </a>
              <a href="profile.html?user=${encodeURIComponent(username)}" class="btn btn-outline btn-sm">
                View
              </a>
            </div>
          `;
        }).join('')}
      </div>
    `;
  }

  renderPagination() {
    const paginationContainer = document.getElementById('profile-pagination');
    if (!paginationContainer || this.totalPages <= 1) return;

    let buttons = `
      <button class="page-btn" ${this.currentPage === 0 ? 'disabled' : ''} onclick="profileController.goToPage(${this.currentPage - 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
      </button>
    `;

    for (let i = 0; i < this.totalPages; i++) {
      buttons += `
        <button class="page-btn ${i === this.currentPage ? 'active' : ''}" onclick="profileController.goToPage(${i})">
          ${i + 1}
        </button>
      `;
    }

    buttons += `
      <button class="page-btn" ${this.currentPage >= this.totalPages - 1 ? 'disabled' : ''} onclick="profileController.goToPage(${this.currentPage + 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </button>
    `;

    paginationContainer.innerHTML = `<div class="pagination">${buttons}</div>`;
  }

  goToPage(page) {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadActiveTabContent();
    }
  }
}

window.profileController = new ProfileController();
