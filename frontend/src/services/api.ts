import api from '../api/client';
import {
  AuthResponse,
  User,
  PatientProfile,
  ProfessionalProfile,
  Availability,
  Appointment,
  AppointmentReport,
  Consultation,
  MedicalRecord,
  Notification,
  Message,
  ConversationSummary,
  PageResponse,
  AnalyticsOverview,
  SystemSetting,
  AuditLog,
} from '../types';

export const authService = {
  login: async (credentials: { email: string; password: string }): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/login', credentials);
    return res.data;
  },
  register: async (data: {
    name: string;
    email: string;
    password: string;
    phone?: string;
    role: string;
    specialization?: string;
    licenseNumber?: string;
    experienceYears?: number;
    bio?: string;
    consultationFee?: number;
  }): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/register', data);
    return res.data;
  },
  me: async (): Promise<User> => {
    const res = await api.get<User>('/auth/me');
    return res.data;
  },
};

export const professionalService = {
  search: async (params?: { specialization?: string; search?: string; verified?: boolean; page?: number; size?: number }): Promise<PageResponse<ProfessionalProfile>> => {
    const res = await api.get<PageResponse<ProfessionalProfile>>('/professionals', { params });
    return res.data;
  },
  getById: async (id: number): Promise<ProfessionalProfile> => {
    const res = await api.get<ProfessionalProfile>(`/professionals/${id}`);
    return res.data;
  },
  getAvailability: async (id: number): Promise<Availability[]> => {
    const res = await api.get<Availability[]>(`/professionals/${id}/availability`);
    return res.data;
  },
  getMyProfile: async (): Promise<ProfessionalProfile> => {
    const res = await api.get<ProfessionalProfile>('/professionals/me');
    return res.data;
  },
  updateMyProfile: async (data: Partial<ProfessionalProfile>): Promise<ProfessionalProfile> => {
    const res = await api.put<ProfessionalProfile>('/professionals/me', data);
    return res.data;
  },
  getMyAvailability: async (): Promise<Availability[]> => {
    const res = await api.get<Availability[]>('/professionals/me/availability');
    return res.data;
  },
  addAvailability: async (data: { dayOfWeek: string; startTime: string; endTime: string; available: boolean }): Promise<Availability> => {
    const res = await api.post<Availability>('/professionals/me/availability', data);
    return res.data;
  },
  updateAvailability: async (id: number, data: { dayOfWeek: string; startTime: string; endTime: string; available: boolean }): Promise<Availability> => {
    const res = await api.put<Availability>(`/professionals/me/availability/${id}`, data);
    return res.data;
  },
  deleteAvailability: async (id: number): Promise<void> => {
    await api.delete(`/professionals/me/availability/${id}`);
  },
};

export const patientService = {
  getMyProfile: async (): Promise<PatientProfile> => {
    const res = await api.get<PatientProfile>('/patients/me');
    return res.data;
  },
  updateMyProfile: async (data: Partial<PatientProfile>): Promise<PatientProfile> => {
    const res = await api.put<PatientProfile>('/patients/me', data);
    return res.data;
  },
};

export const appointmentService = {
  book: async (data: { professionalId: number; appointmentDate: string; startTime: string; endTime: string; reason: string }): Promise<Appointment> => {
    const res = await api.post<Appointment>('/appointments', data);
    return res.data;
  },
  getMyAppointments: async (page = 0, size = 10): Promise<PageResponse<Appointment>> => {
    const res = await api.get<PageResponse<Appointment>>('/appointments/me', { params: { page, size } });
    return res.data;
  },
  getProfessionalAppointments: async (page = 0, size = 10): Promise<PageResponse<Appointment>> => {
    const res = await api.get<PageResponse<Appointment>>('/professionals/me/appointments', { params: { page, size } });
    return res.data;
  },
  getById: async (id: number): Promise<Appointment> => {
    const res = await api.get<Appointment>(`/appointments/${id}`);
    return res.data;
  },
  cancel: async (id: number, reason?: string): Promise<Appointment> => {
    const res = await api.patch<Appointment>(`/appointments/${id}/cancel`, { reason });
    return res.data;
  },
  getAdminAppointments: async (params?: { status?: string; date?: string; page?: number; size?: number }): Promise<PageResponse<Appointment>> => {
    const res = await api.get<PageResponse<Appointment>>('/admin/appointments', { params });
    return res.data;
  },
  getAppointmentReports: async (params?: {
    status?: string;
    startDate?: string;
    endDate?: string;
    professionalId?: number;
    patientId?: number;
    page?: number;
    size?: number;
  }): Promise<PageResponse<AppointmentReport>> => {
    const res = await api.get<PageResponse<AppointmentReport>>('/admin/appointments/reports', { params });
    return res.data;
  },
  adminCancel: async (id: number, reason?: string): Promise<Appointment> => {
    const res = await api.patch<Appointment>(`/admin/appointments/${id}/cancel`, { reason });
    return res.data;
  },
};

