/**
 * DailyBook Story Editor Controller
 * Handles post creation, editing existing posts, live markdown preview, and word counting.
 */

class StoryEditorController {
  constructor() {
    this.editPostId = null;
    this.isEditMode = false;
    this.selectedVisibility = 'PUBLIC'; // Default to PUBLIC for great sharing experience, backend defaults to PRIVATE
    this.isSubmitting = false;
  }

  async init() {
    // Route guard: require authentication
    if (!window.Auth.requireAuth('create-post.html')) {
      return;
    }

    const params = new URLSearchParams(window.location.search);
    this.editPostId = params.get('edit');
    this.isEditMode = !!this.editPostId;

    this.setupUI();
    this.attachEvents();

    if (this.isEditMode) {
      await this.loadExistingPost();
    }
  }

  setupUI() {
    const pageTitle = document.getElementById('editor-page-title');
    const submitBtnText = document.getElementById('editor-submit-btn-text');

    if (this.isEditMode) {
      if (pageTitle) pageTitle.textContent = 'Edit Story';
      if (submitBtnText) submitBtnText.textContent = 'Save Changes';
      document.title = 'Edit Story — DailyBook';
    } else {
      if (pageTitle) pageTitle.textContent = 'Create a new story';
      if (submitBtnText) submitBtnText.textContent = 'Publish Story';
      document.title = 'Create a new story — DailyBook';
    }

    this.updateVisibilitySelection('PUBLIC');
  }

