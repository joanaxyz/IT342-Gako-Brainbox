import { useContext } from 'react';
import { AudioPlayerContext } from '../contexts/AudioPlayerContext';
import { createContextHook } from '../utils/createContextHook';
import { LoadingContext } from '../contexts/ActiveContexts';
import { NotificationContext } from '../contexts/NotificationContext';

export const useAudioPlayer = createContextHook(AudioPlayerContext, 'useLoading', 'AudioPlayerProvider');

export const useLoading = createContextHook(LoadingContext, 'useLoading', 'LoadingProvider');

export const useNotification = createContextHook(
    NotificationContext,
    'useNotification',
    'NotificationProvider'
);
