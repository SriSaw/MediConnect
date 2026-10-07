import React, { useState, useEffect } from 'react';
import { consultationService } from '../services/api';
import { Consultation } from '../types';
import { FileText, Calendar, Edit3, X, AlertCircle } from 'lucide-react';

export const DoctorConsultations: React.FC = () => {
  const [consultations, setConsultations] = useState<Consultation[]>([]);
  const [loading, setLoading] = useState(true);

  // Edit modal
  const [modalOpen, setModalOpen] = useState(false);
  const [activeConsult, setActiveConsult] = useState<Consultation | null>(null);
  const [medicalAdvice, setMedicalAdvice] = useState('');
  const [notes, setNotes] = useState('');
  const [followUpDate, setFollowUpDate] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

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

  useEffect(() => {
    fetchConsultations();
  }, []);

  const openEdit = (c: Consultation) => {
    setActiveConsult(c);
    setMedicalAdvice(c.medicalAdvice);
    setNotes(c.notes || '');
    setFollowUpDate(c.followUpDate || '');
    setError('');
    setModalOpen(true);
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeConsult) return;
    setError('');
    setSaving(true);
    try {
      await consultationService.update(activeConsult.id, {
        medicalAdvice: medicalAdvice.trim(),
        notes: notes ? notes.trim() : undefined,
        followUpDate: followUpDate || undefined,
      });
      setModalOpen(false);
      await fetchConsultations();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update consultation');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">Completed Consultations & Notes</h1>
        <p className="text-sm text-slate-500 mt-1">Review and amend previously recorded patient advice and notes</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : consultations.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p className="text-slate-600 font-medium">No recorded consultations</p>
          <p className="text-slate-400 text-xs mt-1">Completed visits will appear here.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {consultations.map((c) => (
            <div key={c.id} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
              <div className="flex justify-between items-start pb-3 border-b border-slate-100">
                <div>
                  <h3 className="font-bold text-slate-900 text-base">Patient: {c.patientName}</h3>
                  <div className="flex items-center gap-3 text-xs text-slate-500 mt-1">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-slate-400" /> {c.createdAt.substring(0, 10)}
                    </span>
                    {c.followUpDate && (
                      <span className="px-2 py-0.5 rounded bg-amber-50 text-amber-700 border border-amber-200">
                        Follow-up: {c.followUpDate}
                      </span>
                    )}
                  </div>
                </div>
                <button
                  onClick={() => openEdit(c)}
                  className="flex items-center gap-1 px-3 py-1.5 border border-slate-200 hover:bg-slate-50 rounded-lg text-xs font-semibold text-slate-700"
                >
                  <Edit3 className="w-3.5 h-3.5" /> Edit
                </button>
              </div>

              <div>
                <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">Medical Advice</h4>
                <p className="text-sm text-slate-800 bg-teal-50/50 p-3 rounded-lg border border-teal-100 whitespace-pre-wrap font-medium">
                  {c.medicalAdvice}
                </p>
              </div>

              {c.notes && (
                <div>
                  <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">Notes</h4>
                  <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-200">
                    {c.notes}
                  </p>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Edit modal */}
      {modalOpen && activeConsult && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex justify-center items-center p-4">
          <div className="bg-white rounded-xl max-w-xl w-full p-6 shadow-xl border border-slate-200">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-bold text-slate-900">Edit Consultation Advice</h2>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg flex items-center gap-2">
                <AlertCircle className="w-4 h-4" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleUpdate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                  Medical Advice & Instructions
                </label>
                <textarea
                  rows={4}
                  required
                  value={medicalAdvice}
                  onChange={(e) => setMedicalAdvice(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Doctor Notes</label>
                <textarea
                  rows={2}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Follow-up Date</label>
                <input
                  type="date"
                  value={followUpDate}
                  onChange={(e) => setFollowUpDate(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 border border-slate-300 text-slate-700 rounded-lg text-sm font-medium hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium shadow-sm disabled:opacity-50"
                >
                  {saving ? 'Updating...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
