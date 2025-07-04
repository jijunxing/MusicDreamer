import { reactive } from 'vue'
import {ElMessage} from "element-plus";

export const player = reactive({
    queue: [],
    current: null,
    isPlaying: false,
    audio: null, // 改为初始化为 null
    listeners: new Set(), // 状态监听器集合
    playMode: 'order',
    currentTime: 0, // 新增全局播放时间
    duration: 0,    // 新增全局总时长
    // 新增方法：设置音频元素并初始化事件监听
    setAudioElement(audioElement) {
        this.audio = audioElement;
        this.initAudioEvents(); // 初始化音频事件监听
    },
    togglePlayMode() {
        const modes = ['order', 'list-loop', 'loop', 'random'];
        const currentIndex = modes.indexOf(this.playMode);
        this.playMode = modes[(currentIndex + 1) % modes.length];
        console.log(`播放模式切换为: ${this.playMode}`);
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
            // 单曲循环模式特殊处理
            if (this.playMode === 'loop') {
                this.audio.currentTime = 0;
                this.audio.play().catch(err => console.warn('循环播放失败', err));
            } else {
                this.next();
            }
        });

        this.audio.addEventListener('timeupdate', () => {
            this.currentTime = this.audio.currentTime
            this.notify() // 触发所有监听器更新
        })

        this.audio.addEventListener('durationchange', () => {
            this.duration = this.audio.duration
        })
    },

    // 新增方法：通知所有监听器
    notify() {
        this.listeners.forEach(fn => fn());
    },

    async play(song) {
        if (!this.audio) return;
        if (song.activation === 0){
            ElMessage.warning("该歌曲已被冻结")
            return
        }
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
        request.put('music/update')
    },


    // 其他方法保持不变...
    addToQueue(song) {
        if(song.activation === 0){
            ElMessage.warning("该音乐已被冻结")
            return
        }
        if (this.queue.find(m => m.musicId === song.musicId)) {
            return
        }
        this.queue.push(song)
        this.notify()
    },

    next() {
        if (this.queue.length === 0) return;

        const currentIndex = this.queue.findIndex(m => m.musicId === this.current?.musicId);
        let nextIndex = -1;
        console.log("当前模式:", this.playMode, "当前索引:", currentIndex, "队列长度:", this.queue.length); // 调试日志
        switch (this.playMode) {
            case 'order': // 顺序播放
                if (currentIndex < this.queue.length - 1) {
                    nextIndex = currentIndex + 1;
                } else {
                    // 最后一首停止播放
                    this.audio.currentTime = 0;
                    this.audio.pause();
                    this.isPlaying = false;
                }
                break;

            case 'list-loop': // 列表循环
                nextIndex = (currentIndex + 1) % this.queue.length;
                console.log("列表循环下一首索引:", nextIndex); // 调试日志
                break;

            case 'loop': // 单曲循环
                // 重新播放当前歌曲
                this.audio.currentTime = 0;
                this.audio.play().catch(err => console.warn('播放失败', err));
                return;

            case 'random': // 随机播放
                if (this.queue.length > 1) {
                    let randomIndex;
                    do {
                        randomIndex = Math.floor(Math.random() * this.queue.length);
                        console.log("随机索引"+randomIndex)
                        console.log("当前索引"+currentIndex)
                    } while (randomIndex === currentIndex);
                    nextIndex = randomIndex;
                } else {
                    nextIndex = 0;
                }
                break;
        }

        if (nextIndex >= 0 && nextIndex < this.queue.length) {
            this.play(this.queue[nextIndex]);
        }
    },

    prev() {
        if (this.queue.length === 0) return;

        const currentIndex = this.queue.findIndex(m => m.musicId === this.current?.musicId);
        let prevIndex = -1;

        switch (this.playMode) {
            case 'order': // 顺序播放


            case 'list-loop': // 列表循环
                prevIndex = (currentIndex - 1 + this.queue.length) % this.queue.length;
                break;

            case 'loop': // 单曲循环
                // 重新播放当前歌曲
                this.audio.currentTime = 0;
                this.audio.play().catch(err => console.warn('播放失败', err));
                return;

            case 'random': // 随机播放
                if (this.queue.length > 1) {
                    let randomIndex;
                    do {
                        randomIndex = Math.floor(Math.random() * this.queue.length);
                    } while (randomIndex === currentIndex);
                    prevIndex = randomIndex;
                } else {
                    prevIndex = 0;
                }
                break;
        }

        if (prevIndex >= 0 && prevIndex < this.queue.length) {
            this.play(this.queue[prevIndex]);
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

    clear(){
        this.queue = []
    }
})