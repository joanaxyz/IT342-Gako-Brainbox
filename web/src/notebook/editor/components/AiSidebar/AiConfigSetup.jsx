import { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { useNotification } from '../../../../common/hooks/hooks';
import { useAiConfig } from '../../../../common/hooks/useAiConfig';

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
  const [proxyUrl, setProxyUrl] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [showKey, setShowKey] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim() || !model.trim() || !proxyUrl.trim() || !apiKey.trim()) {
      addNotification('Please fill in all fields.', 'error');
      return;
    }

    setSaving(true);
    const res = await saveConfig({
      id: null,
      name: name.trim(),
      model: model.trim(),
      proxyUrl: proxyUrl.trim(),
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
        Add your proxy URL and API key to start using the AI assistant. Your key is encrypted and stored securely.
      </p>

      <form className="ai-config-setup-form" onSubmit={handleSubmit}>
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
          placeholder="Model (e.g. gpt-4o)"
          value={model}
          onChange={(e) => setModel(e.target.value)}
          autoComplete="off"
        />

        <input
          type="url"
          className="ai-config-input"
          placeholder="Proxy URL (e.g. https://api.openai.com/v1)"
          value={proxyUrl}
          onChange={(e) => setProxyUrl(e.target.value)}
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
          disabled={saving || !name.trim() || !model.trim() || !proxyUrl.trim() || !apiKey.trim()}
        >
          {saving ? 'Saving…' : 'Connect'}
        </button>
      </form>
    </div>
  );
};

export default AiConfigSetup;
