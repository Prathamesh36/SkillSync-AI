import RecruiterSidebar from './RecruiterSidebar';

const RecruiterDashboardLayout = ({ children }) => {
    return (
        <div className="dashboard-container">
            <RecruiterSidebar />
            <main className="dashboard-main">
                {children}
            </main>
        </div>
    );
};

export default RecruiterDashboardLayout;
