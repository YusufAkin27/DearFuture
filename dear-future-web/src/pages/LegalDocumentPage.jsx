import { useState, useEffect } from 'react';
import { getContractByType } from '../api/contracts';
import './LegalPage.css';

const PILL_LABELS = {
    GIZLILIK: 'Gizlilik',
    KULLANIM: 'Yasal',
    CEREZ: 'Çerezler',
    KVKK: 'KVKK',
    SATIS: 'Satış',
    IADE: 'İade',
    KARGO: 'Kargo',
};

/**
 * Backend'den sözleşme türüne göre içerik çeker ve gösterir.
 * @param {Object} props
 * @param {string} props.contractType - ContractType: GIZLILIK, KULLANIM, CEREZ, vb.
 */
const LegalDocumentPage = ({ contractType }) => {
    const [contract, setContract] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        setError(null);
        getContractByType(contractType)
            .then((res) => {
                if (!cancelled) {
                    setContract(res.data);
                }
            })
            .catch((err) => {
                if (!cancelled) {
                    setError(err.response?.status === 404 ? 'İçerik bulunamadı.' : 'İçerik yüklenemedi.');
                }
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => { cancelled = true; };
    }, [contractType]);

    if (loading) {
        return (
            <section className="legal-container">
                <div className="legal-inner">
                    <div className="legal-loading">
                        <div className="legal-spinner" />
                        <p>Yükleniyor...</p>
                    </div>
                </div>
            </section>
        );
    }

    if (error || !contract) {
        return (
            <section className="legal-container">
                <div className="legal-inner">
                    <div className="legal-error">
                        <p>{error || 'İçerik bulunamadı.'}</p>
                    </div>
                </div>
            </section>
        );
    }

    const updatedAt = contract.updatedAt
        ? new Date(contract.updatedAt).toLocaleDateString('tr-TR', { year: 'numeric', month: 'long', day: 'numeric' })
        : null;

    return (
        <section className="legal-container">
            <div className="legal-inner">
                <header className="legal-hero">
                    <span className="legal-pill">{PILL_LABELS[contract.type] || contract.type}</span>
                    <h1>{contract.title}</h1>
                    {updatedAt && <p className="legal-updated">Son güncelleme: {updatedAt}</p>}
                </header>

                <div className="legal-content">
                    <div
                        className="legal-content-body"
                        dangerouslySetInnerHTML={{ __html: contract.content || '' }}
                    />
                </div>
            </div>
        </section>
    );
};

export default LegalDocumentPage;
