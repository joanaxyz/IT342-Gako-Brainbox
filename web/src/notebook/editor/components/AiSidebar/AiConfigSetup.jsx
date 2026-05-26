import { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { useNotification } from '../../../../common/hooks/hooks';
import { useAiConfig } from '../../../../ai/hooks/useAiConfig';
import {
  AI_PROVIDER_PRESETS,
  DEFAULT_PROVIDER_PRESET_ID,
  getProviderPreset,
} from '../../../../ai/config/providerPresets';

const EyeIcon = ({ open }) => open ? (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
) : (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

const AiConfigSetup = () => {
  const { saveConfig } = useAiConfig();
  const { addNotification } = useNotification();

  const [name, setName] = useState('');
  const [model, setModel] = useState('');
  const [baseUrl, setBaseUrl] = useState('');
  const [providerPresetId, setProviderPresetId] = useState(DEFAULT_PROVIDER_PRESET_ID);
  const [apiKey, setApiKey] = useState('');
  const [showKey, setShowKey] = useState(false);
  const [saving, setSaving] = useState(false);
  const providerPreset = getProviderPreset(providerPresetId);

  const handleProviderPresetChange = (event) => {
    const nextPreset = getProviderPreset(event.target.value);
    setProviderPresetId(nextPreset.id);
    setName((currentName) => currentName.trim() ? currentName : nextPreset.name);
    setModel(nextPreset.model);
    setBaseUrl(nextPreset.baseUrl);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || !model.trim() || !baseUrl.trim() || !apiKey.trim()) {
      addNotification('Please fill in all fields.', 'error');
      return;
    }

    setSaving(true);
    const res = await saveConfig({
      id: null,
      name: name.trim(),
      model: model.trim(),
      baseUrl: baseUrl.trim(),
      apiKey: apiKey.trim(),
    });
    setSaving(false);

    if (res.success) {
      addNotification('AI configured! You can start chatting.', 'success');
    } else {
      addNotification(res.message || 'Failed to save configuration.', 'error');
    }
  };

  return (
    <div className="ai-config-setup">
      <div className="ai-config-setup-icon">
        <Sparkles size={24} />
      </div>
      <h3 className="ai-config-setup-title">Connect your AI</h3>
      <p className="ai-config-setup-desc">
        Choose an AI provider and enter its API base URL, model, and API key. Your key is encrypted and stored securely.
      </p>

      <form className="ai-config-setup-form" onSubmit={handleSubmit}>
        <select
          className="ai-config-input"
          value={providerPresetId}
          onChange={handleProviderPresetChange}
        >
          {AI_PROVIDER_PRESETS.map((preset) => (
            <option key={preset.id} value={preset.id}>
              {preset.label}
            </option>
          ))}
        </select>

        <input
          type="text"
          className="ai-config-input"
          placeholder="Configuration name (e.g. My OpenAI)"
          value={name}
          onChange={(e) => setName(e.target.value)}
          autoComplete="off"
        />

        <input
          type="text"
          className="ai-config-input"
          placeholder={`Model (e.g. ${providerPreset.modelPlaceholder})`}
          value={model}
          onChange={(e) => setModel(e.target.value)}
          autoComplete="off"
        />

        <input
          type="url"
          className="ai-config-input"
          placeholder="API Base URL (e.g. https://openrouter.ai/api/v1)"
          value={baseUrl}
          onChange={(e) => setBaseUrl(e.target.value)}
          autoComplete="off"
        />

        <div className="ai-config-key-wrap">
          <input
            type={showKey ? 'text' : 'password'}
            className="ai-config-input"
            placeholder="Paste your API key"
            value={apiKey}
            onChange={(e) => setApiKey(e.target.value)}
            autoComplete="off"
          />
          <button
            type="button"
            className="ai-config-eye"
            onClick={() => setShowKey((v) => !v)}
            tabIndex={-1}
          >
            <EyeIcon open={showKey} />
          </button>
        </div>

        <button
          type="submit"
          className="btn btn-primary btn-sm ai-config-submit"
          disabled={saving || !name.trim() || !model.trim() || !baseUrl.trim() || !apiKey.trim()}
        >
          {saving ? 'Saving…' : 'Connect'}
        </button>
      </form>
    </div>
  );
};

export default AiConfigSetup;
