import { useEffect, useMemo, useState } from 'react';
import { useNotification } from '../../common/hooks/hooks';
import {
  AI_PROVIDER_PRESETS,
  DEFAULT_PROVIDER_PRESET_ID,
  getProviderPreset,
} from '../config/providerPresets';
import { useAiConfig } from '../hooks/useAiConfig';

const EMPTY_FORM = {
  name: '',
  model: '',
  baseUrl: '',
  apiKey: '',
};

const EyeIcon = ({ open }) => open ? (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
) : (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
    <line x1="1" y1="1" x2="23" y2="23" />
  </svg>
);

const resolveEditTarget = ({
  requestedEditTarget,
  configs,
  selectedConfigId,
  aiConfigLoading,
}) => {
  if (aiConfigLoading) {
    return requestedEditTarget;
  }

  if (requestedEditTarget === 'new') {
    return 'new';
  }

  if (
    typeof requestedEditTarget === 'number'
    && configs.some((config) => config.id === requestedEditTarget)
  ) {
    return requestedEditTarget;
  }

  if (configs.length === 0) {
    return 'new';
  }

  return selectedConfigId || configs[0].id;
};

const getInitialFormValues = (editTarget, row) => {
  if (editTarget === 'new' || !row) {
    return EMPTY_FORM;
  }

  return {
    name: row.name || '',
    model: row.model || '',
    baseUrl: row.baseUrl || row.proxyUrl || '',
    apiKey: '',
  };
};

