import { http } from './http'

type LoginResult = { token: string; user: { id: number; username: string; nickname: string } }

export async function login(username: string, password: string) {
  const response = await http.post<{ code: number; message: string; data: LoginResult }>('/user/login', { username, password })
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function register(username: string, password: string, nickname: string) {
  const response = await http.post<{ code: number; message: string; data: { id: number; username: string; nickname: string } }>('/user/register', { username, password, nickname })
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function getProfile() {
  const response = await http.get<{ code: number; message: string; data: { id: number; username: string; nickname: string } }>('/user/profile')
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

export async function changePassword(oldPassword: string, newPassword: string) {
  const response = await http.put<{ code: number; message: string }>('/user/password', { oldPassword, newPassword })
  if (response.data.code !== 0) throw new Error(response.data.message)
}
