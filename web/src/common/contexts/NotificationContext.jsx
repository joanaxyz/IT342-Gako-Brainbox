import { useState, useCallback, useMemo, useRef, useEffect } from 'react';
import { NotificationContext } from './NotificationContextValue';

export const NotificationProvider = ({ children }) => {
    const [notifications, setNotifications] = useState([]);
    const timeoutsRef = useRef(new Map());

    const removeNotification = useCallback((id) => {
        const timeoutId = timeoutsRef.current.get(id);
        if (timeoutId !== undefined) {
            clearTimeout(timeoutId);
            timeoutsRef.current.delete(id);
        }
        setNotifications(prev => prev.filter(n => n.id !== id));
    }, []);

    const addNotification = useCallback((message, type = 'info', duration = 5000) => {
        const id = Date.now() + Math.random();
        let wasAdded = false;

        setNotifications(prev => {
            if (prev.some(n => n.message === message && n.type === type)) {
                return prev;
            }
            wasAdded = true;
            return [...prev, { id, message, type }];
        });

        if (wasAdded && duration > 0) {
            const timeoutId = setTimeout(() => {
                timeoutsRef.current.delete(id);
                setNotifications(current => current.filter(n => n.id !== id));
            }, duration);

            timeoutsRef.current.set(id, timeoutId);
        }

        return id;
    }, []);

    useEffect(() => {
        return () => {
            timeoutsRef.current.forEach((timeoutId) => clearTimeout(timeoutId));
            timeoutsRef.current.clear();
        };
    }, []);

    const value = useMemo(() => ({
        notifications,
        addNotification,
        removeNotification
    }), [notifications, addNotification, removeNotification]);

    return (
        <NotificationContext.Provider value={value}>
            {children}
        </NotificationContext.Provider>
    );
};
