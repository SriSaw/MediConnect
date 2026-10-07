import React, { useState, useEffect } from 'react';
import { adminService } from '../services/api';
import { User, UserRole, UserStatus } from '../types';
import { Search, Shield, CheckCircle, Ban, RefreshCw, UserCheck } from 'lucide-react';

export const AdminUsers: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await adminService.getUsers({
        query: query || undefined,
        role: roleFilter || undefined,
        status: statusFilter || undefined,
        size: 50,
      });
      setUsers(res.content);
    } catch {
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, statusFilter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchUsers();
  };

  const handleStatusChange = async (userId: number, newStatus: string) => {
    try {
      await adminService.updateUserStatus(userId, newStatus);
      await fetchUsers();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update user status');
    }
  };

  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      await adminService.updateUserRole(userId, newRole);
      await fetchUsers();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update user role');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">User Management</h1>
        <p className="text-sm text-slate-500 mt-1">Manage platform accounts, security roles, and user access states</p>

        <form onSubmit={handleSearch} className="mt-4 flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search by name or email..."
              className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-sm"
            />
          </div>
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
          >
            <option value="">All Roles</option>
            <option value="ADMIN">ADMIN</option>
            <option value="HEALTHCARE_PROFESSIONAL">HEALTHCARE_PROFESSIONAL</option>
            <option value="PATIENT">PATIENT</option>
          </select>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="SUSPENDED">SUSPENDED</option>
          </select>
          <button
            type="submit"
            className="px-5 py-2 bg-teal-600 hover:bg-teal-700 text-white font-medium rounded-lg text-sm shadow-sm"
          >
            Filter
          </button>
        </form>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
          </div>
        ) : users.length === 0 ? (
          <div className="text-center py-12 text-slate-500">No users found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-xs font-semibold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                <tr>
                  <th className="px-6 py-3">User</th>
                  <th className="px-6 py-3">Role</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3">Phone</th>
                  <th className="px-6 py-3">Created</th>
                  <th className="px-6 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-900">{u.name}</div>
                      <div className="text-xs text-slate-400">{u.email}</div>
                    </td>
                    <td className="px-6 py-4">
                      <select
                        value={u.role}
                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                        className="text-xs font-semibold bg-slate-100 border border-slate-200 rounded px-2 py-1"
                      >
                        <option value="ADMIN">ADMIN</option>
                        <option value="HEALTHCARE_PROFESSIONAL">HEALTHCARE_PROFESSIONAL</option>
                        <option value="PATIENT">PATIENT</option>
                      </select>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${
                          u.status === 'ACTIVE'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : u.status === 'SUSPENDED'
                            ? 'bg-rose-50 text-rose-700 border border-rose-200'
                            : 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        {u.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs">{u.phone || '—'}</td>
                    <td className="px-6 py-4 text-xs text-slate-400">{u.createdAt.substring(0, 10)}</td>
                    <td className="px-6 py-4 text-right">
                      {u.status === 'ACTIVE' ? (
                        <button
                          onClick={() => handleStatusChange(u.id, 'SUSPENDED')}
                          className="px-2.5 py-1 text-xs border border-rose-200 text-rose-600 hover:bg-rose-50 rounded"
                          title="Suspend User"
                        >
                          Suspend
                        </button>
                      ) : (
                        <button
                          onClick={() => handleStatusChange(u.id, 'ACTIVE')}
                          className="px-2.5 py-1 text-xs border border-emerald-200 text-emerald-600 hover:bg-emerald-50 rounded"
                          title="Activate User"
                        >
                          Activate
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
