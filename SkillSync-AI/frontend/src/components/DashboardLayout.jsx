import Sidebar from './Sidebar';

const DashboardLayout = ({ children }) => {
    return (
        <div className="dashboard-container">
            <Sidebar />
            <main className="dashboard-main">
                {children}
            </main>
        </div>
    );
};

export default DashboardLayout;
