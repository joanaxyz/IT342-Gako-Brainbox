import React from 'react';
import { 
    X, 
    CheckCircle2, 
    AlertCircle, 
    AlertTriangle, 
    Info 
} from 'lucide-react';
import { useNotification } from '../hooks/hooks';

const getIcon = (type) => {
    switch (type) {
        case 'success':
            return <CheckCircle2 size={18} className="notification-icon success" />;
        case 'error':
            return <AlertCircle size={18} className="notification-icon error" />;
        case 'warning':
            return <AlertTriangle size={18} className="notification-icon warning" />;
        case 'info':
        default:
            return <Info size={18} className="notification-icon info" />;
    }
};

export const Notification = ({ notification }) => {
    const { removeNotification } = useNotification();

    return (
        <div className={`notification notification-${notification.type}`}>
            <div className="notification-content">
                {getIcon(notification.type)}
                <span className="notification-message">{notification.message}</span>
            </div>
            <button 
                className="notification-close" 
                onClick={() => removeNotification(notification.id)}
                aria-label="Close notification"
            >
                <X size={16} />
            </button>
        </div>
    );
};

export const NotificationContainer = () => {
    const { notifications } = useNotification();

    return (
        <div className="notification-container">
            {notifications.map(notification => (
                <Notification key={notification.id} notification={notification} />
            ))}
        </div>
    );
};