const AiConfigEditorForm = ({
  compact,
  configs,
  editTarget,
  row,
  selectedConfigId,
  aiConfigLoading,
  saveConfig,
  deleteConfig,
  selectConfig,
  onClose,
  onTargetChange,
  onTargetSaved,
  onTargetDeleted,
}) => {
  const { addNotification } = useNotification();
  const [formValues, setFormValues] = useState(() => getInitialFormValues(editTarget, row));
  const [providerPresetId, setProviderPresetId] = useState(DEFAULT_PROVIDER_PRESET_ID);
  const [showApiKey, setShowApiKey] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const isNew = editTarget === 'new';
  const canSubmit = editTarget !== null && !aiConfigLoading;
  const providerPreset = getProviderPreset(providerPresetId);

  const updateField = (field) => (event) => {
    setFormValues((currentValues) => ({
      ...currentValues,
      [field]: event.target.value,
    }));
  };

  const handleProviderPresetChange = (event) => {
    const nextPreset = getProviderPreset(event.target.value);
    setProviderPresetId(nextPreset.id);
    setFormValues((currentValues) => ({
      ...currentValues,
      name: currentValues.name.trim() ? currentValues.name : nextPreset.name,
      model: nextPreset.model,
      baseUrl: nextPreset.baseUrl,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!canSubmit) {
      return;
    }

    if (!formValues.name.trim()) {
      addNotification('Please enter a configuration name.', 'error');
      return;
    }

    if (!formValues.model.trim()) {
      addNotification('Please enter a model name.', 'error');
      return;
    }

    if (!formValues.baseUrl.trim()) {
      addNotification('Please enter an API Base URL.', 'error');
      return;
    }

    if (isNew && !formValues.apiKey.trim()) {
      addNotification('API key is required for a new configuration.', 'error');
      return;
    }

    setAiLoading(true);
    const response = await saveConfig({
      id: isNew ? null : editTarget,
      name: formValues.name.trim(),
      model: formValues.model.trim(),
      baseUrl: formValues.baseUrl.trim(),
      apiKey: formValues.apiKey.trim() || null,
    });
    setAiLoading(false);

    if (response.success) {
      addNotification('AI configuration saved.', 'success');
      setFormValues((currentValues) => ({ ...currentValues, apiKey: '' }));
      if (isNew && response.data?.id) {
        onTargetSaved(response.data.id);
      }
      return;
    }

    addNotification(response.message || 'Failed to save AI configuration.', 'error');
  };

  const handleDelete = async () => {
    if (editTarget === 'new' || editTarget === null) {
      return;
    }

    setDeleteLoading(true);
    const response = await deleteConfig(editTarget);
    setDeleteLoading(false);

    if (response.success) {
      addNotification('AI configuration removed.', 'success');
      onTargetDeleted();
      return;
    }

    addNotification(response.message || 'Failed to remove AI configuration.', 'error');
  };

  return (
    <form className={`settings-form${compact ? ' settings-form--compact' : ''}`} onSubmit={handleSubmit}>
      <p className="settings-hint">
        Choose an AI provider and enter its API base URL, model, and API key. Your key is encrypted and stored securely.
        You can save multiple profiles and choose which one the assistant uses.
      </p>

      {aiConfigLoading && <p className="settings-hint">Loading AI configuration...</p>}

      {!aiConfigLoading && (
        <div className="settings-field">
          <label className="settings-label" htmlFor="ai-config-panel-target">Configuration</label>
          <select
            id="ai-config-panel-target"
            className="settings-input"
            value={editTarget === 'new' ? 'new' : String(editTarget ?? selectedConfigId ?? configs[0]?.id ?? 'new')}
            onChange={(event) => {
              const value = event.target.value;
              onTargetChange(value === 'new' ? 'new' : Number(value));
            }}
          >
            {configs.map((config) => (
              <option key={config.id} value={String(config.id)}>
                {config.name}{config.id === selectedConfigId ? ' (Active)' : ''}
              </option>
            ))}
            <option value="new">+ Add new configuration</option>
          </select>
        </div>
      )}

      <div className="settings-field">
        <label className="settings-label" htmlFor="ai-config-panel-provider">Provider</label>
        <select
          id="ai-config-panel-provider"
          className="settings-input"
          value={providerPresetId}
          onChange={handleProviderPresetChange}
        >
          {AI_PROVIDER_PRESETS.map((preset) => (
            <option key={preset.id} value={preset.id}>
              {preset.label}
            </option>
          ))}
        </select>
      </div>

      <div className="settings-field">
        <label className="settings-label">Configuration Name</label>
        <input
          type="text"
          className="settings-input"
          placeholder="e.g. My OpenAI, Groq Free Tier"
          value={formValues.name}
          onChange={updateField('name')}
          autoComplete="off"
        />
      </div>

      <div className="settings-field">
        <label className="settings-label">Model</label>
        <input
          type="text"
          className="settings-input"
          placeholder={`e.g. ${providerPreset.modelPlaceholder}`}
          value={formValues.model}
          onChange={updateField('model')}
          autoComplete="off"
        />
      </div>

      <div className="settings-field">
        <label className="settings-label">API Base URL</label>
        <input
          type="url"
          className="settings-input"
          placeholder="https://openrouter.ai/api/v1"
          value={formValues.baseUrl}
          onChange={updateField('baseUrl')}
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
            placeholder={(editTarget !== null && editTarget !== 'new') ? '****************' : 'Enter your API key'}
            value={formValues.apiKey}
            onChange={updateField('apiKey')}
            autoComplete="off"
          />
          <button type="button" className="settings-eye" onClick={() => setShowApiKey((value) => !value)} tabIndex={-1}>
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
            {deleteLoading ? 'Removing...' : 'Remove'}
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
        <button type="submit" className="btn btn-primary btn-sm" disabled={aiLoading || !canSubmit}>
          {aiLoading ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  );
};

const AiConfigPanel = ({ onClose, compact = false }) => {
  const {
    configs,
    selectedConfigId,
    loading: aiConfigLoading,
    saveConfig,
    deleteConfig,
    selectConfig,
    refetch,
  } = useAiConfig();
  const [requestedEditTarget, setRequestedEditTarget] = useState(null);
  const editTarget = useMemo(() => resolveEditTarget({
    requestedEditTarget,
    configs,
    selectedConfigId,
    aiConfigLoading,
  }), [aiConfigLoading, configs, requestedEditTarget, selectedConfigId]);
  const row = typeof editTarget === 'number'
    ? configs.find((config) => config.id === editTarget) || null
    : null;

  useEffect(() => {
    void refetch();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <AiConfigEditorForm
      key={editTarget ?? 'loading'}
      compact={compact}
      configs={configs}
      editTarget={editTarget}
      row={row}
      selectedConfigId={selectedConfigId}
      aiConfigLoading={aiConfigLoading}
      saveConfig={saveConfig}
      deleteConfig={deleteConfig}
      selectConfig={selectConfig}
      onClose={onClose}
      onTargetChange={setRequestedEditTarget}
      onTargetSaved={setRequestedEditTarget}
      onTargetDeleted={() => setRequestedEditTarget(null)}
    />
  );
};

export default AiConfigPanel;
