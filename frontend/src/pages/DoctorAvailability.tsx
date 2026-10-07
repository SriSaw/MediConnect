import React, { useState, useEffect } from 'react';
import { professionalService } from '../services/api';
import { Availability } from '../types';
import { Clock, Plus, Trash2, AlertCircle, CheckCircle2 } from 'lucide-react';

const DAYS = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export const DoctorAvailability: React.FC = () => {
  const [availabilities, setAvailabilities] = useState<Availability[]>([]);
  const [loading, setLoading] = useState(true);

  // New slot form
  const [dayOfWeek, setDayOfWeek] = useState('MONDAY');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('17:00');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchAvailability = async () => {
    setLoading(true);
    try {
      const data = await professionalService.getMyAvailability();
      setAvailabilities(data);
    } catch {
      setAvailabilities([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAvailability();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      await professionalService.addAvailability({
        dayOfWeek,
        startTime: startTime + (startTime.length === 5 ? ':00' : ''),
        endTime: endTime + (endTime.length === 5 ? ':00' : ''),
        available: true,
      });
      setSuccess('Availability slot successfully added!');
      await fetchAvailability();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add availability');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to remove this availability window?')) return;
    try {
      await professionalService.deleteAvailability(id);
      await fetchAvailability();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete availability');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">Doctor Working Hours & Availability</h1>
        <p className="text-sm text-slate-500 mt-1">
          Define recurring weekly windows when patients are permitted to book appointments
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Form card */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-4 flex items-center gap-1.5">
            <Plus className="w-4 h-4 text-teal-600" /> Add Available Window
          </h2>

          {error && (
            <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg flex items-center gap-1.5">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs rounded-lg flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
              <span>{success}</span>
            </div>
          )}

          <form onSubmit={handleAdd} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Day of Week</label>
              <select
                value={dayOfWeek}
                onChange={(e) => setDayOfWeek(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
              >
                {DAYS.map((d) => (
                  <option key={d} value={d}>
                    {d}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
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

            <button
              type="submit"
              disabled={saving}
              className="w-full py-2.5 px-4 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg shadow-sm text-sm disabled:opacity-50"
            >
              {saving ? 'Adding...' : 'Add Window'}
            </button>
          </form>
        </div>

        {/* Existing schedules table */}
        <div className="md:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider mb-4 flex items-center gap-1.5">
            <Clock className="w-4 h-4 text-teal-600" /> Current Weekly Schedule
          </h2>

          {loading ? (
            <div className="flex justify-center py-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600"></div>
            </div>
          ) : availabilities.length === 0 ? (
            <p className="text-sm text-slate-500 py-6 text-center">
              No availability windows set. Add your first working hours on the left.
            </p>
          ) : (
            <div className="divide-y divide-slate-100">
              {availabilities.map((avail) => (
                <div key={avail.id} className="py-3 flex justify-between items-center">
                  <div className="flex items-center gap-3">
                    <span className="w-28 text-sm font-semibold text-slate-800">{avail.dayOfWeek}</span>
                    <span className="text-sm font-mono text-teal-700 bg-teal-50 px-2 py-0.5 rounded border border-teal-200">
                      {avail.startTime.substring(0, 5)} - {avail.endTime.substring(0, 5)}
                    </span>
                  </div>
                  <button
                    onClick={() => handleDelete(avail.id)}
                    className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded"
                    title="Delete Window"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
