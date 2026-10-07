import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/api';
import { Appointment } from '../types';
import { Calendar, Clock, Filter, XCircle } from 'lucide-react';

export const AdminAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const res = await appointmentService.getAdminAppointments({
        status: statusFilter || undefined,
        date: dateFilter || undefined,
        size: 50,
      });
      setAppointments(res.content);
    } catch {
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, [statusFilter, dateFilter]);

  const handleCancel = async (id: number) => {
    const reason = prompt('Enter reason for administrative cancellation:');
    if (!reason) return;
    try {
      await appointmentService.adminCancel(id, reason);
      await fetchAppointments();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">Platform Appointments</h1>
        <p className="text-sm text-slate-500 mt-1">Supervise consultations scheduled across the entire platform</p>

        <div className="mt-4 flex flex-col sm:flex-row gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
          >
            <option value="">All Statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>

          <input
            type="date"
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm"
          />

          <button
            onClick={() => {
              setStatusFilter('');
              setDateFilter('');
            }}
            className="px-4 py-2 text-slate-600 hover:text-slate-900 text-sm font-medium"
          >
            Reset Filters
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
          </div>
        ) : appointments.length === 0 ? (
          <div className="text-center py-12 text-slate-500">No appointments found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                <tr>
                  <th className="px-6 py-3">ID</th>
                  <th className="px-6 py-3">Patient</th>
                  <th className="px-6 py-3">Doctor</th>
                  <th className="px-6 py-3">Date & Time</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3">Reason</th>
                  <th className="px-6 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {appointments.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4 font-mono text-xs">#{a.id}</td>
                    <td className="px-6 py-4 font-semibold text-slate-900">{a.patientName}</td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-slate-800">{a.professionalName}</div>
                      <div className="text-xs text-slate-400">{a.specialization}</div>
                    </td>
                    <td className="px-6 py-4 text-xs">
                      <div>{a.appointmentDate}</div>
                      <div className="text-slate-400">
                        {a.startTime.substring(0, 5)} - {a.endTime.substring(0, 5)}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${
                          a.status === 'CONFIRMED'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : a.status === 'COMPLETED'
                            ? 'bg-blue-50 text-blue-700 border border-blue-200'
                            : a.status === 'CANCELLED'
                            ? 'bg-rose-50 text-rose-700 border border-rose-200'
                            : 'bg-amber-50 text-amber-700 border border-amber-200'
                        }`}
                      >
                        {a.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-600 max-w-xs truncate">{a.reason}</td>
                    <td className="px-6 py-4 text-right">
                      {a.status !== 'CANCELLED' && a.status !== 'COMPLETED' && (
                        <button
                          onClick={() => handleCancel(a.id)}
                          className="px-2.5 py-1 text-xs border border-rose-200 text-rose-600 hover:bg-rose-50 rounded"
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
