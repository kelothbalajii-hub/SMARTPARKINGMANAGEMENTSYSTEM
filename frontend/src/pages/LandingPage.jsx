import React from 'react';
import { Link } from 'react-router-dom';

function LandingPage() {
  const features = [
    { icon: '🚗', title: 'Easy Reservation', desc: 'Reserve parking spots in advance with just a few clicks.' },
    { icon: '📊', title: 'Real-time Status', desc: 'See live availability of parking slots across all areas.' },
    { icon: '💰', title: 'Auto Fee Calculation', desc: 'Parking fees are calculated automatically on exit.' },
    { icon: '📍', title: 'Multiple Areas', desc: 'Manage multiple parking areas across your campus.' },
    { icon: '👤', title: 'User Management', desc: 'Easy account management for students and staff.' },
    { icon: '📜', title: 'Parking History', desc: 'Complete transaction records and parking history.' },
  ];

  return (
    <div className="landing-page">
      {/* Navbar */}
      <nav className="landing-navbar">
        <div className="landing-logo">
          <span>🅿️</span>
          <span>Smart Parking</span>
        </div>
        <div className="landing-nav-links">
          <Link to="/login" className="btn btn-secondary">Login</Link>
          <Link to="/register" className="btn btn-primary">Register</Link>
        </div>
      </nav>

      {/* Hero */}
      <div className="landing-hero">
        <div style={{ fontSize: '5rem', marginBottom: '20px' }}>🅿️</div>
        <h1>Smart Parking<br />Management System</h1>
        <p>
          A complete digital solution for managing university parking.
          Reserve slots, track sessions, and calculate fees automatically.
        </p>
        <div className="landing-hero-buttons">
          <Link to="/register" className="btn btn-primary btn-lg">
            🚀 Get Started
          </Link>
          <Link to="/login" className="btn" style={{ background: 'rgba(255,255,255,0.15)', color: 'white', border: '1px solid rgba(255,255,255,0.3)' }}>
            Sign In →
          </Link>
        </div>


      </div>

      {/* Features */}
      <div className="landing-features">
        {features.map((f, i) => (
          <div key={i} className="feature-card">
            <div className="feature-icon">{f.icon}</div>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div style={{ textAlign: 'center', padding: '24px', color: 'rgba(255,255,255,0.5)', fontSize: '0.85rem' }}>
        <p>🎓 Anurag University | Full Stack Java College Project</p>
        <p style={{ marginTop: '4px' }}>Smart Parking Management System © 2024</p>
      </div>
    </div>
  );
}

export default LandingPage;
