import React, { useState, useEffect } from 'react';
import { notificationService } from '../services/api';
import { Notification } from '../types';
import { Bell, Check, CheckCheck } from 'lucide-react';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationService.getAll(0, 50);
      setNotifications(res.content);
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch {}
  };

  const markAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch {}
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Notifications</h1>
          <p className="text-sm text-slate-500 mt-1">Updates on your appointments, advice, and direct messages</p>
        </div>
        <button
          onClick={markAllRead}
          className="flex items-center gap-1.5 px-3 py-1.5 border border-slate-200 hover:bg-slate-50 rounded-lg text-xs font-semibold text-slate-700"
        >
          <CheckCheck className="w-4 h-4 text-teal-600" /> Mark all as read
        </button>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm divide-y divide-slate-100 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-12 text-slate-400 text-sm">
            <Bell className="w-8 h-8 mx-auto mb-2 text-slate-300" />
            No notifications.
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`p-4 flex items-start justify-between gap-4 transition-colors ${
                !n.read ? 'bg-teal-50/40' : 'hover:bg-slate-50'
              }`}
            >
              <div className="flex items-start gap-3">
                <div
                  className={`mt-1 w-2.5 h-2.5 rounded-full flex-shrink-0 ${
                    !n.read ? 'bg-teal-500' : 'bg-transparent'
                  }`}
                />
                <div>
                  <h4 className="text-sm font-semibold text-slate-800">{n.title}</h4>
                  <p className="text-xs text-slate-600 mt-0.5">{n.message}</p>
                  <span className="text-[10px] text-slate-400 mt-1 block">
                    {n.createdAt.replace('T', ' ').substring(0, 16)}
                  </span>
                </div>
              </div>

              {!n.read && (
                <button
                  onClick={() => markRead(n.id)}
                  className="p-1 text-slate-400 hover:text-teal-600 rounded"
                  title="Mark as read"
                >
                  <Check className="w-4 h-4" />
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};
