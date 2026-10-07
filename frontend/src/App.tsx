import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Layout } from './layouts/Layout';
import { ProtectedRoute } from './routes/ProtectedRoute';

// Pages
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { ProfessionalDirectory } from './pages/ProfessionalDirectory';
import { BookAppointment } from './pages/BookAppointment';
import { PatientAppointments } from './pages/PatientAppointments';
import { MedicalRecords } from './pages/MedicalRecords';
import { PatientConsultations } from './pages/PatientConsultations';
import { PatientProfilePage } from './pages/PatientProfilePage';
import { DoctorAppointments } from './pages/DoctorAppointments';
import { DoctorAvailability } from './pages/DoctorAvailability';
import { DoctorConsultations } from './pages/DoctorConsultations';
import { DoctorProfilePage } from './pages/DoctorProfilePage';
import { AdminDashboard } from './pages/AdminDashboard';
import { AdminUsers } from './pages/AdminUsers';
import { AdminAppointments } from './pages/AdminAppointments';
import { AdminAuditLogs } from './pages/AdminAuditLogs';
import { AdminSettings } from './pages/AdminSettings';
import { Messages } from './pages/Messages';
import { NotificationsPage } from './pages/NotificationsPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Layout>
          <Routes>
            {/* Public */}
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/professionals" element={<ProfessionalDirectory />} />

            {/* Authenticated Patient Routes */}
            <Route
              path="/professionals/:id/book"
              element={
                <ProtectedRoute allowedRoles={['PATIENT']}>
                  <BookAppointment />
                </ProtectedRoute>
              }
            />
            <Route
              path="/patient/appointments"
              element={
                <ProtectedRoute allowedRoles={['PATIENT']}>
                  <PatientAppointments />
                </ProtectedRoute>
              }
            />
            <Route
              path="/patient/medical-records"
              element={
                <ProtectedRoute allowedRoles={['PATIENT']}>
                  <MedicalRecords />
                </ProtectedRoute>
              }
            />
            <Route
              path="/patient/consultations"
              element={
                <ProtectedRoute allowedRoles={['PATIENT']}>
                  <PatientConsultations />
                </ProtectedRoute>
              }
            />
            <Route
              path="/patient/profile"
              element={
                <ProtectedRoute allowedRoles={['PATIENT']}>
                  <PatientProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Authenticated Doctor Routes */}
            <Route
              path="/doctor/appointments"
              element={
                <ProtectedRoute allowedRoles={['HEALTHCARE_PROFESSIONAL']}>
                  <DoctorAppointments />
                </ProtectedRoute>
              }
            />
            <Route
              path="/doctor/availability"
              element={
                <ProtectedRoute allowedRoles={['HEALTHCARE_PROFESSIONAL']}>
                  <DoctorAvailability />
                </ProtectedRoute>
              }
            />
            <Route
              path="/doctor/consultations"
              element={
                <ProtectedRoute allowedRoles={['HEALTHCARE_PROFESSIONAL']}>
                  <DoctorConsultations />
                </ProtectedRoute>
              }
            />
            <Route
              path="/doctor/profile"
              element={
                <ProtectedRoute allowedRoles={['HEALTHCARE_PROFESSIONAL']}>
                  <DoctorProfilePage />
                </ProtectedRoute>
              }
            />

            {/* Authenticated Admin Routes */}
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminUsers />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/appointments"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminAppointments />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/audit-logs"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminAuditLogs />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/settings"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminSettings />
                </ProtectedRoute>
              }
            />

            {/* Shared Authenticated Routes */}
            <Route
              path="/messages"
              element={
                <ProtectedRoute>
                  <Messages />
                </ProtectedRoute>
              }
            />
            <Route
              path="/notifications"
              element={
                <ProtectedRoute>
                  <NotificationsPage />
                </ProtectedRoute>
              }
            />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
