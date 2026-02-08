import React from 'react';
import './PressPage.css';

const PressPage = () => {
    return (
        <div className="press-container">
            <div className="press-header">
                <h1>Press & Media</h1>
                <p>Latest news, updates, and brand assets from DearFuture.</p>
            </div>

            <section className="press-section">
                <h2>In the News</h2>
                <div className="press-grid">
                    <div className="press-card">
                        <span className="press-date">Jan 10, 2026</span>
                        <h3>TechCrunch</h3>
                        <p>DearFuture raises Series A to redefine digital legacy.</p>
                        <a href="#" className="read-link">Read Article</a>
                    </div>
                    <div className="press-card">
                        <span className="press-date">Dec 05, 2025</span>
                        <h3>Wired</h3>
                        <p>The time capsule apps securing memories for the next generation.</p>
                        <a href="#" className="read-link">Read Article</a>
                    </div>
                    <div className="press-card">
                        <span className="press-date">Nov 20, 2025</span>
                        <h3>The Verge</h3>
                        <p>How encryption ensures your secrets stay safe until you're gone.</p>
                        <a href="#" className="read-link">Read Article</a>
                    </div>
                </div>
            </section>

            <section className="press-section">
                <h2>Brand Assets</h2>
                <div className="assets-grid">
                    <div className="asset-card">
                        <div className="asset-preview logo-preview"></div>
                        <h3>Logo Pack</h3>
                        <button className="download-btn">Download .ZIP</button>
                    </div>
                    <div className="asset-card">
                        <div className="asset-preview color-preview"></div>
                        <h3>Brand Guidelines</h3>
                        <button className="download-btn">Download .PDF</button>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default PressPage;
