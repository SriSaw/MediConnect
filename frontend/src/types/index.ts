export type UserRole = 'ADMIN' | 'HEALTHCARE_PROFESSIONAL' | 'PATIENT';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export type NotificationType =
  | 'APPOINTMENT_BOOKED'
  | 'APPOINTMENT_CONFIRMED'
  | 'APPOINTMENT_CANCELLED'
  | 'CONSULTATION_COMPLETED'
  | 'NEW_MESSAGE'
  | 'SYSTEM_ALERT';

export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface PatientProfile {
  id: number;
  userId: number;
  userName: string;
  email: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: string;
  bloodGroup?: string;
  address?: string;
  emergencyContact?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProfessionalProfile {
  id: number;
  userId: number;
  name: string;
  email: string;
  phone?: string;
  specialization: string;
  licenseNumber: string;
  experienceYears: number;
  bio?: string;
  consultationFee: number;
  verified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Availability {
  id: number;
  professionalId: number;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  available: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  patientEmail: string;
  professionalId: number;
  professionalName: string;
  specialization: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  reason: string;
  createdAt: string;
  updatedAt: string;
}

export interface Consultation {
  id: number;
  appointmentId: number;
  patientId: number;
  patientName: string;
  professionalId: number;
  professionalName: string;
  notes?: string;
  medicalAdvice: string;
  followUpDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MedicalRecord {
  id: number;
  patientId: number;
  recordType: string;
  title: string;
  description: string;
  recordDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
}

export interface Message {
  id: number;
  senderId: number;
  senderName: string;
  receiverId: number;
  receiverName: string;
  appointmentId?: number;
  content: string;
  read: boolean;
  createdAt: string;
}

export interface ConversationSummary {
  otherUserId: number;
  otherUserName: string;
  lastMessage: string;
  lastMessageTimestamp: string;
  unreadCount: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface AnalyticsOverview {
  totalUsers: number;
  totalPatients: number;
  totalProfessionals: number;
  totalAppointments: number;
  totalConsultations: number;
  pendingAppointments: number;
  confirmedAppointments: number;
  completedAppointments: number;
  unverifiedProfessionals: number;
}

export interface SystemSetting {
  id: number;
  settingKey: string;
  settingValue: string;
  description?: string;
  updatedAt: string;
}

export interface AuditLog {
  id: number;
  actorUserId?: number;
  action: string;
  resourceType: string;
  resourceId?: string;
  timestamp: string;
  ipAddress?: string;
}
