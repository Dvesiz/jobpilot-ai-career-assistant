import { http } from './http'

export type AiConfig = {
  baseUrl: string
  model: string
  hasApiKey: boolean
}

export async function getAiConfig() {
  const response = await http.get<{ code: number; message: string; data: AiConfig }>('/ai/config')
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function updateAiConfig(config: { baseUrl: string; model: string; apiKey?: string; clearApiKey?: boolean }) {
  const response = await http.put<{ code: number; message: string }>('/ai/config', config)
  if (response.data.code !== 0) throw new Error(response.data.message)
}

export async function listModels(baseUrl: string, apiKey?: string) {
  const response = await http.post<{ code: number; message: string; data: string[] }>('/ai/config/models', { baseUrl, apiKey })
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}
