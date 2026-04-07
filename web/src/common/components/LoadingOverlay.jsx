import React from 'react';
import '../styles/loading_overlay.css';

const LoadingOverlay = ({ isActive }) => {
    return (
        <div className={`loading-overlay ${isActive ? 'active' : ''}`}>
            <div className="running-box-container">
                <div className="logo-box">
                    <div className="box-face top"></div>
                    <div className="box-face left"></div>
                    <div className="box-face right"></div>
                    <div className="box-face back"></div>
                    <div className="box-face bottom"></div>
                    <div className="box-face left-back"></div>
                </div>
                <div className="shadow"></div>
            </div>
        </div>
    );
};

export default LoadingOverlay;
