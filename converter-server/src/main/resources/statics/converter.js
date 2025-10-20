// 模式管理
let currentMode = 'image'; // 'image' 或 'video'
let isProcessingVideo = false; // 视频处理状态
// 全局变量
let videoFrames = []; // 实时缓存帧
let currentFrameIndex = 0;
let isPlaying = false;
let playInterval;
let currentSpeed = 1;
let frames = [];
let isMonospace = true;
let fps = 10;//字符帧播放的每秒帧数

// DOM 元素
const fileContainer = document.getElementById('file-container');
const fileUpload = document.getElementById('file-upload');
const previewImage = document.getElementById('preview-image');
const previewVideo = document.getElementById('preview-video');
const convertBtn = document.getElementById('convert-btn');
const saveBtn = document.getElementById('save-btn');
const copyBtn = document.getElementById('copy-btn');
const resultContent = document.getElementById('result-content');
const loading = document.getElementById('loading');
const loadingText = document.getElementById('loading-text');
const loadingDesc = document.getElementById('loading-desc');
const lowerSection = document.getElementById('lower-section');
const charWidthRange = document.getElementById('char-width-range');
const charWidthInput = document.getElementById('char-width');
const switchModeBtn = document.getElementById('switch-mode');
const pageTitle = document.getElementById('page-title');
const previewIcon = document.getElementById('preview-icon');
const previewTitle = document.getElementById('preview-title');
const uploadText = document.getElementById('upload-text');
const fileFormats = document.getElementById('file-formats');
const resultIcon = document.getElementById('result-icon');
const resultPlaceholderIcon = document.getElementById('result-placeholder-icon');
const resultPlaceholderText = document.getElementById('result-placeholder-text');
const successMessage = document.getElementById('success-message');

let isFirstLoad = true; // 首次加载标志

// 初始化
window.addEventListener('DOMContentLoaded', () => {
    // 初始化模式切换
    initModeSwitch();

    // 初始化字符宽度同步
    initCharWidthSync();

    // 初始化滚动同步
    initScrollSync();

    //视频可以点击播放
    setupVideoPlayback();
});

// 初始化模式切换
function initModeSwitch() {
    switchModeBtn.addEventListener('click', () => {
        currentMode = currentMode === 'image' ? 'video' : 'image';
        updateModeUI();
        resetPreview();
    });

    // 初始更新UI
    updateModeUI();
}

// 更新模式UI
function updateModeUI() {
    // 初始化测试图片或视频
    loadTestContent();

    // 更新标题
    pageTitle.textContent = currentMode === 'image' ? '图片转字符工具' : '视频转字符工具';

    // 更新图标
    previewIcon.className = currentMode === 'image' ? 'fa fa-image mr-3 text-blue-500' : 'fa fa-film mr-3 text-blue-500';
    resultIcon.className = currentMode === 'image' ? 'fa fa-file-text-o mr-3' : 'fa fa-film mr-3';
    resultPlaceholderIcon.className = currentMode === 'image' ? 'fa fa-file-text-o text-7xl mb-6 text-blue-200' : 'fa fa-film text-7xl mb-6 text-blue-200';

    // 更新预览标题
    previewTitle.textContent = currentMode === 'image' ? '图片预览区' : '视频预览区';

    // 更新上传提示
    uploadText.textContent = currentMode === 'image' ? '支持拖放图片' : '支持拖放视频';
    fileFormats.textContent = currentMode === 'image' ? '支持 JPG、PNG、BMP 等常见图片格式' : '支持 MP4、WebM、MOV 等常见视频格式';

    // 更新文件选择器类型
    fileUpload.accept = currentMode === 'image' ? 'image/*' : 'video/*';

    // 显示/隐藏复制按钮
    copyBtn.style.display = currentMode === 'image' ? 'flex' : 'none';

    // 更新占位文本
    resultPlaceholderText.textContent = currentMode === 'image' ? '请上传图片并点击转换按钮' : '请上传视频并点击转换按钮';

    // 更新加载文本
    //loadingText.textContent = currentMode === 'image' ? '正在处理图片' : '正在处理视频';
    //loadingDesc.textContent = currentMode === 'image' ? '正在将图片转换为字符艺术，请稍候...' : '正在将视频转换为字符艺术，这可能需要几分钟...';

    // 更新选择文件按钮文本
    const selectBtn = document.getElementById('select-file-btn');
    selectBtn.innerHTML = currentMode === 'image'
        ? '<i class="fa fa-plus-circle mr-1"></i> 选择图片'
        : '<i class="fa fa-plus-circle mr-1"></i> 选择视频';
}

