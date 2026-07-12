import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import {
  AimOutlined, AppstoreOutlined, CheckCircleFilled, DownloadOutlined,
  FileTextOutlined, HomeOutlined, LockOutlined, LogoutOutlined, PlayCircleOutlined,
  ProfileOutlined, ReloadOutlined, RightOutlined, SafetyCertificateOutlined, SendOutlined,
  SettingOutlined, StarOutlined, UploadOutlined, UserOutlined,
} from '@ant-design/icons'
import { Avatar, Button, Checkbox, Input, Select, Spin, Tag, message } from 'antd'
import { BrowserRouter, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { changePassword, getCaptcha, getProfile, login, register } from './api/auth'
import type { CaptchaChallenge } from './api/auth'
import { getDashboardSummary } from './api/dashboard'
import type { DashboardSummary } from './api/dashboard'
import { getAiConfig, listModels, updateAiConfig } from './api/ai'
import { downloadInterview, followUpInterview, listInterviews, startInterview, streamInterviewAnswer, streamResumeOptimize } from './api/interview'
import type { InterviewRecord } from './api/interview'
import { deleteResume, getResume, listResumes, matchResume, uploadResume } from './api/resume'
import type { ResumeParseResult, ResumeSummary } from './api/resume'
import './App.css'
import { LoginCharacters } from './LoginCharacters'

type NavItem = { label: string; path: string; icon: ReactNode }
const navigation: NavItem[] = [
  { label: '工作台', path: '/', icon: <AppstoreOutlined /> },
  { label: '我的简历', path: '/resume', icon: <FileTextOutlined /> },
  { label: '岗位匹配', path: '/matching', icon: <AimOutlined /> },
  { label: '模拟面试', path: '/interview', icon: <PlayCircleOutlined /> },
  { label: '面试复盘', path: '/review', icon: <ProfileOutlined /> },
  { label: '账户设置', path: '/settings', icon: <SettingOutlined /> },
]

function App() {
  return <BrowserRouter><Routes><Route path="/login" element={<Login />} /><Route path="/*" element={<Workspace />} /><Route path="*" element={<Navigate to="/" replace />} /></Routes></BrowserRouter>
}

function Workspace() {
  const location = useLocation()
  const navigate = useNavigate()
  const [profile, setProfile] = useState<{ username: string; nickname: string }>()
  const [summary, setSummary] = useState<DashboardSummary>()
  const active = navigation.find((item) => item.path === location.pathname) ?? navigation[0]
  useEffect(() => {
    if (!localStorage.getItem('resumeor_token')) {
      navigate('/login', { replace: true })
      return
    }
    getProfile().then((current) => {
      setProfile(current)
      localStorage.setItem('resumeor_user', current.nickname)
    }).catch(() => navigate('/login', { replace: true }))
    getDashboardSummary().then(setSummary).catch(() => undefined)
  }, [navigate])
  const nickname = profile?.nickname ?? localStorage.getItem('resumeor_user') ?? '求职者'
  const avatarText = nickname.slice(0, 1)
  const greeting = getGreeting()
  const logout = () => { localStorage.removeItem('resumeor_token'); navigate('/login') }
  const page = active.path === '/resume' ? <ResumePage />
    : active.path === '/matching' ? <MatchingPage />
      : active.path === '/interview' ? <InterviewPage />
        : active.path === '/review' ? <ReviewPage />
          : active.path === '/settings' ? <SettingsPage />
          : <Dashboard onNavigate={navigate} nickname={nickname} greeting={greeting} summary={summary} />

  return <main className="app-shell">
    <aside className="side-nav">
      <button className="brand" onClick={() => navigate('/')} aria-label="返回工作台"><img className="brand-mark" src="/jobpilot-logo.svg" alt="JobPilot" /><span>JobPilot</span></button>
      <nav aria-label="主导航"><p className="nav-label">工作空间</p>{navigation.map((item) => <button key={item.path} className={`nav-item ${item.path === active.path ? 'active' : ''}`} onClick={() => navigate(item.path)}>{item.icon}<span>{item.label}</span></button>)}</nav>
      <div className="side-bottom"><div className="plan-card"><span className="plan-kicker">本月完成面试</span><strong>{summary?.monthlyInterviewCount ?? 0} 次</strong><span>已提交回答并保存的记录</span></div><button className="profile-mini" onClick={() => navigate('/settings')}><Avatar size={34} style={{ background: '#d8e7df', color: '#1f5b4f' }}>{avatarText}</Avatar><span><strong>{nickname}</strong><small>账户设置</small></span><SettingOutlined /></button><button className="side-logout" onClick={logout}><LogoutOutlined /> 退出登录</button></div>
    </aside>
    <section className="workspace"><header className="topbar"><div className="crumb"><HomeOutlined /><RightOutlined /><span>{active.label}</span></div><Avatar size={34} style={{ background: '#174d43' }}>{avatarText}</Avatar></header>{page}</section>
  </main>
}

function Dashboard({ onNavigate, nickname, greeting, summary }: { onNavigate: (path: string) => void; nickname: string; greeting: string; summary?: DashboardSummary }) {
  return <div className="content-wrap">
    <section className="page-heading"><div><p className="eyebrow">求职成长中心</p><h1>{greeting}，{nickname}。</h1><p className="subtitle">把准备变成可以被看见的进展。</p></div><Button type="primary" icon={<StarOutlined />} onClick={() => onNavigate('/resume')}>开始优化简历</Button></section>
    <section className="overview-grid"><article className="resume-summary"><div className="section-heading"><div><p className="eyebrow">简历资产</p><h2>{summary?.resumeCount ? `已保存 ${summary.resumeCount} 份简历` : '还没有已保存简历'}</h2></div><Tag color={summary?.resumeCount ? 'success' : 'default'}>{summary?.resumeCount ? '已同步' : '待上传'}</Tag></div><div className="score-row"><div className="score-orbit"><strong>{summary?.resumeCount ?? 0}</strong><span>简历数量</span></div><div className="score-notes"><p><CheckCircleFilled /> 上传后可解析内容并生成优化版本</p><p><CheckCircleFilled /> 岗位匹配与面试记录会按当前简历关联</p><button onClick={() => onNavigate('/resume')}>管理我的简历 <RightOutlined /></button></div></div></article><article className="goal-card"><p className="eyebrow">下一步动作</p><h2>{summary?.nextAction ?? '正在加载当前状态'}</h2><p>{summary?.latestMatchScore != null ? `最近岗位匹配得分：${summary.latestMatchScore} 分` : '完成当前动作后，系统会生成下一步建议。'}</p><button onClick={() => onNavigate(summary?.nextActionPath ?? '/resume')}>{summary?.nextAction ?? '上传简历'} <RightOutlined /></button></article></section>
    <section className="section-heading split-heading"><div><p className="eyebrow">下一步</p><h2>用三个动作完成一次准备循环</h2></div></section><section className="action-grid"><ActionCard index="01" title="上传并解析简历" body="读取文字版 PDF，确认内容提取结果。" action="我的简历" icon={<UploadOutlined />} onClick={() => onNavigate('/resume')} /><ActionCard index="02" title="分析目标岗位" body="输入 JD，获得匹配评分与补强建议。" action="岗位匹配" icon={<AimOutlined />} onClick={() => onNavigate('/matching')} /><ActionCard index="03" title="模拟真实面试" body="以简历和岗位为上下文进行问答练习。" action="开始面试" icon={<PlayCircleOutlined />} onClick={() => onNavigate('/interview')} /></section>
  </div>
}

function ResumePage() {
  const [file, setFile] = useState<File>()
  const [result, setResult] = useState<ResumeParseResult>()
  const [previewUrl, setPreviewUrl] = useState('')
  const [resumes, setResumes] = useState<ResumeSummary[]>([])
  const [selectedId, setSelectedId] = useState<number>()
  const [uploading, setUploading] = useState(false)
  const [optimizing, setOptimizing] = useState(false)
  const [optimized, setOptimized] = useState('')

  const loadResumes = async () => {
    const items = await listResumes()
    setResumes(items)
    const savedId = Number(localStorage.getItem('resumeor_resume_id'))
    const nextId = items.some((item) => item.id === savedId) ? savedId : items[0]?.id
    if (nextId) await selectResume(nextId, items)
  }

  const selectResume = async (resumeId: number, source = resumes) => {
    const detail = await getResume(resumeId)
    const summary = source.find((item) => item.id === resumeId)
    setSelectedId(resumeId)
    localStorage.setItem('resumeor_resume_id', String(resumeId))
    setOptimized(detail.optimizedContent ?? '')
    setResult({
      resumeId,
      fileName: summary?.fileName ?? `简历 #${resumeId}`,
      pageCount: 0,
      content: detail.originalContent,
      sections: [{ title: detail.optimizedContent ? '已优化简历' : '已保存简历', content: detail.optimizedContent ?? detail.originalContent }],
    })
  }

  useEffect(() => {
    if (localStorage.getItem('resumeor_token')) {
      loadResumes().catch((error) => message.error(error instanceof Error ? error.message : '简历列表加载失败'))
    }
  }, [])

  const selectFile = (selected?: File) => {
    if (!selected) return
    if (!selected.name.toLowerCase().endsWith('.pdf') || selected.size > 10 * 1024 * 1024) { message.error('请选择 10MB 以内的 PDF 简历'); return }
    if (previewUrl) URL.revokeObjectURL(previewUrl)
    setPreviewUrl(URL.createObjectURL(selected)); setFile(selected); setResult(undefined); setOptimized('')
  }
  const submit = async () => {
    if (!file) { message.warning('请先选择一份 PDF 简历'); return }
    try {
      setUploading(true)
      const parsed = await uploadResume(file)
      setResult(parsed)
      setSelectedId(parsed.resumeId)
      localStorage.setItem('resumeor_resume_id', String(parsed.resumeId))
      setOptimized('')
      await loadResumes()
      message.success('简历解析完成')
    } catch (error) { message.error(error instanceof Error ? error.message : '上传失败') } finally { setUploading(false) }
  }
  const optimize = async () => {
    if (!result) return
    setOptimizing(true); setOptimized('')
    try {
      await streamResumeOptimize(result.resumeId, (chunk) => setOptimized((value) => value + chunk))
      const detail = await getResume(result.resumeId)
      setOptimized(detail.optimizedContent ?? '')
      message.success('简历优化完成并已同步')
    } catch (error) { message.error(error instanceof Error ? error.message : '优化失败') } finally { setOptimizing(false) }
  }
  const removeSelected = async () => {
    if (!selectedId || !window.confirm('确定删除当前简历及其相关分析吗？')) return
    try {
      await deleteResume(selectedId)
      localStorage.removeItem('resumeor_resume_id')
      setResult(undefined); setOptimized(''); setSelectedId(undefined)
      await loadResumes()
      message.success('简历已删除')
    } catch (error) { message.error(error instanceof Error ? error.message : '删除失败') }
  }
  return <div className="content-wrap resume-page"><section className="page-heading"><div><p className="eyebrow">简历工作区</p><h1>把经历写得更有分量。</h1><p className="subtitle">上传、选择和优化你的每一版简历。</p></div></section><section className="resume-workspace"><div className="upload-panel"><p className="eyebrow">简历管理</p><h2>选择或上传简历</h2>{resumes.length ? <div className="resume-picker"><select value={selectedId ?? ''} onChange={(event) => selectResume(Number(event.target.value)).catch((error) => message.error(error.message))}>{resumes.map((item) => <option key={item.id} value={item.id}>{item.fileName ?? `简历 #${item.id}`}</option>)}</select><button className="text-button danger-button" onClick={removeSelected}>删除当前简历</button></div> : <p>暂无已保存简历，上传第一份开始准备。</p>}<label className="file-dropzone"><UploadOutlined /><strong>{file ? file.name : '点击选择文件'}</strong><span>{file ? `${(file.size / 1024 / 1024).toFixed(2)} MB` : '仅支持 10MB 以内的 PDF'}</span><input type="file" accept="application/pdf,.pdf" onChange={(event) => selectFile(event.target.files?.[0])} /></label><Button type="primary" icon={<UploadOutlined />} loading={uploading} onClick={submit}>上传并解析</Button>{previewUrl && <object className="pdf-preview" data={previewUrl} type="application/pdf"><span>当前浏览器无法预览该 PDF。</span></object>}</div><div className="parse-preview">{result ? <><div className="section-heading"><div><p className="eyebrow">解析结果</p><h2>{result.fileName}</h2></div><Tag color="success">{result.pageCount ? `${result.pageCount} 页` : '已保存'}</Tag></div><div className="parse-stats"><span><strong>{result.content.length}</strong> 字符</span><span><strong>{resumes.length}</strong> 份已保存</span><button className="text-button" onClick={optimize} disabled={optimizing}>{optimizing ? '正在优化' : 'AI 优化表达'} <StarOutlined /></button></div><div className="parsed-content">{optimized ? <article><h3>AI 优化结果</h3><p>{optimized}</p></article> : result.sections.map((section, index) => <article key={`${section.title}-${index}`}><h3>{section.title}</h3><p>{section.content}</p></article>)}</div></> : <EmptyPreview label="从左侧选择或上传简历" />}</div></section></div>
}

function MatchingPage() {
  const [jobName, setJobName] = useState('产品设计师')
  const [jobJd, setJobJd] = useState('负责产品体验设计，开展用户研究，使用 Figma 完成原型与设计规范，能与产品和研发协作推进项目。')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<{ score: number; matchedSkills: string[]; missingSkills: string[]; report: string }>()
  const submit = async () => {
    const resumeId = Number(localStorage.getItem('resumeor_resume_id'))
    if (!resumeId) { message.warning('请先上传一份简历'); return }
    try { setLoading(true); setResult(await matchResume(resumeId, jobName, jobJd)) } catch (error) { message.error(error instanceof Error ? error.message : '匹配失败') } finally { setLoading(false) }
  }
  return <div className="content-wrap"><section className="page-heading"><div><p className="eyebrow">岗位匹配</p><h1>让 JD 指导你的准备。</h1><p className="subtitle">系统会对照当前简历，给出匹配度、优势和待补强能力。</p></div></section><section className="matching-layout"><div className="form-panel"><p className="eyebrow">目标岗位</p><Input value={jobName} onChange={(event) => setJobName(event.target.value)} placeholder="岗位名称" /><Input.TextArea value={jobJd} onChange={(event) => setJobJd(event.target.value)} rows={12} placeholder="粘贴岗位描述 JD" /><Button type="primary" icon={<AimOutlined />} loading={loading} onClick={submit}>生成匹配报告</Button></div><div className="match-result">{result ? <><div className="match-score"><div className="score-orbit"><strong>{result.score}</strong><span>匹配评分</span></div><div><p className="eyebrow">岗位分析</p><h2>{jobName}</h2><p>结果已保存到当前简历。</p></div></div><div className="skill-block"><h3>已匹配能力</h3>{result.matchedSkills.length ? result.matchedSkills.map((item) => <Tag key={item} color="success">{item}</Tag>) : <span>请结合具体 JD 完善经历关键词。</span>}</div><div className="skill-block missing"><h3>建议补强</h3>{result.missingSkills.length ? result.missingSkills.map((item) => <Tag key={item} color="orange">{item}</Tag>) : <span>重点补充可量化的项目成果。</span>}</div><ReportContent report={result.report} /></> : <EmptyPreview label="填写目标 JD 后生成匹配报告" />}</div></section></div>
}

function InterviewPage() {
  const [jobName, setJobName] = useState('产品设计师')
  const [jobJd, setJobJd] = useState('关注用户研究、产品体验和跨团队协作能力。')
  const [question, setQuestion] = useState('')
  const [recordId, setRecordId] = useState<number>()
  const [answer, setAnswer] = useState('')
  const [comment, setComment] = useState('')
  const [focusOnResume, setFocusOnResume] = useState(true)
  const [loading, setLoading] = useState(false)
  const start = async () => { const resumeId = Number(localStorage.getItem('resumeor_resume_id')); if (!resumeId) { message.warning('请先上传一份简历'); return }; try { setLoading(true); const data = await startInterview(resumeId, jobName, jobJd, focusOnResume); setRecordId(data.recordId); setQuestion(data.question); setComment(''); setAnswer('') } catch (error) { message.error(error instanceof Error ? error.message : '无法开始面试') } finally { setLoading(false) } }
  const submit = async () => { if (!recordId || !answer.trim()) { message.warning('请先完成回答'); return }; setLoading(true); setComment(''); try { await streamInterviewAnswer(recordId, answer, (chunk) => setComment((value) => value + chunk)) } catch (error) { message.error(error instanceof Error ? error.message : '点评生成失败') } finally { setLoading(false) } }
  const followUp = async () => { if (!recordId) return; try { setLoading(true); const next = await followUpInterview(recordId); setRecordId(next.recordId); setQuestion(next.question); setAnswer(''); setComment('') } catch (error) { message.error(error instanceof Error ? error.message : '追问生成失败') } finally { setLoading(false) } }
  return <div className="content-wrap"><section className="page-heading"><div><p className="eyebrow">模拟面试</p><h1>练习一次，离从容更近一点。</h1><p className="subtitle">问题从你的简历和目标岗位中生成，回答后获得流式点评。</p></div></section><section className="interview-layout"><div className="interview-setup"><p className="eyebrow">面试设置</p><Input value={jobName} onChange={(event) => setJobName(event.target.value)} /><Input.TextArea value={jobJd} onChange={(event) => setJobJd(event.target.value)} rows={6} /><Checkbox checked={focusOnResume} onChange={(event) => setFocusOnResume(event.target.checked)}>优先围绕简历中与岗位相关的项目和技能提问</Checkbox><Button type="primary" loading={loading} onClick={start}>生成第一题</Button></div><div className="interview-room">{question ? <><div className="question-card"><p className="eyebrow">面试官提问</p><h2>{question}</h2></div><Input.TextArea value={answer} onChange={(event) => setAnswer(event.target.value)} rows={7} placeholder="组织好你的回答，再提交给面试官。" /><Button type="primary" icon={<SendOutlined />} loading={loading} onClick={submit}>提交回答</Button>{comment && <article className="comment-card"><p className="eyebrow">AI 点评</p><p>{comment}</p><Button type="default" loading={loading} onClick={followUp}>继续下一题</Button></article>}</> : <EmptyPreview label="设置岗位信息后生成第一道问题" />}</div></section></div>
}

function ReportContent({ report }: { report: string }) {
  const cleaned = report.replace(/^#{1,6}\s*/gm, '').replace(/\*\*/g, '').replace(/^---+$/gm, '').replace(/^\s*[-*]\s*/gm, '').trim()
  const sections = cleaned.split(/(?=核心优势：|关注点：|面试准备：)/).filter(Boolean)
  return <div className="report-content">{sections.length ? sections.map((section, index) => { const [title, ...content] = section.split('：'); return <article key={index}><h3>{title}</h3><p>{content.join('：').replace(/；/g, '；\n')}</p></article> }) : <article><h3>岗位建议</h3><p>{cleaned}</p></article>}</div>
}

function ReviewPage() {
  const [records, setRecords] = useState<InterviewRecord[]>([])
  const [loading, setLoading] = useState(true)
  useEffect(() => { listInterviews().then(setRecords).catch(() => message.warning('请先登录并完成一次模拟面试')).finally(() => setLoading(false)) }, [])
  return <div className="content-wrap"><section className="page-heading"><div><p className="eyebrow">面试复盘</p><h1>每一次回答，都值得复看。</h1><p className="subtitle">查看已保存的问题、回答、AI 点评与参考答案。</p></div></section><section className="review-list">{loading ? <div className="loading-state"><Spin /> 正在读取记录</div> : records.length ? records.map((record) => <article className="review-card" key={record.id}><div><p className="eyebrow">{record.jobName} · {new Date(record.createTime).toLocaleDateString()}</p><h2>{record.question}</h2><p>{record.aiComment ?? '这道题尚未提交回答。'}</p></div><Button icon={<DownloadOutlined />} onClick={() => downloadInterview(record.id)}>导出复盘</Button></article>) : <EmptyPreview label="完成模拟面试后，记录会自动保存在这里" />}</section></div>
}

function SettingsPage() {
  const [profile, setProfile] = useState<{ username: string; nickname: string }>()
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [saving, setSaving] = useState(false)
  const [aiBaseUrl, setAiBaseUrl] = useState('https://api.openai.com/v1')
  const [aiModel, setAiModel] = useState('gpt-4o-mini')
  const [aiKey, setAiKey] = useState('')
  const [hasAiKey, setHasAiKey] = useState(false)
  const [savingAi, setSavingAi] = useState(false)
  const [modelOptions, setModelOptions] = useState<string[]>([])
  const [loadingModels, setLoadingModels] = useState(false)
  useEffect(() => {
    getProfile().then(setProfile).catch((error) => message.error(error.message))
    getAiConfig().then((config) => { setAiBaseUrl(config.baseUrl); setAiModel(config.model); setHasAiKey(config.hasApiKey) }).catch((error) => message.error(error.message))
  }, [])
  const submit = async () => {
    if (newPassword.length < 6) { message.warning('新密码至少需要 6 位'); return }
    try {
      setSaving(true)
      await changePassword(oldPassword, newPassword)
      setOldPassword(''); setNewPassword('')
      message.success('密码已更新，请使用新密码登录')
    } catch (error) { message.error(error instanceof Error ? error.message : '密码修改失败') } finally { setSaving(false) }
  }
  const saveAi = async () => {
    if (!aiBaseUrl || !aiModel) { message.warning('请填写模型地址和模型名'); return }
    try {
      setSavingAi(true)
      await updateAiConfig({ baseUrl: aiBaseUrl, model: aiModel, apiKey: aiKey || undefined })
      if (aiKey) setHasAiKey(true)
      setAiKey('')
      message.success('模型配置已保存')
    } catch (error) { message.error(error instanceof Error ? error.message : '模型配置保存失败') } finally { setSavingAi(false) }
  }
  const applyPreset = (preset: 'openai' | 'deepseek' | 'modelscope' | 'siliconflow' | 'custom') => {
    const presets = {
      openai: 'https://api.openai.com/v1',
      deepseek: 'https://api.deepseek.com',
      modelscope: 'https://api-inference.modelscope.cn/v1',
      siliconflow: 'https://api.siliconflow.cn/v1',
      custom: '',
    }
    setAiBaseUrl(presets[preset]); setAiModel(''); setModelOptions([])
  }
  const fetchModels = async () => {
    try { setLoadingModels(true); const models = await listModels(aiBaseUrl, aiKey || undefined); setModelOptions(models); if (!models.includes(aiModel)) setAiModel(models[0]); message.success(`已获取 ${models.length} 个模型`) } catch (error) { message.error(error instanceof Error ? error.message : '获取模型列表失败') } finally { setLoadingModels(false) }
  }
  return <div className="content-wrap"><section className="page-heading"><div><p className="eyebrow">账户设置</p><h1>管理你的账户。</h1><p className="subtitle">你的求职记录和模型配置都按账户独立保存。</p></div></section><section className="settings-layout"><article className="profile-card"><p className="eyebrow">个人资料</p><Avatar size={58} style={{ background: '#d8e7df', color: '#1f5b4f' }}>{profile?.nickname?.slice(0, 1) ?? '我'}</Avatar><h2>{profile?.nickname ?? '正在加载'}</h2><p>{profile?.username ?? ''}</p></article><article className="password-card"><p className="eyebrow">安全设置</p><h2>修改登录密码</h2><Input.Password value={oldPassword} onChange={(event) => setOldPassword(event.target.value)} placeholder="当前密码" /><Input.Password value={newPassword} onChange={(event) => setNewPassword(event.target.value)} placeholder="新密码（至少 6 位）" /><Button type="primary" loading={saving} onClick={submit}>更新密码</Button></article></section><section className="ai-settings"><div className="section-heading"><div><p className="eyebrow">AI 模型</p><h2>配置你的模型服务</h2></div><Tag color={hasAiKey ? 'success' : 'default'}>{hasAiKey ? '密钥已保存' : '未配置密钥'}</Tag></div><p>先选择预设或填写自定义请求地址，再填 API Key 获取模型列表。支持 OpenAI-compatible 服务；保存时根地址会自动补全为 Chat Completions 请求地址。</p><div className="preset-row"><button onClick={() => applyPreset('openai')}>OpenAI</button><button onClick={() => applyPreset('deepseek')}>DeepSeek</button><button onClick={() => applyPreset('modelscope')}>ModelScope</button><button onClick={() => applyPreset('siliconflow')}>SiliconFlow</button><button onClick={() => applyPreset('custom')}>自定义</button></div><Input value={aiBaseUrl} onChange={(event) => setAiBaseUrl(event.target.value)} placeholder="请求根地址，例如 https://.../v1" /><Input.Password value={aiKey} onChange={(event) => setAiKey(event.target.value)} placeholder={hasAiKey ? '留空则使用已保存的密钥' : '输入 API Key'} /><Button loading={loadingModels} onClick={fetchModels}>获取模型列表</Button><Select value={aiModel || undefined} onChange={setAiModel} options={modelOptions.map((model) => ({ value: model, label: model }))} placeholder="先获取模型列表，再选择模型" notFoundContent={loadingModels ? <Spin size="small" /> : '尚未获取模型'} /><Button type="primary" loading={savingAi} onClick={saveAi}>保存模型配置</Button></section></div>
}

function ActionCard({ index, title, body, action, icon, onClick }: { index: string; title: string; body: string; action: string; icon: ReactNode; onClick: () => void }) { return <article className="action-card"><span className="card-index">{index}</span><span className="action-icon">{icon}</span><h3>{title}</h3><p>{body}</p><button onClick={onClick}>{action} <RightOutlined /></button></article> }
function EmptyPreview({ label = '解析内容会显示在这里' }: { label?: string }) { return <div className="empty-preview"><FileTextOutlined /><strong>{label}</strong><span>完成当前步骤后，即可进入下一阶段。</span></div> }

function Login() {
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('demo')
  const [password, setPassword] = useState('demo123')
  const [nickname, setNickname] = useState('')
  const [loading, setLoading] = useState(false)
  const [captcha, setCaptcha] = useState<CaptchaChallenge>()
  const [captchaCode, setCaptchaCode] = useState('')
  const [activeField, setActiveField] = useState<'username' | 'password' | 'captcha' | null>(null)
  const [showPassword, setShowPassword] = useState(false)
  const loadCaptcha = async () => {
    try { setCaptcha(await getCaptcha()); setCaptchaCode('') } catch (error) { message.error(error instanceof Error ? error.message : '验证码加载失败') }
  }
  useEffect(() => { if (mode === 'login') loadCaptcha() }, [mode])
  const submit = async () => {
    try {
      setLoading(true)
      if (mode === 'register') {
        await register(username, password, nickname)
        message.success('注册成功，请登录')
        setMode('login')
        return
      }
      if (!captcha || !captchaCode.trim()) { message.warning('请输入图形验证码'); return }
      const result = await login(username, password, captcha.id, captchaCode.trim())
      localStorage.setItem('resumeor_token', result.token)
      localStorage.setItem('resumeor_user', result.user.nickname)
      message.success('登录成功')
      navigate('/')
    } catch (error) {
      message.error(error instanceof Error ? error.message : '操作失败')
      if (mode === 'login') await loadCaptcha()
    } finally { setLoading(false) }
  }
  const switchMode = () => {
    setMode((current) => current === 'login' ? 'register' : 'login')
    setUsername(mode === 'login' ? '' : 'demo')
    setPassword(mode === 'login' ? '' : 'demo123')
    setNickname('')
    setCaptchaCode('')
    setShowPassword(false)
  }
  return <main className="login-page">
    <section className="login-hero">
      <button className="login-brand" onClick={() => navigate('/')}><img src="/jobpilot-logo.svg" alt="" /><span>JobPilot</span></button>
      <div className="login-hero-copy"><h1>让每一段经历，<br />更有说服力。</h1><p>从简历优化到模拟面试，把求职准备变成清晰、可执行的下一步。</p></div>
      <LoginCharacters isTyping={activeField === 'username'} showPassword={showPassword} passwordLength={password.length} />
    </section>
    <section className="login-form-panel">
      <div className="login-form-inner">
        <div className="login-mobile-brand"><img src="/jobpilot-logo.svg" alt="" /><span>JobPilot</span></div>
        <header><h2>{mode === 'login' ? '欢迎回来' : '创建账户'}</h2><p>{mode === 'login' ? '登录后继续你的求职准备' : '保存你的简历、匹配和面试记录'}</p></header>
        <div className="login-fields">
          {mode === 'register' && <label><span>昵称</span><Input value={nickname} onChange={(event) => setNickname(event.target.value)} placeholder="你的称呼" size="large" /></label>}
          <label><span>账号</span><Input prefix={<UserOutlined />} value={username} onFocus={() => setActiveField('username')} onBlur={() => setActiveField(null)} onChange={(event) => setUsername(event.target.value)} placeholder="请输入账号" size="large" /></label>
          <label><span>密码</span><Input.Password prefix={<LockOutlined />} value={password} visibilityToggle={{ visible: showPassword, onVisibleChange: setShowPassword }} onFocus={() => setActiveField('password')} onBlur={() => setActiveField(null)} onChange={(event) => setPassword(event.target.value)} onPressEnter={mode === 'register' ? submit : undefined} placeholder="至少 6 位密码" size="large" /></label>
          {mode === 'login' && <label><span>图形验证码</span><div className="captcha-row"><Input prefix={<SafetyCertificateOutlined />} value={captchaCode} onFocus={() => setActiveField('captcha')} onBlur={() => setActiveField(null)} onChange={(event) => setCaptchaCode(event.target.value.toUpperCase())} onPressEnter={submit} maxLength={4} placeholder="输入验证码" size="large" /><button className="captcha-image" type="button" onClick={loadCaptcha} aria-label="刷新验证码" title="点击刷新验证码">{captcha ? <img src={captcha.image} alt="图形验证码" /> : <span>加载中</span>}<ReloadOutlined /></button></div></label>}
        </div>
        <Button className="login-submit" type="primary" size="large" block loading={loading} onClick={submit}>{mode === 'login' ? '登录 JobPilot' : '创建账户'}</Button>
        <div className="login-divider"><span>或</span></div>
        <button className="login-mode-switch" onClick={switchMode}>{mode === 'login' ? '创建一个新账户' : '返回账号登录'}</button>
      </div>
    </section>
  </main>
}

function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 11) return '早上好'
  if (hour < 13) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

export default App
