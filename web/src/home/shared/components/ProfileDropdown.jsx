import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import Modal from '../../../common/components/Modal';

const ProfileDropdown = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const dropdownRef = useRef(null);
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const toggleDropdown = () => setIsOpen(!isOpen);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogoutClick = () => {
        setIsOpen(false);
        setShowLogoutModal(true);
    };

    const confirmLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="profile-dropdown" ref={dropdownRef}>
            <button className="profile-trigger" onClick={toggleDropdown}>
                <div className="avatar">
                    {user?.username?.[0]?.toUpperCase() || 'U'}
                </div>
                <span>{user?.username || 'User'}</span>
            </button>
            <div className={`dropdown-menu ${isOpen ? 'active' : ''}`}>
                <Link to="/profile" className="dropdown-item" onClick={() => setIsOpen(false)}>
                    View Profile
                </Link>
                <div className="dropdown-divider" />
                <button className="dropdown-item logout-item" onClick={handleLogoutClick}>
                    Logout
                </button>
            </div>

            <Modal
                isOpen={showLogoutModal}
                onClose={() => setShowLogoutModal(false)}
                title="Confirm Logout"
            >
                <p>Are you sure you want to log out?</p>
                <div className="modal-actions">
                    <button type="button" className="btn btn-secondary" onClick={() => setShowLogoutModal(false)}>
                        Cancel
                    </button>
                    <button type="button" className="btn btn-danger" onClick={confirmLogout}>
                        Logout
                    </button>
                </div>
            </Modal>
        </div>
    );
};

export default ProfileDropdown;
