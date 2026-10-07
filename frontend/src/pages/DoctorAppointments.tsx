import React, { useState, useEffect } from 'react';
import { appointmentService, consultationService } from '../services/api';
import { Appointment } from '../types';
import { Calendar, Clock, CheckCircle, FileEdit, X, AlertCircle } from 'lucide-react';

export const DoctorAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);

  // Modal for consultation
  const [modalOpen, setModalOpen] = useState(false);
  const [activeAppt, setActiveAppt] = useState<Appointment | null>(null);
  const [medicalAdvice, setMedicalAdvice] = useState('');
  const [notes, setNotes] = useState('');
  const [followUpDate, setFollowUpDate] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const res = await appointmentService.getProfessionalAppointments(0, 50);
      setAppointments(res.content);
    } catch {
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const openConsultationModal = (appt: Appointment) => {
    setActiveAppt(appt);
    setMedicalAdvice('');
    setNotes('');
    setFollowUpDate('');
    setError('');
    setModalOpen(true);
  };

  const handleCompleteConsultation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeAppt) return;
    setError('');
    setSubmitting(true);

    try {
      await consultationService.create({
        appointmentId: activeAppt.id,
        medicalAdvice: medicalAdvice.trim(),
        notes: notes ? notes.trim() : undefined,
        followUpDate: followUpDate || undefined,
      });
      setModalOpen(false);
      await fetchAppointments();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to complete consultation');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">My Consultation Schedule</h1>
        <p className="text-sm text-slate-500 mt-1">Review scheduled patient appointments and record clinical advice</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : appointments.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <Calendar className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p className="text-slate-600 font-medium">No appointments scheduled</p>
          <p className="text-slate-400 text-xs mt-1">Patients booking appointments will appear on your schedule.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {appointments.map((appt) => (
            <div
              key={appt.id}
              className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
            >
              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <h3 className="font-bold text-slate-900 text-base">{appt.patientName}</h3>
                  <span className="text-xs px-2 py-0.5 bg-slate-100 rounded text-slate-500">{appt.patientEmail}</span>
                  <span
                    className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                      appt.status === 'COMPLETED'
                        ? 'bg-blue-50 text-blue-700 border border-blue-200'
                        : appt.status === 'CANCELLED'
                        ? 'bg-rose-50 text-rose-700 border border-rose-200'
                        : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                    }`}
                  >
                    {appt.status}
                  </span>
                </div>

                <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                  <span className="flex items-center gap-1 font-medium text-slate-700">
                    <Calendar className="w-3.5 h-3.5 text-teal-600" /> {appt.appointmentDate}
                  </span>
                  <span className="flex items-center gap-1 font-medium text-slate-700">
                    <Clock className="w-3.5 h-3.5 text-teal-600" /> {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                  </span>
                </div>

                <p className="text-xs text-slate-600 italic">Chief Complaint: "{appt.reason}"</p>
              </div>

              <div>
                {appt.status === 'CONFIRMED' && (
                  <button
                    onClick={() => openConsultationModal(appt)}
                    className="flex items-center gap-1.5 px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition-colors shadow-sm"
                  >
                    <FileEdit className="w-4 h-4" /> Complete Consultation
                  </button>
                )}
                {appt.status === 'COMPLETED' && (
                  <span className="text-xs font-semibold text-blue-700 flex items-center gap-1">
                    <CheckCircle className="w-4 h-4" /> Consultation Completed
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Complete Consultation Modal */}
      {modalOpen && activeAppt && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex justify-center items-center p-4">
          <div className="bg-white rounded-xl max-w-xl w-full p-6 shadow-xl border border-slate-200">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="text-lg font-bold text-slate-900">Record Consultation</h2>
                <p className="text-xs text-slate-500">
                  Patient: {activeAppt.patientName} | {activeAppt.appointmentDate}
                </p>
              </div>
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

            <form onSubmit={handleCompleteConsultation} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                  Medical Advice & Prescription *
                </label>
                <textarea
                  rows={4}
                  required
                  value={medicalAdvice}
                  onChange={(e) => setMedicalAdvice(e.target.value)}
                  placeholder="Official instructions, diagnosis, medication dosages, care plan..."
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Internal Doctor Notes</label>
                <textarea
                  rows={2}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Clinical observations, differential diagnoses..."
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Follow-up Date (optional)</label>
                <input
                  type="date"
                  min={new Date().toISOString().split('T')[0]}
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
                  disabled={submitting}
                  className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium shadow-sm disabled:opacity-50"
                >
                  {submitting ? 'Submitting...' : 'Save & Complete Consultation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
