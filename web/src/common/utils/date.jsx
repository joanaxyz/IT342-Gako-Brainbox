export const formatUpdatedAt = (dateString) => {
    if (!dateString) return 'Never';

    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInSeconds < 60) {
        return 'just now';
    }

    if (diffInMinutes < 60) {
        return `${diffInMinutes}m ago`;
    }

    if (diffInHours < 24) {
        return `${diffInHours}h ago`;
    }

    if (diffInDays < 7) {
        return `${diffInDays}d ago`;
    }

    // Text format: e.g., "October 24, 2023"
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
};
