// DOM 元素
const imageContainer = document.getElementById('image-container');
const imageUpload = document.getElementById('image-upload');
const previewImage = document.getElementById('preview-image');
const convertBtn = document.getElementById('convert-btn');
const saveBtn = document.getElementById('save-btn');
const resultContent = document.getElementById('result-content');
const loading = document.getElementById('loading');
const lowerSection = document.getElementById('lower-section');

let isFirstLoad = true; // 添加标志变量

// 初始化预览图片
window.addEventListener('DOMContentLoaded', () => {
    const img = new Image();
    img.crossOrigin = 'Anonymous'; // 处理跨域问题
    img.onload = function() {
        const canvas = document.createElement('canvas');
        canvas.width = this.width;
        canvas.height = this.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(this, 0, 0);
        const dataURL = canvas.toDataURL('image/png');
        previewImage.src = dataURL;
        previewImage.classList.remove('hidden');
        const placeholders = imageContainer.querySelectorAll('i, p');
        placeholders.forEach(el => el.classList.add('hidden'));
    };
    img.src = 'test.png';
});


document.addEventListener('DOMContentLoaded', function() {
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
});


// 图片上传区域点击事件
imageContainer.addEventListener('click', () => {
    imageUpload.click();
});

// 图片选择变化事件
imageUpload.addEventListener('change', (e) => {
    if (e.target.files && e.target.files[0]) {
        const file = e.target.files[0];
        // 验证文件类型
        if (!file.type.startsWith('image/')) {
            alert('请选择图片文件');
            return;
        }

        const reader = new FileReader();

        reader.onload = (e) => {
            previewImage.src = e.target.result;
            previewImage.classList.remove('hidden');
            // 隐藏提示文字和图标
            const placeholders = imageContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };

        reader.readAsDataURL(file);
    }
});

// 拖放功能实现
imageContainer.addEventListener('dragover', (e) => {
    e.preventDefault();
    imageContainer.classList.add('border-primary', 'bg-primary/10');
});

imageContainer.addEventListener('dragleave', () => {
    imageContainer.classList.remove('border-primary', 'bg-primary/10');
});

imageContainer.addEventListener('drop', (e) => {
    e.preventDefault();
    imageContainer.classList.remove('border-primary', 'bg-primary/10');

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        const file = e.dataTransfer.files[0];
        if (!file.type.startsWith('image/')) {
            alert('请选择图片文件');
            return;
        }

        const reader = new FileReader();

        reader.onload = (e) => {
            previewImage.src = e.target.result;
            previewImage.classList.remove('hidden');
            // 隐藏提示文字和图标
            const placeholders = imageContainer.querySelectorAll('i, p');
            placeholders.forEach(el => el.classList.add('hidden'));
        };

        reader.readAsDataURL(file);
    }
});

// 转换按钮点击事件
convertBtn.addEventListener('click', async () => {
    // 验证是否选择了图片
    if (!previewImage.src || previewImage.classList.contains('hidden')) {
        alert('请先选择一张图片');
        return;
    }

    // 获取字符宽度
    const charWidth = parseInt(document.getElementById('char-width').value);
    if (isNaN(charWidth) || charWidth < 10 || charWidth > 1200) {
        alert('请输入有效的字符宽度（10-1200）');
        return;
    }

    // 显示加载状态
    loading.classList.remove('hidden');

    // 重置标志，准备居中
    isFirstLoad = true;

    // 构建请求数据
    const requestData = {
        image: {
            imageBase64: previewImage.src,
            fileName: "image.png", // 可以从文件输入获取实际文件名
            fileType: previewImage.src.split(':')[1].split(';')[0] // 从data URL获取MIME类型
        },
        config: {
            isDithering: document.getElementById('dither-algorithm').checked,
            isMonospaced: document.getElementById('monospace').checked,
            isInverted: document.getElementById('invert-colors').checked,
            outputWidth: charWidth
        }
    };

    try {
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

        // 隐藏加载状态
        loading.classList.add('hidden');

        // 滚动到结果区域
        lowerSection.scrollIntoView({ behavior: 'smooth' });

    } catch (error) {
        console.error('转换失败:', error);
        alert('转换失败，请重试');
        loading.classList.add('hidden');
    }
});

// 显示结果函数
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

// 保存按钮点击事件
saveBtn.addEventListener('click', () => {
    const textElement = resultContent.querySelector('pre');
    if (!textElement) {
        alert('没有可保存的结果');
        return;
    }

    const text = textElement.textContent;
    const filename = '盲文转换结果.txt';

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
});

// 复制按钮点击事件
document.getElementById('copy-btn').addEventListener('click', () => {
    const textElement = resultContent.querySelector('pre');
    if (!textElement) {
        alert('没有可复制的内容');
        return;
    }

    const text = textElement.textContent;
    navigator.clipboard.writeText(text).then(() => {
        alert('已复制到剪贴板');
    }).catch(err => {
        console.error('复制失败:', err);
        alert('复制失败，请重试');
    });
});


// 窗口大小变化时确保结果区域宽度正确
window.addEventListener('resize', () => {
    const textElement = resultContent.querySelector('pre');
    if (textElement) {
        const text = textElement.textContent;
        const isMonospace = document.getElementById('monospace').checked;
        displayResult(text, isMonospace);
    }
});