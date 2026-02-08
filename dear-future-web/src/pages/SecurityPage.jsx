import React from 'react';
import './PrivacyPage.css'; // Reusing legal styles

const SecurityPage = () => {
    return (
        <div className="legal-container">
            <div className="legal-content">
                <h1>Security</h1>
                <p className="last-updated">Last Updated: February 1, 2026</p>

                <section>
                    <h2>Our Security Architecture</h2>
                    <p>
                        Security is not an afterthought at DearFuture; it is the foundation of our platform.
                        We use industry-leading practices to ensure your digital legacy remains secure for decades.
                    </p>
                </section>

                <section>
                    <h2>Zero-Knowledge Encryption</h2>
                    <p>
                        All messages contents are encrypted using AES-256 GCM on the client side before being transmitted.
                        Only the intended recipient possesses the decryption key. DearFuture staff cannot read your messages.
                    </p>
                </section>

                <section>
                    <h2>Infrastructure Security</h2>
                    <p>
                        Our servers are hosted in secure, ISO 27001 certified data centers. We employ strict access controls,
                        firewalls, and regular security audits.
                    </p>
                </section>

                <section>
                    <h2>Data Integrity</h2>
                    <p>
                        We use cryptographic hashing to ensure that your data has not been tampered with during storage or transit.
                    </p>
                </section>

                <section>
                    <h2>Reporting Vulnerabilities</h2>
                    <p>
                        We maintain a bug bounty program. If you believe you have found a security vulnerability,
                        please report it to security@dearfuture.com.
                    </p>
                </section>
            </div>
        </div>
    );
};

export default SecurityPage;
