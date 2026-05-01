import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import EmbeddedEditorApp from './app/EmbeddedEditorApp';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <EmbeddedEditorApp />
  </StrictMode>,
);
