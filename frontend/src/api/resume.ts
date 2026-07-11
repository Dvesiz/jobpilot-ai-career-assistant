import { http } from './http'

export type ResumeSection = {
  title: string
  content: string
}

export type ResumeParseResult = {
  resumeId: number
  fileName: string
  pageCount: number
  content: string
  sections: ResumeSection[]
}

export type ResumeSummary = {
  id: number
  fileName: string
  matchScore: number | null
  createTime: string
}

export type ResumeDetail = {
  id: number
  originalContent: string
  optimizedContent: string | null
  jobJd: string | null
  matchScore: number | null
  matchReport: string | null
}

type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export async function uploadResume(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await http.post<ApiResponse<ResumeParseResult>>('/resume/upload', formData)
  if (response.data.code !== 0) {
    throw new Error(response.data.message)
  }
  return response.data.data
}

export async function matchResume(resumeId: number, jobName: string, jobJd: string) {
  const response = await http.post<ApiResponse<{ score: number; matchedSkills: string[]; missingSkills: string[]; report: string }>>('/resume/match', {
    resumeId,
    jobName,
    jobJd,
  })
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function listResumes() {
  const response = await http.get<ApiResponse<ResumeSummary[]>>('/resume/list')
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function getResume(resumeId: number) {
  const response = await http.get<ApiResponse<ResumeDetail>>(`/resume/${resumeId}`)
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function deleteResume(resumeId: number) {
  const response = await http.delete<ApiResponse<null>>(`/resume/${resumeId}`)
  if (response.data.code !== 0) throw new Error(response.data.message)
}
