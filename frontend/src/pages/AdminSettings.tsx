import React, { useState, useEffect } from 'react';
import { adminService } from '../services/api';
import { SystemSetting } from '../types';
import { Settings, Save, AlertCircle, CheckCircle2 } from 'lucide-react';

export const AdminSettings: React.FC = () => {
  const [settings, setSettings] = useState<SystemSetting[]>([]);
  const [loading, setLoading] = useState(true);
  const [savingKey, setSavingKey] = useState<string | null>(null);
  const [values, setValues] = useState<Record<string, string>>({});
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchSettings = async () => {
    setLoading(true);
    try {
      const data = await adminService.getSettings();
      setSettings(data);
      const valMap: Record<string, string> = {};
      data.forEach((s) => {
        valMap[s.settingKey] = s.settingValue;
      });
      setValues(valMap);
    } catch {
      setSettings([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleSave = async (key: string) => {
    setMessage(null);
    setSavingKey(key);
    try {
      await adminService.updateSetting(key, { settingValue: values[key] });
      setMessage({ type: 'success', text: `Setting '${key}' saved successfully.` });
    } catch (err: any) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update setting' });
    } finally {
      setSavingKey(null);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-800">System Configuration Settings</h1>
        <p className="text-sm text-slate-500 mt-1">Configure global platform options and defaults</p>
      </div>

      {message && (
        <div
          className={`p-3 rounded-lg text-sm flex items-center gap-2 ${
            message.type === 'success'
              ? 'bg-emerald-50 border border-emerald-200 text-emerald-700'
              : 'bg-rose-50 border border-rose-200 text-rose-700'
          }`}
        >
          {message.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
          <span>{message.text}</span>
        </div>
      )}

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm divide-y divide-slate-100">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
          </div>
        ) : settings.length === 0 ? (
          <p className="text-center py-12 text-slate-500">No system settings available.</p>
        ) : (
          settings.map((s) => (
            <div key={s.id} className="p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
              <div className="flex-1">
                <span className="font-mono text-xs font-bold text-teal-700 bg-teal-50 px-2 py-0.5 rounded border border-teal-200">
                  {s.settingKey}
                </span>
                <p className="text-xs text-slate-500 mt-1">{s.description || 'Global configuration parameter.'}</p>
              </div>

              <div className="flex items-center gap-2 w-full sm:w-auto">
                <input
                  type="text"
                  value={values[s.settingKey] || ''}
                  onChange={(e) => setValues({ ...values, [s.settingKey]: e.target.value })}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-sm w-full sm:w-64 focus:outline-none focus:ring-2 focus:ring-teal-500"
                />
                <button
                  onClick={() => handleSave(s.settingKey)}
                  disabled={savingKey === s.settingKey}
                  className="flex items-center gap-1 px-3 py-1.5 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-xs font-medium transition-colors shadow-sm disabled:opacity-50"
                >
                  <Save className="w-3.5 h-3.5" />
                  {savingKey === s.settingKey ? 'Saving...' : 'Save'}
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
