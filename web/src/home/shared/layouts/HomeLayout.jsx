import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import PlayerBar from '../components/PlayerBar';
import { useNotebook } from '../../../notebook/shared/hooks/hooks';
import '../styles/home.css';

const HomeLayout = () => {
    const { fetchNotebooks } = useNotebook();

    useEffect(() => {
        fetchNotebooks();
    }, [fetchNotebooks]);

    return (
        <div className="home-layout">
            <Sidebar />
            <div className="home-main">
                <div className="home-content page-enter">
                    <Outlet />
                </div>
            </div>
            <PlayerBar />
        </div>
    );
};

export default HomeLayout;
