import { useEffect, useRef, useState } from 'react'
import type { CSSProperties } from 'react'

type LoginCharactersProps = {
  isTyping: boolean
  showPassword: boolean
  passwordLength: number
}

type Point = { x: number; y: number }

export function LoginCharacters({ isTyping, showPassword, passwordLength }: LoginCharactersProps) {
  const [mouse, setMouse] = useState<Point>({ x: 0, y: 0 })
  const [blobBlink, setBlobBlink] = useState(false)
  const [squareBlink, setSquareBlink] = useState(false)
  const [lookingAtEachOther, setLookingAtEachOther] = useState(false)
  const [peeking, setPeeking] = useState(false)
  const blobRef = useRef<HTMLDivElement>(null)
  const pentRef = useRef<HTMLDivElement>(null)
  const circleRef = useRef<HTMLDivElement>(null)
  const squareRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const followMouse = (event: MouseEvent) => setMouse({ x: event.clientX, y: event.clientY })
    window.addEventListener('mousemove', followMouse)
    return () => window.removeEventListener('mousemove', followMouse)
  }, [])

  useEffect(() => {
    let blobTimer: number
    let blobResetTimer: number
    let squareTimer: number
    let squareResetTimer: number
    const scheduleBlobBlink = () => {
      blobTimer = window.setTimeout(() => {
        setBlobBlink(true)
        blobResetTimer = window.setTimeout(() => { setBlobBlink(false); scheduleBlobBlink() }, 150)
      }, Math.random() * 4000 + 2500)
    }
    const scheduleSquareBlink = () => {
      squareTimer = window.setTimeout(() => {
        setSquareBlink(true)
        squareResetTimer = window.setTimeout(() => { setSquareBlink(false); scheduleSquareBlink() }, 150)
      }, Math.random() * 4000 + 2500)
    }
    scheduleBlobBlink()
    scheduleSquareBlink()
    return () => {
      window.clearTimeout(blobTimer); window.clearTimeout(blobResetTimer)
      window.clearTimeout(squareTimer); window.clearTimeout(squareResetTimer)
    }
  }, [])

  useEffect(() => {
    if (!isTyping) { setLookingAtEachOther(false); return }
    setLookingAtEachOther(true)
    const timer = window.setTimeout(() => setLookingAtEachOther(false), 800)
    return () => window.clearTimeout(timer)
  }, [isTyping])

  useEffect(() => {
    setPeeking(false)
    if (passwordLength === 0 || !showPassword) return
    let resetTimer: number
    const timer = window.setTimeout(() => {
      setPeeking(true)
      resetTimer = window.setTimeout(() => setPeeking(false), 800)
    }, Math.random() * 3000 + 2000)
    return () => { window.clearTimeout(timer); window.clearTimeout(resetTimer) }
  }, [passwordLength, showPassword])

  const calcPosition = (element: HTMLDivElement | null) => {
    if (!element) return { faceX: 0, faceY: 0, skew: 0 }
    const rect = element.getBoundingClientRect()
    const dx = mouse.x - (rect.left + rect.width / 2)
    const dy = mouse.y - (rect.top + rect.height / 3)
    return {
      faceX: Math.max(-15, Math.min(15, dx / 20)),
      faceY: Math.max(-10, Math.min(10, dy / 30)),
      skew: Math.max(-6, Math.min(6, -dx / 120)),
    }
  }

  const calcPupil = (element: HTMLDivElement | null, max = 5) => {
    if (!element) return { x: 0, y: 0 }
    const rect = element.getBoundingClientRect()
    const dx = mouse.x - (rect.left + rect.width / 2)
    const dy = mouse.y - (rect.top + rect.height / 2)
    const distance = Math.min(Math.sqrt(dx * dx + dy * dy), max * 30)
    const angle = Math.atan2(dy, dx)
    return {
      x: Math.cos(angle) * Math.min(distance / 30 * max, max),
      y: Math.sin(angle) * Math.min(distance / 30 * max, max),
    }
  }

  const isHiding = passwordLength > 0 && !showPassword
  const isPasswordShown = passwordLength > 0 && showPassword
  const blobPosition = calcPosition(blobRef.current)
  const pentPosition = calcPosition(pentRef.current)
  const circlePosition = calcPosition(circleRef.current)
  const squarePosition = calcPosition(squareRef.current)
  const blobPupil = isPasswordShown ? { x: peeking ? 4 : -4, y: peeking ? 5 : -4 }
    : lookingAtEachOther ? { x: 3, y: 4 } : calcPupil(blobRef.current, 5)
  const pentPupil = isPasswordShown ? { x: -3, y: -3 }
    : lookingAtEachOther ? { x: -3, y: -2 } : calcPupil(pentRef.current, 4)
  const circlePupil = isPasswordShown ? { x: -4, y: -3 } : calcPupil(circleRef.current, 4)
  const squarePupil = isPasswordShown ? { x: -4, y: -4 } : calcPupil(squareRef.current, 5)

  const blobStyle: CSSProperties = {
    height: isTyping || isHiding ? 380 : 350,
    transform: isPasswordShown ? 'skewX(0deg)'
      : isTyping || isHiding ? `skewX(${blobPosition.skew - 10}deg) translateX(30px)` : `skewX(${blobPosition.skew}deg)`,
  }
  const pentStyle: CSSProperties = {
    transform: isPasswordShown ? 'none'
      : lookingAtEachOther ? `skewX(${pentPosition.skew + 8}deg)` : `skewX(${pentPosition.skew * 1.3}deg)`,
  }
  const blobEyes: CSSProperties = isPasswordShown ? { left: 25, top: 50 }
    : lookingAtEachOther ? { left: 58, top: 72 } : { left: 48 + blobPosition.faceX, top: 55 + blobPosition.faceY }
  const pentEyes: CSSProperties = isPasswordShown ? { left: 36, top: 120 }
    : lookingAtEachOther ? { left: 30, top: 110 } : { left: 42 + pentPosition.faceX, top: 125 + pentPosition.faceY }
  const circleFace: CSSProperties = isPasswordShown ? { left: 58, top: 58 }
    : { left: 72 + circlePosition.faceX, top: 60 + circlePosition.faceY }
  const squareEyes: CSSProperties = isPasswordShown ? { left: 20, top: 45 }
    : { left: 32 + squarePosition.faceX, top: 52 + squarePosition.faceY }
  const squareMouth: CSSProperties = isPasswordShown ? { left: 22, top: 105 }
    : { left: 28 + squarePosition.faceX, top: 105 + squarePosition.faceY }

  return <div className="ac-stage" data-looking={lookingAtEachOther} data-peeking={peeking} data-password-shown={isPasswordShown} aria-hidden="true">
    <div ref={blobRef} className="ac-char ac-blob" style={blobStyle}>
      <Eyes className="ac-eyes--lg" style={blobEyes} pupil={blobPupil} blink={blobBlink} large />
    </div>
    <div ref={pentRef} className="ac-char ac-pent" style={pentStyle}>
      <Eyes className="ac-eyes--md" style={pentEyes} pupil={pentPupil} rectangular />
    </div>
    <div ref={circleRef} className="ac-char ac-circ" style={{ transform: isPasswordShown ? 'none' : `skewX(${circlePosition.skew}deg)` }}>
      <div className="ac-face" style={circleFace}>
        <div className="ac-dot-eyes"><i style={translate(circlePupil)} /><i style={translate(circlePupil)} /></div>
        <div className="ac-cheeks"><i /><i /></div>
      </div>
    </div>
    <div ref={squareRef} className="ac-char ac-sq" style={{ transform: isPasswordShown ? 'none' : `skewX(${squarePosition.skew}deg)` }}>
      <Eyes className="ac-eyes--sm" style={squareEyes} pupil={squarePupil} blink={squareBlink} />
      <div className="ac-mouth" style={squareMouth} />
    </div>
  </div>
}

function Eyes({ className, style, pupil, blink = false, large = false, rectangular = false }: {
  className: string; style: CSSProperties; pupil: Point; blink?: boolean; large?: boolean; rectangular?: boolean
}) {
  const eyeClass = rectangular ? 'ac-eye ac-eye--rect' : `ac-eye ${large ? 'ac-eye--lg' : 'ac-eye--md'} ${blink ? 'ac-eye--blink' : ''}`
  return <div className={`ac-eyes ${className}`} style={style}>
    <span className={eyeClass}><i className={`ac-pupil ${large ? '' : 'ac-pupil--sm'}`} style={translate(pupil)} /></span>
    <span className={eyeClass}><i className={`ac-pupil ${large ? '' : 'ac-pupil--sm'}`} style={translate(pupil)} /></span>
  </div>
}

function translate(point: Point): CSSProperties {
  return { transform: `translate(${point.x}px, ${point.y}px)` }
}
