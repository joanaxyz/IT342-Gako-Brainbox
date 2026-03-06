import { Routes, Route, Navigate } from 'react-router-dom';
import Register from './auth/register/Register';
import Login from './auth/login/Login';
import ForgotPassword from './auth/forgot-password/ForgotPassword';
import Logout from './auth/shared/components/Logout';
import Dashboard from './home/dashboard/Dashboard';
import HomeLayout from './home/shared/layouts/HomeLayout';
import AuthLayout from './auth/shared/layouts/AuthLayout';
import Library from './home/library/Library';
import Quizzes from './home/quizzes/Quizzes';
import Flashcards from './home/flashcards/Flashcards';
import Profile from './home/profile/Profile';
import Playlists from './home/playlists/Playlists';
import EditorLayout from './notebook/editor/layouts/EditorLayout';
import NoteEditorPage from './notebook/editor/NoteEditor';
import ProtectedRoute from './auth/shared/components/ProtectedRoute';
import { AuthProvider } from './auth/shared/contexts/AuthContext';
import { NotebookProvider } from './notebook/shared/contexts/NotebookContext';
import { CategoryProvider } from './notebook/shared/contexts/CategoryContext';
import { AudioPlayerProvider } from './common/contexts/AudioPlayerContext';
import { LoadingProvider } from './common/contexts/ActiveContexts';
import { NotificationProvider } from './common/contexts/NotificationContext';
import { NotificationContainer } from './common/components/Notification';
import LoadingOverlay from './common/components/LoadingOverlay';
import { useLoading } from './common/hooks/hooks';

const Placeholder = ({ title }) => (
  <div style={{ padding: '20px' }}>
    <h1>{title}</h1>
    <p>This section is under development.</p>
  </div>
);

const AppContent = () => {
  const { active: isLoading } = useLoading();
  
  return (
    <>
      <LoadingOverlay isActive={isLoading} />
      <NotificationContainer />
      <Routes>
        <Route element={<AuthLayout />}>
          <Route path="/register" element={<Register/>} />
          <Route path="/login" element={<Login/>} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
        </Route>
        
        <Route element={<ProtectedRoute><HomeLayout /></ProtectedRoute>}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/library" element={<Library />} />
          <Route path="/quizzes" element={<Quizzes />} />
          <Route path="/flashcards" element={<Flashcards />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/playlists" element={<Playlists />} />
        </Route>

        <Route element={<ProtectedRoute><EditorLayout /></ProtectedRoute>}>
          <Route path="/notebook/:id" element={<NoteEditorPage />} />
        </Route>

        <Route path="/logout" element={<Logout />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </>
  );
};

function App() {
  return (
    <NotificationProvider>
      <LoadingProvider>
        <AuthProvider>
          <NotebookProvider>
            <CategoryProvider>
              <AudioPlayerProvider>
                <AppContent />
              </AudioPlayerProvider>
            </CategoryProvider>
          </NotebookProvider>
        </AuthProvider>
      </LoadingProvider>
    </NotificationProvider>
  )
}

export default App
