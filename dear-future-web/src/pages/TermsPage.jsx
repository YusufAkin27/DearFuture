import React from 'react';
import './PrivacyPage.css'; // Reusing legal styles

const TermsPage = () => {
    return (
        <div className="legal-container">
            <div className="legal-content">
                <h1>Terms of Service</h1>
                <p className="last-updated">Last Updated: February 1, 2026</p>

                <section>
                    <h2>1. Acceptance of Terms</h2>
                    <p>
                        By accessing or using DearFuture, you agree to be bound by these Terms of Service.
                    </p>
                </section>

                <section>
                    <h2>2. Use of Service</h2>
                    <p>
                        You agree to use DearFuture only for lawful purposes and in accordance with these Terms.
                        You are responsible for maintaining the confidentiality of your account credentials.
                    </p>
                </section>

                <section>
                    <h2>3. User Content</h2>
                    <p>
                        You retain ownership of the content you create. However, you grant us a license to store
                        and deliver your content as per your instructions.
                    </p>
                </section>

                <section>
                    <h2>4. Termination</h2>
                    <p>
                        We reserve the right to terminate or suspend your account if you violate these Terms.
                    </p>
                </section>

                <section>
                    <h2>5. Disclaimer</h2>
                    <p>
                        The service is provided "as is" without warranties of any kind.
                    </p>
                </section>
            </div>
        </div>
    );
};

export default TermsPage;
