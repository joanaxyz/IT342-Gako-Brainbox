import { useState, useEffect } from 'react';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { useNotification } from '../../../common/hooks/hooks';
import AiConfigPanel from '../../../common/components/AiConfigPanel';
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

  useEffect(() => {
    if (initialTab) setActiveTab(initialTab);
  }, [initialTab]);

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
            <AiConfigPanel onClose={onClose} />
          )}
        </div>
      </div>
    </div>
  );
};

export default SettingsModal;
