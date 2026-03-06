import { useState, useCallback, useEffect } from 'react';

export const useEditorResize = (editorBodyRef) => {
  const [editorMaxWidth, setEditorMaxWidth] = useState(() => {
    const saved = localStorage.getItem('noteEditorMaxWidth');
    return saved ? parseInt(saved, 10) : 720;
  });
  
  const [isResizing, setIsResizing] = useState(false);

  const handleMouseDown = useCallback((e) => {
    e.preventDefault();
    setIsResizing(true);
  }, []);

  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!isResizing) return;
      
      const editorBody = editorBodyRef.current;
      if (!editorBody) return;
      
      const rect = editorBody.getBoundingClientRect();
      const tocWidth = 260; // Fixed TOC sidebar width
      const editorMainLeft = rect.left + tocWidth;
      const editorMainWidth = rect.width - tocWidth;
      
      const mouseXInMain = e.clientX - editorMainLeft;
      const centerXInMain = editorMainWidth / 2;
      
      const distanceFromCenter = Math.abs(mouseXInMain - centerXInMain);
      const newWidth = distanceFromCenter * 2;
      
      const maxAllowedWidth = editorMainWidth * 0.95;
      const constrainedWidth = Math.max(400, Math.min(maxAllowedWidth, newWidth));
      
      setEditorMaxWidth(constrainedWidth);
    };

    const handleMouseUp = () => {
      if (isResizing) {
        setIsResizing(false);
      }
    };

    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [isResizing, editorBodyRef]);

  useEffect(() => {
    localStorage.setItem('noteEditorMaxWidth', editorMaxWidth.toString());
  }, [editorMaxWidth]);

  return {
    editorMaxWidth,
    isResizing,
    handleMouseDown
  };
};
