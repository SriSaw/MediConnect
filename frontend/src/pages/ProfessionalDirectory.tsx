import React, { useState, useEffect } from 'react';
import { professionalService } from '../services/api';
import { ProfessionalProfile } from '../types';
import { Link } from 'react-router-dom';
import { Search, Award, Clock, DollarSign, Calendar } from 'lucide-react';

export const ProfessionalDirectory: React.FC = () => {
  const [professionals, setProfessionals] = useState<ProfessionalProfile[]>([]);
  const [search, setSearch] = useState('');
  const [specialization, setSpecialization] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchProfessionals = async () => {
    setLoading(true);
    try {
      const res = await professionalService.search({
        search: search || undefined,
        specialization: specialization || undefined,
        verified: true,
      });
      setProfessionals(res.content);
    } catch {
      setProfessionals([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfessionals();
  }, [specialization]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchProfessionals();
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">Find Healthcare Professionals</h1>
        <p className="text-sm text-slate-500 mt-1">Book consultations with verified doctors & specialists</p>

        <form onSubmit={handleSearch} className="mt-4 flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by doctor name or keywords..."
              className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-sm"
            />
          </div>
          <select
            value={specialization}
            onChange={(e) => setSpecialization(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
          >
            <option value="">All Specializations</option>
            <option value="Cardiology">Cardiology</option>
            <option value="Dermatology">Dermatology</option>
            <option value="Pediatrics">Pediatrics</option>
            <option value="Neurology">Neurology</option>
            <option value="General Medicine">General Medicine</option>
          </select>
          <button
            type="submit"
            className="px-5 py-2 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg text-sm shadow-sm"
          >
            Search
          </button>
        </form>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : professionals.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <p className="text-slate-500">No verified healthcare professionals found matching your criteria.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {professionals.map((prof) => (
            <div
              key={prof.id}
              className="bg-white rounded-xl border border-slate-200 p-6 flex flex-col justify-between hover:shadow-md transition-shadow"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-bold text-lg text-slate-900">{prof.name}</h3>
                    <span className="inline-block mt-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-50 text-teal-700 border border-teal-200">
                      {prof.specialization}
                    </span>
                  </div>
                  {prof.verified && (
                    <span className="flex items-center gap-1 text-xs text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 font-medium">
                      <Award className="w-3.5 h-3.5" /> Verified
                    </span>
                  )}
                </div>

                <p className="mt-3 text-sm text-slate-600 line-clamp-3">
                  {prof.bio || 'Experienced healthcare specialist dedicated to patient care.'}
                </p>

                <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-slate-400" /> {prof.experienceYears} yrs exp.
                  </span>
                  <span className="flex items-center gap-1 font-semibold text-slate-700">
                    <DollarSign className="w-3.5 h-3.5 text-teal-600" /> ${prof.consultationFee.toFixed(2)}
                  </span>
                </div>
              </div>

              <div className="mt-6 flex gap-2">
                <Link
                  to={`/professionals/${prof.id}/book`}
                  className="flex-1 text-center py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition-colors shadow-sm flex items-center justify-center gap-1.5"
                >
                  <Calendar className="w-4 h-4" /> Book Appointment
                </Link>
                <Link
                  to={`/messages?userId=${prof.userId}`}
                  className="px-3 py-2 border border-slate-300 hover:bg-slate-50 rounded-lg text-sm text-slate-600 font-medium"
                  title="Message Doctor"
                >
                  Chat
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
