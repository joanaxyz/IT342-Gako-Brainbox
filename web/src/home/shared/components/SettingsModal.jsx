import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { useNotification } from '../../../common/hooks/hooks';
import { useAiConfig } from '../../../common/hooks/useAiConfig';
import './SettingsModal.css';

const TAB_PROFILE = 'profile';
const TAB_PASSWORD = 'password';
const TAB_AI = 'ai';

const EyeIcon = ({ open }) => open ? (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
) : (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

const SettingsModal = ({ isOpen, onClose, initialTab }) => {
  const { user, updateProfile, changePassword } = useAuth();
  const { addNotification } = useNotification();
  const {
    config,
    configs,
    selectedConfigId,
    isConfigured,
    loading: aiConfigLoading,
    saveConfig,
    deleteConfig,
    selectConfig,
    refetch,
  } = useAiConfig();

  const [activeTab, setActiveTab] = useState(initialTab || TAB_PROFILE);

  // Profile form
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [profileLoading, setProfileLoading] = useState(false);

  // Password form
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);

  // AI config form
  const [aiName, setAiName] = useState('');
  const [aiModel, setAiModel] = useState('');
  const [aiProxyUrl, setAiProxyUrl] = useState('');
  const [aiApiKey, setAiApiKey] = useState('');
  const [showApiKey, setShowApiKey] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  /** 'new' = create another profile; number = edit existing row */
  const [editTarget, setEditTarget] = useState(null);
  /** true when editTarget was auto-set to 'new' because configs were empty at that moment */
  const autoSetNew = useRef(false);

  useEffect(() => {
    if (initialTab) setActiveTab(initialTab);
  }, [initialTab]);

  // Force-refresh configs whenever the AI tab becomes active
  useEffect(() => {
    if (activeTab === TAB_AI) {
      void refetch();
    }
  }, [activeTab]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (activeTab !== TAB_AI || aiConfigLoading) return;
    if (configs.length === 0) {
      autoSetNew.current = true;
      setEditTarget('new');
      return;
    }
    // Auto-select: either editTarget is null (initial), or it was auto-set to 'new'
    // because configs were empty at the time (not an explicit user choice).
    if (editTarget === null || (editTarget === 'new' && autoSetNew.current)) {
      autoSetNew.current = false;
      setEditTarget(selectedConfigId || configs[0].id);
    }
  }, [activeTab, configs, selectedConfigId, editTarget, aiConfigLoading]);

  useEffect(() => {
    if (activeTab !== TAB_AI) return;
    if (typeof editTarget === 'number' && !configs.some((c) => c.id === editTarget)) {
      setEditTarget(configs[0]?.id ?? 'new');
    }
  }, [configs, editTarget, activeTab]);

  useEffect(() => {
    if (activeTab !== TAB_AI || editTarget === null) return;
    if (editTarget === 'new') {
      setAiName('');
      setAiModel('');
      setAiProxyUrl('');
      setAiApiKey('');
      return;
    }
    const row = configs.find((c) => c.id === editTarget);
    if (row) {
      setAiName(row.name || '');
      setAiModel(row.model || '');
      setAiProxyUrl(row.proxyUrl || '');
      setAiApiKey('');
    }
  }, [editTarget, configs, activeTab]);

  if (!isOpen) return null;

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    const trimmedUsername = username.trim();
    const trimmedEmail = email.trim();

    if (!trimmedUsername && !trimmedEmail) {
      addNotification('No changes to save.', 'info');
      return;
    }

    const payload = {};
    if (trimmedUsername && trimmedUsername !== user?.username) payload.username = trimmedUsername;
    if (trimmedEmail && trimmedEmail !== user?.email) payload.email = trimmedEmail;

    if (Object.keys(payload).length === 0) {
      addNotification('No changes detected.', 'info');
      return;
    }

    setProfileLoading(true);
    const res = await updateProfile(payload);
    setProfileLoading(false);

    if (res.success) {
      addNotification('Profile updated successfully.', 'success');
      setUsername('');
      setEmail('');
    } else {
      addNotification(res.message || 'Failed to update profile.', 'error');
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      addNotification('New passwords do not match.', 'error');
      return;
    }
    if (newPassword.length < 8) {
      addNotification('Password must be at least 8 characters.', 'error');
      return;
    }

    setPasswordLoading(true);
    const res = await changePassword({ currentPassword, newPassword });
    setPasswordLoading(false);

    if (res.success) {
      addNotification('Password changed successfully.', 'success');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } else {
      addNotification(res.message || 'Failed to change password.', 'error');
    }
  };

  const handleAiSubmit = async (e) => {
    e.preventDefault();
    if (!aiName.trim()) {
      addNotification('Please enter a configuration name.', 'error');
      return;
    }
    if (!aiModel.trim()) {
      addNotification('Please enter a model name.', 'error');
      return;
    }
    if (!aiProxyUrl.trim()) {
      addNotification('Please enter a proxy URL.', 'error');
      return;
    }
    const isNewProfile = editTarget === 'new';
    if (isNewProfile && !aiApiKey.trim()) {
      addNotification('API key is required for a new configuration.', 'error');
      return;
    }

    setAiLoading(true);
    const res = await saveConfig({
      id: isNewProfile ? null : editTarget,
      name: aiName.trim(),
      model: aiModel.trim(),
      proxyUrl: aiProxyUrl.trim(),
      apiKey: aiApiKey.trim() || null,
    });
    setAiLoading(false);

    if (res.success) {
      addNotification('AI configuration saved.', 'success');
      setAiApiKey('');
      if (isNewProfile && res.data?.id) {
        setEditTarget(res.data.id);
      }
    } else {
      addNotification(res.message || 'Failed to save AI configuration.', 'error');
    }
  };

  const handleAiDelete = async () => {
    if (editTarget === 'new' || editTarget === null) {
      return;
    }
    setDeleteLoading(true);
    const res = await deleteConfig(editTarget);
    setDeleteLoading(false);

    if (res.success) {
      addNotification('AI configuration removed.', 'success');
      setEditTarget(null);
      setAiName('');
      setAiModel('');
      setAiProxyUrl('');
      setAiApiKey('');
    } else {
      addNotification(res.message || 'Failed to remove AI configuration.', 'error');
    }
  };

  return (
    <div className="settings-overlay" onClick={onClose}>
      <div className="settings-panel" onClick={(e) => e.stopPropagation()}>
        <div className="settings-sidebar">
          <div className="settings-brand">Settings</div>
          <nav className="settings-nav">
            <button
              className={`settings-nav-item${activeTab === TAB_PROFILE ? ' active' : ''}`}
              onClick={() => handleTabChange(TAB_PROFILE)}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              Profile
            </button>
            <button
              className={`settings-nav-item${activeTab === TAB_PASSWORD ? ' active' : ''}`}
              onClick={() => handleTabChange(TAB_PASSWORD)}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              Password
            </button>
            <button
              className={`settings-nav-item${activeTab === TAB_AI ? ' active' : ''}`}
              onClick={() => handleTabChange(TAB_AI)}
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
              AI Provider
            </button>
          </nav>
        </div>

        <div className="settings-main">
          <div className="settings-header">
            <h2 className="settings-title">
              {activeTab === TAB_PROFILE ? 'Edit Profile' : activeTab === TAB_PASSWORD ? 'Change Password' : 'AI Provider'}
            </h2>
            <button className="settings-close" onClick={onClose} aria-label="Close">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          {activeTab === TAB_PROFILE && (
            <form className="settings-form" onSubmit={handleProfileSubmit}>
              <div className="settings-current-info">
                <div className="settings-avatar">{user?.username?.slice(0, 2).toUpperCase() || 'U'}</div>
                <div>
                  <div className="settings-current-name">{user?.username}</div>
                  <div className="settings-current-email">{user?.email}</div>
                </div>
              </div>

              <div className="settings-field">
                <label className="settings-label">New Username</label>
                <input
                  type="text"
                  className="settings-input"
                  placeholder={user?.username || 'Username'}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                />
              </div>

              <div className="settings-field">
                <label className="settings-label">New Email</label>
                <input
                  type="email"
                  className="settings-input"
                  placeholder={user?.email || 'Email'}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                />
              </div>

              <div className="settings-actions">
                <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={profileLoading}>
                  {profileLoading ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </form>
          )}

          {activeTab === TAB_PASSWORD && (
            <form className="settings-form" onSubmit={handlePasswordSubmit}>
              <p className="settings-hint">
                Choose a strong password at least 8 characters long.
              </p>

              <div className="settings-field">
                <label className="settings-label">Current Password</label>
                <div className="settings-input-wrap">
                  <input
                    type={showCurrent ? 'text' : 'password'}
                    className="settings-input"
                    placeholder="Enter current password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                  />
                  <button type="button" className="settings-eye" onClick={() => setShowCurrent(v => !v)} tabIndex={-1}>
                    <EyeIcon open={showCurrent} />
                  </button>
                </div>
              </div>

              <div className="settings-field">
                <label className="settings-label">New Password</label>
                <div className="settings-input-wrap">
                  <input
                    type={showNew ? 'text' : 'password'}
                    className="settings-input"
                    placeholder="Enter new password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                  />
                  <button type="button" className="settings-eye" onClick={() => setShowNew(v => !v)} tabIndex={-1}>
                    <EyeIcon open={showNew} />
                  </button>
                </div>
              </div>

              <div className="settings-field">
                <label className="settings-label">Confirm New Password</label>
                <div className="settings-input-wrap">
                  <input
                    type={showConfirm ? 'text' : 'password'}
                    className="settings-input"
                    placeholder="Confirm new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                  />
                  <button type="button" className="settings-eye" onClick={() => setShowConfirm(v => !v)} tabIndex={-1}>
                    <EyeIcon open={showConfirm} />
                  </button>
                </div>
              </div>

              <div className="settings-actions">
                <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={passwordLoading}>
                  {passwordLoading ? 'Saving…' : 'Change Password'}
                </button>
              </div>
            </form>
          )}

          {activeTab === TAB_AI && (
            <form className="settings-form" onSubmit={handleAiSubmit}>
              <p className="settings-hint">
                Connect your own AI provider by entering your proxy URL and API key. Your key is encrypted and stored securely.
                You can save multiple profiles and choose which one the assistant uses.
              </p>

              {aiConfigLoading && (
                <p className="settings-hint">Loading AI configuration…</p>
              )}

              {!aiConfigLoading && (
                <div className="settings-field">
                  <label className="settings-label" htmlFor="ai-edit-target">Configuration</label>
                  <select
                    id="ai-edit-target"
                    className="settings-input"
                    value={
                      editTarget === 'new'
                        ? 'new'
                        : editTarget != null
                          ? String(editTarget)
                          : String(selectedConfigId || configs[0]?.id || 'new')
                    }
                    onChange={(e) => {
                      const v = e.target.value;
                      autoSetNew.current = false; // user made an explicit choice
                      setEditTarget(v === 'new' ? 'new' : Number(v));
                    }}
                  >
                    {configs.map((c) => (
                      <option key={c.id} value={String(c.id)}>
                        {c.name}{c.id === selectedConfigId ? ' (Active)' : ''}
                      </option>
                    ))}
                    <option value="new">+ Add new configuration</option>
                  </select>
                </div>
              )}

              <div className="settings-field">
                <label className="settings-label">Configuration Name</label>
                <input
                  type="text"
                  className="settings-input"
                  placeholder="e.g. My OpenAI, Groq Free Tier"
                  value={aiName}
                  onChange={(e) => setAiName(e.target.value)}
                  autoComplete="off"
                />
              </div>

              <div className="settings-field">
                <label className="settings-label">Model</label>
                <input
                  type="text"
                  className="settings-input"
                  placeholder="e.g. gpt-4o, llama-3.3-70b-versatile"
                  value={aiModel}
                  onChange={(e) => setAiModel(e.target.value)}
                  autoComplete="off"
                />
              </div>

              <div className="settings-field">
                <label className="settings-label">Proxy URL</label>
                <input
                  type="url"
                  className="settings-input"
                  placeholder="e.g. https://api.openai.com/v1"
                  value={aiProxyUrl}
                  onChange={(e) => setAiProxyUrl(e.target.value)}
                  autoComplete="off"
                />
              </div>

              <div className="settings-field">
                <label className="settings-label">
                  API Key
                  {editTarget !== null && editTarget !== 'new' && (
                    <span className="settings-label-hint"> (leave blank to keep current)</span>
                  )}
                </label>
                <div className="settings-input-wrap">
                  <input
                    type={showApiKey ? 'text' : 'password'}
                    className="settings-input"
                    placeholder={(editTarget !== null && editTarget !== 'new') ? '••••••••••••••••' : 'Enter your API key'}
                    value={aiApiKey}
                    onChange={(e) => setAiApiKey(e.target.value)}
                    autoComplete="off"
                  />
                  <button type="button" className="settings-eye" onClick={() => setShowApiKey(v => !v)} tabIndex={-1}>
                    <EyeIcon open={showApiKey} />
                  </button>
                </div>
              </div>

              <div className="settings-actions">
                {editTarget !== null && editTarget !== 'new' && (
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    onClick={handleAiDelete}
                    disabled={deleteLoading}
                    style={{ marginRight: 'auto' }}
                  >
                    {deleteLoading ? 'Removing…' : 'Remove'}
                  </button>
                )}
                {editTarget !== null && editTarget !== 'new' && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => void selectConfig(editTarget)}
                    disabled={editTarget === selectedConfigId}
                  >
                    {editTarget === selectedConfigId ? 'In use' : 'Use this config'}
                  </button>
                )}
                <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={aiLoading}>
                  {aiLoading ? 'Saving…' : 'Save'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default SettingsModal;
