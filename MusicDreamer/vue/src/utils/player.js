import { reactive } from 'vue'

export const player = reactive({
    queue: [],
    current: null,
    isPlaying: false,
    audio: null, // 改为初始化为 null
    listeners: new Set(), // 状态监听器集合

    // 新增方法：设置音频元素并初始化事件监听
    setAudioElement(audioElement) {
        this.audio = audioElement;
        this.initAudioEvents(); // 初始化音频事件监听
    },

    // 新增方法：初始化音频事件监听
    initAudioEvents() {
        if (!this.audio) return;

        this.audio.addEventListener('play', () => {
            this.isPlaying = true;
            this.notify();
        });

        this.audio.addEventListener('pause', () => {
            this.isPlaying = false;
            this.notify();
        });

        this.audio.addEventListener('ended', () => {
            this.next();
        });

        this.audio.addEventListener('timeupdate', () => {
            this.notify();
        });
    },

    // 新增方法：通知所有监听器
    notify() {
        this.listeners.forEach(fn => fn());
    },

    async play(song) {
        if (!this.audio) return;

        this.current = song;

        // 移除之前绑定的canplay事件，避免重复触发
        this.audio.oncanplay = null;

        // ✅ 统一编码 URL，防止中文路径引发的问题
        this.audio.src = encodeURI(song.musicUrl);

        await new Promise((resolve, reject) => {
            const onCanPlay = () => {
                this.audio.oncanplay = null;
                resolve();
            };
            const onError = (e) => {
                this.audio.oncanplay = null;
                reject(e);
            };

            this.audio.oncanplay = onCanPlay;
            this.audio.onerror = onError;

            // 若已经可播放直接 resolve
            if (this.audio.readyState >= 3) {
                this.audio.oncanplay = null;
                resolve();
            }
        });

        try {
            await this.audio.play();
            this.isPlaying = true;
        } catch (err) {
            console.warn('播放失败', err);
            this.isPlaying = false;
        }

        // 避免重复加入队列
        const exists = this.queue.find(m => m.musicId === song.musicId);
        if (!exists) {
            this.queue.push(song);
        }

        this.notify();
    },


    // 其他方法保持不变...
    addToQueue(song) {
        const exists = this.queue.find(m => m.musicId === song.musicId);
        if (!exists) {
            this.queue.push(song);
            this.notify(); // 添加队列变更通知
        }
    },

    next() {
        const currentIndex = this.queue.findIndex(m => m.musicId === this.current?.musicId)
        if (currentIndex !== -1 && currentIndex < this.queue.length - 1) {
            this.play(this.queue[currentIndex + 1])
        }
        this.notify()
    },

    prev() {
        const currentIndex = this.queue.findIndex(m => m.musicId === this.current?.musicId)
        if (currentIndex > 0) {
            this.play(this.queue[currentIndex - 1])
        }
    },

    toggle() {
        if (!this.audio) return;
        if (!this.current && this.queue.length > 0) {
            this.play(this.queue[0]);
        } else if (this.isPlaying) {
            this.audio.pause();
        } else {
            this.audio.play().catch(err => {
                console.warn("播放失败", err);
            });
        }
    },
})