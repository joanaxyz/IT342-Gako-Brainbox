import { QueryClientProvider } from '@tanstack/react-query';
import { NotificationProvider } from '../common/contexts/NotificationContext';
import { LoadingProvider } from '../common/contexts/ActiveContexts';
import { AuthProvider } from '../auth/shared/contexts/AuthContext';
import { NotebookProvider } from '../notebook/shared/contexts/NotebookContext';
import { CategoryProvider } from '../notebook/shared/contexts/CategoryContext';
import { PlaylistProvider } from '../notebook/shared/contexts/PlaylistContext';
import { QuizProvider } from '../notebook/shared/contexts/QuizContext';
import { FlashcardProvider } from '../notebook/shared/contexts/FlashcardContext';
import { AudioPlayerProvider } from '../common/contexts/AudioPlayerContext';
import { SettingsModalProvider } from '../common/contexts/SettingsModalContext';
import { appQueryClient } from '../common/query/queryClient';
import { QuerySyncBridge } from '../common/query/QuerySyncBridge';

const AppProviders = ({ children }) => (
  <NotificationProvider>
    <QueryClientProvider client={appQueryClient}>
      <QuerySyncBridge />
      <LoadingProvider>
        <AuthProvider>
          <SettingsModalProvider>
          <NotebookProvider>
            <CategoryProvider>
              <PlaylistProvider>
                <QuizProvider>
                  <FlashcardProvider>
                    <AudioPlayerProvider>
                      {children}
                    </AudioPlayerProvider>
                  </FlashcardProvider>
                </QuizProvider>
              </PlaylistProvider>
            </CategoryProvider>
          </NotebookProvider>
          </SettingsModalProvider>
        </AuthProvider>
      </LoadingProvider>
    </QueryClientProvider>
  </NotificationProvider>
);

export default AppProviders;
