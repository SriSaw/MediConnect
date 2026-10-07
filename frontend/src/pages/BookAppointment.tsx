import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { professionalService, appointmentService } from '../services/api';
import { ProfessionalProfile, Availability } from '../types';
import { Calendar, Clock, DollarSign, Award, AlertCircle, CheckCircle2 } from 'lucide-react';

export const BookAppointment: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [professional, setProfessional] = useState<ProfessionalProfile | null>(null);
  const [availabilities, setAvailabilities] = useState<Availability[]>([]);
  const [loading, setLoading] = useState(true);

  // Form fields
  const [selectedDate, setSelectedDate] = useState('');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('09:30');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (id) {
      const profId = Number(id);
      Promise.all([
        professionalService.getById(profId),
        professionalService.getAvailability(profId),
      ])
        .then(([prof, avail]) => {
          setProfessional(prof);
          setAvailabilities(avail);
        })
        .catch((err) => {
          setError('Failed to load doctor profile');
        })
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!professional) return;
    setError('');
    setSubmitting(true);

    try {
      await appointmentService.book({
        professionalId: professional.id,
        appointmentDate: selectedDate,
        startTime: startTime.slice(0, 5),
        endTime: endTime.slice(0, 5),
        reason: reason.trim(),
      });
      setSuccess(true);
      setTimeout(() => {
        navigate('/patient/appointments');
      }, 1500);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          'Failed to book appointment. Please verify the doctor is available at the selected time.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
      </div>
    );
  }

  if (!professional) {
    return (
      <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
        <p className="text-slate-500">Healthcare professional not found.</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row justify-between items-start gap-4">
        <div>
          <span className="text-xs font-semibold uppercase text-teal-600 tracking-wider">Book Consultation</span>
          <h1 className="text-2xl font-bold text-slate-800">{professional.name || (professional as any).user?.name || 'Healthcare Professional'}</h1>
          <p className="text-slate-500 text-sm mt-1">{professional.specialization}</p>
        </div>
        <div className="text-right">
          <span className="text-xs text-slate-500">Consultation Fee</span>
          <p className="text-2xl font-black text-slate-900">${professional.consultationFee.toFixed(2)}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Availability Schedule card */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-4 flex items-center gap-1.5">
            <Clock className="w-4 h-4 text-teal-600" /> Available Hours
          </h2>
          {availabilities.length === 0 ? (
            <p className="text-xs text-slate-500">No scheduled hours configured.</p>
          ) : (
            <ul className="space-y-2.5 text-xs">
              {availabilities.map((a) => (
                <li key={a.id} className="flex justify-between items-center py-1 border-b border-slate-100">
                  <span className="font-semibold text-slate-700">{a.dayOfWeek}</span>
                  <span className="text-slate-500">
                    {a.startTime.substring(0, 5)} - {a.endTime.substring(0, 5)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Booking Form card */}
        <div className="md:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          {error && (
            <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-sm rounded-lg flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-lg flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
              <span>Appointment successfully booked! Redirecting...</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Appointment Date</label>
              <input
                type="date"
                required
                min={new Date().toISOString().split('T')[0]}
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Start Time</label>
                <input
                  type="time"
                  required
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">End Time</label>
                <input
                  type="time"
                  required
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Reason for Visit</label>
              <textarea
                rows={3}
                required
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Describe symptoms, reason for consultation, or questions..."
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
              />
            </div>

            <button
              type="submit"
              disabled={submitting || success}
              className="w-full py-2.5 px-4 bg-teal-600 hover:bg-teal-700 disabled:opacity-50 text-white font-medium rounded-lg shadow-sm transition-colors text-sm"
            >
              {submitting ? 'Confirming Booking...' : 'Confirm Appointment'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
