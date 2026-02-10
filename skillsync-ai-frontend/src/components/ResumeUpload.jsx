import { useState, useRef } from 'react';
import { userAPI } from '../services/api';

const ResumeUpload = ({ onUploadSuccess }) => {
    const [isDragging, setIsDragging] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState(null);
    const fileInputRef = useRef(null);

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setIsDragging(true);
        } else if (e.type === 'dragleave') {
            setIsDragging(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleUpload(e.dataTransfer.files[0]);
        }
    };

    const handleChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            handleUpload(e.target.files[0]);
        }
    };

    const handleUpload = async (file) => {
        // Validate file type
        const validTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
        if (!validTypes.includes(file.type)) {
            setError('Please upload a PDF or DOCX file.');
            return;
        }

        setIsUploading(true);
        setError(null);

        const formData = new FormData();
        formData.append('file', file);

        try {
            await userAPI.uploadResume(formData);
            if (onUploadSuccess) onUploadSuccess();
        } catch (err) {
            console.error('Upload failed:', err);
            setError('Failed to upload resume. Please try again.');
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <div
            className={`resume-upload-zone ${isDragging ? 'dragging' : ''}`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            style={{
                border: '2px dashed #e5e7eb',
                borderRadius: 'var(--radius-card)',
                padding: '3rem',
                textAlign: 'center',
                cursor: 'pointer',
                backgroundColor: isDragging ? '#fdfbf7' : '#ffffff',
                transition: 'all 0.2s',
                borderColor: isDragging ? 'var(--primary)' : '#e5e7eb'
            }}
        >
            <input
                type="file"
                ref={fileInputRef}
                onChange={handleChange}
                style={{ display: 'none' }}
                accept=".pdf,.docx"
            />

            <div style={{ marginBottom: '1rem', fontSize: '3rem' }}>📄</div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.5rem' }}>
                {isUploading ? 'Uploading & Parsing...' : 'Upload your Resume'}
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', maxWidth: '300px', margin: '0 auto' }}>
                {isUploading
                    ? 'Our AI is analyzing your skills and experience.'
                    : 'Drag & drop or click to upload (PDF/DOCX). We will extract your skills automatically.'}
            </p>

            {error && (
                <div style={{ marginTop: '1rem', color: '#dc2626', fontSize: '0.85rem' }}>
                    {error}
                </div>
            )}
        </div>
    );
};

export default ResumeUpload;
