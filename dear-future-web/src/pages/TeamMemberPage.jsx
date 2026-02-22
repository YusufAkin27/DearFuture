import { useEffect, useState } from 'react';
import { useParams, Link, Navigate } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import { FaArrowLeft, FaBriefcase, FaGithub, FaLinkedin, FaInstagram, FaGlobe, FaEnvelope, FaExternalLinkAlt, FaTimes } from 'react-icons/fa';
import { getTeamMemberBySlug } from '../data/teamMembers';
import { getGitHubUser } from '../api/team';
import './TeamMemberPage.css';

/** README Markdown: kod blokları gösterilmez, metin güzel formatlanır */
const ReadmeMarkdown = ({ content }) => (
    <div className="team-member-readme-md">
        <ReactMarkdown
            components={{
            code: () => null,
            pre: () => null,
            a: ({ href, children }) => (
                <a href={href} target="_blank" rel="noopener noreferrer" className="team-member-readme-link">
                    {children}
                </a>
            ),
        }}
        >
            {content}
        </ReactMarkdown>
    </div>
);

const BASE_TITLE = 'Dear Future – Geleceğe Mesaj Yaz';

const TeamMemberPage = () => {
    const { slug } = useParams();
    const member = getTeamMemberBySlug(slug);
    const [githubData, setGithubData] = useState(null);
    const [githubLoading, setGithubLoading] = useState(false);
    /** Tıklanan proje: README modalda açılır */
    const [readmeModal, setReadmeModal] = useState(null);

    useEffect(() => {
        if (member) document.title = `${member.name} | ${BASE_TITLE}`;
    }, [member]);

    useEffect(() => {
        if (!member?.githubUsername) return;
        setGithubLoading(true);
        getGitHubUser(member.githubUsername)
            .then(setGithubData)
            .catch(() => setGithubData(null))
            .finally(() => setGithubLoading(false));
    }, [member?.githubUsername]);

    useEffect(() => {
        if (!readmeModal) return;
        const onEscape = (e) => { if (e.key === 'Escape') setReadmeModal(null); };
        document.addEventListener('keydown', onEscape);
        document.body.style.overflow = 'hidden';
        return () => {
            document.removeEventListener('keydown', onEscape);
            document.body.style.overflow = '';
        };
    }, [readmeModal]);

    if (!member) {
        return <Navigate to="/about" replace />;
    }

    const links = [
        member.portfolioUrl && { url: member.portfolioUrl, label: 'Portföy', icon: FaGlobe },
        member.linkedInUrl && { url: member.linkedInUrl, label: 'LinkedIn', icon: FaLinkedin },
        member.instagramUrl && { url: member.instagramUrl, label: 'Instagram', icon: FaInstagram },
        member.githubUsername && githubData?.profileUrl && { url: githubData.profileUrl, label: 'GitHub', icon: FaGithub },
        member.email && { url: `mailto:${member.email}`, label: 'E-posta', icon: FaEnvelope },
    ].filter(Boolean);

    const repos = githubData?.repos?.length ? githubData.repos : null;

    return (
        <section className="team-member-container">
            <div className="team-member-inner">
                <Link to="/about" className="team-member-back">
                    <FaArrowLeft aria-hidden="true" />
                    <span>Hakkımızda</span>
                </Link>

                <header className="team-member-header">
                    <div className="team-member-photo-wrap">
                        <img
                            src={member.image}
                            alt={member.name}
                            className="team-member-photo"
                        />
                    </div>
                    <h1 className="team-member-name">{member.name}</h1>
                    <p className="team-member-role">{member.role}</p>
                    {links.length > 0 && (
                        <div className="team-member-links">
                            {links.map(({ url, label, icon: Icon }) => (
                                <a
                                    key={label}
                                    href={url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="team-member-link"
                                    aria-label={label}
                                >
                                    <Icon aria-hidden="true" />
                                    <span>{label}</span>
                                </a>
                            ))}
                        </div>
                    )}
                </header>

                <div className="team-member-bio">
                    <h2>Özgeçmiş</h2>
                    <p className="team-member-bio-text">{member.bio}</p>
                </div>

                {(githubLoading || (repos && repos.length) || (member.projects && member.projects.length > 0)) && (
                    <div className="team-member-projects">
                        <h2>
                            <FaBriefcase className="team-member-projects-icon" aria-hidden="true" />
                            Projeler
                        </h2>
                        {githubLoading && <p className="team-member-loading">GitHub projeleri yükleniyor...</p>}
                        {repos && !githubLoading && (
                            <ul className="team-member-project-list">
                                {repos.map((repo, index) => (
                                    <li key={index} className="team-member-project-item team-member-repo-card">
                                        <div
                                            className="team-member-repo-card-inner"
                                            role="button"
                                            tabIndex={0}
                                            onClick={() => repo.readmeContent && setReadmeModal(repo)}
                                            onKeyDown={(e) => {
                                                if ((e.key === 'Enter' || e.key === ' ') && repo.readmeContent) {
                                                    e.preventDefault();
                                                    setReadmeModal(repo);
                                                }
                                            }}
                                            aria-label={`${repo.name} – README aç`}
                                        >
                                            <strong className="team-member-repo-name">{repo.name}</strong>
                                            {repo.description && <p className="team-member-repo-desc">{repo.description}</p>}
                                            {repo.readmeContent && (
                                                <span className="team-member-repo-readme-hint">README’yi görmek için tıklayın</span>
                                            )}
                                        </div>
                                        <a
                                            href={repo.htmlUrl}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="team-member-repo-github-link"
                                            aria-label={`${repo.name} GitHub’da aç`}
                                            onClick={(e) => e.stopPropagation()}
                                        >
                                            <FaExternalLinkAlt />
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        )}
                        {!repos && !githubLoading && member.projects?.length > 0 && (
                            <ul className="team-member-project-list">
                                {member.projects.map((project, index) => (
                                    <li key={index} className="team-member-project-item">
                                        <strong>{project.name}</strong>
                                        {project.description && <span> – {project.description}</span>}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}
            </div>

            {readmeModal && (
                <div
                    className="team-member-readme-overlay"
                    role="dialog"
                    aria-modal="true"
                    aria-labelledby="readme-modal-title"
                    onClick={() => setReadmeModal(null)}
                >
                    <div
                        className="team-member-readme-modal"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="team-member-readme-modal-header">
                            <h2 id="readme-modal-title">{readmeModal.name}</h2>
                            <a
                                href={readmeModal.htmlUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="team-member-readme-modal-github"
                            >
                                GitHub’da aç <FaExternalLinkAlt />
                            </a>
                            <button
                                type="button"
                                className="team-member-readme-modal-close"
                                onClick={() => setReadmeModal(null)}
                                aria-label="Kapat"
                            >
                                <FaTimes />
                            </button>
                        </div>
                        <div className="team-member-readme-modal-body">
                            <ReadmeMarkdown content={readmeModal.readmeContent} />
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
};

export default TeamMemberPage;
