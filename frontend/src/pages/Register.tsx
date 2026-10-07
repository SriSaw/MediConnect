import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/api';

export const Register: React.FC = () => {
  const [role, setRole] = useState<'PATIENT' | 'HEALTHCARE_PROFESSIONAL'>('PATIENT');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [specialization, setSpecialization] = useState('');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [experienceYears, setExperienceYears] = useState(1);
  const [bio, setBio] = useState('');
  const [consultationFee, setConsultationFee] = useState(50);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const payload: any = {
        name,
        email,
        password,
        phone: phone || undefined,
        role,
      };

      if (role === 'HEALTHCARE_PROFESSIONAL') {
        payload.specialization = specialization;
        payload.licenseNumber = licenseNumber;
        payload.experienceYears = Number(experienceYears);
        payload.bio = bio;
        payload.consultationFee = Number(consultationFee);
      }

      const res = await authService.register(payload);
      login(res);
      if (res.user.role === 'HEALTHCARE_PROFESSIONAL') {
        navigate('/doctor/appointments');
      } else {
        navigate('/patient/appointments');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto my-8 p-8 bg-white rounded-xl shadow-sm border border-slate-200">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-slate-800">Create an Account</h2>
        <p className="text-sm text-slate-500 mt-1">Join the MediConnect healthcare network</p>
      </div>

      {/* Role Toggle */}
      <div className="flex bg-slate-100 p-1 rounded-lg mb-6">
        <button
          type="button"
          onClick={() => setRole('PATIENT')}
          className={`flex-1 py-2 text-sm font-semibold rounded-md transition-all ${
            role === 'PATIENT' ? 'bg-white shadow text-teal-700' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          I am a Patient
        </button>
        <button
          type="button"
          onClick={() => setRole('HEALTHCARE_PROFESSIONAL')}
          className={`flex-1 py-2 text-sm font-semibold rounded-md transition-all ${
            role === 'HEALTHCARE_PROFESSIONAL' ? 'bg-white shadow text-teal-700' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          I am a Healthcare Professional
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-lg">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Full Name</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
              placeholder="Dr. John Doe / Jane Smith"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Phone Number</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
              placeholder="+1-555-0100"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Email address</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
            placeholder="you@domain.com"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Password</label>
          <input
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
            placeholder="At least 8 characters"
          />
        </div>

        {role === 'HEALTHCARE_PROFESSIONAL' && (
          <div className="p-4 bg-teal-50/50 rounded-lg border border-teal-100 space-y-4">
            <h3 className="text-sm font-bold text-teal-900">Professional Credentials</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Specialization</label>
                <input
                  type="text"
                  required
                  value={specialization}
                  onChange={(e) => setSpecialization(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                  placeholder="e.g. Cardiology"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">License Number</label>
                <input
                  type="text"
                  required
                  value={licenseNumber}
                  onChange={(e) => setLicenseNumber(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                  placeholder="e.g. MED-12345"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Experience (Years)</label>
                <input
                  type="number"
                  min="0"
                  value={experienceYears}
                  onChange={(e) => setExperienceYears(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Consultation Fee ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={consultationFee}
                  onChange={(e) => setConsultationFee(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Professional Bio</label>
              <textarea
                rows={2}
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                placeholder="Background, qualifications, areas of interest..."
              />
            </div>
          </div>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="w-full py-2.5 px-4 bg-teal-600 hover:bg-teal-700 disabled:opacity-50 text-white font-medium rounded-lg shadow-sm transition-colors text-sm"
        >
          {submitting ? 'Registering...' : 'Register'}
        </button>
      </form>

      <p className="text-center text-xs text-slate-500 mt-6">
        Already have an account?{' '}
        <Link to="/login" className="text-teal-600 hover:underline font-semibold">
          Sign In
        </Link>
      </p>
    </div>
  );
};
