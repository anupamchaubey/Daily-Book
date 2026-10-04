/**
 * DailyBook Search Controller
 * Handles query execution, keyword highlighting, empty states, and pagination.
 */

class SearchController {
  constructor() {
    this.currentQuery = '';
    this.currentPage = 0;
    this.pageSize = (window.CONFIG && window.CONFIG.DEFAULT_PAGE_SIZE) || 10;
    this.totalPages = 1;
    this.totalElements = 0;
    this.isLoading = false;
  }

  init() {
    const params = new URLSearchParams(window.location.search);
    const query = params.get('q') || '';

    const input = document.getElementById('search-main-input');
    if (input) {
      input.value = query;
      input.focus();
    }

    this.attachEvents();

    if (query.trim()) {
      this.currentQuery = query.trim();
      this.executeSearch();
    } else {
      this.renderInitialEmptyState();
    }
  }

  attachEvents() {
    const form = document.getElementById('search-form');
    const input = document.getElementById('search-main-input');
    const clearBtn = document.getElementById('search-clear-btn');

    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        const val = input.value.trim();
        if (val) {
          this.currentQuery = val;
          this.currentPage = 0;
          this.updateUrlQuery(val);
          this.executeSearch();
        }
      });
    }

    if (clearBtn && input) {
      clearBtn.addEventListener('click', () => {
        input.value = '';
        input.focus();
        this.currentQuery = '';
        this.updateUrlQuery('');
        this.renderInitialEmptyState();
      });

      input.addEventListener('input', () => {
        clearBtn.style.display = input.value ? 'flex' : 'none';
      });
      clearBtn.style.display = input.value ? 'flex' : 'none';
    }
  }

  updateUrlQuery(q) {
    const url = new URL(window.location);
    if (q) {
      url.searchParams.set('q', q);
    } else {
      url.searchParams.delete('q');
    }
    window.history.pushState({}, '', url);
  }

  async executeSearch() {
    if (!this.currentQuery) return;
    this.isLoading = true;

    const resultsContainer = document.getElementById('search-results-container');
    const headerInfo = document.getElementById('search-results-header');
    const paginationContainer = document.getElementById('search-pagination');

    if (resultsContainer) {
      resultsContainer.innerHTML = window.App.renderSkeletons(3);
    }
    if (paginationContainer) paginationContainer.innerHTML = '';

    document.title = `Search: "${this.currentQuery}" — DailyBook`;

    try {
      const pageData = await window.API.searchPosts(this.currentQuery, this.currentPage, this.pageSize);
      const posts = (pageData && pageData.content) ? pageData.content : [];
      this.totalPages = (pageData && pageData.totalPages) || 1;
      this.totalElements = (pageData && pageData.totalElements) || posts.length;

      if (headerInfo) {
        headerInfo.innerHTML = `
          <div style="font-size: 1rem; color: var(--text-secondary); margin-bottom: 24px;">
            Found <strong style="color: var(--text-primary);">${this.totalElements}</strong> stories matching <strong style="color: var(--accent);">&ldquo;${window.App.escapeHtml(this.currentQuery)}&rdquo;</strong>
          </div>
        `;
      }

      if (posts.length === 0) {
        this.renderNoResultsState();
        return;
      }

      resultsContainer.innerHTML = posts.map(post => this.renderSearchResultCard(post, this.currentQuery)).join('');
      this.renderPagination();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      console.error('Search error:', err);
      if (resultsContainer) {
        resultsContainer.innerHTML = `
          <div class="empty-state">
            <div class="empty-state-icon" style="color: var(--danger);">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </div>
            <h3 class="empty-state-title">Search Failed</h3>
            <p class="empty-state-desc">${window.App.escapeHtml(err.message || 'Unable to complete search request.')}</p>
          </div>
        `;
      }
    } finally {
      this.isLoading = false;
    }
  }

  highlightMatch(text, query) {
    if (!text || !query) return window.App.escapeHtml(text);
    const escaped = window.App.escapeHtml(text);
    const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    return escaped.replace(regex, '<mark style="background: rgba(37, 99, 235, 0.18); color: inherit; padding: 0 2px; border-radius: 2px;">$1</mark>');
  }

  renderSearchResultCard(post, query) {
    const author = post.authorId || 'Anonymous';
    const initials = window.App.getInitials(author);
    const avatarTint = window.App.getAvatarClass(author);
    const dateFormatted = window.App.formatDate(post.createdAt);
    const readingTime = window.App.getReadingTime(post.content);

    // Visibility Badge
    let visibilityBadge = '<span class="badge badge-public">Public</span>';

    // Tags
    const tagsHtml = (post.tags && Array.isArray(post.tags) && post.tags.length > 0)
      ? post.tags.map(tag => `<a href="search.html?q=${encodeURIComponent(tag)}" class="tag-pill">#${window.App.escapeHtml(tag)}</a>`).join('')
      : '';

    // Excerpt with highlighting
    const rawExcerpt = (post.content || '').replace(/[#*`>]/g, '').trim();
    const excerptClipped = rawExcerpt.length > 220 ? rawExcerpt.slice(0, 220) + '...' : rawExcerpt;
    const highlightedTitle = this.highlightMatch(post.title, query);
    const highlightedExcerpt = this.highlightMatch(excerptClipped, query);

    return `
      <article class="post-card">
        <div class="post-card-header">
          <div class="post-author-info">
            <a href="profile.html?user=${encodeURIComponent(author)}" class="avatar avatar-sm ${avatarTint}">
              ${initials}
            </a>
            <div>
              <a href="profile.html?user=${encodeURIComponent(author)}" class="post-author-name">
                @${window.App.escapeHtml(author)}
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
          <a href="post.html?id=${encodeURIComponent(post.id)}">${highlightedTitle}</a>
        </h3>

        <p class="post-card-excerpt">${highlightedExcerpt}</p>

        <div class="post-card-footer">
          <div class="post-tags-list">${tagsHtml}</div>
          <a href="post.html?id=${encodeURIComponent(post.id)}" class="post-read-more">
            Read story
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
          </a>
        </div>
      </article>
    `;
  }

  renderInitialEmptyState() {
    const resultsContainer = document.getElementById('search-results-container');
    const headerInfo = document.getElementById('search-results-header');
    const paginationContainer = document.getElementById('search-pagination');

    if (headerInfo) headerInfo.innerHTML = '';
    if (paginationContainer) paginationContainer.innerHTML = '';

    if (resultsContainer) {
      resultsContainer.innerHTML = `
        <div class="empty-state" style="padding: 64px 20px;">
          <div class="empty-state-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          </div>
          <h3 class="empty-state-title">Explore DailyBook</h3>
          <p class="empty-state-desc">Type keywords, tags, or topics above to find articles written by the community.</p>
        </div>
      `;
    }
  }

  renderNoResultsState() {
    const resultsContainer = document.getElementById('search-results-container');
    if (!resultsContainer) return;

    resultsContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg>
        </div>
        <h3 class="empty-state-title">No stories found</h3>
        <p class="empty-state-desc">We couldn't find any public stories matching &ldquo;${window.App.escapeHtml(this.currentQuery)}&rdquo;. Try searching for broader terms or exploring the feed.</p>
        <a href="feed.html" class="btn btn-secondary btn-sm">Explore Feed</a>
      </div>
    `;
  }

  renderPagination() {
    const paginationContainer = document.getElementById('search-pagination');
    if (!paginationContainer || this.totalPages <= 1) return;

    let buttons = `
      <button class="page-btn" ${this.currentPage === 0 ? 'disabled' : ''} onclick="searchController.goToPage(${this.currentPage - 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
      </button>
    `;

    for (let i = 0; i < this.totalPages; i++) {
      buttons += `
        <button class="page-btn ${i === this.currentPage ? 'active' : ''}" onclick="searchController.goToPage(${i})">
          ${i + 1}
        </button>
      `;
    }

    buttons += `
      <button class="page-btn" ${this.currentPage >= this.totalPages - 1 ? 'disabled' : ''} onclick="searchController.goToPage(${this.currentPage + 1})">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
      </button>
    `;

    paginationContainer.innerHTML = `<div class="pagination">${buttons}</div>`;
  }

  goToPage(page) {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.executeSearch();
    }
  }
}

window.searchController = new SearchController();
