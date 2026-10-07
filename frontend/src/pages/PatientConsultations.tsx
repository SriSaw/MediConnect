import React, { useState, useEffect } from 'react';
import { consultationService } from '../services/api';
import { Consultation } from '../types';
import { FileText, Calendar, Clock, User, Award } from 'lucide-react';
import { useLocation } from 'react-router-dom';

export const PatientConsultations: React.FC = () => {
  const [consultations, setConsultations] = useState<Consultation[]>([]);
  const [loading, setLoading] = useState(true);
  const location = useLocation();

  useEffect(() => {
    const fetchConsultations = async () => {
      setLoading(true);
      try {
        const res = await consultationService.getMyConsultations(0, 50);
        setConsultations(res.content);
      } catch {
        setConsultations([]);
      } finally {
        setLoading(false);
      }
    };
    fetchConsultations();
  }, [location.search]);

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">My Consultation Notes & Advice</h1>
        <p className="text-sm text-slate-500 mt-1">Official diagnoses and instructions from your healthcare professionals</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : consultations.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p className="text-slate-600 font-medium">No completed consultations yet</p>
          <p className="text-slate-400 text-xs mt-1">After your doctor completes an appointment, advice will appear here.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {consultations.map((c) => (
            <div key={c.id} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 pb-3 border-b border-slate-100">
                <div className="flex items-center gap-2">
                  <User className="w-4 h-4 text-teal-600" />
                  <span className="font-bold text-slate-900 text-base">Dr. {c.professionalName}</span>
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-500">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" /> Recorded: {c.createdAt.substring(0, 10)}
                  </span>
                  {c.followUpDate && (
                    <span className="px-2 py-0.5 rounded bg-amber-50 text-amber-700 border border-amber-200 font-medium">
                      Follow-up: {c.followUpDate}
                    </span>
                  )}
                </div>
              </div>

              <div>
                <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">
                  Medical Advice & Prescription
                </h4>
                <div className="p-4 bg-teal-50/50 rounded-lg border border-teal-100 text-sm text-slate-800 font-medium whitespace-pre-wrap">
                  {c.medicalAdvice}
                </div>
              </div>

              {c.notes && (
                <div>
                  <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">
                    Doctor Notes
                  </h4>
                  <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-200">
                    {c.notes}
                  </p>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
