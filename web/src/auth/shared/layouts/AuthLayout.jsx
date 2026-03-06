import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import '../styles/AuthLayout.css';
import logo from '../../../assets/logo.svg';

/**
 * AuthLayout - A reusable layout component for authentication pages using Outlet.
 */
const AuthLayout = () => {
  const [header, setHeader] = useState({ title: '', subtitle: '' });

  return (
    <div className="auth-page-container">
      <div className="auth-left-panel">
        <div className="auth-brand">
          <img src={logo} alt="BrainBox Logo" className="auth-logo" />
          <h2 className="auth-brand-name">BrainBox</h2>
        </div>
        <p className="auth-description">
          BrainBox isn’t a notebook. It’s a place to offload memory, organize thought, and recall with clarity.
        </p>
      </div>
      <div className="auth-right-panel">
        <div className="auth-card">
          <header className="auth-header">
            <h1 className="auth-title">{header.title}</h1>
            {header.subtitle && <p className="auth-subtitle">{header.subtitle}</p>}
          </header>
          <main className="auth-content">
            <Outlet context={{ setHeader }} />
          </main>
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;