// 重置预览区域
function resetPreview() {
    previewImage.src = '';
    previewImage.classList.add('hidden');
    previewVideo.src = '';
    previewVideo.classList.add('hidden');

    // 显示提示文字和图标
    const placeholders = fileContainer.querySelectorAll('i, p');
    placeholders.forEach(el => el.classList.remove('hidden'));

    // 清空结果
    resultContent.innerHTML = `
        <div class="flex items-center justify-center h-[500px] text-gray-400">
            <div class="text-center max-w-md">
                <i id="result-placeholder-icon" class="${currentMode === 'image' ? 'fa fa-file-text-o' : 'fa fa-film'} text-7xl mb-6 text-blue-200"></i>
                <p class="text-xl font-medium text-gray-500 mb-3">等待转换结果</p>
                <p class="text-gray-400" id="result-placeholder-text">${currentMode === 'image' ? '请上传图片并点击转换按钮' : '请上传视频并点击转换按钮'}</p>
            </div>
        </div>
    `;
}

// 加载测试内容
function loadTestContent() {
    if (currentMode === 'image') {
        const img = new Image();
        img.crossOrigin = 'Anonymous';
        img.onload = function() {
            const canvas = document.createElement('canvas');
            canvas.width = this.width;
            canvas.height = this.height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(this, 0, 0);
            const dataURL = canvas.toDataURL('image/png');
            previewImage.src = dataURL;
            previewImage.classList.remove('hidden');
            const placeholders = fileContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };
        img.src = 'test.png';
    } else {
        // 测试视频处理逻辑
        const video = document.createElement('video');
        video.crossOrigin = 'Anonymous'; // 处理跨域（如果需要）
        video.controls = true; // 显示播放控件（方便测试）
        // 视频加载完成并能播放时触发
        video.onloadeddata = function() {
            // 显示视频预览（假设页面有previewVideo元素用于视频预览）
            previewVideo.src = video.src; // 将视频源赋值给预览元素
            previewVideo.controls = true;
            previewVideo.classList.remove('hidden');
            // 隐藏占位元素（与图片逻辑一致）
            const placeholders = fileContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };
        // 设置测试视频资源
        video.src = 'test.mp4';
        // 触发视频加载（部分浏览器需要显式调用）
        video.load();
    }
}

// 初始化字符宽度同步功能
function initCharWidthSync() {
    // 设置进度条和输入框的最大值
    charWidthRange.max = 1500;
    charWidthInput.max = 1500;

    // 进度条变化时更新输入框
    charWidthRange.addEventListener('input', () => {
        charWidthInput.value = charWidthRange.value;
    });

    // 输入框变化时更新进度条
    charWidthInput.addEventListener('input', () => {
        let value = parseInt(charWidthInput.value);

        // 只在输入有效数字时更新进度条
        if (!isNaN(value)) {
            charWidthRange.value = value;
        }
    });

    // 只在失去焦点时进行完整验证
    charWidthInput.addEventListener('blur', () => {
        let value = parseInt(charWidthInput.value);

        if (isNaN(value) || value < 10) {
            value = 10;
        } else if (value > 1500) {
            value = 1500;
        }

        charWidthInput.value = value;
        charWidthRange.value = value;
    });
}

