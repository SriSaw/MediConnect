import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { professionalService, appointmentService } from '../services/api';
import { ProfessionalProfile, Availability } from '../types';
import { Calendar, Clock, DollarSign, Award, AlertCircle, CheckCircle2, Info } from 'lucide-react';

export const BookAppointment: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [professional, setProfessional] = useState<ProfessionalProfile | null>(null);
  const [availabilities, setAvailabilities] = useState<Availability[]>([]);
  const [loading, setLoading] = useState(true);

  // Form fields
  const [selectedSlotId, setSelectedSlotId] = useState<number | null>(null);
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
        .catch(() => {
          setError('Failed to load healthcare professional profile');
        })
        .finally(() => setLoading(false));
    }
  }, [id]);

  const getNextDateForDay = (dayName: string): string => {
    const daysMap: Record<string, number> = {
      SUNDAY: 0,
      MONDAY: 1,
      TUESDAY: 2,
      WEDNESDAY: 3,
      THURSDAY: 4,
      FRIDAY: 5,
      SATURDAY: 6,
    };
    const targetDay = daysMap[dayName.toUpperCase()];
    if (targetDay === undefined) return '';

    const today = new Date();
    const currentDay = today.getDay();
    let daysToAdd = (targetDay - currentDay + 7) % 7;
    if (daysToAdd === 0) {
      daysToAdd = 7;
    }
    const nextDate = new Date(today);
    nextDate.setDate(today.getDate() + daysToAdd);
    return nextDate.toISOString().split('T')[0];
  };

  const handleSelectSlot = (slot: Availability) => {
    setSelectedSlotId(slot.id);
    const start = slot.startTime.substring(0, 5);
    setStartTime(start);

    // Calculate a default 30-minute end time
    const [h, m] = start.split(':').map(Number);
    const endMinutes = h * 60 + m + 30;
    const endH = Math.floor(endMinutes / 60);
    const endM = endMinutes % 60;
    const calculatedEnd = `${String(endH).padStart(2, '0')}:${String(endM).padStart(2, '0')}`;
    const slotEnd = slot.endTime.substring(0, 5);
    setEndTime(calculatedEnd <= slotEnd ? calculatedEnd : slotEnd);

    // Auto-populate the next matching calendar date
    const nextDate = getNextDateForDay(slot.dayOfWeek);
    if (nextDate) {
      setSelectedDate(nextDate);
    }
    setError('');
  };

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
      let msg = 'Failed to book appointment. Please verify the doctor is available at the selected time.';
      if (err.response?.data) {
        const d = err.response.data;
        if (d.message && typeof d.message === 'string') {
          msg = d.message;
        } else if (d.errors && typeof d.errors === 'object') {
          const vals = Object.values(d.errors).filter(Boolean);
          if (vals.length > 0) msg = vals.join('; ');
        } else if (d.validationErrors && typeof d.validationErrors === 'object') {
          const vals = Object.values(d.validationErrors).filter(Boolean);
          if (vals.length > 0) msg = vals.join('; ');
        }
      } else if (err.message) {
        msg = err.message;
      }
      setError(msg);
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

  // Validate if current selectedDate matches any availability day
  const chosenDayOfWeek = selectedDate
    ? new Date(selectedDate + 'T12:00:00').toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase()
    : null;
  const isDayAvailable = chosenDayOfWeek && availabilities.length > 0
    ? availabilities.some((a) => a.dayOfWeek.toUpperCase() === chosenDayOfWeek)
    : true;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row justify-between items-start gap-4">
        <div>
          <span className="text-xs font-semibold uppercase text-teal-600 tracking-wider">Book Consultation</span>
          <h1 className="text-2xl font-bold text-slate-800">
            {professional.name || (professional as any).user?.name || 'Healthcare Professional'}
          </h1>
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
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-2 flex items-center gap-1.5">
            <Clock className="w-4 h-4 text-teal-600" /> Available Hours
          </h2>
          <p className="text-xs text-slate-400 mb-3">Click any window to auto-select date and time:</p>

          {availabilities.length === 0 ? (
            <p className="text-xs text-slate-500">No scheduled hours configured.</p>
          ) : (
            <div className="space-y-2">
              {availabilities.map((a) => {
                const isSelected = selectedSlotId === a.id;
                return (
                  <button
                    key={a.id}
                    type="button"
                    onClick={() => handleSelectSlot(a)}
                    className={`w-full text-left p-2.5 rounded-lg border transition-all text-xs flex justify-between items-center ${
                      isSelected
                        ? 'border-teal-500 bg-teal-50/70 text-teal-900 ring-1 ring-teal-500'
                        : 'border-slate-200 hover:border-teal-300 hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    <div>
                      <span className="font-semibold block">{a.dayOfWeek}</span>
                      <span className="text-slate-500">
                        {a.startTime.substring(0, 5)} - {a.endTime.substring(0, 5)}
                      </span>
                    </div>
                    {isSelected && (
                      <span className="px-1.5 py-0.5 bg-teal-600 text-white text-[10px] font-bold rounded">
                        Selected
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
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
              <span>Appointment successfully booked! Redirecting to appointments...</span>
            </div>
          )}

          {!isDayAvailable && chosenDayOfWeek && (
            <div className="mb-4 p-3 bg-amber-50 border border-amber-200 text-amber-800 text-xs rounded-lg flex items-center gap-2">
              <Info className="w-4 h-4 flex-shrink-0 text-amber-600" />
              <span>
                Note: Doctor is not scheduled on <strong>{chosenDayOfWeek}s</strong>. Please pick one of the available windows on the left.
              </span>
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
                onChange={(e) => {
                  setSelectedDate(e.target.value);
                  setError('');
                }}
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
                  onChange={(e) => {
                    setStartTime(e.target.value);
                    setError('');
                  }}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">End Time</label>
                <input
                  type="time"
                  required
                  value={endTime}
                  onChange={(e) => {
                    setEndTime(e.target.value);
                    setError('');
                  }}
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
                onChange={(e) => {
                  setReason(e.target.value);
                  setError('');
                }}
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
