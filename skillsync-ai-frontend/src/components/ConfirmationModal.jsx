import React from 'react';

const ConfirmationModal = ({
    show,
    title,
    message,
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    onConfirm,
    onCancel,
    type = 'primary' // 'primary', 'danger'
}) => {
    if (!show) return null;



    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.4)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 10000,
            padding: '1rem'
        }} onClick={onCancel}>
            <div style={{
                backgroundColor: 'white',
                borderRadius: 'var(--radius-card)',
                width: '100%',
                maxWidth: '450px',
                padding: '2.5rem',
                position: 'relative',
                boxShadow: '0 20px 40px rgba(0,0,0,0.15)',
                animation: 'modalFadeUp 0.3s ease-out'
            }} onClick={e => e.stopPropagation()}>

                <style>{`
                    @keyframes modalFadeUp {
                        from { opacity: 0; transform: translateY(20px) scale(0.95); }
                        to { opacity: 1; transform: translateY(0) scale(1); }
                    }
                `}</style>

                <h3 style={{
                    fontSize: '1.25rem',
                    fontWeight: '700',
                    color: 'var(--text-main)',
                    marginBottom: '1rem'
                }}>
                    {title}
                </h3>

                <p style={{
                    lineHeight: '1.6',
                    color: 'var(--text-muted)',
                    fontSize: '0.95rem',
                    marginBottom: '2rem'
                }}>
                    {message}
                </p>

                <div style={{
                    display: 'flex',
                    justifyContent: 'flex-end',
                    gap: '1rem'
                }}>
                    <button
                        onClick={onCancel}
                        className="btn-secondary"
                        style={{ border: 'none', background: '#f3f4f6' }}
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={onConfirm}
                        className={type === 'danger' ? "btn-danger" : "btn-primary"}
                        style={{
                            width: 'auto', // Override the 100% width from CSS for modal buttons
                            minWidth: '100px'
                        }}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationModal;
