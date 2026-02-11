import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/DashboardLayout';
import { mockInterviewAPI } from '../../services/api';
import { useToast } from '../../components/Toast';

const CandidateInterviews = () => {
    const navigate = useNavigate();
    const toast = useToast();
    const [mode, setMode] = useState('SETUP'); // SETUP, TOPIC_FORM, ACTIVE, RESULTS, TRANSCRIPT_VIEW
    const [loading, setLoading] = useState(false);
    const [topics, setTopics] = useState('');
    const [difficulty, setDifficulty] = useState('MEDIUM');
    const [sessionId, setSessionId] = useState(null);

    // Refactored State for Single Question View
    const [currentQuestion, setCurrentQuestion] = useState('');
    const [currentAnswer, setCurrentAnswer] = useState('');
    const [history, setHistory] = useState([]); // Stores { question, answer, evaluation }
    const [finalResult, setFinalResult] = useState(null);

    // Interview History State
    const [interviewHistory, setInterviewHistory] = useState([]);
    const [viewingTranscript, setViewingTranscript] = useState(null);
    const [transcriptLoading, setTranscriptLoading] = useState(false);

    // Speech Recognition State
    const [isListening, setIsListening] = useState(false);
    const [interimResult, setInterimResult] = useState(''); // Store interim speech
    const [speechError, setSpeechError] = useState(null);
    const [inputMode, setInputMode] = useState('VOICE'); // 'VOICE' or 'TEXT'

    const recognitionRef = useRef(null);
    const isManuallyStoppedString = useRef("false");

    // Text-to-Speech Helper
    const speakText = (text) => {
        if ('speechSynthesis' in window) {
            window.speechSynthesis.cancel(); // Stop any previous speech
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'en-US';
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            window.speechSynthesis.speak(utterance);
        } else {
            console.warn('Text-to-Speech not supported');
        }
    };

    // Auto-speak question when it changes (regardless of input mode)
    useEffect(() => {
        if (mode === 'ACTIVE' && currentQuestion) {
            // Small delay to ensure smooth transition
            const timer = setTimeout(() => {
                speakText(currentQuestion);
            }, 500);
            return () => clearTimeout(timer);
        }
    }, [currentQuestion, mode]);

    useEffect(() => {
        // Initialize Speech Recognition
        if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            recognitionRef.current = new SpeechRecognition();
            recognitionRef.current.continuous = true;
            recognitionRef.current.interimResults = true;
            recognitionRef.current.lang = 'en-US';

            recognitionRef.current.onstart = () => {
                setIsListening(true);
                setSpeechError(null);
            };

            recognitionRef.current.onresult = (event) => {
                let finalTranscriptChunk = '';
                let interimTranscriptChunk = '';

                for (let i = event.resultIndex; i < event.results.length; ++i) {
                    if (event.results[i].isFinal) {
                        finalTranscriptChunk += event.results[i][0].transcript;
                    } else {
                        interimTranscriptChunk += event.results[i][0].transcript;
                    }
                }

                if (finalTranscriptChunk) {
                    setCurrentAnswer(prev => prev + (prev ? ' ' : '') + finalTranscriptChunk);
                    setInterimResult('');
                } else {
                    setInterimResult(interimTranscriptChunk);
                }
            };

            recognitionRef.current.onerror = (event) => {
                const errorMsg = event.error;

                if (errorMsg === 'no-speech') {
                    // Ignore, will try to restart in onend if not manually stopped
                    return;
                }

                if (errorMsg === 'not-allowed') {
                    setSpeechError('Microphone access denied. Please allow permission.');
                    setInputMode('TEXT');
                    isManuallyStoppedString.current = "true";
                    setIsListening(false);
                } else if (errorMsg === 'network') {
                    setSpeechError('Network error. Check connection.');
                    // Let it try to restart or fail naturally
                } else {
                    console.error(`Speech recognition error: ${errorMsg}`);
                    isManuallyStoppedString.current = "true";
                    setIsListening(false);
                }
            };

            recognitionRef.current.onend = () => {
                // Auto-restart if not manually stopped (handles no-speech timeout)
                if (isManuallyStoppedString.current === "false") {
                    try {
                        recognitionRef.current.start();
                    } catch (err) {
                        // console.log("Restart failed", err);
                    }
                } else {
                    setIsListening(false);
                    setInterimResult('');
                }
            };
        } else {
            setSpeechError('Speech recognition not supported in this browser.');
            setInputMode('TEXT');
        }

        return () => {
            if (recognitionRef.current) {
                isManuallyStoppedString.current = "true";
                recognitionRef.current.stop();
            }
            window.speechSynthesis.cancel(); // Stop speaking on unmount
        };
    }, []);

    const toggleListening = () => {
        if (!recognitionRef.current) {
            toast.warning('Speech recognition is not supported in your browser.');
            return;
        }

        if (isListening) {
            isManuallyStoppedString.current = "true";
            recognitionRef.current.stop();
            setIsListening(false);
        } else {
            setSpeechError(null);
            isManuallyStoppedString.current = "false";
            try {
                recognitionRef.current.start();
            } catch (err) {
                console.error("Failed to start recognition:", err);
            }
        }
    };

    const handleStartResumeInterview = async () => {
        setLoading(true);
        try {
            const data = await mockInterviewAPI.startResumeInterview();
            setSessionId(data.sessionId);
            setCurrentQuestion(data.firstQuestion);
            setHistory([]);
            setMode('ACTIVE');
        } catch (error) {
            console.error('Failed to start resume interview:', error);
            toast.error('Failed to start interview. Please ensure your resume is uploaded.');
        } finally {
            setLoading(false);
        }
    };

    const handleStartTopicInterview = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const topicList = topics.split(',').map(t => t.trim()).filter(Boolean);
            if (topicList.length === 0) {
                toast.warning('Please enter at least one topic.');
                setLoading(false);
                return;
            }
            const data = await mockInterviewAPI.startTopicInterview(topicList, difficulty);
            setSessionId(data.sessionId);
            setCurrentQuestion(data.firstQuestion);
            setHistory([]);
            setMode('ACTIVE');
        } catch (error) {
            console.error('Failed to start topic interview:', error);
            toast.error('Failed to start interview. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitAnswer = async () => {
        const fullAnswer = (currentAnswer + (interimResult ? ' ' + interimResult : '')).trim();

        if (!fullAnswer) return;

        if (isListening && recognitionRef.current) {
            isManuallyStoppedString.current = "true";
            recognitionRef.current.stop();
            setIsListening(false);
        }

        const answer = fullAnswer;
        setLoading(true);

        window.speechSynthesis.cancel();

        try {
            const data = await mockInterviewAPI.submitAnswer(sessionId, answer);

            setHistory(prev => [...prev, {
                question: currentQuestion,
                answer: answer,
                evaluation: data.evaluation
            }]);

            setCurrentAnswer('');
            setInterimResult('');
            setSpeechError(null);

            if (data.interviewComplete) {
                await handleEndInterview();
            } else {
                setCurrentQuestion(data.nextQuestion);
            }
        } catch (error) {
            console.error('Failed to submit answer:', error);
            toast.error('Failed to submit answer. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleEndInterview = async () => {
        setLoading(true);
        window.speechSynthesis.cancel(); // Safety stop
        try {
            const data = await mockInterviewAPI.endInterview(sessionId);
            setFinalResult(data);
            setMode('RESULTS');
        } catch (error) {
            console.error('Failed to end interview:', error);
        } finally {
            setLoading(false);
        }
    };

    // Fetch interview history on mount and when returning to setup
    useEffect(() => {
        if (mode === 'SETUP') {
            const fetchHistory = async () => {
                try {
                    const data = await mockInterviewAPI.getHistory();
                    setInterviewHistory(data || []);
                } catch (error) {
                    console.error('Failed to fetch interview history:', error);
                }
            };
            fetchHistory();
        }
    }, [mode]);

    const handleViewTranscript = async (sessionId) => {
        setTranscriptLoading(true);
        try {
            const transcript = await mockInterviewAPI.getTranscript(sessionId);
            setViewingTranscript(transcript);
            setMode('TRANSCRIPT_VIEW');
        } catch (error) {
            toast.error('Failed to load transcript');
        } finally {
            setTranscriptLoading(false);
        }
    };

    const renderTranscriptView = () => (
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
            <div className="stat-card">
                <button
                    onClick={() => { setMode('SETUP'); setViewingTranscript(null); }}
                    style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', marginBottom: '1rem' }}
                >
                    ← Back to Interview Setup
                </button>
                <h2 style={{ marginBottom: '1.5rem' }}>📜 Interview Transcript</h2>
                {viewingTranscript?.messages?.map((msg, idx) => (
                    <div key={idx} style={{
                        padding: '1rem',
                        background: msg.role === 'INTERVIEWER' ? '#eff6ff' : '#f0fdf4',
                        borderRadius: '0.75rem',
                        marginBottom: '0.75rem',
                        borderLeft: `4px solid ${msg.role === 'INTERVIEWER' ? '#3b82f6' : '#22c55e'}`
                    }}>
                        <div style={{ fontWeight: '600', marginBottom: '0.5rem', color: msg.role === 'INTERVIEWER' ? '#1e3a8a' : '#166534' }}>
                            {msg.role === 'INTERVIEWER' ? '🤖 Interviewer' : '👤 You'}
                        </div>
                        <div style={{ lineHeight: '1.6' }}>{msg.content}</div>
                    </div>
                ))}
                {viewingTranscript?.evaluations?.length > 0 && (
                    <div style={{ marginTop: '2rem', padding: '1.5rem', background: '#fefce8', borderRadius: '0.75rem' }}>
                        <h3 style={{ marginBottom: '1rem', color: '#854d0e' }}>📊 Performance Summary</h3>
                        {viewingTranscript.evaluations.map((ev, idx) => (
                            <div key={idx} style={{ marginBottom: '1rem', padding: '0.75rem', background: 'white', borderRadius: '0.5rem' }}>
                                <div style={{ fontWeight: '600', marginBottom: '0.5rem' }}>Question {ev.questionIndex || idx + 1}: Score {ev.score}/10</div>
                                {ev.strengths?.length > 0 && <div style={{ color: '#166534', fontSize: '0.9rem' }}>✓ {ev.strengths.join(', ')}</div>}
                                {ev.weaknesses?.length > 0 && <div style={{ color: '#b91c1c', fontSize: '0.9rem' }}>✗ {ev.weaknesses.join(', ')}</div>}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );

    const renderSetup = () => (
        <div className="dashboard-grid">
            <div className="stat-card" style={{ gridColumn: 'span 2', textAlign: 'center', padding: '3.5rem 2rem' }}>
                <h2 style={{ marginBottom: '1rem', fontSize: '2rem', fontWeight: '700' }}>Choose Interview Type</h2>
                <p style={{ color: 'var(--text-muted)', marginBottom: '3rem' }}>Select how you want to prepare for your next big opportunity</p>

                <div style={{ display: 'flex', gap: '2.5rem', justifyContent: 'center', flexWrap: 'wrap' }}>
                    <div
                        onClick={handleStartResumeInterview}
                        className="interview-type-card"
                        style={{
                            background: 'white',
                            border: '1px solid #e5e7eb',
                            borderRadius: 'var(--radius-card)',
                            padding: '3rem 2rem',
                            width: '320px',
                            cursor: 'pointer',
                            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            boxShadow: 'var(--shadow-card)',
                            textAlign: 'center',
                            position: 'relative',
                            overflow: 'hidden'
                        }}
                        onMouseOver={e => {
                            e.currentTarget.style.transform = 'translateY(-8px)';
                            e.currentTarget.style.borderColor = 'var(--primary)';
                            e.currentTarget.style.boxShadow = '0 30px 60px -12px rgba(245, 200, 66, 0.2)';
                        }}
                        onMouseOut={e => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.borderColor = '#e5e7eb';
                            e.currentTarget.style.boxShadow = 'var(--shadow-card)';
                        }}
                    >
                        <div style={{
                            width: '80px',
                            height: '80px',
                            background: '#fffbeb',
                            borderRadius: '50%',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '2.5rem',
                            marginBottom: '1.5rem',
                            border: '1px solid #fef3c7'
                        }}>
                            📄
                        </div>
                        <h3 style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--text-main)' }}>Resume Based</h3>
                        <p style={{ color: 'var(--text-muted)', marginTop: '0.75rem', fontSize: '0.95rem', lineHeight: '1.6' }}>
                            AI scans your resume and asks relevant questions based on your skills and experience.
                        </p>
                        <div style={{
                            marginTop: '1.5rem',
                            color: 'var(--primary)',
                            fontWeight: '600',
                            fontSize: '0.9rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem'
                        }}>
                            Start Prep →
                        </div>
                    </div>

                    <div
                        onClick={() => setMode('TOPIC_FORM')}
                        className="interview-type-card"
                        style={{
                            background: 'white',
                            border: '1px solid #e5e7eb',
                            borderRadius: 'var(--radius-card)',
                            padding: '3rem 2rem',
                            width: '320px',
                            cursor: 'pointer',
                            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            boxShadow: 'var(--shadow-card)',
                            textAlign: 'center',
                            position: 'relative',
                            overflow: 'hidden'
                        }}
                        onMouseOver={e => {
                            e.currentTarget.style.transform = 'translateY(-8px)';
                            e.currentTarget.style.borderColor = 'var(--primary)';
                            e.currentTarget.style.boxShadow = '0 30px 60px -12px rgba(245, 200, 66, 0.2)';
                        }}
                        onMouseOut={e => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.borderColor = '#e5e7eb';
                            e.currentTarget.style.boxShadow = 'var(--shadow-card)';
                        }}
                    >
                        <div style={{
                            width: '80px',
                            height: '80px',
                            background: '#fffbeb',
                            borderRadius: '50%',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '2.5rem',
                            marginBottom: '1.5rem',
                            border: '1px solid #fef3c7'
                        }}>
                            💡
                        </div>
                        <h3 style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--text-main)' }}>Topic Based</h3>
                        <p style={{ color: 'var(--text-muted)', marginTop: '0.75rem', fontSize: '0.95rem', lineHeight: '1.6' }}>
                            Choose specific topics (e.g., Java, React, System Design) to practice focused questions.
                        </p>
                        <div style={{
                            marginTop: '1.5rem',
                            color: 'var(--primary)',
                            fontWeight: '600',
                            fontSize: '0.9rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem'
                        }}>
                            Configure Prep →
                        </div>
                    </div>
                </div>
            </div>

            {/* Previous Interview Transcripts */}
            {interviewHistory.length > 0 && (
                <div className="stat-card" style={{ gridColumn: 'span 2' }}>
                    <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        📜 Previous Interview Transcripts
                    </h3>
                    <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {interviewHistory.map((session) => (
                            <div key={session.sessionId} style={{
                                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                padding: '1rem', background: '#f8fafc', borderRadius: '0.75rem',
                                border: '1px solid #e2e8f0'
                            }}>
                                <div>
                                    <div style={{ fontWeight: '600', marginBottom: '0.25rem' }}>
                                        {session.mode === 'RESUME_BASED' ? '📄 Resume Based' : '💡 Topic Based'}
                                        {session.topics?.length > 0 && (
                                            <span style={{ fontWeight: '400', color: 'var(--text-muted)', marginLeft: '0.5rem' }}>
                                                ({session.topics.join(', ')})
                                            </span>
                                        )}
                                    </div>
                                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                                        {new Date(session.startedAt).toLocaleDateString()} • {session.questionCount} questions
                                        {session.finalScore && (
                                            <span style={{ marginLeft: '0.5rem', color: session.finalScore >= 7 ? '#22c55e' : session.finalScore >= 5 ? '#f59e0b' : '#ef4444' }}>
                                                • Score: {session.finalScore.toFixed(1)}/10
                                            </span>
                                        )}
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleViewTranscript(session.sessionId)}
                                    disabled={transcriptLoading}
                                    style={{
                                        padding: '0.5rem 1rem', background: 'var(--primary)',
                                        color: 'white', border: 'none', borderRadius: 'var(--radius-pill)',
                                        cursor: 'pointer', fontSize: '0.85rem', fontWeight: '600'
                                    }}
                                >
                                    View Transcript
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );

    const renderTopicForm = () => (
        <div style={{ maxWidth: '600px', margin: '0 auto' }}>
            <div className="stat-card">
                <button
                    onClick={() => setMode('SETUP')}
                    style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', marginBottom: '1rem' }}
                >
                    ← Back
                </button>
                <h2 style={{ marginBottom: '1.5rem' }}>Configure Topic Interview</h2>
                <form onSubmit={handleStartTopicInterview}>
                    <div className="form-group">
                        <label className="form-label">Topics (comma separated)</label>
                        <input
                            type="text"
                            className="form-input"
                            placeholder="e.g. Java, Spring Boot, Microservices"
                            value={topics}
                            onChange={(e) => setTopics(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Difficulty Level</label>
                        <div style={{ display: 'flex', gap: '1rem' }}>
                            {['EASY', 'MEDIUM', 'HARD'].map(level => (
                                <button
                                    key={level}
                                    type="button"
                                    onClick={() => setDifficulty(level)}
                                    style={{
                                        flex: 1,
                                        padding: '0.75rem',
                                        borderRadius: 'var(--radius-sm)',
                                        border: difficulty === level ? '2px solid var(--primary)' : '1px solid #e5e7eb',
                                        background: difficulty === level ? '#fffbeb' : 'white',
                                        fontWeight: '600',
                                        color: difficulty === level ? 'var(--primary-text)' : 'var(--text-muted)',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {level}
                                </button>
                            ))}
                        </div>
                    </div>

                    <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '1rem' }}>
                        {loading ? 'Starting Interview...' : 'Start Interview'}
                    </button>
                </form>
            </div>
        </div>
    );

    const renderActiveInterview = () => (
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
            {/* Reduced vertical padding here */}
            <div className="stat-card" style={{ padding: '0', overflow: 'hidden' }}>
                <div style={{ padding: '1rem 1.5rem', borderBottom: '1px solid #e5e7eb', background: '#f8fafc', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '1rem' }}>AI Interviewer</h3>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Question {history.length + 1}</span>
                    </div>
                    <span className="status-badge status-scheduled" style={{ fontSize: '0.65rem', padding: '0.2rem 0.5rem' }}>LIVE</span>
                </div>

                <div style={{ padding: '1.5rem', textAlign: 'center' }}>
                    <div style={{
                        background: '#eff6ff',
                        padding: '1.5rem',
                        borderRadius: '0.75rem',
                        marginBottom: '1.5rem',
                        fontSize: '1.15rem',
                        fontWeight: '500',
                        color: '#1e3a8a',
                        lineHeight: '1.5',
                        boxShadow: '0 2px 4px -1px rgba(0, 0, 0, 0.1)',
                        position: 'relative' // For speaker icon
                    }}>
                        {currentQuestion}
                        <button
                            onClick={() => speakText(currentQuestion)}
                            title="Replay Question"
                            style={{
                                position: 'absolute', top: '10px', right: '10px',
                                background: 'transparent', border: 'none', cursor: 'pointer', fontSize: '1.1rem', opacity: 0.7
                            }}
                        >
                            🔊
                        </button>
                    </div>

                    {/* Mode Toggle */}
                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem', gap: '0.75rem' }}>
                        <button
                            onClick={() => setInputMode('VOICE')}
                            style={{
                                padding: '0.4rem 0.8rem', borderRadius: '2rem', border: 'none', cursor: 'pointer',
                                background: inputMode === 'VOICE' ? 'var(--primary)' : '#f3f4f6',
                                color: inputMode === 'VOICE' ? 'white' : 'var(--text-muted)',
                                fontWeight: '600', fontSize: '0.85rem'
                            }}
                        >
                            🎤 Voice Mode
                        </button>
                        <button
                            onClick={() => setInputMode('TEXT')}
                            style={{
                                padding: '0.4rem 0.8rem', borderRadius: '2rem', border: 'none', cursor: 'pointer',
                                background: inputMode === 'TEXT' ? 'var(--primary)' : '#f3f4f6',
                                color: inputMode === 'TEXT' ? 'white' : 'var(--text-muted)',
                                fontWeight: '600', fontSize: '0.85rem'
                            }}
                        >
                            ⌨️ Type Answer
                        </button>
                    </div>

                    {speechError && (
                        <div style={{ color: '#dc2626', marginBottom: '1rem', fontSize: '0.85rem', background: '#fef2f2', padding: '0.4rem', borderRadius: '0.4rem', display: 'inline-block' }}>
                            ⚠️ {speechError}
                        </div>
                    )}

                    {inputMode === 'VOICE' ? (
                        /* Voice Interaction Area */
                        <div style={{ marginBottom: '1.5rem', opacity: isListening ? 1 : 0.8 }}>
                            <button
                                onClick={toggleListening}
                                disabled={!!speechError && speechError.includes('not supported')}
                                className={isListening ? 'animate-pulse-ring' : ''}
                                style={{
                                    width: '80px',
                                    height: '80px',
                                    borderRadius: '50%',
                                    border: 'none',
                                    background: isListening ? '#fecaca' : '#f3f4f6',
                                    color: isListening ? '#dc2626' : '#4b5563',
                                    fontSize: '2rem',
                                    cursor: 'pointer',
                                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                                    boxShadow: isListening ? 'none' : '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    margin: '0 auto 1rem',
                                    position: 'relative'
                                }}
                            >
                                {isListening ? (
                                    <span style={{ position: 'relative', zIndex: 10 }}>⏹️</span>
                                ) : (
                                    <span>🎤</span>
                                )}
                            </button>

                            <p style={{
                                fontSize: '1rem',
                                color: isListening ? '#dc2626' : 'var(--text-muted)',
                                fontWeight: isListening ? '600' : '400',
                                minHeight: '1.25rem'
                            }}>
                                {isListening ? 'Listening...' : 'Tap microphone to answer'}
                            </p>
                        </div>
                    ) : (
                        /* Text Input Area */
                        <div style={{ marginBottom: '1.5rem' }}>
                            <textarea
                                className="form-input"
                                rows="5"
                                placeholder="Type your answer here..."
                                value={currentAnswer}
                                onChange={(e) => setCurrentAnswer(e.target.value)}
                                style={{ width: '100%', maxWidth: '600px', resize: 'vertical', fontSize: '0.9rem' }}
                            />
                        </div>
                    )}

                    {inputMode === 'VOICE' && (
                        /* Minimal Transcript Display (Read-only) */
                        <div style={{
                            maxWidth: '600px',
                            margin: '0 auto',
                            textAlign: 'left',
                            opacity: (currentAnswer || interimResult) ? 1 : 0,
                            transition: 'opacity 0.3s',
                            display: inputMode === 'VOICE' ? 'block' : 'none'
                        }}>
                            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.4rem', textAlign: 'center' }}>TRANSCRIPT PREVIEW</p>
                            <div style={{
                                padding: '0.75rem',
                                background: '#f9fafb',
                                borderRadius: '0.5rem',
                                border: '1px dashed #e5e7eb',
                                minHeight: '50px',
                                maxHeight: '100px', // Limit transcript height
                                overflowY: 'auto',
                                color: '#374151',
                                fontSize: '0.9rem',
                                lineHeight: '1.4'
                            }}>
                                <span>{currentAnswer}</span>
                                <span style={{ color: '#9ca3af', fontStyle: 'italic' }}> {interimResult}</span>
                                {!(currentAnswer || interimResult) && "Your speech will appear here..."}
                            </div>
                        </div>
                    )}

                    <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'center', gap: '0.75rem' }}>
                        <button
                            onClick={() => { setCurrentAnswer(''); setInterimResult(''); }}
                            className="btn-secondary"
                            disabled={loading || (!currentAnswer && !interimResult)}
                            style={{ padding: '0.6rem 1.5rem', fontSize: '0.9rem' }}
                        >
                            Clear
                        </button>
                        <button
                            onClick={handleSubmitAnswer}
                            className="btn-primary"
                            disabled={loading || (!currentAnswer.trim() && !interimResult.trim())}
                            style={{ padding: '0.6rem 2.5rem', fontSize: '1rem' }}
                        >
                            {loading ? 'Submitting...' : 'Submit Answer'}
                        </button>
                    </div>
                </div>

                <div style={{ padding: '0.75rem', borderTop: '1px solid #e5e7eb', background: '#f9fafb', textAlign: 'center' }}>
                    <button
                        onClick={handleEndInterview}
                        style={{ color: '#ef4444', background: 'none', border: 'none', fontWeight: '600', cursor: 'pointer', fontSize: '0.85rem' }}
                    >
                        End Interview Early
                    </button>
                </div>
            </div>
        </div>
    );

    const renderResults = () => (
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            <div className="stat-card" style={{ textAlign: 'center', padding: '3rem', marginBottom: '2rem' }}>
                <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🎉</div>
                <h2 style={{ marginBottom: '0.5rem' }}>Interview Completed!</h2>
                <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>Here is your comprehensive performance review.</p>

                <div style={{ display: 'flex', justifyContent: 'center', gap: '2rem', flexWrap: 'wrap', marginBottom: '2rem' }}>
                    <div style={{
                        background: '#f8fafc', padding: '2rem', borderRadius: '1rem',
                        display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: '200px'
                    }}>
                        <div style={{ fontSize: '3rem', fontWeight: '800', color: 'var(--primary-text)' }}>
                            {Number((finalResult?.finalScore || 0) / 10).toFixed(1)}/10
                        </div>
                        <div style={{ color: 'var(--text-muted)', fontWeight: '500' }}>Overall Score</div>
                    </div>

                    <div style={{
                        textAlign: 'left', background: '#fffbeb', padding: '1.5rem', borderRadius: '1rem', border: '1px solid #fcd34d',
                        maxWidth: '400px', flex: 1
                    }}>
                        <h3 style={{ color: '#92400e', marginBottom: '0.5rem', fontSize: '1.1rem' }}>💡 Final Feedback</h3>
                        <p style={{ color: '#78350f', lineHeight: '1.5', fontSize: '0.95rem' }}>{finalResult?.finalFeedback}</p>
                    </div>
                </div>

                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                    <button
                        onClick={() => {
                            setMode('SETUP');
                            setHistory([]);
                            setFinalResult(null);
                        }}
                        className="btn-primary"
                    >
                        Start New Interview
                    </button>
                    <button
                        onClick={() => toast.info('Feature coming soon!')}
                        className="btn-secondary"
                    >
                        Download Report
                    </button>
                </div>
            </div>

            <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ marginBottom: '1.5rem', fontSize: '1.4rem' }}>Question Breakdown</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    {history.map((item, idx) => (
                        <div key={idx} className="stat-card" style={{ padding: '0', overflow: 'hidden' }}>
                            <div style={{ padding: '1.25rem', background: '#f8fafc', borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between' }}>
                                <span style={{ fontWeight: '700', color: 'var(--text-muted)' }}>Question {idx + 1}</span>
                                <span style={{
                                    fontWeight: '700',
                                    color: item.evaluation?.score >= 7 ? '#166534' : item.evaluation?.score >= 4 ? '#d97706' : '#991b1b'
                                }}>
                                    Score: {item.evaluation?.score}/10
                                </span>
                            </div>
                            <div style={{ padding: '1.5rem' }}>
                                <p style={{ fontWeight: '600', marginBottom: '1rem', fontSize: '1.1rem' }}>{item.question}</p>
                                <div style={{ background: '#f9fafb', padding: '1rem', borderRadius: '0.5rem', marginBottom: '1.5rem', borderLeft: '4px solid var(--primary)' }}>
                                    <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>Your Answer:</p>
                                    <p style={{ color: 'var(--text-main)' }}>{item.answer}</p>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                                    <div>
                                        <h4 style={{ fontSize: '0.9rem', color: '#166534', marginBottom: '0.5rem' }}>✅ Strengths</h4>
                                        <ul style={{ paddingLeft: '1.25rem', fontSize: '0.85rem', color: '#14532d' }}>
                                            {item.evaluation?.strengths?.map((s, i) => <li key={i}>{s}</li>) || <li>No specific strengths noted.</li>}
                                        </ul>
                                    </div>
                                    <div>
                                        <h4 style={{ fontSize: '0.9rem', color: '#991b1b', marginBottom: '0.5rem' }}>❌ Weaknesses</h4>
                                        <ul style={{ paddingLeft: '1.25rem', fontSize: '0.85rem', color: '#7f1d1d' }}>
                                            {item.evaluation?.weaknesses?.map((w, i) => <li key={i}>{w}</li>) || <li>No specific weaknesses noted.</li>}
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );

    return (
        <DashboardLayout>
            <div className="section-header">
                <div>
                    <h1 className="section-title">AI Mock Interview</h1>
                    {mode !== 'RESULTS' && <p style={{ color: 'var(--text-muted)' }}>Master your interview skills with real-time AI feedback.</p>}
                </div>
            </div>

            {loading && mode === 'SETUP' && (
                <div style={{ textAlign: 'center', padding: '2rem' }}>
                    <div className="spinner"></div>
                    <p>Initializing...</p>
                </div>
            )}

            {!loading && mode === 'SETUP' && renderSetup()}
            {!loading && mode === 'TOPIC_FORM' && renderTopicForm()}
            {mode === 'ACTIVE' && renderActiveInterview()}
            {mode === 'RESULTS' && renderResults()}
            {mode === 'TRANSCRIPT_VIEW' && renderTranscriptView()}
        </DashboardLayout>
    );
};

export default CandidateInterviews;
