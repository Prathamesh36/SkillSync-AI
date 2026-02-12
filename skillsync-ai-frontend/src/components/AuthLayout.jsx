const AuthLayout = ({ children, title, subtitle }) => {
    return (
        <div className="auth-layout-container">
            <div className="auth-card-frame">

                {/* Left Panel: Form */}
                <div className="auth-left-panel">
                    <div className="brand-badge">SkillSync AI</div>

                    <div className="auth-header">
                        <h1 className="auth-title">{title}</h1>
                        <p className="auth-subtitle">{subtitle}</p>
                    </div>

                    <div className="auth-form-content">
                        {children}
                    </div>

                    <div className="auth-footer">
                        <span>Terms & Conditions</span>
                    </div>
                </div>

                {/* Right Panel: Image & Floating Elements */}
                <div className="auth-right-panel">
                    <div className="auth-image-container">
                        <img
                            src="https://images.unsplash.com/photo-1551836022-d5d88e9218df?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
                            alt="SkillSync AI Talent Matching"
                            className="auth-image"
                        />

                        {/* Floating Glass Cards */}
                        <div className="overlay-card top-left">
                            <div className="overlay-title">AI Profile Analysis</div>
                            <div className="overlay-subtitle">Smart matching in progress</div>
                        </div>

                        <div className="overlay-card bottom-center">
                            <div className="overlay-title">Skill Score: 98%</div>
                            <div className="overlay-subtitle">Perfect match for this role</div>
                        </div>

                    </div>
                </div>

            </div>
        </div>
    );
};

export default AuthLayout;
