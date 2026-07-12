import { http } from './http'

export type InterviewRecord = {
  id: number
  resumeId: number
  jobName: string
  question: string
  userAnswer: string | null
  aiComment: string | null
  standardAnswer: string | null
  createTime: string
}

export async function startInterview(resumeId: number, jobName: string, jobJd: string, focusOnResume: boolean) {
  const response = await http.post<{ code: number; message: string; data: { recordId: number; question: string } }>('/interview/start', { resumeId, jobName, jobJd, focusOnResume })
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function listInterviews() {
  const response = await http.get<{ code: number; message: string; data: InterviewRecord[] }>('/interview/list')
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function followUpInterview(recordId: number) {
  const response = await http.post<{ code: number; message: string; data: { recordId: number; question: string } }>(`/interview/follow-up/${recordId}`)
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function streamInterviewAnswer(recordId: number, answer: string, onChunk: (chunk: string) => void) {
  return stream('/interview/answer/stream', { recordId, answer }, onChunk)
}

export async function streamResumeOptimize(resumeId: number, onChunk: (chunk: string) => void) {
  return stream(`/resume/optimize/stream?resumeId=${resumeId}`, undefined, onChunk)
}

export async function downloadInterview(recordId: number) {
  const response = await http.get(`/interview/${recordId}/export`, { responseType: 'blob' })
  const href = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = href
  link.download = `interview-review-${recordId}.txt`
  link.click()
  URL.revokeObjectURL(href)
}

async function stream(path: string, body: object | undefined, onChunk: (chunk: string) => void) {
  const token = localStorage.getItem('resumeor_token')
  const response = await fetch(`/api${path}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token ?? ''}`, ...(body ? { 'Content-Type': 'application/json' } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  })
  if (!response.ok || !response.body) {
    const payload = await response.json().catch(() => undefined)
    throw new Error(payload?.message ?? 'AI 服务暂时不可用')
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split('\n\n')
    buffer = events.pop() ?? ''
    for (const event of events) {
      const data = event.split('\n')
        .filter((line) => line.startsWith('data:'))
        .map((line) => line.slice(5).replace(/^ /, ''))
        .join('\n')
      if (data && data !== '[DONE]') onChunk(data)
    }
  }
}
