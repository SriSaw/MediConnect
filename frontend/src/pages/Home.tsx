import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Calendar, Activity, Lock, Users, Clock } from 'lucide-react';

export const Home: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-16 py-8">
      {/* Hero */}
      <div className="text-center max-w-3xl mx-auto space-y-4">
        <span className="px-3 py-1 bg-teal-50 text-teal-700 border border-teal-200 rounded-full text-xs font-bold tracking-wide uppercase">
          HIPAA & Security Conscious Telehealth
        </span>
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight leading-tight">
          Role-Based Healthcare Consultations Made Simple
        </h1>
        <p className="text-base sm:text-lg text-slate-600">
          Connect securely with verified physicians, book non-conflicting appointment slots, and access your full clinical history anytime.
        </p>

        <div className="pt-4 flex flex-wrap justify-center gap-4">
          {user ? (
            <Link
              to={
                user.role === 'ADMIN'
                  ? '/admin/dashboard'
                  : user.role === 'HEALTHCARE_PROFESSIONAL'
                  ? '/doctor/appointments'
                  : '/patient/appointments'
              }
              className="px-6 py-3 bg-teal-600 hover:bg-teal-700 text-white font-semibold rounded-xl shadow transition-colors"
            >
              Go to Your Dashboard
            </Link>
          ) : (
            <>
              <Link
                to="/register"
                className="px-6 py-3 bg-teal-600 hover:bg-teal-700 text-white font-semibold rounded-xl shadow transition-colors"
              >
                Create Free Account
              </Link>
              <Link
                to="/professionals"
                className="px-6 py-3 bg-white hover:bg-slate-50 text-slate-700 font-semibold rounded-xl border border-slate-300 transition-colors shadow-sm"
              >
                Browse Doctors
              </Link>
            </>
          )}
        </div>
      </div>

      {/* Feature pillars */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-12 h-12 bg-teal-50 text-teal-600 rounded-xl flex items-center justify-center">
            <Lock className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-lg text-slate-900">Zero Trust Resource Security</h3>
          <p className="text-sm text-slate-600">
            Strict row-level authorization ensures patients only see their own records, and doctors only access authorized medical records.
          </p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-xl flex items-center justify-center">
            <Calendar className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-lg text-slate-900">Double-Booking Prevention</h3>
          <p className="text-sm text-slate-600">
            Pessimistic concurrency locking and schedule validation prevent overlapping consultation slots across time zones.
          </p>
        </div>

        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3">
          <div className="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-xl flex items-center justify-center">
            <Shield className="w-6 h-6" />
          </div>
          <h3 className="font-bold text-lg text-slate-900">Audit-Logged Compliance</h3>
          <p className="text-sm text-slate-600">
            Every clinical advice entry, status change, and medical record access is permanently captured in the system audit trail.
          </p>
        </div>
      </div>
    </div>
  );
};