// 初始化滚动同步
function initScrollSync() {
    const horizontalScrollBottom = document.querySelector('.horizontal-scroll-bottom');
    const resultWrapper = document.querySelector('.result-content-wrapper');
    const scrollContent = document.querySelector('.scroll-content');

    function updateScrollBars() {
        const rect = resultWrapper.getBoundingClientRect();
        const needHorizontalScroll = resultContent.scrollWidth > resultWrapper.clientWidth;

        if (needHorizontalScroll) {
            horizontalScrollBottom.style.left = rect.left + 'px';
            horizontalScrollBottom.style.right = (window.innerWidth - rect.right) + 'px';
            horizontalScrollBottom.style.display = 'block';

            const contentWidth = resultContent.scrollWidth;
            scrollContent.style.width = contentWidth + 'px';

            // 只在初次加载时居中
            if (isFirstLoad) {
                const maxScroll = contentWidth - horizontalScrollBottom.clientWidth;
                const centerScroll = maxScroll / 2;
                horizontalScrollBottom.scrollLeft = centerScroll;
                resultWrapper.scrollLeft = centerScroll;
                isFirstLoad = false;
            }
        } else {
            horizontalScrollBottom.style.display = 'none';
        }
    }

    if (horizontalScrollBottom && resultContent && resultWrapper) {
        const syncHorizontalScroll = (e) => {
            const scrollLeft = e.target.scrollLeft;
            resultWrapper.scrollLeft = scrollLeft;
            horizontalScrollBottom.scrollLeft = scrollLeft;
        };

        horizontalScrollBottom.addEventListener('scroll', syncHorizontalScroll);

        const observer = new MutationObserver(() => {
            setTimeout(updateScrollBars, 100);
        });
        observer.observe(resultContent, { childList: true, subtree: true });

        window.addEventListener('resize', () => {
            setTimeout(updateScrollBars, 100);
        });
    }
}

// 添加新按钮的点击事件
document.getElementById('select-file-btn').addEventListener('click', () => {
    fileUpload.click();
});

// 文件选择变化事件
fileUpload.addEventListener('change', (e) => {
    if (e.target.files && e.target.files[0]) {
        const file = e.target.files[0];
        // 验证文件类型
        if (currentMode === 'image' && !file.type.startsWith('image/')) {
            alert('请选择图片文件');
            return;
        }
        if (currentMode === 'video' && !file.type.startsWith('video/')) {
            alert('请选择视频文件');
            return;
        }

        const reader = new FileReader();

        reader.onload = (e) => {
            if (currentMode === 'image') {
                previewImage.src = e.target.result;
                previewImage.classList.remove('hidden');
                previewVideo.classList.add('hidden');
            }else if (currentMode === 'video') {
                previewVideo.src = e.target.result;
                previewVideo.classList.remove('hidden');
                previewImage.classList.add('hidden');
                previewVideo.load();
                // 显式设置控件
                previewVideo.controls = true;
            }else {
                previewVideo.src = e.target.result;
                previewVideo.classList.remove('hidden');
                previewImage.classList.add('hidden');
                previewVideo.load();
            }
            // 隐藏提示文字和图标
            const placeholders = fileContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };

        reader.readAsDataURL(file);
    }
});

// 拖放功能实现
fileContainer.addEventListener('dragover', (e) => {
    e.preventDefault();
    fileContainer.classList.add('border-primary', 'bg-primary/10');
});

fileContainer.addEventListener('dragleave', () => {
    fileContainer.classList.remove('border-primary', 'bg-primary/10');
});

fileContainer.addEventListener('drop', (e) => {
    e.preventDefault();
    fileContainer.classList.remove('border-primary', 'bg-primary/10');

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        const file = e.dataTransfer.files[0];
        if (currentMode === 'image' && !file.type.startsWith('image/')) {
            alert('请选择图片文件');
            return;
        }
        if (currentMode === 'video' && !file.type.startsWith('video/')) {
            alert('请选择视频文件');
            return;
        }

        const reader = new FileReader();

        reader.onload = (e) => {
            if (currentMode === 'image') {
                previewImage.src = e.target.result;
                previewImage.classList.remove('hidden');
                previewVideo.classList.add('hidden');
            }else if (currentMode === 'video') {
                previewVideo.src = e.target.result;
                previewVideo.classList.remove('hidden');
                previewImage.classList.add('hidden');
                previewVideo.load();
                // 显式设置控件
                previewVideo.controls = true;
            }else {
                previewVideo.src = e.target.result;
                previewVideo.classList.remove('hidden');
                previewImage.classList.add('hidden');
                previewVideo.load();
            }
            // 隐藏提示文字和图标
            const placeholders = fileContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };

        reader.readAsDataURL(file);
    }
});

// 修复视频预览播放问题
function setupVideoPlayback() {
    // 确保视频加载完成后可以播放
    previewVideo.addEventListener('loadedmetadata', function() {
        // 视频加载完成后，确保控件可用
        this.controls = true;
    });

    // 修复可能的控件显示问题
    previewVideo.addEventListener('play', function() {
        this.classList.add('playing');
    });

    previewVideo.addEventListener('pause', function() {
        this.classList.remove('playing');
    });
}

