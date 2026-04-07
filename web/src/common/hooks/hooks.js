import { AudioPlayerContext } from '../contexts/AudioPlayerContextValue';
import { createContextHook } from '../utils/createContextHook';
import { LoadingContext } from '../contexts/ActiveContexts';
import { NotificationContext } from '../contexts/NotificationContextValue';

export const useAudioPlayer = createContextHook(AudioPlayerContext, 'useAudioPlayer', 'AudioPlayerProvider');

export const useLoading = createContextHook(LoadingContext, 'useLoading', 'LoadingProvider');

export const useNotification = createContextHook(
    NotificationContext,
    'useNotification',
    'NotificationProvider'
);