  attachEvents() {
    const titleInput = document.getElementById('post-title');
    const contentTextarea = document.getElementById('post-content');
    const tagsInput = document.getElementById('post-tags');
    const form = document.getElementById('editor-form');

    // Title counter
    const titleCounter = document.getElementById('title-char-count');
    if (titleInput && titleCounter) {
      titleInput.addEventListener('input', () => {
        titleCounter.textContent = `${titleInput.value.length}/100`;
        if (titleInput.value.length > 100) {
          titleCounter.style.color = 'var(--danger)';
        } else {
          titleCounter.style.color = 'var(--text-muted)';
        }
      });
    }

    // Content live word & character stats
    if (contentTextarea) {
      contentTextarea.addEventListener('input', () => {
        this.updateContentStats();
      });
    }

    // Visibility card selection
    document.querySelectorAll('.visibility-card').forEach(card => {
      card.addEventListener('click', () => {
        const val = card.getAttribute('data-value');
        this.updateVisibilitySelection(val);
      });
    });

    // Write / Preview Tabs
    const writeTab = document.getElementById('tab-write-btn');
    const previewTab = document.getElementById('tab-preview-btn');
    const writeArea = document.getElementById('editor-write-pane');
    const previewArea = document.getElementById('editor-preview-pane');
    const previewContent = document.getElementById('editor-preview-content');

    if (writeTab && previewTab && writeArea && previewArea) {
      writeTab.addEventListener('click', () => {
        writeTab.classList.add('active');
        previewTab.classList.remove('active');
        writeArea.style.display = 'block';
        previewArea.style.display = 'none';
      });

      previewTab.addEventListener('click', () => {
        previewTab.classList.add('active');
        writeTab.classList.remove('active');
        writeArea.style.display = 'none';
        previewArea.style.display = 'block';

        const raw = contentTextarea ? contentTextarea.value : '';
        previewContent.innerHTML = raw.trim() 
          ? window.App.renderMarkdown(raw) 
          : '<p style="color: var(--text-muted); font-style: italic;">Nothing to preview yet. Write some markdown content above.</p>';
      });
    }

    // Markdown Quick Toolbar
    this.setupMarkdownToolbar(contentTextarea);

    // Form submit
    if (form) {
      form.addEventListener('submit', (e) => this.handleSubmit(e));
    }

    // Cancel button
    const cancelBtn = document.getElementById('editor-cancel-btn');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => {
        if (this.isEditMode && this.editPostId) {
          window.location.href = `post.html?id=${encodeURIComponent(this.editPostId)}`;
        } else {
          window.location.href = 'feed.html';
        }
      });
    }
  }

  setupMarkdownToolbar(textarea) {
    if (!textarea) return;

    const insertText = (prefix, suffix = '') => {
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const selected = textarea.value.substring(start, end);
      const replacement = prefix + (selected || 'text') + suffix;

      textarea.value = textarea.value.substring(0, start) + replacement + textarea.value.substring(end);
      textarea.focus();
      textarea.setSelectionRange(start + prefix.length, start + prefix.length + (selected ? selected.length : 4));
      this.updateContentStats();
    };

    const actions = {
      'tool-bold': () => insertText('**', '**'),
      'tool-italic': () => insertText('*', '*'),
      'tool-heading': () => insertText('## ', ''),
      'tool-quote': () => insertText('> ', ''),
      'tool-code': () => insertText('`', '`'),
      'tool-codeblock': () => insertText('```\n', '\n```'),
      'tool-list': () => insertText('- ', ''),
      'tool-link': () => insertText('[', '](https://)')
    };

    Object.entries(actions).forEach(([id, fn]) => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', fn);
    });
  }

  updateContentStats() {
    const textarea = document.getElementById('post-content');
    const wordCountSpan = document.getElementById('word-count-badge');
    const charCountSpan = document.getElementById('char-count-badge');
    const readingTimeSpan = document.getElementById('reading-time-badge');

    if (!textarea) return;

    const text = textarea.value;
    const words = text.trim() ? text.trim().split(/\s+/).length : 0;
    const chars = text.length;

    if (wordCountSpan) wordCountSpan.textContent = `${words} words`;
    if (charCountSpan) charCountSpan.textContent = `${chars} chars`;
    if (readingTimeSpan) readingTimeSpan.textContent = window.App.getReadingTime(text);
  }

  updateVisibilitySelection(value) {
    this.selectedVisibility = value;
    document.querySelectorAll('.visibility-card').forEach(card => {
      if (card.getAttribute('data-value') === value) {
        card.classList.add('selected');
      } else {
        card.classList.remove('selected');
      }
    });
  }

  async loadExistingPost() {
    try {
      const post = await window.API.getPostById(this.editPostId);
      const currentUsername = window.Auth.getUser()?.username;

      // Ownership check
      if (post.authorId !== currentUsername) {
        window.Toast.error('You are not authorized to edit this story');
        setTimeout(() => {
          window.location.href = `post.html?id=${encodeURIComponent(this.editPostId)}`;
        }, 1000);
        return;
      }

      // Populate inputs
      const titleInput = document.getElementById('post-title');
      const contentTextarea = document.getElementById('post-content');
      const tagsInput = document.getElementById('post-tags');

      if (titleInput) titleInput.value = post.title || '';
      if (contentTextarea) contentTextarea.value = post.content || '';
      if (tagsInput && post.tags) tagsInput.value = post.tags.join(', ');

      if (post.visibility) {
        this.updateVisibilitySelection(post.visibility);
      }

      this.updateContentStats();
      const titleCounter = document.getElementById('title-char-count');
      if (titleCounter && titleInput) {
        titleCounter.textContent = `${titleInput.value.length}/100`;
      }
    } catch (err) {
      console.error('Failed to load post for editing:', err);
      window.Toast.error('Could not load story for editing: ' + err.message);
      setTimeout(() => { window.location.href = 'feed.html'; }, 1500);
    }
  }

  async handleSubmit(e) {
    e.preventDefault();
    if (this.isSubmitting) return;

    const titleInput = document.getElementById('post-title');
    const contentTextarea = document.getElementById('post-content');
    const tagsInput = document.getElementById('post-tags');
    const submitBtn = document.getElementById('editor-submit-btn');
    const submitText = document.getElementById('editor-submit-btn-text');

    const title = titleInput.value.trim();
    const content = contentTextarea.value.trim();

    // Frontend validation according to backend constraints
    if (title.length < 3 || title.length > 100) {
      window.Toast.warning('Title must be between 3 and 100 characters');
      titleInput.focus();
      return;
    }

    if (content.length < 10) {
      window.Toast.warning('Content must be at least 10 characters long');
      contentTextarea.focus();
      return;
    }

    // Parse comma-separated tags
    const rawTags = tagsInput ? tagsInput.value : '';
    const tags = rawTags
      .split(',')
      .map(t => t.trim().replace(/^#/, ''))
      .filter(t => t.length > 0);

    const payload = {
      title,
      content,
      tags,
      visibility: this.selectedVisibility
    };

    this.isSubmitting = true;
    submitBtn.disabled = true;
    submitText.innerHTML = '<span class="btn-spinner"></span> Saving...';

    try {
      if (this.isEditMode) {
        const updated = await window.API.updatePost(this.editPostId, payload);
        window.Toast.success('Story updated successfully!');
        setTimeout(() => {
          window.location.href = `post.html?id=${encodeURIComponent(updated.id || this.editPostId)}`;
        }, 400);
      } else {
        const created = await window.API.createPost(payload);
        window.Toast.success('Story published successfully!');
        setTimeout(() => {
          window.location.href = `post.html?id=${encodeURIComponent(created.id)}`;
        }, 400);
      }
    } catch (err) {
      this.isSubmitting = false;
      submitBtn.disabled = false;
      submitText.textContent = this.isEditMode ? 'Save Changes' : 'Publish Story';
      window.Toast.error(err.message || 'Failed to save story. Please try again.');
    }
  }
}

window.storyEditorController = new StoryEditorController();