// 转换按钮点击事件
convertBtn.addEventListener('click', async () => {
    // 验证是否选择了文件
    if (currentMode === 'image') {
        if (!previewImage.src || previewImage.classList.contains('hidden')) {
            alert('请先选择一张图片');
            return;
        }
    } else {
        if (!previewVideo.src || previewVideo.classList.contains('hidden')) {
            alert('请先选择一个视频');
            return;
        }
        if (isProcessingVideo) {
            //alert('视频正在处理中，请稍候...');
            return;
        }
    }

    // 获取字符宽度
    const charWidth = parseInt(charWidthInput.value);
    if (isNaN(charWidth) || charWidth < 10 || charWidth > 1500) {
        alert('请输入有效的字符宽度（10-1500）');
        return;
    }

    // 显示加载状态
    loading.classList.remove('hidden');

    // 重置标志，准备居中
    isFirstLoad = true;

    try {
        if (currentMode === 'image') {
            await processImage(charWidth);
        } else {
            await processVideo(charWidth);
        }

        // 隐藏加载状态
        loading.classList.add('hidden');

        // 显示成功提示
        showSuccessToast(currentMode === 'image' ? '图片转换完成' : '视频转换完成');

        // 滚动到结果区域
        lowerSection.scrollIntoView({ behavior: 'smooth' });

    } catch (error) {
        console.error('转换失败:', error);
        alert('转换失败，请重试');
        loading.classList.add('hidden');
        isProcessingVideo = false;
    }
});

// 处理图片
async function processImage(charWidth) {
    // 构建请求数据
    const requestData = {
        image: {
            imageBase64: previewImage.src,
            fileName: "image.png",
            fileType: previewImage.src.split(':')[1].split(';')[0]
        },
        config: {
            isDithering: document.getElementById('dither-algorithm').checked,
            isMonospaced: document.getElementById('monospace').checked,
            isInverted: document.getElementById('invert-colors').checked,
            outputWidth: charWidth
        }
    };

    const response = await fetch('http://localhost:8080/api/image/toBraille', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    // 检查响应状态
    if (result.code !== 1) {
        throw new Error(result.msg || '转换失败');
    }

    // 获取盲文数据
    const brailleText = result.data.brailleStr;
    if (!brailleText) {
        throw new Error('未获取到转换结果');
    }

    // 显示结果
    displayResult(brailleText, requestData.config.isMonospaced);
}

// ======================== WebSocket 初始化 ========================
let ws;
let wsOpen = false;
let frameQueue = [];
let clientId = null; // 保存后端分配的ID

function initWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws/video-char');

    ws.addEventListener('open', () => {
        console.log('WebSocket 已连接');
        wsOpen = true;

        // 如果有缓存帧，发送
        frameQueue.forEach(data => ws.send(data));
        frameQueue = [];
    });

    ws.addEventListener('message', (event) => {
        let data = event.data;
        try {
            const json = JSON.parse(data);

            if (json.clientId) {
                clientId = json.clientId;
                console.log("🎯 已分配客户端 ID:", clientId);
                // ✅ WebSocket 建立完成后再启动视频处理逻辑
                if (typeof startVideoProcess === 'function') {
                    startVideoProcess();
                }
                return; // 不继续处理
            }
            if (json.frame) {
                const frameText = json.frame.replace(/\\n/g, '\n');
                displayCharFrame(frameText);
                return;
            }
            console.log("📩 收到未知类型消息:", json);

        } catch (e) {
            console.warn('⚠️ 非 JSON 消息:', data);
            // 有时后端可能直接发纯文本帧
            displayCharFrame(data.replace(/\\n/g, '\n'));
        }
    });

    ws.addEventListener('close', () => {
        console.log('WebSocket 已关闭');
        wsOpen = false;
    });

    ws.addEventListener('error', (err) => {
        console.error('WebSocket 出错:', err);
    });
}

