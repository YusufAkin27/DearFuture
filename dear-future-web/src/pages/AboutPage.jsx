import React from 'react';
import './AboutPage.css';

const AboutPage = () => {
    return (
        <div className="about-container">
            <div className="about-hero">
                <h1>Our Mission is to Preserve Time</h1>
                <p>We believe that every moment, every thought, and every feeling deserves to be remembered.</p>
            </div>

            <div className="about-section">
                <div className="about-content">
                    <h2>Who We Are</h2>
                    <p>
                        DearFuture was born from a simple idea: What if you could talk to the future?
                        We are a team of dreamers, engineers, and memory-keepers dedicated to building
                        the most secure and intuitive digital time capsule platform in the world.
                    </p>
                </div>
                <div className="about-image placeholder-image"></div>
            </div>

            <div className="about-section reverse">
                <div className="about-content">
                    <h2>Why We Do It</h2>
                    <p>
                        In a fast-paced world where digital content disappears as quickly as it appears,
                        we want to create a slow-moving sanctuary. A place where messages travel at the
                        speed of life, arriving exactly when they are needed most.
                    </p>
                </div>
                <div className="about-image placeholder-image"></div>
            </div>

            <div className="team-section">
                <h2>Meet the Team</h2>
                <div className="team-grid">
                    {[1, 2, 3].map((member) => (
                        <div key={member} className="team-member">
                            <div className="member-avatar"></div>
                            <h3>Team Member {member}</h3>
                            <p>Co-Founder</p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default AboutPage;
