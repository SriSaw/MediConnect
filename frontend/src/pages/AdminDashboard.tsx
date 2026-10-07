import React, { useState, useEffect } from 'react';
import { adminService } from '../services/api';
import { AnalyticsOverview } from '../types';
import { Users, UserCheck, Calendar, FileText, Clock, AlertTriangle, ShieldCheck } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const [analytics, setAnalytics] = useState<AnalyticsOverview | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService
      .getAnalyticsOverview()
      .then(setAnalytics)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
      </div>
    );
  }

  const statCards = [
    { label: 'Total Users', value: analytics?.totalUsers || 0, icon: Users, color: 'text-indigo-600 bg-indigo-50 border-indigo-200' },
    { label: 'Registered Patients', value: analytics?.totalPatients || 0, icon: Users, color: 'text-teal-600 bg-teal-50 border-teal-200' },
    { label: 'Doctors / Specialists', value: analytics?.totalProfessionals || 0, icon: UserCheck, color: 'text-emerald-600 bg-emerald-50 border-emerald-200' },
    { label: 'Total Appointments', value: analytics?.totalAppointments || 0, icon: Calendar, color: 'text-sky-600 bg-sky-50 border-sky-200' },
    { label: 'Completed Consultations', value: analytics?.totalConsultations || 0, icon: FileText, color: 'text-blue-600 bg-blue-50 border-blue-200' },
    { label: 'Pending Appointments', value: analytics?.pendingAppointments || 0, icon: Clock, color: 'text-amber-600 bg-amber-50 border-amber-200' },
    { label: 'Confirmed Appointments', value: analytics?.confirmedAppointments || 0, icon: Calendar, color: 'text-teal-600 bg-teal-50 border-teal-200' },
    { label: 'Unverified Doctors', value: analytics?.unverifiedProfessionals || 0, icon: AlertTriangle, color: 'text-rose-600 bg-rose-50 border-rose-200' },
  ];

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Admin Control Center</h1>
          <p className="text-sm text-slate-500 mt-1">Platform analytics, health indicators, and operational metrics</p>
        </div>
        <div className="flex items-center gap-1.5 px-3 py-1 bg-teal-50 text-teal-700 border border-teal-200 rounded-lg text-xs font-semibold">
          <ShieldCheck className="w-4 h-4 text-teal-600" /> System Healthy
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((card, i) => {
          const Icon = card.icon;
          return (
            <div key={i} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
              <div className={`p-3 rounded-lg border ${card.color}`}>
                <Icon className="w-6 h-6" />
              </div>
              <div>
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{card.label}</p>
                <p className="text-2xl font-black text-slate-900 mt-0.5">{card.value}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
