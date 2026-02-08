import React from 'react';
import './PrivacyPage.css';

const PrivacyPage = () => {
    return (
        <div className="legal-container">
            <div className="legal-content">
                <h1>Privacy Policy</h1>
                <p className="last-updated">Last Updated: February 1, 2026</p>

                <section>
                    <h2>1. Introduction</h2>
                    <p>
                        At DearFuture, we prioritize your privacy above all else. This Privacy Policy
                        explains how we collect, use, and protect your information.
                    </p>
                </section>

                <section>
                    <h2>2. Data Collection</h2>
                    <p>
                        We collect minimal data necessary to provide our services, such as your email address
                        for account creation and notification purposes.
                    </p>
                </section>

                <section>
                    <h2>3. End-to-End Encryption</h2>
                    <p>
                        Your messages ("Time Capsules") are encrypted on your device before they reach our servers.
                        We do not have the keys to decrypt your messages. Only your intended recipient can read them.
                    </p>
                </section>

                <section>
                    <h2>4. Data Retention</h2>
                    <p>
                        We retain your data only for as long as necessary to fulfill the purposes outlined in this
                        policy, or as required by law.
                    </p>
                </section>

                <section>
                    <h2>5. Contact Us</h2>
                    <p>
                        If you have any questions about this Privacy Policy, please contact us at privacy@dearfuture.com.
                    </p>
                </section>
            </div>
        </div>
    );
};

export default PrivacyPage;
