import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from './components/AdminLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import UsersPage from './pages/UsersPage';
import MessagesPage from './pages/MessagesPage';
import PaymentsPage from './pages/PaymentsPage';
import PlansPage from './pages/PlansPage';
import ContactMessagesPage from './pages/ContactMessagesPage';
import ContractsPage from './pages/ContractsPage';

function App() {
  const token = localStorage.getItem('adminToken');

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={token ? <Navigate to="/" replace /> : <LoginPage onSuccess={() => window.location.replace('/')} />} />
        <Route path="/" element={token ? <AdminLayout /> : <Navigate to="/login" replace />}>
          <Route index element={<DashboardPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="messages" element={<MessagesPage />} />
          <Route path="payments" element={<PaymentsPage />} />
          <Route path="plans" element={<PlansPage />} />
          <Route path="contact-messages" element={<ContactMessagesPage />} />
          <Route path="contracts" element={<ContractsPage />} />
        </Route>
        <Route path="*" element={<Navigate to={token ? '/' : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
