export const DEFAULT_PROVIDER_PRESET_ID = 'custom';

export const AI_PROVIDER_PRESETS = [
  {
    id: 'custom',
    label: 'OpenAI Compatible / Custom',
    name: '',
    baseUrl: '',
    model: '',
    modelPlaceholder: 'gpt-4o-mini, llama-3.3-70b-versatile',
  },
  {
    id: 'openai',
    label: 'OpenAI',
    name: 'OpenAI',
    baseUrl: 'https://api.openai.com/v1',
    model: 'gpt-4o-mini',
    modelPlaceholder: 'gpt-4o-mini',
  },
  {
    id: 'openrouter',
    label: 'OpenRouter',
    name: 'OpenRouter',
    baseUrl: 'https://openrouter.ai/api/v1',
    model: 'google/gemini-2.0-flash-001',
    modelPlaceholder: 'google/gemini-2.0-flash-001',
  },
  {
    id: 'google-ai-studio',
    label: 'Google AI Studio Gemini',
    name: 'Google AI Studio Gemini',
    baseUrl: 'https://generativelanguage.googleapis.com/v1beta/openai',
    model: 'gemini-2.0-flash',
    modelPlaceholder: 'gemini-2.0-flash',
  },
  {
    id: 'groq',
    label: 'Groq',
    name: 'Groq',
    baseUrl: 'https://api.groq.com/openai/v1',
    model: 'llama-3.3-70b-versatile',
    modelPlaceholder: 'llama-3.3-70b-versatile',
  },
];

export const getProviderPreset = (presetId) => (
  AI_PROVIDER_PRESETS.find((preset) => preset.id === presetId)
  || AI_PROVIDER_PRESETS[0]
);
