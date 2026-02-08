import React from 'react';
import './BlogPage.css';

const BlogPage = () => {
    const posts = [
        {
            id: 1,
            title: 'The Importance of Digital Legacies',
            excerpt: 'Why preparing your digital footprint for the future matters more than ever in the 21st century.',
            date: 'Oct 15, 2025',
            author: 'Sarah Johnson',
            category: 'Legacy',
            image: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
        },
        {
            id: 2,
            title: '5 Ways to Use Time Capsules',
            excerpt: 'Creative ideas for scheduled messages that will surprise and delight your loved ones.',
            date: 'Nov 02, 2025',
            author: 'Michael Chen',
            category: 'Tips',
            image: 'linear-gradient(135deg, #3b82f6, #06b6d4)',
        },
        {
            id: 3,
            title: 'Security at DearFuture',
            excerpt: 'A deep dive into our zero-knowledge encryption architecture and how we protect your data.',
            date: 'Nov 20, 2025',
            author: 'Alex Davila',
            category: 'Engineering',
            image: 'linear-gradient(135deg, #10b981, #3b82f6)',
        },
    ];

    return (
        <div className="blog-container">
            <div className="blog-header">
                <h1>DearFuture Blog</h1>
                <p>Stories about time, memory, and the future.</p>
            </div>

            <div className="blog-grid">
                {posts.map((post) => (
                    <article key={post.id} className="blog-card">
                        <div className="blog-image" style={{ background: post.image }}></div>
                        <div className="blog-content">
                            <div className="blog-meta">
                                <span className="category">{post.category}</span>
                                <span className="date">{post.date}</span>
                            </div>
                            <h2>{post.title}</h2>
                            <p>{post.excerpt}</p>
                            <div className="blog-footer">
                                <span className="author">By {post.author}</span>
                                <button className="read-more">Read More →</button>
                            </div>
                        </div>
                    </article>
                ))}
            </div>
        </div>
    );
};

export default BlogPage;
