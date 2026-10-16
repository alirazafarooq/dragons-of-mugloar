
export interface Mission {
    adId: string
    message: string
    reward: number
    expiresIn: number
    encrypted: string | null
    probability: string
}

export interface MissionSolveResult {
    success: boolean
    message: string
    lives: number
    gold: number
    score: number
    highScore: number
    turn: number
}
