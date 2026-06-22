export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'RECRUITER' | 'MANAGER'
  companyId: string
}

export interface Company {
  id: string
  name: string
  slug: string
  logoUrl?: string
  website?: string
  industry?: string
  sizeRange?: string
  country?: string
}

export interface Job {
  id: string
  companyId: string
  title: string
  description: string
  location?: string
  remotePolicy?: string
  contractType?: string
  experienceYearsMin?: number
  experienceYearsMax?: number
  status: 'DRAFT' | 'PUBLISHED' | 'PAUSED' | 'CLOSED'
  aiAnalyzedAt?: string
  createdAt: string
}

export interface Resume {
  id: string
  candidateId: string
  originalFilename: string
  status: 'UPLOADED' | 'PROCESSING' | 'EXTRACTED' | 'FAILED'
  extractedData?: ExtractedResumeData
  createdAt: string
}

export interface ExtractedResumeData {
  firstName?: string
  lastName?: string
  email?: string
  phone?: string
  location?: string
  summary?: string
  skills: SkillEntry[]
  experiences: ExperienceEntry[]
  education: EducationEntry[]
  languages: LanguageEntry[]
}

export interface SkillEntry {
  name: string
  yearsExperience?: number
  proficiencyLevel?: string
}

export interface ExperienceEntry {
  company: string
  title: string
  startDate?: string
  endDate?: string
  description?: string
  current: boolean
}

export interface EducationEntry {
  institution: string
  degree: string
  field?: string
  graduationYear?: number
}

export interface LanguageEntry {
  language: string
  level: string
}

export interface AiReport {
  id: string
  applicationId: string
  matchScore: number
  skillScore?: number
  experienceScore?: number
  educationScore?: number
  languageScore?: number
  strengths: string[]
  weaknesses: string[]
  recommendation?: string
  generatedAt: string
}

export interface ApiError {
  status: number
  detail: string
  type: string
  timestamp: string
}
