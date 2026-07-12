import { http } from './http'

export type DashboardSummary = {
  resumeCount: number
  latestMatchScore: number | null
  monthlyInterviewCount: number
  nextAction: string
  nextActionPath: string
}

export async function getDashboardSummary() {
  const response = await http.get<{ code: number; message: string; data: DashboardSummary }>('/dashboard/summary')
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}
