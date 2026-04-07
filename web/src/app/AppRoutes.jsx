import { lazy, Suspense } from 'react';
import { createBrowserRouter, createRoutesFromElements, Navigate, Outlet, Route } from 'react-router-dom';
import App from '../App';
import AuthLayout from '../auth/shared/layouts/AuthLayout';
import Logout from '../auth/shared/components/Logout';
import ProtectedRoute from '../auth/shared/components/ProtectedRoute';
import HomeLayout from '../home/shared/layouts/HomeLayout';
import EditorLayout from '../notebook/editor/layouts/EditorLayout';
import RouteFallback from './RouteFallback';

const RegisterPage = lazy(() => import('../auth/register/Register'));
const LoginPage = lazy(() => import('../auth/login/Login'));
const ForgotPasswordPage = lazy(() => import('../auth/forgot-password/ForgotPassword'));
const DashboardPage = lazy(() => import('../home/dashboard/Dashboard'));
const LibraryPage = lazy(() => import('../home/library/Library'));
const QuizzesPage = lazy(() => import('../home/quizzes/Quizzes'));
const FlashcardsPage = lazy(() => import('../home/flashcards/Flashcards'));
const ProfilePage = lazy(() => import('../home/profile/Profile'));
const PlaylistsPage = lazy(() => import('../home/playlists/Playlists'));
const NoteEditorPage = lazy(() => import('../notebook/editor/NoteEditor'));

const SuspenseLayout = () => (
  <Suspense fallback={<RouteFallback />}>
    <Outlet />
  </Suspense>
);

export const appRouter = createBrowserRouter(
  createRoutesFromElements(
    <Route element={<App />}>
      <Route element={<SuspenseLayout />}>
        <Route element={<AuthLayout />}>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        </Route>

        <Route element={<ProtectedRoute><HomeLayout /></ProtectedRoute>}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/quizzes" element={<QuizzesPage />} />
          <Route path="/flashcards" element={<FlashcardsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/playlists" element={<PlaylistsPage />} />
        </Route>

        <Route element={<ProtectedRoute><EditorLayout /></ProtectedRoute>}>
          <Route path="/notebook/:id" element={<NoteEditorPage />} />
        </Route>

        <Route path="/logout" element={<Logout />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Route>
  )
);
