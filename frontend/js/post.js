/**
 * DailyBook Post Reader Controller
 * Manages article rendering, author follow/unfollow, and post owner edit/delete actions.
 */

class PostReaderController {
  constructor() {
    this.postId = null;
    this.post = null;
    this.isOwner = false;
    this.isFollowingAuthor = false;
  }

  async init() {
    const params = new URLSearchParams(window.location.search);
    this.postId = params.get('id');

    if (!this.postId) {
      window.location.href = 'feed.html';
      return;
    }

    await this.loadPost();
  }

  async loadPost() {
    const container = document.getElementById('post-reader-container');
    if (!container) return;

    // Show skeleton while loading
    container.innerHTML = `
      <div style="margin-bottom: 32px;">
        <div class="skeleton skeleton-title" style="height: 40px; margin-bottom: 20px; width: 85%;"></div>
        <div style="display: flex; align-items: center; gap: 14px; padding: 14px 0;">
          <div class="skeleton skeleton-avatar"></div>
          <div style="display: flex; flex-direction: column; gap: 6px;">
            <div class="skeleton skeleton-text" style="width: 140px;"></div>
            <div class="skeleton skeleton-text-short" style="width: 80px;"></div>
          </div>
        </div>
      </div>
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <div class="skeleton skeleton-text"></div>
        <div class="skeleton skeleton-text"></div>
        <div class="skeleton skeleton-text" style="width: 70%;"></div>
        <div class="skeleton skeleton-text" style="margin-top: 16px;"></div>
        <div class="skeleton skeleton-text"></div>
      </div>
    `;

    try {
      this.post = await window.API.getPostById(this.postId);
      const currentUsername = window.Auth.getUser()?.username;
      this.isOwner = !!(currentUsername && currentUsername === this.post.authorId);

      // Check following status if viewer is logged in and not the author
      if (window.Auth.isAuthenticated() && !this.isOwner) {
        try {
          const myFollowing = await window.API.getMyFollowing();
          this.isFollowingAuthor = Array.isArray(myFollowing) && myFollowing.includes(this.post.authorId);
        } catch {
          this.isFollowingAuthor = false;
        }
      }

      this.renderArticle();
    } catch (err) {
      console.error('Failed to load post:', err);
      let errorMsg = 'This story could not be found.';
      if (err.status === 404) {
        errorMsg = 'This story either does not exist, or you do not have permission to view it (it may be Private or Followers-Only).';
      } else if (err.status === 403) {
        errorMsg = 'Access denied. You do not have permission to view this story.';
      }
      container.innerHTML = `
        <div class="empty-state" style="padding: 72px 24px;">
          <div class="empty-state-icon" style="color: var(--danger);">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <h2 class="empty-state-title">Story Unavailable</h2>
          <p class="empty-state-desc">${window.App.escapeHtml(errorMsg)}</p>
          <div style="display: flex; justify-content: center; gap: 12px;">
            <a href="feed.html" class="btn btn-primary btn-sm">Return to Feed</a>
            ${!window.Auth.isAuthenticated() ? `<a href="login.html?redirect=post.html?id=${encodeURIComponent(this.postId)}" class="btn btn-secondary btn-sm">Log In to Check Access</a>` : ''}
          </div>
        </div>
      `;
    }
  }