export const consultationService = {
  create: async (data: { appointmentId: number; notes?: string; medicalAdvice: string; followUpDate?: string }): Promise<Consultation> => {
    const res = await api.post<Consultation>('/consultations', data);
    return res.data;
  },
  getMyConsultations: async (page = 0, size = 10): Promise<PageResponse<Consultation>> => {
    const res = await api.get<PageResponse<Consultation>>('/consultations/me', { params: { page, size } });
    return res.data;
  },
  getById: async (id: number): Promise<Consultation> => {
    const res = await api.get<Consultation>(`/consultations/${id}`);
    return res.data;
  },
  getByAppointmentId: async (appointmentId: number): Promise<Consultation> => {
    const res = await api.get<Consultation>(`/consultations/appointment/${appointmentId}`);
    return res.data;
  },
  update: async (id: number, data: { notes?: string; medicalAdvice: string; followUpDate?: string }): Promise<Consultation> => {
    const res = await api.put<Consultation>(`/consultations/${id}`, data);
    return res.data;
  },
};

export const medicalRecordService = {
  getMyRecords: async (page = 0, size = 10): Promise<PageResponse<MedicalRecord>> => {
    const res = await api.get<PageResponse<MedicalRecord>>('/medical-records/me', { params: { page, size } });
    return res.data;
  },
  create: async (data: { recordType: string; title: string; description: string; recordDate: string }): Promise<MedicalRecord> => {
    const res = await api.post<MedicalRecord>('/medical-records/me', data);
    return res.data;
  },
  update: async (id: number, data: { recordType: string; title: string; description: string; recordDate: string }): Promise<MedicalRecord> => {
    const res = await api.put<MedicalRecord>(`/medical-records/me/${id}`, data);
    return res.data;
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/medical-records/me/${id}`);
  },
  getPatientRecords: async (patientId: number, page = 0, size = 10): Promise<PageResponse<MedicalRecord>> => {
    const res = await api.get<PageResponse<MedicalRecord>>(`/medical-records/patient/${patientId}`, { params: { page, size } });
    return res.data;
  },
};

export const notificationService = {
  getAll: async (page = 0, size = 20): Promise<PageResponse<Notification>> => {
    const res = await api.get<PageResponse<Notification>>('/notifications', { params: { page, size } });
    return res.data;
  },
  getUnreadCount: async (): Promise<number> => {
    const res = await api.get<{ unreadCount: number }>('/notifications/unread-count');
    return res.data.unreadCount;
  },
  markAsRead: async (id: number): Promise<Notification> => {
    const res = await api.patch<Notification>(`/notifications/${id}/read`);
    return res.data;
  },
  markAllAsRead: async (): Promise<void> => {
    await api.patch('/notifications/read-all');
  },
};

export const messageService = {
  send: async (data: { receiverId: number; appointmentId?: number; content: string }): Promise<Message> => {
    const res = await api.post<Message>('/messages', data);
    return res.data;
  },
  getConversations: async (): Promise<ConversationSummary[]> => {
    const res = await api.get<ConversationSummary[]>('/messages/conversations');
    return res.data;
  },
  getConversationWithUser: async (userId: number, page = 0, size = 50): Promise<PageResponse<Message>> => {
    const res = await api.get<PageResponse<Message>>(`/messages/${userId}`, { params: { page, size } });
    return res.data;
  },
};

export const adminService = {
  getUsers: async (params?: { role?: string; status?: string; query?: string; page?: number; size?: number }): Promise<PageResponse<User>> => {
    const res = await api.get<PageResponse<User>>('/admin/users', { params });
    return res.data;
  },
  updateUserStatus: async (userId: number, status: string): Promise<User> => {
    const res = await api.patch<User>(`/admin/users/${userId}/status`, { status });
    return res.data;
  },
  updateUserRole: async (userId: number, role: string): Promise<User> => {
    const res = await api.patch<User>(`/admin/users/${userId}/role`, { role });
    return res.data;
  },
  verifyProfessional: async (profId: number, verified: boolean): Promise<void> => {
    await api.patch(`/admin/professionals/${profId}/verify`, { verified });
  },
  getAnalyticsOverview: async (): Promise<AnalyticsOverview> => {
    const res = await api.get<AnalyticsOverview>('/admin/analytics/overview');
    return res.data;
  },
  getSettings: async (): Promise<SystemSetting[]> => {
    const res = await api.get<SystemSetting[]>('/admin/settings');
    return res.data;
  },
  updateSetting: async (key: string, data: { settingValue: string; description?: string }): Promise<SystemSetting> => {
    const res = await api.put<SystemSetting>(`/admin/settings/${key}`, data);
    return res.data;
  },
  getAuditLogs: async (page = 0, size = 20): Promise<PageResponse<AuditLog>> => {
    const res = await api.get<PageResponse<AuditLog>>('/admin/audit-logs', { params: { page, size } });
    return res.data;
  },
};
