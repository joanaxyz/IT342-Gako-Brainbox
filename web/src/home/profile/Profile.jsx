import React from 'react';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import '../dashboard/styles/dashboard.css';

const Profile = () => {
    const { user } = useAuth();

    return (
        <div className="profile-container">
            <div className="profile-header">
                <h1>My Profile</h1>
                <p>Manage your account settings and preferences.</p>
            </div>
            
            <div className="profile-card">
                <div className="profile-info-section">
                    <h2>Personal Information</h2>
                    <div className="info-grid">
                        <div className="info-item">
                            <label>Username</label>
                            <p>{user?.username || 'Loading...'}</p>
                        </div>
                        <div className="info-item">
                            <label>Email Address</label>
                            <p>{user?.email || 'Loading...'}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;
