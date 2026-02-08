import React from 'react';
import './FeaturesPage.css';
import { FaLock, FaClock, FaVideo, FaEnvelopeOpenText } from 'react-icons/fa';

const FeaturesPage = () => {
    const features = [
        {
            icon: <FaClock />,
            title: 'Time Capsules',
            description: 'Schedule messages to be delivered years into the future. Perfect for birthdays, anniversaries, or advice for your children.',
        },
        {
            icon: <FaLock />,
            title: 'End-to-End Encryption',
            description: 'Your memories are safe with us. We use military-grade encryption to ensure only your intended recipient can read your messages.',
        },
        {
            icon: <FaVideo />,
            title: 'Multi-Media Support',
            description: 'Don\'t just write text. Attach photos, record voice notes, or film video messages to capture the full emotion of the moment.',
        },
        {
            icon: <FaEnvelopeOpenText />,
            title: 'Delivery Confirmation',
            description: 'Get notified when your message is delivered and read. Know that your legacy has reached its destination.',
        },
    ];

    return (
        <div className="features-container">
            <div className="features-hero">
                <h1>Features built for your legacy</h1>
                <p>Everything you need to leave a lasting impact on the future.</p>
            </div>

            <div className="features-grid">
                {features.map((feature, index) => (
                    <div key={index} className="feature-card">
                        <div className="feature-icon">{feature.icon}</div>
                        <h2>{feature.title}</h2>
                        <p>{feature.description}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default FeaturesPage;
