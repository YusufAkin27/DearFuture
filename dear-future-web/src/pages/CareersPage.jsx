import React from 'react';
import './CareersPage.css';

const CareersPage = () => {
    const jobs = [
        {
            title: 'Senior Frontend Engineer',
            department: 'Engineering',
            location: 'Remote',
            type: 'Full-time'
        },
        {
            title: 'Product Designer',
            department: 'Design',
            location: 'New York, NY',
            type: 'Full-time'
        },
        {
            title: 'Customer Success Manager',
            department: 'Support',
            location: 'Remote',
            type: 'Full-time'
        }
    ];

    return (
        <div className="careers-container">
            <div className="careers-header">
                <h1>Join Our Mission</h1>
                <p>Help us build the most lasting platform on the internet.</p>
            </div>

            <div className="jobs-list">
                {jobs.map((job, index) => (
                    <div key={index} className="job-card">
                        <div className="job-info">
                            <h2>{job.title}</h2>
                            <div className="job-meta">
                                <span>{job.department}</span>
                                <span>•</span>
                                <span>{job.location}</span>
                                <span>•</span>
                                <span>{job.type}</span>
                            </div>
                        </div>
                        <button className="apply-btn">Apply Now</button>
                    </div>
                ))}
            </div>
            <div className="no-positions">
                <p>Don't see a role that fits? Email us your resume at careers@dearfuture.com</p>
            </div>
        </div>
    );
};

export default CareersPage;
