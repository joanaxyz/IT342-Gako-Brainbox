import { useEffect, useMemo, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Menu } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import PlayerBar from '../components/PlayerBar';
import QueuePanel from '../components/QueuePanel';
import { useNotebook } from '../../../notebook/shared/hooks/hooks';
import { useAudioPlayer } from '../../../common/hooks/hooks';
import '../styles/home.css';

const MOBILE_PAGE_LABELS = {
    '/dashboard': 'Dashboard',
    '/library': 'Library',
    '/quizzes': 'Quizzes',
    '/flashcards': 'Flashcards',
    '/playlists': 'Playlists',
    '/profile': 'Profile',
};

const HomeLayout = () => {
    const { fetchNotebooks } = useNotebook();
    const { showQueue } = useAudioPlayer();
    const { pathname } = useLocation();
    const [isNavOpen, setIsNavOpen] = useState(false);

    useEffect(() => {
        fetchNotebooks();
    }, [fetchNotebooks]);

    useEffect(() => {
        setIsNavOpen(false);
    }, [pathname]);

    useEffect(() => {
        if (!isNavOpen) {
            return undefined;
        }

        const handleKeyDown = (event) => {
            if (event.key === 'Escape') {
                setIsNavOpen(false);
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [isNavOpen]);

    useEffect(() => {
        if (typeof window === 'undefined' || !isNavOpen || window.innerWidth > 1024) {
            return undefined;
        }

        const { overflow } = document.body.style;
        document.body.style.overflow = 'hidden';

        return () => {
            document.body.style.overflow = overflow;
        };
    }, [isNavOpen]);

    const currentPageLabel = useMemo(() => {
        if (pathname.startsWith('/notebook/')) {
            return 'Notebook';
        }

        return MOBILE_PAGE_LABELS[pathname] || 'Workspace';
    }, [pathname]);

    return (
        <div className={`home-layout${showQueue ? ' queue-open' : ''}${isNavOpen ? ' nav-open' : ''}`}>
            {isNavOpen && (
                <button
                    type="button"
                    className="home-sidebar-backdrop"
                    onClick={() => setIsNavOpen(false)}
                    aria-label="Close navigation"
                />
            )}
            <Sidebar isOpen={isNavOpen} onClose={() => setIsNavOpen(false)} />
            <div className="home-main">
                <div className="home-mobile-topbar">
                    <button
                        type="button"
                        className="home-mobile-nav-trigger"
                        onClick={() => setIsNavOpen(true)}
                        aria-label="Open navigation"
                    >
                        <Menu size={18} />
                    </button>
                    <div className="home-mobile-topbar-copy">
                        <span className="home-mobile-topbar-brand">BrainBox</span>
                        <span className="home-mobile-topbar-label">{currentPageLabel}</span>
                    </div>
                </div>
                <div className="home-content page-enter">
                    <Outlet />
                </div>
            </div>
            <QueuePanel />
            <PlayerBar />
        </div>
    );
};

export default HomeLayout;
