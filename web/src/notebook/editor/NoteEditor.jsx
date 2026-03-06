import { useState, useCallback, useRef } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { useNotebook } from '../shared/hooks/hooks';
import { useEditorResize } from './hooks/useEditorResize';
import { useNoteEditorData } from './hooks/useNoteEditorData';
import EditorNavbar from './components/EditorNavbar/EditorNavbar';
import FormatToolbar, { EDITOR_FONTS } from './components/FormatToolbar/FormatToolbar';
import NoteEditorContent from './components/NoteEditorContent/NoteEditorContent';
import OutlineNav from './components/OutlineNav/OutlineNav';
import AiSidebar from './components/AiSidebar/AiSidebar';
import AiFab from './components/AiFab/AiFab';
import ReviewMode from './components/ReviewMode/ReviewMode';
import CreateQuizModal from '../../home/quizzes/components/CreateQuizModal';
import CreateDeckModal from '../../home/flashcards/components/CreateDeckModal';
import { useAudioPlayer } from '../../common/hooks/hooks';
import './editor.css';

const NoteEditor = () => {
  const { id } = useParams();
  const { state: locationState } = useLocation();
  
  const editorBodyRef = useRef(null);
  const editorRef = useRef(null);
  const { editorMaxWidth, handleMouseDown } = useEditorResize(editorBodyRef);
  
  const { currentNotebook, notebooks, fetchNotebook, updateNotebook } = useNotebook();

  useNoteEditorData({
    id,
    currentNotebook,
    fetchNotebook,
  });

  const [outline, setOutline] = useState([]);

  const [aiSidebarOpen, setAiSidebarOpen] = useState(false);
  const [activeEditor, setActiveEditor] = useState(null);
  const [isReviewModeOpen, setIsReviewModeOpen] = useState(locationState?.mode === 'review');
  const [showCreateQuiz, setShowCreateQuiz] = useState(false);
  const [showCreateFlashcards, setShowCreateFlashcards] = useState(false);
  const [editorFont, setEditorFont] = useState('default');
  const [showLines, setShowLines] = useState(false);

  const { togglePlay } = useAudioPlayer();

  const fontFamily = EDITOR_FONTS.find((f) => f.value === editorFont)?.family ?? 'inherit';

  const notebookTitle = id === 'new'
    ? (locationState?.title || 'New notebook')
    : (currentNotebook?.title || 'Loading...');

  const handleUpdateNotebookTitle = useCallback((newTitle) => {
    if (currentNotebook?.id) {
      updateNotebook(currentNotebook.id, { title: newTitle });
    }
  }, [currentNotebook, updateNotebook]);

  const handleUpdateNotebookContent = useCallback((newContent) => {
    if (currentNotebook?.id) {
      updateNotebook(currentNotebook.id, { content: newContent });
    }
  }, [currentNotebook, updateNotebook]);

  const handleTogglePlay = useCallback(() => {
    togglePlay(currentNotebook);
  }, [togglePlay, currentNotebook]);

  const handleSelectHeading = useCallback((pos) => {
    editorRef.current?.scrollToHeading(pos);
  }, []);

  return (
    <>
      <EditorNavbar
        notebookTitle={notebookTitle}
        onTitleChange={handleUpdateNotebookTitle}
        onReviewMode={() => setIsReviewModeOpen(true)}
      />
      <FormatToolbar
        editor={activeEditor}
        font={editorFont}
        onFontChange={setEditorFont}
        showLines={showLines}
        onLinesToggle={() => setShowLines((v) => !v)}
      />
      <div className="editor-body" ref={editorBodyRef}>
        <OutlineNav 
          outline={outline} 
          onSelect={handleSelectHeading} 
        />
        <main className="editor-main">
          <div 
            className="editor-container"
            style={{ maxWidth: `${editorMaxWidth}px` }}
          >
            <div 
              className="editor-resize-handle editor-resize-handle-left" 
              onMouseDown={handleMouseDown}
            />
            <div 
              className="editor-resize-handle editor-resize-handle-right" 
              onMouseDown={handleMouseDown}
            />
            {currentNotebook && (
              <NoteEditorContent 
                ref={editorRef}
                content={currentNotebook.content || ''} 
                onUpdateContent={handleUpdateNotebookContent} 
                onFocus={setActiveEditor}
                fontFamily={fontFamily}
                showLines={showLines}
                onOutlineChange={setOutline}
              />
            )}
          </div>
        </main>
      </div>

      <AiFab onClick={() => setAiSidebarOpen((o) => !o)} isActive={aiSidebarOpen} />
      <AiSidebar
        isOpen={aiSidebarOpen}
        onClose={() => setAiSidebarOpen(false)}
        onCreateQuiz={() => setShowCreateQuiz(true)}
        onCreateFlashcards={() => setShowCreateFlashcards(true)}
      />
      
      <ReviewMode 
        isOpen={isReviewModeOpen}
        onClose={() => setIsReviewModeOpen(false)}
        notebookTitle={notebookTitle}
        onTogglePlay={handleTogglePlay}
        content={currentNotebook?.content || ''}
        outline={outline}
      />

      <CreateQuizModal
        isOpen={showCreateQuiz}
        onClose={() => setShowCreateQuiz(false)}
        onCreate={() => {}}
        notebooks={notebooks}
        preselectedNotebookId={currentNotebook?.id ?? null}
      />

      <CreateDeckModal
        isOpen={showCreateFlashcards}
        onClose={() => setShowCreateFlashcards(false)}
        onCreate={() => {}}
        notebooks={notebooks}
        preselectedNotebookId={currentNotebook?.id ?? null}
      />
    </>
  );
};

export default NoteEditor;
