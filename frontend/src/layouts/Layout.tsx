import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notificationService } from '../services/api';
import {
  Activity,
  Calendar,
  FileText,
  MessageSquare,
  Bell,
  User as UserIcon,
  LogOut,
  Settings,
  Shield,
  Clock,
  Menu,
  X,
} from 'lucide-react';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    if (user) {
      notificationService
        .getUnreadCount()
        .then(setUnreadNotifications)
        .catch(() => {});
    }
  }, [user, location.pathname]);

  const navItems = () => {
    if (!user) return [];
    if (user.role === 'PATIENT') {
      return [
        { label: 'Find Doctors', path: '/professionals', icon: Activity },
        { label: 'My Appointments', path: '/patient/appointments', icon: Calendar },
        { label: 'Medical Records', path: '/patient/medical-records', icon: FileText },
        { label: 'Consultations', path: '/patient/consultations', icon: Clock },
        { label: 'Messages', path: '/messages', icon: MessageSquare },
        { label: 'My Profile', path: '/patient/profile', icon: UserIcon },
      ];
    }
    if (user.role === 'HEALTHCARE_PROFESSIONAL') {
      return [
        { label: 'My Schedule', path: '/doctor/appointments', icon: Calendar },
        { label: 'Set Availability', path: '/doctor/availability', icon: Clock },
        { label: 'Consultations', path: '/doctor/consultations', icon: FileText },
        { label: 'Messages', path: '/messages', icon: MessageSquare },
        { label: 'Doctor Profile', path: '/doctor/profile', icon: UserIcon },
      ];
    }
    if (user.role === 'ADMIN') {
      return [
        { label: 'Dashboard', path: '/admin/dashboard', icon: Shield },
        { label: 'User Directory', path: '/admin/users', icon: UserIcon },
        { label: 'All Appointments', path: '/admin/appointments', icon: Calendar },
        { label: 'Audit Logs', path: '/admin/audit-logs', icon: FileText },
        { label: 'System Settings', path: '/admin/settings', icon: Settings },
      ];
    }
    return [];
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
      {/* Top Navbar */}
      <header className="sticky top-0 z-50 bg-white border-b border-slate-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="flex items-center gap-2 text-teal-600 font-bold text-xl tracking-tight">
              <div className="w-9 h-9 rounded-lg bg-teal-600 text-white flex items-center justify-center font-black">
                +
              </div>
              <span>MediConnect</span>
            </Link>
            {user && (
              <span className="hidden sm:inline-block ml-3 px-2 py-0.5 text-xs font-semibold rounded bg-teal-50 text-teal-700 border border-teal-200">
                {user.role === 'HEALTHCARE_PROFESSIONAL' ? 'DOCTOR' : user.role}
              </span>
            )}
          </div>

          <div className="hidden md:flex items-center gap-6">
            {navItems().map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname.startsWith(item.path);
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-1.5 text-sm font-medium transition-colors ${
                    isActive ? 'text-teal-600 font-semibold' : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {item.label}
                </Link>
              );
            })}
          </div>

          <div className="flex items-center gap-4">
            {user ? (
              <>
                <Link
                  to="/notifications"
                  className="relative p-2 text-slate-500 hover:text-teal-600 rounded-full hover:bg-slate-100 transition-colors"
                  title="Notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadNotifications > 0 && (
                    <span className="absolute top-1 right-1 w-4 h-4 bg-rose-500 text-white rounded-full text-[10px] flex items-center justify-center font-bold">
                      {unreadNotifications > 9 ? '9+' : unreadNotifications}
                    </span>
                  )}
                </Link>

                <div className="hidden sm:flex flex-col text-right">
                  <span className="text-sm font-semibold text-slate-800 leading-tight">{user.name}</span>
                  <span className="text-xs text-slate-400">{user.email}</span>
                </div>

                <button
                  onClick={logout}
                  className="flex items-center gap-1 text-sm text-slate-500 hover:text-rose-600 px-3 py-1.5 rounded-lg border border-slate-200 hover:border-rose-200 hover:bg-rose-50 transition-colors"
                  title="Sign Out"
                >
                  <LogOut className="w-4 h-4" />
                  <span className="hidden sm:inline">Logout</span>
                </button>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <Link
                  to="/login"
                  className="text-sm font-medium text-slate-700 hover:text-teal-600 px-3 py-1.5 rounded-lg hover:bg-slate-100"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="text-sm font-medium bg-teal-600 text-white px-4 py-2 rounded-lg hover:bg-teal-700 transition-colors shadow-sm"
                >
                  Get Started
                </Link>
              </div>
            )}

            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-slate-600 hover:bg-slate-100 rounded-lg"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile menu dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-slate-200 bg-white px-4 pt-2 pb-4 space-y-1">
            {navItems().map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-3 px-3 py-2 text-base font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
                >
                  <Icon className="w-5 h-5 text-teal-600" />
                  {item.label}
                </Link>
              );
            })}
          </div>
        )}
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 text-center text-sm text-slate-500">
        <p>© 2026 MediConnect - Role-Based Healthcare Platform. Verified & Secure.</p>
      </footer>
    </div>
  );
};