  renderArticle() {
    const container = document.getElementById('post-reader-container');
    if (!container || !this.post) return;

    // Document Title
    document.title = `${this.post.title} — DailyBook`;

    const author = this.post.authorId || 'Anonymous';
    const avatarTint = window.App.getAvatarClass(author);
    const initials = window.App.getInitials(author);
    const dateFormatted = window.App.formatDate(this.post.createdAt);
    const readingTime = window.App.getReadingTime(this.post.content);

    // Tags
    const tagsHtml = (this.post.tags && Array.isArray(this.post.tags) && this.post.tags.length > 0)
      ? this.post.tags.map(tag => `<a href="search.html?q=${encodeURIComponent(tag)}" class="tag-pill">#${window.App.escapeHtml(tag)}</a>`).join('')
      : '';

    // Visibility Badge
    let visibilityBadge = '';
    if (this.post.visibility === 'PUBLIC') {
      visibilityBadge = '<span class="badge badge-public">Public</span>';
    } else if (this.post.visibility === 'FOLLOWERS_ONLY') {
      visibilityBadge = '<span class="badge badge-followers">Followers Only</span>';
    } else if (this.post.visibility === 'PRIVATE') {
      visibilityBadge = '<span class="badge badge-private">Private Journal</span>';
    }

    // Follow Button for Author
    let followBtnHtml = '';
    if (!this.isOwner) {
      if (this.isFollowingAuthor) {
        followBtnHtml = `
          <button class="btn btn-sm btn-outline follow-toggle-btn" data-author="${window.App.escapeHtml(author)}">
            Following
          </button>
        `;
      } else {
        followBtnHtml = `
          <button class="btn btn-sm btn-primary follow-toggle-btn" data-author="${window.App.escapeHtml(author)}">
            Follow
          </button>
        `;
      }
    }

    // Owner action controls (Edit / Delete)
    let ownerActionsHtml = '';
    if (this.isOwner) {
      ownerActionsHtml = `
        <div style="display: flex; align-items: center; gap: 8px;">
          <a href="create-post.html?edit=${encodeURIComponent(this.post.id)}" class="btn btn-sm btn-outline">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>
            Edit
          </a>
          <button class="btn btn-sm btn-danger-outline" id="delete-post-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
            Delete
          </button>
        </div>
      `;
    }

    // Rendered safe markdown content
    const parsedContent = window.App.renderMarkdown(this.post.content);

    container.innerHTML = `
      <!-- Back Navigation & Meta Bar -->
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;">
        <a href="feed.html" class="nav-link" style="padding-left: 0;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          Back to stories
        </a>
        <div style="display: flex; align-items: center; gap: 10px;">
          <button class="btn btn-sm btn-ghost" id="copy-story-link-btn" title="Copy link to story">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
            Share
          </button>
          ${ownerActionsHtml}
        </div>
      </div>

      <!-- Article Header -->
      <header class="article-header">
        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 14px;">
          ${visibilityBadge}
          <div style="display: flex; gap: 6px;">${tagsHtml}</div>
        </div>

        <h1 class="article-title">${window.App.escapeHtml(this.post.title)}</h1>

        <div class="article-meta-bar">
          <div class="article-author-cluster">
            <a href="profile.html?user=${encodeURIComponent(author)}" class="avatar avatar-md ${avatarTint}">
              ${initials}
            </a>
            <div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <a href="profile.html?user=${encodeURIComponent(author)}" style="font-weight: 600; font-size: 1rem; color: var(--text-primary);">
                  @${window.App.escapeHtml(author)}
                </a>
                ${followBtnHtml}
              </div>
              <div class="post-meta-sub" style="margin-top: 2px;">
                <span>Published ${dateFormatted}</span>
                <span class="post-dot-sep">&middot;</span>
                <span>${readingTime}</span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Article Main Content -->
      <article class="article-content">
        ${parsedContent}
      </article>

      <!-- Article Footer / Author Card -->
      <footer style="margin-top: 64px; padding-top: 36px; border-top: 1px solid var(--border-color);">
        <div style="background: var(--bg-surface); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 28px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 20px;">
          <div style="display: flex; align-items: center; gap: 16px;">
            <a href="profile.html?user=${encodeURIComponent(author)}" class="avatar avatar-lg ${avatarTint}">
              ${initials}
            </a>
            <div>
              <div style="font-size: 0.8125rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted);">Written by</div>
              <a href="profile.html?user=${encodeURIComponent(author)}" style="font-size: 1.15rem; font-weight: 700; color: var(--text-primary); display: block;">
                @${window.App.escapeHtml(author)}
              </a>
              <span style="font-size: 0.875rem; color: var(--text-secondary);">Author on DailyBook</span>
            </div>
          </div>

          <div style="display: flex; gap: 10px;">
            <a href="profile.html?user=${encodeURIComponent(author)}" class="btn btn-secondary btn-sm">
              View Profile
            </a>
            ${followBtnHtml}
          </div>
        </div>
      </footer>
    `;

    this.attachPostListeners();
  }

  attachPostListeners() {
    // Share / Copy Link
    const copyBtn = document.getElementById('copy-story-link-btn');
    if (copyBtn) {
      copyBtn.addEventListener('click', async () => {
        try {
          await navigator.clipboard.writeText(window.location.href);
          window.Toast.success('Story URL copied to clipboard!');
        } catch {
          window.Toast.info('Link: ' + window.location.href);
        }
      });
    }

    // Follow Toggle Buttons
    document.querySelectorAll('.follow-toggle-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (!window.Auth.isAuthenticated()) {
          window.location.href = `login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
          return;
        }

        const author = btn.getAttribute('data-author');
        btn.disabled = true;

        try {
          if (this.isFollowingAuthor) {
            await window.API.unfollowUser(author);
            this.isFollowingAuthor = false;
            window.Toast.info(`Unfollowed @${author}`);
          } else {
            await window.API.followUser(author);
            this.isFollowingAuthor = true;
            window.Toast.success(`Now following @${author}`);
          }
          // Re-render button states
          this.renderArticle();
        } catch (err) {
          window.Toast.error(err.message || 'Follow action failed');
          btn.disabled = false;
        }
      });
    });

    // Delete post button
    const deleteBtn = document.getElementById('delete-post-btn');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', async () => {
        const confirmed = await window.App.confirmModal({
          title: 'Delete Story',
          message: 'Are you sure you want to permanently delete this story? This action cannot be undone.',
          confirmText: 'Delete Story',
          cancelText: 'Cancel',
          isDanger: true
        });

        if (confirmed) {
          try {
            await window.API.deletePost(this.postId);
            window.Toast.success('Story deleted successfully');
            setTimeout(() => {
              window.location.href = 'feed.html';
            }, 500);
          } catch (err) {
            window.Toast.error(err.message || 'Failed to delete story');
          }
        }
      });
    }
  }
}

window.postReaderController = new PostReaderController();
