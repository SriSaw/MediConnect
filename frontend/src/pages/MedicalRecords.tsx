import React, { useState, useEffect } from 'react';
import { medicalRecordService } from '../services/api';
import { MedicalRecord } from '../types';
import { FileText, Plus, Calendar, Trash2, Edit3, X, AlertCircle } from 'lucide-react';

export const MedicalRecords: React.FC = () => {
  const [records, setRecords] = useState<MedicalRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  // Form fields
  const [recordType, setRecordType] = useState('Diagnosis');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [recordDate, setRecordDate] = useState(new Date().toISOString().split('T')[0]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetchRecords = async () => {
    setLoading(true);
    try {
      const res = await medicalRecordService.getMyRecords(0, 50);
      setRecords(res.content);
    } catch {
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecords();
  }, []);

  const openCreateModal = () => {
    setEditingId(null);
    setRecordType('Diagnosis');
    setTitle('');
    setDescription('');
    setRecordDate(new Date().toISOString().split('T')[0]);
    setError('');
    setModalOpen(true);
  };

  const openEditModal = (rec: MedicalRecord) => {
    setEditingId(rec.id);
    setRecordType(rec.recordType);
    setTitle(rec.title);
    setDescription(rec.description);
    setRecordDate(rec.recordDate);
    setError('');
    setModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      if (editingId) {
        await medicalRecordService.update(editingId, {
          recordType,
          title,
          description,
          recordDate,
        });
      } else {
        await medicalRecordService.create({
          recordType,
          title,
          description,
          recordDate,
        });
      }
      setModalOpen(false);
      await fetchRecords();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save medical record');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this medical record?')) return;
    try {
      await medicalRecordService.delete(id);
      await fetchRecords();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete record');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">My Medical Records</h1>
          <p className="text-sm text-slate-500 mt-1">Keep track of diagnoses, test results, and clinical history</p>
        </div>
        <button
          onClick={openCreateModal}
          className="flex items-center gap-1.5 px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          <Plus className="w-4 h-4" /> Add Medical Record
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-600"></div>
        </div>
      ) : records.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-slate-200">
          <FileText className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p className="text-slate-600 font-medium">No medical records on file</p>
          <p className="text-slate-400 text-xs mt-1">Add your test reports or diagnoses for your consultations.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {records.map((rec) => (
            <div key={rec.id} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between">
              <div>
                <div className="flex justify-between items-start">
                  <span className="px-2.5 py-0.5 rounded text-xs font-semibold bg-teal-50 text-teal-700 border border-teal-200 uppercase">
                    {rec.recordType}
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => openEditModal(rec)}
                      className="p-1 text-slate-400 hover:text-slate-700"
                      title="Edit"
                    >
                      <Edit3 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(rec.id)}
                      className="p-1 text-slate-400 hover:text-rose-600"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <h3 className="font-bold text-slate-900 mt-2 text-base">{rec.title}</h3>
                <p className="text-slate-600 text-sm mt-2 whitespace-pre-wrap">{rec.description}</p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center gap-1.5 text-xs text-slate-400">
                <Calendar className="w-3.5 h-3.5" /> Date: {rec.recordDate}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal for create/edit */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex justify-center items-center p-4">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl border border-slate-200">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-bold text-slate-900">
                {editingId ? 'Edit Medical Record' : 'Add New Medical Record'}
              </h2>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="mb-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg flex items-center gap-2">
                <AlertCircle className="w-4 h-4" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Record Type</label>
                  <select
                    value={recordType}
                    onChange={(e) => setRecordType(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white"
                  >
                    <option value="Diagnosis">Diagnosis</option>
                    <option value="Lab Report">Lab Report</option>
                    <option value="Prescription">Prescription</option>
                    <option value="Immunization">Immunization</option>
                    <option value="Allergy">Allergy Record</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Record Date</label>
                  <input
                    type="date"
                    required
                    value={recordDate}
                    onChange={(e) => setRecordDate(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Title</label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Annual Blood Work / Chest X-Ray"
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Description / Notes</label>
                <textarea
                  rows={4}
                  required
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Clinical observations, test results, doctor comments..."
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 border border-slate-300 text-slate-700 rounded-lg text-sm font-medium hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium shadow-sm disabled:opacity-50"
                >
                  {saving ? 'Saving...' : 'Save Record'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
