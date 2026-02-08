import React, { useState } from 'react';
import './ContactPage.css';
import { FaEnvelope, FaMapMarkerAlt, FaPhone } from 'react-icons/fa';

const ContactPage = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        message: ''
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        alert('Message sent! (Mock Action)');
    };

    return (
        <div className="contact-container">
            <div className="contact-header">
                <h1>By All Means, Contact Us</h1>
                <p>We'd love to hear from you. Here's how to reach us.</p>
            </div>

            <div className="contact-grid">
                <div className="contact-info">
                    <div className="info-item">
                        <FaEnvelope className="info-icon" />
                        <div>
                            <h3>Email Us</h3>
                            <p>hello@dearfuture.com</p>
                            <p>support@dearfuture.com</p>
                        </div>
                    </div>
                    <div className="info-item">
                        <FaMapMarkerAlt className="info-icon" />
                        <div>
                            <h3>Visit Us</h3>
                            <p>123 Future Street</p>
                            <p>Innovation City, 34000</p>
                        </div>
                    </div>
                    <div className="info-item">
                        <FaPhone className="info-icon" />
                        <div>
                            <h3>Call Us</h3>
                            <p>+1 (555) 123-4567</p>
                        </div>
                    </div>
                </div>

                <form className="contact-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="message">Message</label>
                        <textarea
                            id="message"
                            name="message"
                            value={formData.message}
                            onChange={handleChange}
                            rows="5"
                            required
                        ></textarea>
                    </div>
                    <button type="submit" className="submit-btn">Send Message</button>
                </form>
            </div>
        </div>
    );
};

export default ContactPage;