// ======================== 视频处理 ========================
async function processVideo(charWidth) {
    isProcessingVideo = true;

    // 确保 WebSocket 已初始化
    if (!ws || ws.readyState === WebSocket.CLOSED) {
        console.log("WebSocket 未初始化，正在建立连接...");
        initWebSocket();
    }
    // 等待 WebSocket 完成连接 + 拿到 clientId
    let retryCount = 0;
    while ((!ws || ws.readyState !== WebSocket.OPEN || !clientId) && retryCount < 30) {
        console.log("⏳ 等待 WebSocket 连接中...");
        await new Promise(r => setTimeout(r, 200)); // 每0.2秒检测一次
        retryCount++;
    }
    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("❌ WebSocket 未连接，请稍后重试。");
        return;
    }
    if (!clientId) {
        alert("❌ 未获取到客户端ID，请重试。");
        return;
    }
    console.log("✅ WebSocket 已准备好，clientId =", clientId);

    // 创建视频画布
    const videoCanvas = document.createElement('canvas');
    const videoCtx = videoCanvas.getContext('2d');

    const video = previewVideo;
    const fps = 10; // 每秒提取10帧
    const duration = video.duration;
    const totalFrames = Math.floor(duration * fps);

    //loadingDesc.textContent = `正在提取视频帧 (0/${totalFrames})...`;

    for (let i = 0; i < totalFrames; i++) {
        //loadingDesc.textContent = `正在提取视频帧 (${i}/${totalFrames})...`;

        // 跳转时间
        video.currentTime = i / fps;

        // 等待一帧渲染
        await new Promise(resolve => {
            video.addEventListener('seeked', function handler() {
                resolve();
                video.removeEventListener('seeked', handler);
            });
        });

        // 绘制到 canvas
        videoCanvas.width = video.videoWidth;
        videoCanvas.height = video.videoHeight;
        videoCtx.drawImage(video, 0, 0, videoCanvas.width, videoCanvas.height);

        // 构建请求数据
        const requestData = {
            image: {
                imageBase64: videoCanvas.toDataURL('image/png'),
                fileName: "image.png",
                fileType: videoCanvas.toDataURL('image/png').split(':')[1].split(';')[0]
            },
            config: {
                isDithering: document.getElementById('dither-algorithm').checked,
                isMonospaced: document.getElementById('monospace').checked,
                isInverted: document.getElementById('invert-colors').checked,
                outputWidth: charWidth
            }
        };
        // 发给后端处理
        const response = await fetch(`http://localhost:8080/api/video/convertFrame/${clientId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();
        if (result.code !== 1 || !result.data.brailleStr) {
            console.error(`帧 ${i} 转换失败`);
            continue;
        }
    }

    isProcessingVideo = false;
    //loadingDesc.textContent = '视频处理完成';
}
// ======================== 视频显示控件 ========================
function displayCharFrame(charFrame) {
    console.log(
        "帧数:", frames.length + 1, // +1 因为马上就要 push
        "最新帧前20字符:", (charFrame || "").slice(0, 20)
    );

    frames.push(charFrame);

    // 如果是第一帧，初始化控件
    if (frames.length === 1) {
        renderVideoControls();
    }

    // 实时显示最新帧（如果当前播放刚好在最后一帧）
    if (!isPlaying) {
        updateFrame(frames.length - 1);
    }
}

// 显示图片结果
function displayResult(text, isMonospace) {
    if (!text) {
        console.error('无效的文本数据');
        return;
    }

    resultContent.innerHTML = `
        <pre class="result-text ${isMonospace ? 'font-mono' : ''}"
             style="font-size: 12px; line-height: 1.2; white-space: pre-wrap; word-break: normal;">${text}</pre>
    `;
}

function renderVideoControls() {
    const resultHtml = `
        <div class="video-result-controls mb-4 flex items-center gap-4">
            <button id="play-video-result" class="btn-primary px-4 py-2 rounded-lg">
                <i class="fa fa-play mr-2"></i>播放
            </button>
            <button id="pause-video-result" class="btn-secondary px-4 py-2 rounded-lg hidden">
                <i class="fa fa-pause mr-2"></i>暂停
            </button>
            <div class="ml-auto">
                <label for="video-speed" class="text-gray-600 mr-2">速度:</label>
                <select id="video-speed" class="param-control px-2 py-1 rounded">
                    <option value="0.5">0.5x</option>
                    <option value="1" selected>1x</option>
                    <option value="1.5">1.5x</option>
                    <option value="2">2x</option>
                    <option value="3">3x</option>
                </select>
            </div>
        </div>
        <div id="video-frame-container" class="overflow-auto h-[1000px] border rounded p-2 bg-gray-50">
            <pre id="current-video-frame" class="result-text ${isMonospace ? 'font-mono' : ''}"
                 style="font-size: 12px; line-height: 1.2; white-space: pre-wrap; word-break: normal;">${frames[0]}</pre>
        </div>
        <div class="mt-4">
            <div class="w-full bg-gray-200 rounded-full h-2.5">
                <div id="video-progress" class="bg-gradient-to-r from-blue-500 to-purple-600 h-2.5 rounded-full" style="width: 0%"></div>
            </div>
        </div>
    `;
    resultContent.innerHTML = resultHtml;

    initVideoResultControls();
}

function initVideoResultControls() {
    const playBtn = document.getElementById('play-video-result');
    const pauseBtn = document.getElementById('pause-video-result');
    const currentFrameEl = document.getElementById('current-video-frame');
    const progressBar = document.getElementById('video-progress');
    const speedControl = document.getElementById('video-speed');

    playBtn.addEventListener('click', play);
    pauseBtn.addEventListener('click', pause);
    speedControl.addEventListener('change', () => {
        currentSpeed = parseFloat(speedControl.value);
        if (isPlaying) {
            pause();
            play();
        }
    });

    function updateFrame(index) {
        currentFrameIndex = index;
        if (!frames[index]) return;
        currentFrameEl.textContent = frames[index];
        progressBar.style.width = `${(index / (frames.length - 1)) * 100}%`;
    }

    function play() {
        if (isPlaying) return;
        isPlaying = true;
        playBtn.classList.add('hidden');
        pauseBtn.classList.remove('hidden');

        playInterval = setInterval(() => {
            let nextIndex = currentFrameIndex + 1;
            if (nextIndex >= frames.length) {
                nextIndex = 0;
            }
            updateFrame(nextIndex);
        }, 1000 / (fps * currentSpeed));
    }

    function pause() {
        if (!isPlaying) return;
        isPlaying = false;
        clearInterval(playInterval);
        pauseBtn.classList.add('hidden');
        playBtn.classList.remove('hidden');
    }

    window.updateFrame = updateFrame; // 方便外部调用
}

// 保存按钮点击事件
saveBtn.addEventListener('click', () => {
    if (currentMode === 'image') {
        saveImageResult();
    } else {
        saveVideoResult();
    }
});

// 保存图片结果
function saveImageResult() {
    const textElement = resultContent.querySelector('pre');
    if (!textElement) {
        alert('没有可保存的结果');
        return;
    }

    const text = textElement.textContent;
    const filename = '字符转换结果.txt';

    // 创建Blob对象并触发浏览器下载
    const blob = new Blob([text], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();

    // 清理临时资源
    setTimeout(() => {
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }, 0);
}

// 保存视频结果
function saveVideoResult() {
    const frameContainer = document.getElementById('video-frame-container');
    if (!frameContainer || frames.length === 0) {
        alert('没有可保存的视频结果');
        return;
    }

    // 创建帧分隔符（使用特殊字符组合，避免与内容冲突）
    const frameSeparator = '\n---FRAME_END---\n';

    // 组合所有字符帧
    const allFramesText = frames.join(frameSeparator);
    const filename = '视频字符帧集合.txt';

    // 创建Blob对象并触发浏览器下载
    const blob = new Blob([allFramesText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();

    // 清理临时资源
    setTimeout(() => {
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    }, 0);
}

// 复制按钮点击事件
copyBtn.addEventListener('click', () => {
    const textElement = resultContent.querySelector('pre');
    if (!textElement) {
        alert('没有可复制的内容');
        return;
    }

    const text = textElement.textContent;
    navigator.clipboard.writeText(text).then(() => {
        showSuccessToast('已复制到剪贴板');
    }).catch(err => {
        console.error('复制失败:', err);
        alert('复制失败，请重试');
    });
});

// 显示成功提示
function showSuccessToast(message) {
    successMessage.textContent = message;
    const toast = document.getElementById('success-toast');
    toast.classList.remove('translate-x-full');

    // 3秒后隐藏
    setTimeout(() => {
        toast.classList.add('translate-x-full');
    }, 3000);
}

// 窗口大小变化时确保结果区域宽度正确
window.addEventListener('resize', () => {
    if (currentMode === 'image') {
        const textElement = resultContent.querySelector('pre');
        if (textElement) {
            const text = textElement.textContent;
            const isMonospace = document.getElementById('monospace').checked;
            displayResult(text, isMonospace);
        }
    }
    // 视频模式下不需要重新渲染，因为会自动适应
});