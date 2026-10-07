import React, { useState, useEffect } from 'react';
import { adminService } from '../services/api';
import { AuditLog } from '../types';
import { Shield, Clock, User } from 'lucide-react';

export const AdminAuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService
      .getAuditLogs(0, 100)
      .then((res) => setLogs(res.content))
      .catch(() => setLogs([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">Security Audit Logs</h1>
        <p className="text-sm text-slate-500 mt-1">Immutable track of all clinical, access, and administrative operations</p>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
          </div>
        ) : logs.length === 0 ? (
          <div className="text-center py-12 text-slate-500">No audit records found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                <tr>
                  <th className="px-6 py-3">Timestamp</th>
                  <th className="px-6 py-3">Action</th>
                  <th className="px-6 py-3">Resource Type</th>
                  <th className="px-6 py-3">Resource ID</th>
                  <th className="px-6 py-3">Actor User ID</th>
                  <th className="px-6 py-3">IP Address</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono text-xs">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50">
                    <td className="px-6 py-3 text-slate-500">{log.timestamp.replace('T', ' ').substring(0, 19)}</td>
                    <td className="px-6 py-3 font-semibold text-teal-700">{log.action}</td>
                    <td className="px-6 py-3 text-slate-800">{log.resourceType}</td>
                    <td className="px-6 py-3 text-slate-600">{log.resourceId || '—'}</td>
                    <td className="px-6 py-3 text-slate-800">{log.actorUserId ? `#${log.actorUserId}` : 'SYSTEM'}</td>
                    <td className="px-6 py-3 text-slate-400">{log.ipAddress || '127.0.0.1'}</td>
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
