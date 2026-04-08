import { useState, useEffect, useRef } from 'react';
import { useNotification } from '../hooks/hooks';
import { useAiConfig } from '../hooks/useAiConfig';

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

/**
 * Reusable AI configuration form panel.
 * Used in both the home SettingsModal and the editor AI sidebar.
 *
 * Props:
 *   onClose   – optional callback for a Cancel button (omit to hide Cancel)
 *   compact   – when true, renders with tighter styling for the sidebar
 */
const AiConfigPanel = ({ onClose, compact = false }) => {
  const { addNotification } = useNotification();
  const {
    configs,
    selectedConfigId,
    loading: aiConfigLoading,
    saveConfig,
    deleteConfig,
    selectConfig,
    refetch,
  } = useAiConfig();

  const [aiName, setAiName] = useState('');
  const [aiModel, setAiModel] = useState('');
  const [aiProxyUrl, setAiProxyUrl] = useState('');
  const [aiApiKey, setAiApiKey] = useState('');
  const [showApiKey, setShowApiKey] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  /** 'new' = create new; number = edit existing row */
  const [editTarget, setEditTarget] = useState(null);
  const autoSetNew = useRef(false);

  useEffect(() => {
    void refetch();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (aiConfigLoading) return;
    if (configs.length === 0) {
      autoSetNew.current = true;
      setEditTarget('new');
      return;
    }
    if (editTarget === null || (editTarget === 'new' && autoSetNew.current)) {
      autoSetNew.current = false;
      setEditTarget(selectedConfigId || configs[0].id);
    }
  }, [configs, selectedConfigId, editTarget, aiConfigLoading]);

  useEffect(() => {
    if (typeof editTarget === 'number' && !configs.some((c) => c.id === editTarget)) {
      setEditTarget(configs[0]?.id ?? 'new');
    }
  }, [configs, editTarget]);

  useEffect(() => {
    if (editTarget === null) return;
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
  }, [editTarget, configs]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!aiName.trim()) { addNotification('Please enter a configuration name.', 'error'); return; }
    if (!aiModel.trim()) { addNotification('Please enter a model name.', 'error'); return; }
    if (!aiProxyUrl.trim()) { addNotification('Please enter a proxy URL.', 'error'); return; }
    const isNew = editTarget === 'new';
    if (isNew && !aiApiKey.trim()) { addNotification('API key is required for a new configuration.', 'error'); return; }

    setAiLoading(true);
    const res = await saveConfig({
      id: isNew ? null : editTarget,
      name: aiName.trim(),
      model: aiModel.trim(),
      proxyUrl: aiProxyUrl.trim(),
      apiKey: aiApiKey.trim() || null,
    });
    setAiLoading(false);

    if (res.success) {
      addNotification('AI configuration saved.', 'success');
      setAiApiKey('');
      if (isNew && res.data?.id) {
        setEditTarget(res.data.id);
      }
    } else {
      addNotification(res.message || 'Failed to save AI configuration.', 'error');
    }
  };

  const handleDelete = async () => {
    if (editTarget === 'new' || editTarget === null) return;
    setDeleteLoading(true);
    const res = await deleteConfig(editTarget);
    setDeleteLoading(false);
    if (res.success) {
      addNotification('AI configuration removed.', 'success');
      setEditTarget(null);
      setAiName(''); setAiModel(''); setAiProxyUrl(''); setAiApiKey('');
    } else {
      addNotification(res.message || 'Failed to remove AI configuration.', 'error');
    }
  };

  return (
    <form className={`settings-form${compact ? ' settings-form--compact' : ''}`} onSubmit={handleSubmit}>
      <p className="settings-hint">
        Connect your own AI provider by entering your proxy URL and API key. Your key is encrypted and stored securely.
        You can save multiple profiles and choose which one the assistant uses.
      </p>

      {aiConfigLoading && <p className="settings-hint">Loading AI configuration…</p>}

      {!aiConfigLoading && (
        <div className="settings-field">
          <label className="settings-label" htmlFor="ai-config-panel-target">Configuration</label>
          <select
            id="ai-config-panel-target"
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
              autoSetNew.current = false;
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
          <button type="button" className="settings-eye" onClick={() => setShowApiKey((v) => !v)} tabIndex={-1}>
            <EyeIcon open={showApiKey} />
          </button>
        </div>
      </div>

      <div className="settings-actions">
        {editTarget !== null && editTarget !== 'new' && (
          <button
            type="button"
            className="btn btn-danger btn-sm"
            onClick={handleDelete}
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
        {onClose && (
          <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
            Cancel
          </button>
        )}
        <button type="submit" className="btn btn-primary btn-sm" disabled={aiLoading}>
          {aiLoading ? 'Saving…' : 'Save'}
        </button>
      </div>
    </form>
  );
};

export default AiConfigPanel;
