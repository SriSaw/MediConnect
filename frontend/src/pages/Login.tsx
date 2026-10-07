import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/api';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const response = await authService.login({ email, password });
      login(response);
      if (response.user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else if (response.user.role === 'HEALTHCARE_PROFESSIONAL') {
        navigate('/doctor/appointments');
      } else {
        navigate('/patient/appointments');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  };

  const fillDemo = (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('Password123!');
  };

  return (
    <div className="max-w-md mx-auto my-12 p-8 bg-white rounded-xl shadow-sm border border-slate-200">
      <div className="text-center mb-8">
        <h2 className="text-2xl font-bold text-slate-800">Sign in to MediConnect</h2>
        <p className="text-sm text-slate-500 mt-1">Access your healthcare appointments & records</p>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-lg">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Email address</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
            placeholder="you@domain.com"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Password</label>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm"
            placeholder="••••••••"
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full py-2.5 px-4 bg-teal-600 hover:bg-teal-700 disabled:opacity-50 text-white font-medium rounded-lg shadow-sm transition-colors text-sm"
        >
          {submitting ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      {/* Quick Demo Logins */}
      <div className="mt-8 pt-6 border-t border-slate-200">
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider text-center mb-3">
          Quick Demo Credentials
        </p>
        <div className="grid grid-cols-3 gap-2">
          <button
            type="button"
            onClick={() => fillDemo('admin@mediconnect.local')}
            className="px-2 py-1.5 text-xs bg-slate-100 hover:bg-slate-200 rounded text-slate-700 font-medium text-center"
          >
            Admin
          </button>
          <button
            type="button"
            onClick={() => fillDemo('doctor1@mediconnect.local')}
            className="px-2 py-1.5 text-xs bg-slate-100 hover:bg-slate-200 rounded text-slate-700 font-medium text-center"
          >
            Doctor
          </button>
          <button
            type="button"
            onClick={() => fillDemo('patient1@mediconnect.local')}
            className="px-2 py-1.5 text-xs bg-slate-100 hover:bg-slate-200 rounded text-slate-700 font-medium text-center"
          >
            Patient
          </button>
        </div>
      </div>

      <p className="text-center text-xs text-slate-500 mt-6">
        Don't have an account?{' '}
        <Link to="/register" className="text-teal-600 hover:underline font-semibold">
          Create an account
        </Link>
      </p>
    </div>
  );
};
