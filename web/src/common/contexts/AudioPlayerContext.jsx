import { createContext, useState, useCallback, useRef } from 'react';

export const AudioPlayerContext = createContext(null);

export const AudioPlayerProvider = ({ children }) => {
    const [currentNotebook, setCurrentNotebook] = useState(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [progress, setProgress] = useState(0);
    const [queue, setQueue] = useState([]);
    const [isMinimized, setIsMinimized] = useState(true);

    const synth = window.speechSynthesis;
    const utteranceRef = useRef(null);

    const stopPlayback = useCallback(() => {
        if (synth) {
            synth.cancel();
        }
        setIsPlaying(false);
    }, [synth]);

    const play = useCallback(async (notebook) => {
        if (!notebook) return;

        if (currentNotebook?.id === notebook.id) {
            if (synth.paused) {
                synth.resume();
                setIsPlaying(true);
                return;
            }
            if (isPlaying) return;
        }

        stopPlayback();
        setCurrentNotebook(notebook);
        
        const content = notebook.content?.replace(/<[^>]*>/g, '') || notebook.title;
        const utterance = new SpeechSynthesisUtterance(content);
        
        utterance.onend = () => {
            if (queue.length > 0) {
                const nextNotebook = queue[0];
                setQueue(prev => prev.slice(1));
                play(nextNotebook);
            } else {
                setIsPlaying(false);
                setProgress(100);
            }
        };

        utterance.onboundary = (event) => {
            const totalChars = content.length;
            const currentChars = event.charIndex;
            setProgress((currentChars / totalChars) * 100);
        };

        utteranceRef.current = utterance;
        synth.speak(utterance);
        setIsPlaying(true);
        setProgress(0);
    }, [currentNotebook, isPlaying, synth, stopPlayback, queue]);

    const pause = useCallback(() => {
        if (synth) {
            synth.pause();
        }
        setIsPlaying(false);
    }, [synth]);

    const togglePlay = useCallback(async (notebook) => {
        if (currentNotebook?.id === notebook?.id && isPlaying) {
            pause();
        } else {
            await play(notebook || currentNotebook);
        }
    }, [isPlaying, pause, play, currentNotebook]);

    const addToQueue = useCallback((notebook) => {
        setQueue(prev => {
            if (prev.find(n => n.id === notebook.id)) return prev;
            return [...prev, notebook];
        });
    }, []);

    const removeFromQueue = useCallback((notebookId) => {
        setQueue(prev => prev.filter(n => n.id !== notebookId));
    }, []);

    const clearQueue = useCallback(() => {
        setQueue([]);
    }, []);

    const playNext = useCallback(async () => {
        if (queue.length > 0) {
            const nextNotebook = queue[0];
            setQueue(prev => prev.slice(1));
            await play(nextNotebook);
        } else {
            stopPlayback();
        }
    }, [queue, play, stopPlayback]);

    const value = {
        currentNotebook,
        isPlaying,
        progress,
        queue,
        togglePlay,
        play,
        pause,
        stopPlayback,
        addToQueue,
        removeFromQueue,
        clearQueue,
        playNext,
        isMinimized,
        setIsMinimized
    };

    return (
        <AudioPlayerContext.Provider value={value}>
            {children}
        </AudioPlayerContext.Provider>
    );
};

