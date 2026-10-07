import React, { useState, useEffect } from 'react';
import { appointmentService } from '../services/api';
import { Appointment } from '../types';
import { Link } from 'react-router-dom';
import { Calendar, Clock, AlertCircle, CheckCircle, XCircle } from 'lucide-react';

export const PatientAppointments: React.FC = () => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<number | null>(null);

  const fetchAppointments = async () => {
    setLoading(true);
    try {
      const res = await appointmentService.getMyAppointments(0, 50);
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

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this appointment?')) return;
    setCancellingId(id);
    try {
      await appointmentService.cancel(id, 'Patient cancelled via portal');
      await fetchAppointments();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel appointment');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">Confirmed</span>;
      case 'COMPLETED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">Completed</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">Cancelled</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">{status}</span>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">My Appointments</h1>
          <p className="text-sm text-slate-500 mt-1">Manage scheduled consultations and visit history</p>
        </div>
        <Link
          to="/professionals"
          className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          Book New Appointment
        </Link>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : appointments.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <Calendar className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p className="text-slate-600 font-medium">No appointments found</p>
          <p className="text-slate-400 text-xs mt-1">Book your first doctor appointment now.</p>
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
                  <h3 className="font-bold text-slate-900 text-base">{appt.professionalName}</h3>
                  <span className="text-xs px-2 py-0.5 bg-slate-100 rounded text-slate-600">{appt.specialization}</span>
                  {getStatusBadge(appt.status)}
                </div>

                <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                  <span className="flex items-center gap-1 font-medium text-slate-700">
                    <Calendar className="w-3.5 h-3.5 text-teal-600" /> {appt.appointmentDate}
                  </span>
                  <span className="flex items-center gap-1 font-medium text-slate-700">
                    <Clock className="w-3.5 h-3.5 text-teal-600" /> {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                  </span>
                </div>

                <p className="text-xs text-slate-600 italic">"{appt.reason}"</p>
              </div>

              <div className="flex items-center gap-2 w-full md:w-auto">
                {appt.status === 'COMPLETED' && (
                  <Link
                    to={`/patient/consultations?appointmentId=${appt.id}`}
                    className="flex-1 md:flex-initial text-center px-3 py-1.5 bg-teal-50 hover:bg-teal-100 text-teal-700 border border-teal-200 rounded-lg text-xs font-semibold transition-colors"
                  >
                    View Advice
                  </Link>
                )}
                {appt.status !== 'CANCELLED' && appt.status !== 'COMPLETED' && (
                  <button
                    onClick={() => handleCancel(appt.id)}
                    disabled={cancellingId === appt.id}
                    className="flex-1 md:flex-initial text-center px-3 py-1.5 border border-rose-200 hover:bg-rose-50 text-rose-600 rounded-lg text-xs font-semibold transition-colors disabled:opacity-50"
                  >
                    {cancellingId === appt.id ? 'Cancelling...' : 'Cancel'}
                  </button>
                )}
                <Link
                  to={`/messages`}
                  className="px-3 py-1.5 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-lg text-xs font-medium"
                >
                  Message
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
