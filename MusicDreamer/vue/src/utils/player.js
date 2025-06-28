// src/utils/player.js

import { reactive } from 'vue'

export const player = reactive({
    audio: new Audio(),
    queue: [],               // 播放队列
    current: null,           // 当前播放项 { musicName, musicUrl }
    isPlaying: false,

    play(item) {
        if (!item || !item.musicUrl) return
        this.audio.src = item.musicUrl
        this.audio.play()
        this.current = item
        this.isPlaying = true
    },

    toggle() {
        if (!this.audio.src) return
        if (this.isPlaying) {
            this.audio.pause()
        } else {
            this.audio.play()
        }
        this.isPlaying = !this.isPlaying
    },

    next() {
        if (this.queue.length === 0) {
            this.current = null
            this.audio.src = ''
            this.isPlaying = false
            return
        }
        const nextItem = this.queue.shift()
        this.play(nextItem)
    },

    addToQueue(item) {
        this.queue.push(item)
        if (!this.isPlaying && !this.current) {
            this.next()
        }
    }
})

// 自动播放下一首
player.audio.addEventListener('ended', () => {
    player.next()
})
