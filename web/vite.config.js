import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'node:url'

const resolveManualChunk = (id) => {
  if (!id.includes('node_modules')) {
    return undefined
  }

  if (id.includes('node_modules/@tiptap/')) {
    return 'editor-tiptap'
  }

  if (id.includes('node_modules/katex/')) {
    return 'editor-katex'
  }

  if (id.includes('node_modules/docx/') || id.includes('node_modules/file-saver/')) {
    return 'editor-export'
  }

  return undefined
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index.html', import.meta.url)),
        mobileEditor: fileURLToPath(new URL('./mobile-editor.html', import.meta.url)),
      },
      output: {
        manualChunks: resolveManualChunk,
      },
    },
  },
})
